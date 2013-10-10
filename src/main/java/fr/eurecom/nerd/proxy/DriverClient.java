//   NERD - The Named Entity Recognition and Disambiguation framework.
//          It processes textual resources for extracting named entities
//          linked to Web resources.
//
//   Copyright 2011 Politecnico di Torino
//             2011 EURECOM
//             2013 Universita' di Torino
//
//   Authors:
//      Giuseppe Rizzo <giuse.rizzo@gmail.com>
//
//   Licensed under both the CeCILL-B and the Apache License, Version 2.0 
//   (the "License"); you may not use this file except in compliance with 
//   the License. You may obtain a copy of the License at
//     http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package fr.eurecom.nerd.proxy;

import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fr.eurecom.nerd.api.PropFactory;
import fr.eurecom.nerd.db.SQL;
import fr.eurecom.nerd.db.table.TDocument;
import fr.eurecom.nerd.db.table.TEntity;
import fr.eurecom.nerd.exceptions.LanguageException;
import fr.eurecom.nerd.exceptions.NoContentException;
import fr.eurecom.nerd.exceptions.QuotaException;
import fr.eurecom.nerd.exceptions.RouterException;
import fr.eurecom.nerd.exceptions.TimeOutException;
import fr.eurecom.nerd.exceptions.TypeExpection;
import fr.eurecom.nerd.logging.LogFactory;
import fr.eurecom.nerd.ontology.OntologyType;

public class DriverClient {

    private SQL sql = new SQL();
    
    public List<TEntity> run(
                                int idUser, 
                                final String extractor,
                                final OntologyType otype,
                                final TDocument document, 
                                Long timeout, 
                                Integer idTool
                            ) 
    throws LanguageException, QuotaException, 
           TimeOutException, RouterException, 
           NoContentException, TypeExpection                                 
    {
        String key = null;
        List<TEntity> extractions = null;
        
        try {               

            key = sql.selectKeyByName(idUser, extractor);   
            //create the vector od systems which have quota constraints
            List<String> quotated = Arrays.asList( ((String) PropFactory.config
                        .getProperty("fr.eurecom.nerd.extractors.quota")).split(",") );
            if(quotated.contains(extractor) && key == null) {
                //use our own key
                key = PropFactory.config
                        .getProperty("fr.eurecom.nerd.extractor."
                                     .concat(extractor)
                                     .concat(".key")
                                    );
                
                //check whether the user has not anymore extractions for today
                int today = sql.selectUserDailyCounter(idUser);
                int quota = sql.selectUserDailyQuota(idUser);
                if(today < quota - 1)
                    sql.updateDailyCounter(idUser);
                else 
                    throw new QuotaException("User has reached the total number of daily extractions\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
      
        // create 1 timed-thread for the extractor
        ExtractorType etype = null;
        try {
            etype = Extractor.getType(extractor);
        } catch (TypeExpection e) {
            throw e;
        }
        final IClient client = getClient(etype);      
        
        LogFactory.logger.info("create #" + 1 + " thread to time the execution of the extractor: " + extractor);
        int nThreads = 1;
        LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
        ExecutorService threadPool = // Executors.newFixedThreadPool(nThreads);
                new ThreadPoolExecutor(nThreads, nThreads, 100L, TimeUnit.SECONDS, taskQueue);
                                     
        Set<Callable<SimpleEntry<String,List<TEntity>>>> callables = 
                        new HashSet<Callable<SimpleEntry<String,List<TEntity>>>>();
        final String fk = key;
        try {
            callables.add(new Callable<SimpleEntry<String,List<TEntity>>>() {
                public SimpleEntry<String, List<TEntity>> call()
                throws Exception {
                    return new SimpleEntry<String, List<TEntity>>
                               (extractor, client.extract(document, fk, otype));
                }                
                });
            
            // launch the thread
            List<Future<SimpleEntry<String, List<TEntity>>>> 
                                        futures = threadPool.invokeAll(callables, 120L, TimeUnit.SECONDS);

            for(Future<SimpleEntry<String, List<TEntity>>> future : futures){
                //if task is accomplished
                if( !future.isCancelled() ) {
                    extractions = (future.get().getValue() != null) ? 
                            future.get().getValue() : new LinkedList<TEntity>() ;                    
                }
            }
        } catch (InterruptedException e) {
            throw new TimeOutException("The extractor: ".concat(extractor).concat("did not answer in a right time.\n"));
        } catch (ExecutionException e) {
            throw new RouterException(e.getMessage());
        }
        
        //shutdown the thread created to handle the client
        threadPool.shutdown();
        
        //System.out.println(extractions.size());
        
        extractions = (extractions==null) ? new LinkedList<TEntity>() : extractions;
            //throw new NoContentException("The extractor: ".concat(extractor).concat(" has produced an empty annotation list.\n"));
            
        return extractions;
    }
    
    protected IClient getClient(ExtractorType etype) 
    {
        IClient result = null;
        switch (etype) {
        case ALCHEMYAPI:
            result = new AlchemyAPIClient();
            break;  
        case DBSPOTLIGHT:
            result = new DBSpotlightClient();
            break;
        case EXTRACTIV:
            result = new ExtractivClient();
            break;            
        case LUPEDIA:
            result = new LupediaClient();
            break;
        case OPENCALAIS:
            result = new OpenCalaisClient();
            break;
        case SAPLO:
            result = new SaploClient();
            break;            
        case SEMITAGS:
            result = new SemiTagsClient();
            break;            
        case TEXTRAZOR:
            result = new TextRazorClient();
            break;
        case THD:
            result = new THDClient();
            break;
        case WIKIMETA:
            result = new WikimetaClient();
            break;    
        case YAHOO:
            result = new YahooClient();
            break;
        case ZEMANTA:
            result = new ZemantaClient();
            break;                
        default:
            break;
        }
        return result;
    }
}
