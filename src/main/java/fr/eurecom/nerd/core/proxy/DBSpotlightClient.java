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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

public class DBSpotlightClient implements IClient {

    private static String SOURCE = Extractor.getName(ExtractorType.DBSPOTLIGHT);
       
    public List<TEntity> extract(TDocument document, String key, OntologyType otype) 
    throws ClientException 
    {
        if(document.getText()==null) return null;
        
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document: " + document.getIdDocument());
        
        //we discard the information about the language
        List<TEntity> result = null;
        String json = post(document.getText());

        result = parse(json, otype);
        
        //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
        if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
            SRTMapper srt = new SRTMapper();
            result = srt.run(document, result);
        }

        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    
    private String post(String text) 
    throws ClientException 
    {
        // CLI
        // curl -i -X POST -d "text=dbpedia+spotlight+is+neat.&confidence=0.2&support=10" -H "Accept:application/json" -H "Content-Type:application/x-www-form-urlencoded" http://spotlight.dbpedia.org/rest/annotate
        
        Client client = ClientBuilder.newClient();     
        WebTarget target = client.target("http://spotlight.dbpedia.org/rest/annotate");
        Form form = new Form();
        form.param("text", text)
            .param("confidence", "0.0")
            .param("support", "0")
            .param("spotter", "CoOccurrenceBasedSelector") //params.add("spotter", "NESpotter");
            .param("disambiguator", "Default")
            .param("policy", "whitelist");

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
    
    public List<TEntity> parse(String json, OntologyType otype)
    {
        List<TEntity> result = new LinkedList<TEntity>();
        
        try {
            JSONObject o = new JSONObject(json);  

            // check whether it retrieves an empty result set
            boolean found = false;
            Iterator<?> itr = o.keys(); 
            while(itr.hasNext())
                if(itr.next().equals("Resources"))
                    found = true;
            
            if(!found) return result;
            
            JSONArray jsonarray = o.getJSONArray("Resources");
            for(int i=0; i<jsonarray.length(); i++) 
            {
                JSONObject jo = jsonarray.getJSONObject(i);
                String label = jo.getString("@surfaceForm");
                
                String type = (jo.getString("@types").equals("")) ? null : jo.getString("@types");
                //type = (type==null) ? null : type.split(",")[type.split(",").length - 1];
                //we select the most specific DBpedia type (the first)
                if(type != null) {
                    //if not start with DBpedia, then we consider false
                    if(type.startsWith("DBpedia")) 
                    {
                        if(type.split(",").length>1) 
                            type = type.split(",")[0];
                        //now we remove DBpedia
                        type = type.split(":")[1];
                    }
                    else
                        type= null;
                }
                String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, type).toString();
                String uri = jo.getString("@URI");
                double confidence = jo.getDouble("@similarityScore");
                
                int startchar = jo.getInt("@offset");
                int endchar = startchar+label.length();
                
                TEntity extraction = new TEntity(label, type, uri, 
                                nerdType.toString(), startchar, endchar, confidence, SOURCE);
                     
                result.add(extraction);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }       
        return result;
    }

}
