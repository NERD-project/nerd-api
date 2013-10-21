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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.eurecom.nerd.core.db.table.DocumentType;
import fr.eurecom.nerd.core.db.table.TDocument;
import fr.eurecom.nerd.core.db.table.TEntity;
import fr.eurecom.nerd.core.exceptions.ClientException;
import fr.eurecom.nerd.core.logging.LogFactory;
import fr.eurecom.nerd.core.ontology.OntoFactory;
import fr.eurecom.nerd.core.ontology.OntologyType;
import fr.eurecom.nerd.core.srt.SRTMapper;

public class WikimetaClient implements IClient {

    private String endpoint = "http://www.wikimeta.com/wapi/service";
            //"http://www.wikimeta.com/wapi/semtag.pl"; 

    private static String SOURCE = Extractor.getName(ExtractorType.WIKIMETA);
       
    public List<TEntity> extract(TDocument document, String key, OntologyType otype) 
    {
        if(document.getText()==null) return null;
        
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
        
        List<TEntity> result = null;
        
        try {
            String json = post(document.getText(), document.getLanguage(), key);

            result = parse(json, document.getText(), otype);
            
            //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
            if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
                SRTMapper srt = new SRTMapper();
                result = srt.run(document, result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        
        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    
    private String post(String text, String language, String serviceKey) 
    throws ClientException 
    {
        // CLI
        // curl -i -H "accept:text/json" -X POST http://www.wikimeta.com/perl/semtag.pl 
        // -d "api=giusepperizzo&contenu=Je vais ... "
        
        Client client = ClientBuilder.newClient();     
        WebTarget target = client.target(endpoint);
        Form form = new Form();
        form.param("api", serviceKey)
            .param("contenu", text)
            .param("lng", language.toUpperCase());

        Response response = target
                            .request(MediaType.APPLICATION_JSON_TYPE)
                            .post(Entity.entity(
                                   form, 
                                   MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                             );
                
        if( response.getStatus() != 200 ) 
            throw new ClientException("Extractor: " + SOURCE + " is temporary not available.");
                
        return response.readEntity(String.class);
    }
    
    public List<TEntity> parse(String json, String text, OntologyType otype) 
    throws IOException
    {
        List<TEntity> result = new LinkedList<TEntity>();
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        try {
            JSONObject o = new JSONObject(json);          
            JSONArray jadocument = o.getJSONArray("document");
            
            // 3 items is Named Entities
            JSONObject jodocument = jadocument.getJSONObject(2);
            JSONArray jsonarray = jodocument.getJSONArray("Named Entities");         
            
            for(int i=0; i<jsonarray.length(); i++) 
            {
                JSONObject jo = jsonarray.getJSONObject(i);
                String entity = jo.getString("EN");
                String type = (jo.getString("type").equals("")) ? null : jo.getString("type");
                String nerdType = OntoFactory.mapper.getNerdType(otype, entity, SOURCE, type).toString();
                String uri = jo.getString("URI");
                
                //logic to compute the startchar and endchar of the entity within the text
                Integer startchar=null, endchar=null;
                if(map.containsKey(entity)) {
                    int value = map.get(entity);
                    map.remove(entity);
                    map.put(entity, new Integer(value+1));
                }
                else 
                    map.put(entity, new Integer(1));

                try{ 
                    Pattern p = Pattern.compile ("\\b" + entity + "\\b") ; 
                    Matcher m = p.matcher (text) ; 
                    for(int j=0; j < map.get(entity) && m.find ( ); j++) {
                        startchar = m.start ( 0 );
                        endchar = m.end ( 0 ) ; 
                        if ( containsAtIndex(result, startchar, endchar) ) j--;
                    }
                    
                    double confidence = 0.0;
                    if(!jo.getString("confidenceScore").equals(""))
                        confidence = Double.parseDouble( jo.getString("confidenceScore") );
                    
                    if(startchar != null && endchar != null) 
                    {
                        TEntity extraction = 
                                new TEntity(entity, type, uri, nerdType.toString(), 
                                                startchar, endchar,confidence,SOURCE);
    
                        result.add(extraction);
                    }
                }catch(PatternSyntaxException eregex) {
                    eregex.printStackTrace();
                }
            }
        } catch (JSONException e) {
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
