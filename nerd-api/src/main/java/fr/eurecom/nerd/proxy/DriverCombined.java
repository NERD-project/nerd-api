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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fr.eurecom.nerd.db.table.TDocument;
import fr.eurecom.nerd.db.table.TEntity;
import fr.eurecom.nerd.exceptions.TimeOutException;
import fr.eurecom.nerd.language.LangFactory;
import fr.eurecom.nerd.logging.LogFactory;
import fr.eurecom.nerd.ontology.OntologyType;

public class DriverCombined {
  
    public List<TEntity> run(
                            final int idUser, 
                            String extractor, 
                            final OntologyType otype,
                            final TDocument document, 
                            Long timeout, 
                            final Integer idTool,
                            final Boolean force
                            )
    throws TimeOutException 
    {
        final String source = "combined";
        
        List<String> extractors = new ArrayList<String>();
        
        //force annotation results
        if (force) 
        {
            for(Entry<String,List<String>> e :  LangFactory.detector.getExtractorLanguages().entrySet()) {
                if( !e.getKey().equals("combined") )
                    extractors.add(e.getKey());
            }
            
        }
        else {
            for(Entry<String,List<String>> e :  LangFactory.detector.getExtractorLanguages().entrySet()) {
                if(!e.getKey().equals("combined") && e.getValue().contains(document.getLanguage()))
                    extractors.add(e.getKey());
            } 
        }
        
        // get all extractions from the drivers involved
        // keys are extractor names
        Hashtable<String, List<TEntity>> annotations = 
                            new Hashtable<String, List<TEntity>>();
        
        // define a threadPool with n thread equal to the EXTRACTORS.length
        int nThreads = extractors.size();
        LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
        ExecutorService threadPool = // Executors.newFixedThreadPool(nThreads);
                new ThreadPoolExecutor(nThreads, nThreads, 1L, TimeUnit.MILLISECONDS, taskQueue);
                                     
        Set<Callable<SimpleEntry<String,List<TEntity>>>> callables = 
                        new HashSet<Callable<SimpleEntry<String,List<TEntity>>>>();
        
        LogFactory.logger.info("create #" + extractors.size() + 
                " threads to handle the extractors involved in this annotation");
        
        int nOfCompletedTasks = 0;

        for (final String e : extractors) 
        {
            callables.add(new Callable<SimpleEntry<String,List<TEntity>>>() {
                public SimpleEntry<String, List<TEntity>> call()
                throws Exception {
                    //different router instances for different clients
                    final DriverClient driver = new DriverClient();

                    return new SimpleEntry<String, List<TEntity>>(e, 
                                driver.run(idUser, e, otype, document, Long.MAX_VALUE, idTool));
                }
            });
        }
        
        // launch all threads
        LogFactory.logger.info("launch #" + extractors.size() + " threads to gather the extraction results");
        List<Future<SimpleEntry<String, List<TEntity>>>> futures = null;
        try {
            futures = threadPool.invokeAll(callables, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } 
        if (futures == null) return null;

        for(Future<SimpleEntry<String, List<TEntity>>> future : futures){
            //if task is accomplished
            if( !future.isCancelled() ) {
                nOfCompletedTasks ++;
                
                String extractorName = null;  
                List<TEntity> extractions = null;
                try{
                extractorName = future.get().getKey();
                extractions = (future.get().getValue() != null) ? 
                        future.get().getValue() : new LinkedList<TEntity>() ;
                } 
                catch(ExecutionException ee)    { 
                    ee.printStackTrace(); 
                    continue;
                } 
                catch (InterruptedException ie) { 
                    ie.printStackTrace(); 
                    continue;
                }
                                                        
                // store annotations according to the name of the extractor
                annotations.put(extractorName, extractions);

                // merge all annotations in the SOURCE bag
                List<TEntity> temp = annotations.remove(source);
                if(temp == null)  
                    temp = extractions; 
                else              
                    temp.addAll(extractions);

                annotations.put(source, temp);
            }
        }

        LogFactory.logger.info("#" + nOfCompletedTasks + " threads that actually accomplished the task");
        threadPool.shutdown();
       
        //annotations stored all extraction performed by all tools
        List<TEntity> result = (annotations.get(source) != null) ?
                                    annotations.get(source) : new LinkedList<TEntity>();
                                    
        Collections.sort(result, TEntity.ENTITYPOSITION);                     
                                    
        result = fusion(result, document.getText());
        LogFactory.logger.info(source + " has found #entities=" + result.size());
        return result;    
    }
    
    private List<TEntity> fusion(List<TEntity> bagOfEntities, String text) 
    {        
        // remove noise 
        List<TEntity> result = new LinkedList<TEntity>();
        for(TEntity e : bagOfEntities) 
        {
            String entity = e.getLabel();
            // check whether or not the entity has a bad label
            // if it falls in this bucket, just return an empty collection and 
            // update the entity position
            if(entity.length() > 1) //(entity.equals(".") || entity.equals(",") || entity.equals("!") || entity.equals("?")) )
            {
                //FIXME from the extractor side
                TEntity temp = e;
                
                //System.out.println(e.getLabel() + "==" + text.substring(e.getStartChar(), e.getEndChar()));
                
                if(text.substring(e.getStartChar(), e.getEndChar()).equals(e.getLabel()))
                    result.add(temp);
            }
        }
        
        List<TEntity> cleansed = new LinkedList<TEntity>();
        TEntity previous = null;
        for(TEntity current : result) {
            if(previous != null ) 
            {
                if( !(current.getStartChar() > previous.getEndChar()) ) //if current entity starts in the range of the previous one
                {
                    cleansed.remove(previous);
                    previous = conflict (previous, current);
                    cleansed.add(previous);
                }
                else { 
                    cleansed.add(current);
                    previous = current;
                }
            }
            else {
                cleansed.add(current);
                previous = current;
            }
        }
               
        return cleansed;
    }

    private TEntity conflict(TEntity e1, TEntity e2) 
    {
        TEntity result = e1;
        
        // check whether the entity label is the same, i.e. same startChar and endChar are the same
        if( e1.getStartChar() == e2.getStartChar() 
            && e1.getEndChar() == e2.getEndChar() )
        {
            LogFactory.logger.info("fusion: entity mismatching e1:" + e1.getLabel() + " and e2:" +e2.getLabel() + "; relying on gazetters");
            return priority(e1, e2);
        }

        // if not the same, we rely on the logic of the confidence score
        Double confidenceA = e1.getConfidence();
        Double confidenceB = e2.getConfidence();
        if(Math.abs(confidenceA - confidenceB) < 0.1) {
            return (confidenceA>=confidenceB) ? e1 : e2;
        }
        // if confidence spread isn't sensible
        else 
            result = priority(e1, e2);

        return result;
    }     
    
    private TEntity priority (TEntity e1, TEntity e2) 
    {
        if( ( e2.getExtractor().equals(ExtractorType.DBSPOTLIGHT) ||
              e2.getExtractor().equals(ExtractorType.LUPEDIA) ||
              e2.getExtractor().equals(ExtractorType.TEXTRAZOR) ) 
             && !( e1.getExtractor().equals(ExtractorType.DBSPOTLIGHT) ||
                   e1.getExtractor().equals(ExtractorType.LUPEDIA) ||
                   e1.getExtractor().equals(ExtractorType.TEXTRAZOR) ) 
             && !( e1.getExtractor().equals(ExtractorType.DBSPOTLIGHT) ||
                   e1.getExtractor().equals(ExtractorType.LUPEDIA) ||
                   e1.getExtractor().equals(ExtractorType.TEXTRAZOR))      
           )
            return e2;
        else if ( e2.getExtractor().equals(ExtractorType.ALCHEMYAPI) && 
                !e1.getExtractor().equals(ExtractorType.ALCHEMYAPI)) 
            return e2;
        else if ( e2.getExtractor().equals(ExtractorType.EXTRACTIV) && 
            !e1.getExtractor().equals(ExtractorType.EXTRACTIV) )
            return e2;
        else if ( e2.getExtractor().equals(ExtractorType.OPENCALAIS) && 
            !e1.getExtractor().equals(ExtractorType.OPENCALAIS))
            return e2;
        else if ( e2.getExtractor().equals(ExtractorType.EVRI) &&
            !e1.getExtractor().equals(ExtractorType.EVRI))
            return e2;
        else if ( e2.getExtractor().equals(ExtractorType.ZEMANTA) &&
            !e1.getExtractor().equals(ExtractorType.ZEMANTA))
            return e2;
        else if ( e2.getExtractor().equals(ExtractorType.WIKIMETA) &&
            !e1.getExtractor().equals(ExtractorType.WIKIMETA))
            return e2;
        else if ( e2.getExtractor().equals(ExtractorType.YAHOO) && 
            !e1.getExtractor().equals(ExtractorType.YAHOO))
            return e2;
        else if ( e2.getExtractor().equals(ExtractorType.SAPLO) && 
                !e1.getExtractor().equals(ExtractorType.SAPLO))
            return e2;
        return e1;
    }

}