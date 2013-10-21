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

package fr.eurecom.nerd.core.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import mx.bigdata.jcalais.CalaisClient;
import mx.bigdata.jcalais.CalaisObject;
import mx.bigdata.jcalais.CalaisResponse;
import mx.bigdata.jcalais.rest.CalaisRestClient;
import fr.eurecom.nerd.core.db.table.DocumentType;
import fr.eurecom.nerd.core.db.table.TDocument;
import fr.eurecom.nerd.core.db.table.TEntity;
import fr.eurecom.nerd.core.logging.LogFactory;
import fr.eurecom.nerd.core.ontology.OntoFactory;
import fr.eurecom.nerd.core.ontology.OntologyType;
import fr.eurecom.nerd.core.srt.SRTMapper;
import fr.eurecom.nerd.core.utils.PropFactory;

public class OpenCalaisClient implements IClient {

    private static String SOURCE = Extractor.getName(ExtractorType.OPENCALAIS);
    
    public List<TEntity> extract(TDocument document, String key, OntologyType otype) 
    {
        if(document.getText()==null) return null;
        
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
        
        String servicekey = (key!=null) ? key : 
            PropFactory.config.getProperty("fr.eurecom.nerd.extractor.opencalais.key");
        
        List<TEntity> result = parse (document.getText(), servicekey, otype);
        
        //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
        if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
            SRTMapper srt = new SRTMapper();
            result = srt.run(document, result);
        }
        
        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    
    private List<TEntity> parse (String text, String serviceKey, OntologyType otype) {
        
        List<TEntity> result = new LinkedList<TEntity>();
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        CalaisClient client = new CalaisRestClient(serviceKey);
        CalaisResponse response = null;
        try {
            //Posted content is normalized / cleaned (removing ads, navigation links, 
            //and other unimportant content), the primary document language is 
            //detected, and named entities are extracted automatically.
            //language==null    
            
            response = client.analyze(text);
           
            for (CalaisObject co : response.getEntities()) 
            {                
                String label = co.getField("name");
                String type = co.getField("_type");
                String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, type).toString();
                Double confidence = Double.parseDouble(co.getField("relevance"));
                String uri = co.getField("_uri");

//                //FIXME
//                //name contains the expanded NE, so consider the exact NE within the 
//                //branch instances
//                Pattern p = Pattern.compile (entity) ; 
//                Matcher m = p.matcher ( text ) ; 
//                // Now loop to find all matches of the pattern in the text 
//                while ( m.find (  )  )   {  
//                    Integer startchar = -1, endchar = -1;
//                    startchar = m.start();
//                    endchar = m.end();
//                    result.add (
//                                new TExtraction(entity, type, uri, nerdType,
//                                    startchar, endchar,confidence, SOURCE)
//                               );
//                }
                
                //logic to compute the startchar and endchar of the entity within the text
                Integer startchar=null, endchar=null;
                if(map.containsKey(label)) {
                    int value = map.get(label);
                    map.remove(label);
                    map.put(label, new Integer(value+1));
                }
                else 
                    map.put(label, new Integer(1));

                try{
                    Pattern p = Pattern.compile ("\\b" + label + "\\b") ; 
                    Matcher m = p.matcher (text) ; 
                    for(int j=0; j < map.get(label) && m.find ( ); j++) {
                        startchar = m.start ( 0 );
                        endchar = m.end ( 0 ) ; 
                        if ( containsAtIndex(result, startchar, endchar) ) j--;
                    }
                                    
                    if(startchar != null && endchar != null) 
                    {
                        TEntity extraction = 
                                new TEntity(label, type, uri, nerdType.toString(), 
                                                startchar, endchar,confidence,SOURCE);
    
                        result.add(extraction);
                    }  
                }catch(PatternSyntaxException eregex) {
                    eregex.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private boolean containsAtIndex(List<TEntity> result, Integer startChar, Integer endChar) 
    {
        for (TEntity e : result) 
            if(e.getStartChar() == startChar || e.getEndChar() == endChar)
                return true;
        return false;
    } 
}
