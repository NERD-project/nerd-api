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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.json.JSONException;

import com.saplo.api.client.SaploClientException;
import com.saplo.api.client.entity.SaploCollection;
import com.saplo.api.client.entity.SaploCollection.Language;
import com.saplo.api.client.entity.SaploTag;
import com.saplo.api.client.entity.SaploText;
import com.saplo.api.client.manager.SaploCollectionManager;
import com.saplo.api.client.manager.SaploTextManager;

import fr.eurecom.nerd.db.table.DocumentType;
import fr.eurecom.nerd.db.table.TDocument;
import fr.eurecom.nerd.db.table.TEntity;
import fr.eurecom.nerd.logging.LogFactory;
import fr.eurecom.nerd.ontology.OntoFactory;
import fr.eurecom.nerd.ontology.OntologyType;
import fr.eurecom.nerd.srt.SRTMapper;

public class SaploClient implements IClient {

    private static String SOURCE = Extractor.getName(ExtractorType.SAPLO);    
        
    public List<TEntity> extract(TDocument document, String key, OntologyType otype) 
    {
        if (document.getText() == null) return null;
        
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
                
        List<SaploTag> tags = getTags(  document.getText(), 
                                        document.getLanguage(), 
                                        key);
        
        List<TEntity> result = parse (tags, document.getText(), otype);        
                
        //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
        if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
            SRTMapper srt = new SRTMapper();
            result = srt.run(document, result);
        }
        
        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    
    private List<TEntity> parse(List<SaploTag> tags, String text, OntologyType otype) 
    {
        List<TEntity> result = new LinkedList<TEntity>();
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        for(SaploTag tag : tags) {
            String label = tag.getTagWord();
            String type = tag.getCategory().toString();
            String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, type).toString();
            Double confidence = tag.getRelevance();
            
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
                            new TEntity(label, type, null, nerdType.toString(), 
                                            startchar, endchar,confidence,SOURCE);
    
                    result.add(extraction);
                }       
            }catch(PatternSyntaxException eregex) {
                eregex.printStackTrace();
            }
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

    private List<SaploTag> getTags(String text, String language, String key) 
    {
        List<SaploTag> tags = null;
        try {
            String[] keys = key.split(",");
            com.saplo.api.client.SaploClient client = 
                    new com.saplo.api.client.SaploClient.Builder(keys[0], keys[1]).build();
            SaploCollectionManager manager = client.getCollectionManager();
            SaploCollection collection = bootstrapCollection(manager, language);            
            
            SaploTextManager textMgr = new SaploTextManager(client);
            SaploText st = new SaploText(collection, text);
            textMgr.create(st);
            
            // Get a list of tags that exist in your text
            //textMgr.tagsAsync(saploText, wait, skipCategorization)                 
            tags = textMgr.tags(st, 60, false);
            textMgr.delete(st);
            //manager.reset(collection);
            
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SaploClientException e) {
            e.printStackTrace();
        }
        
        return tags;
    }
    
    private SaploCollection bootstrapCollection(
                                        SaploCollectionManager manager, 
                                        String language
                                                ) 
    throws JSONException, SaploClientException 
    {
        SaploCollection collection = null;
        //FIXME we take into account just the _en_ collection
        // check if the collection already exists
        List<SaploCollection> collectionList = manager.list();
         
        // we have already created the en collection
        if(collectionList.size() > 0) 
            collection = collectionList.get(0);
        
        else {
            if(language.equals("en") || language == null)
                collection = new SaploCollection("NERD collection extraction", Language.en);
            if(language.equals("sv"))
                collection = new SaploCollection("NERD collection extraction", Language.sv);                
                
            manager.create(collection);
        }
        
        return collection;
    }
}
