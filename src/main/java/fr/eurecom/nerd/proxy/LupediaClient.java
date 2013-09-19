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

import fr.eurecom.nerd.db.table.DocumentType;
import fr.eurecom.nerd.db.table.TDocument;
import fr.eurecom.nerd.db.table.TEntity;
import fr.eurecom.nerd.exceptions.ClientException;
import fr.eurecom.nerd.logging.LogFactory;
import fr.eurecom.nerd.ontology.OntoFactory;
import fr.eurecom.nerd.ontology.OntologyType;
import fr.eurecom.nerd.srt.SRTMapper;

public class LupediaClient implements IClient {
    
    private static String SOURCE = Extractor.getName(ExtractorType.LUPEDIA);
       
    public List<TEntity> extract(TDocument document, String key, OntologyType otype) 
    {
        if(document.getText()==null) return null;
        
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
        
        List<TEntity> result = null;
        String json;
        try {
            json = post(document.getText(), document.getLanguage());
            result = parse(json, document.getText(), otype);        
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
        if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
            SRTMapper srt = new SRTMapper();
            result = srt.run(document, result);
        }
        
        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    
    private String post(String text, String language) throws ClientException 
    {
        // CLI
        // curl -X POST http://lupedia.ontotext.com/lookup/text2json -d "lookupText='Italian poet and novelist, b. at Milan, 7 March, 1785; d. 22 May, 1873. He was the son of Pietro Manzoni, the representative of an old feudal family of provincial'";
        
        String endpoint = "http://lupedia.ontotext.com/lookup/text2json";
        language = (language==null) ? "en" : language;
        
        Client client = ClientBuilder.newClient();     
        WebTarget target = client.target(endpoint);
        Form form = new Form();
        form.param("lookupText", text)
            .param("lang", language);

        Response response = target
                            .request(MediaType.APPLICATION_JSON_TYPE,
                                     MediaType.APPLICATION_XML_TYPE)
                            .post(Entity.entity(
                                   form, 
                                   MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                             );
                
        if( response.getStatus() != 200 ) 
            throw new ClientException("Extractor: " + SOURCE + " is temporary not available.");
                
        return response.readEntity(String.class);

    }

    private List<TEntity> parse(String json, String text, OntologyType otype) 
    {
        List<TEntity> result = new LinkedList<TEntity>();
        try {
            JSONArray jsonarray = new JSONArray(json);

            for(int i=0; i<jsonarray.length(); i++) 
            {
                JSONObject jo = jsonarray.getJSONObject(i);
                int startOffset = jo.getInt("startOffset");
                int endOffset = jo.getInt("endOffset");
                String label = text.substring(startOffset, endOffset);
                
                String type = null;
                String uriType = jo.getString("instanceClass").replace("\\", "");
                if(uriType!=null) {
                    String[] tree_type = uriType.split("/");
                    type = tree_type[tree_type.length -1];
                }
                String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, type).toString();
                
                String uri = jo.getString("instanceUri").replace("\\", "");
                double confidence = jo.getDouble("weight");

                TEntity extraction = 
                        new TEntity(label,type,uri,nerdType.toString(),
                                        startOffset,endOffset, confidence, SOURCE);
                result.add(extraction);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }       
        return result;
    }
}
