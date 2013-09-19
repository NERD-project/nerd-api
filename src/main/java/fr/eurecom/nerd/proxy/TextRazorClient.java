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

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.eurecom.nerd.db.table.DocumentType;
import fr.eurecom.nerd.db.table.TDocument;
import fr.eurecom.nerd.db.table.TEntity;
import fr.eurecom.nerd.exceptions.ClientException;
import fr.eurecom.nerd.logging.LogFactory;
import fr.eurecom.nerd.ontology.OntologyType;
import fr.eurecom.nerd.srt.SRTMapper;

public class TextRazorClient implements IClient 
{
    private static String SOURCE = Extractor.getName(ExtractorType.TEXTRAZOR);
    private static String ENDPOINT = "http://api.textrazor.com/";
    
    public List<TEntity> extract( TDocument document, 
                                  String key, 
                                  OntologyType otype) 
    throws ClientException 
    {
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
        
        String json = post(key, document.getText());
        List<TEntity> result = parse (json, document.getText(), otype);
      
        //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
        if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
            SRTMapper srt = new SRTMapper();
            result = srt.run(document, result);
        }

        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    
    private List<TEntity> parse(String json, String text, OntologyType otype) 
    {
        List<TEntity> result = new LinkedList<TEntity>();
        Gson gson = new Gson();
        
        try {
            JSONObject o = new JSONObject(json);
            JSONObject response = o.getJSONObject("response");
            String entityjson = response.getJSONArray("entities").toString();

            //System.out.println(entityjson);
            Type listType = new TypeToken<LinkedList<TextRazorEntity>>() {}.getType();
            List<TextRazorEntity>  entities = gson.fromJson(entityjson, listType); 
            for(TextRazorEntity e : entities) 
            {
                Integer startChar = e.getStartingPos();
                Integer endChar = e.getEndingPos();
                String label = text.substring(startChar, endChar);
                
                // we relax a bit this constraint
                //if( ! label.equalsIgnoreCase(e.getMatchedText()) )
                //    continue;

                String uri = e.getWikiLink();
                String extractorType = "";
                Boolean found = false;
                if(e.getType()!=null) {
                    extractorType="DBpedia:";
                    for(String t : e.getType())
                        extractorType += t.concat(",");
                    extractorType = extractorType.substring(0, extractorType.length()-1);
                    found = true;
                }
                if (e.getFreebaseTypes() != null){
                    if (found) extractorType +=";";
                    extractorType += "Freebase:";
                    for(String t : e.getFreebaseTypes())
                        extractorType += t.concat(",");
                    extractorType = extractorType.substring(0, extractorType.length()-1);
                    found = true;
                }
                if(!found) extractorType = "null";
                
                //FIXME
                //String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, extractorType).toString();
                //patch for classifying entities according to the main 3 types
                String nerdType = "http://nerd.eurecom.fr/ontology#Thing";
                if (extractorType != null) {
                    if ( extractorType.indexOf("/organization/organization_")!= -1) {
                        nerdType = "http://nerd.eurecom.fr/ontology#Thing";
                    }
                    else if(extractorType.indexOf("Person")!= -1 || extractorType.indexOf("/people/person")!=-1)
                        nerdType = "http://nerd.eurecom.fr/ontology#Person";
                    else if(extractorType.indexOf("Organization")!= -1 || extractorType.indexOf("Organisation")!= -1 || extractorType.indexOf("/organization/organization")!=-1)
                        nerdType = "http://nerd.eurecom.fr/ontology#Organization";
                    else if (extractorType.indexOf("Place")!= -1 || 
                             extractorType.indexOf("AdministrativeRegion")!= -1 || 
                             extractorType.indexOf("AdministrativeArea")!= -1 || 
                             extractorType.indexOf("Settlement")!= -1 || 
                             extractorType.indexOf("/location/statistical_region")!= -1 || 
                             extractorType.indexOf("/location/citytown")!= -1 ||
                             extractorType.indexOf("/location/location")!= -1 )
                        nerdType = "http://nerd.eurecom.fr/ontology#Location";
                }
                
                //FIXME
                Double confidence = e.getConfidenceScore() / 4;
                Double relevance = e.getRelevanceScore();
                
                TEntity entity = new TEntity(
                                             label,
                                             extractorType,
                                             uri,
                                             nerdType,
                                             startChar,
                                             endChar,
                                             confidence,
                                             relevance,
                                             SOURCE
                                            );
                //System.out.println(entity.toString());
                result.add(entity);
                
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }  
        return result;
    }

    private String post(String key, String text) 
    throws ClientException 
    {
        // CLI
        // curl -i -X POST -d "apiKey=d79480bfd0b17e8cff686532f44ab7ea9d9eac0d98e531c94a0ea1ba" -d "extractors=entities" -d "text=Spain's stricken Bankia expects to sell off its vast portfolio of industrial holdings that includes a stake in the parent company of British Airways and Iberia." http://api.textrazor.com/
        
        Client client = ClientBuilder.newClient();     
        WebTarget target = client.target(ENDPOINT);
        Form form = new Form();
        form.param("apiKey", key)
            .param("extractors", "entities")
            .param("text", text);

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
    
    
    class TextRazorEntity {
        private Integer id;
        private List<String> type;
        private List<Integer> matchingTokens;
        private String entityId;
        private List<String> freebaseTypes;
        private Double confidenceScore;
        private String wikiLink;
        private String matchedText;
        private String freebaseId;
        private Double relevanceScore;
        private String entityEnglishId;
        private Integer startingPos;
        private Integer endingPos;
        
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        
        public List<String> getType() {
            return type;
        }
        public void setType(List<String> type) {
            this.type = type;
        }
        public List<Integer> getMatchingTokens() {
            return matchingTokens;
        }
        public void setMatchingTokens(List<Integer> matchingTokens) {
            this.matchingTokens = matchingTokens;
        }
        public String getEntityId() {
            return entityId;
        }
        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }
        public List<String> getFreebaseTypes() {
            return freebaseTypes;
        }
        public void setFreebaseTypes(List<String> freebaseTypes) {
            this.freebaseTypes = freebaseTypes;
        }
        public Double getConfidenceScore() {
            return confidenceScore;
        }
        public void setConfidenceScore(Double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }
        public String getWikiLink() {
            return wikiLink;
        }
        public void setWikiLink(String wikiLink) {
            this.wikiLink = wikiLink;
        }
        public String getMatchedText() {
            return matchedText;
        }
        public void setMatchedText(String matchedText) {
            this.matchedText = matchedText;
        }
        public String getFreebaseId() {
            return freebaseId;
        }
        public void setFreebaseId(String freebaseId) {
            this.freebaseId = freebaseId;
        }
        public Double getRelevanceScore() {
            return relevanceScore;
        }
        public void setRelevanceScore(Double relevanceScore) {
            this.relevanceScore = relevanceScore;
        }
        public String getEntityEnglishId() {
            return entityEnglishId;
        }
        public void setEntityEnglishId(String entityEnglishId) {
            this.entityEnglishId = entityEnglishId;
        }
        public Integer getStartingPos() {
            return startingPos;
        }
        public void setStartingPos(Integer startingPos) {
            this.startingPos = startingPos;
        }
        public Integer getEndingPos() {
            return endingPos;
        }
        public void setEndingPos(Integer endingPos) {
            this.endingPos = endingPos;
        }      
    }
}
