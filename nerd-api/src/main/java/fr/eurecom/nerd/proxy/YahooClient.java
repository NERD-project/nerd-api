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

import java.io.IOException;
import java.io.StringReader;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.eurecom.nerd.db.table.DocumentType;
import fr.eurecom.nerd.db.table.TDocument;
import fr.eurecom.nerd.db.table.TEntity;
import fr.eurecom.nerd.logging.LogFactory;
import fr.eurecom.nerd.ontology.OntoFactory;
import fr.eurecom.nerd.ontology.OntologyType;
import fr.eurecom.nerd.srt.SRTMapper;

public class YahooClient implements IClient {
    
    private static String SOURCE = Extractor.getName(ExtractorType.YAHOO);
    private Map<String, Integer> map = new HashMap<String, Integer>();
    
    public List<TEntity> extract(TDocument document, String key, OntologyType otype) 
    {
        if(document.getText() == null) return null;
        
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
        
        // example
        // curl -X POST -i http://query.yahooapis.com/v1/public/yql -d "q=select * from contentanalysis.analyze where text='Italian sculptors and painters of the renaissance favored the Virgin Mary for inspiration'"
        String endpoint = "http://query.yahooapis.com/v1/public/yql";
        
        //discard language information
        //language = null
        
//        Client client = Client.create();        
//        WebResource webResource = client.resource(endpoint);
//        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
       
        Client client = ClientBuilder.newClient();     
        WebTarget target = client.target(endpoint);
        
        String syntaxpattern = "[^A-Za-z0-9.?!]";
        String text = document.getText().replaceAll(syntaxpattern, " ");
        
        String spacepattern = "[ ]+";
        text = text.replaceAll(spacepattern, " ");
         
        // yahoo uses the YQL 
        String query = "select * from contentanalysis.analyze where text='" + 
                        text + "'";
        
        Form form = new Form();
        form.param("q", query);
      
        Response response = target
                            .request(MediaType.APPLICATION_XML_TYPE)
                            .post(Entity.entity(
                                   form, 
                                   MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                             );
        
            
        String answer = response.readEntity(String.class);
        
        
        List<TEntity> result = null;
              
        try {          
            result = parse(answer, document.getText(), otype);
            
            //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
            if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
                SRTMapper srt = new SRTMapper();
                result = srt.run(document, result);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    
    private List<TEntity> parse(String xmlString, String text, OntologyType otype) 
    throws ParserConfigurationException, SAXException, IOException 
    {
        List<TEntity> result = new LinkedList<TEntity>();
        
        //get the root element
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlString)));
        Element docEle = document.getDocumentElement();
    
        NodeList nl = docEle.getElementsByTagName("entity");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength(); i++) 
            {
                Element el = (Element)nl.item(i);
                TEntity e = getExtraction(el,text, result, otype);
                if(e!=null) result.add( e );
            }
        }
        return result;
    }
    
    private TEntity getExtraction(Element el, String text, List<TEntity> result, OntologyType otype) 
    {        
        //for each <entity> element get values...
        String label = getTextValue(el, "text");
        
    //    String sStartChar = getAttributeFromTag(el,"text","startchar");
    //    Integer startChar = Integer.parseInt(sStartChar);
    //    String sEndChar = getAttributeFromTag(el,"text","endchar");
    //    Integer endChar = Integer.parseInt(sEndChar);
        
        String uri = getTextValue(el, "wiki_url");
        Double confidence = (el.getAttribute("score")!=null) ? 
                Double.parseDouble(el.getAttribute("score")) : 1;
    
        String type = null;
        NodeList nl = el.getElementsByTagName("types");    
        if(nl != null && nl.getLength() > 0) {
            Element element = (Element)nl.item(0);
            type = getTextValue(element, "type");
        }
        // type is in the format /location/city
        // we got just the last entry
        if(type!=null) {
            String[] tree_type = type.split("/");
            type = tree_type[tree_type.length - 1];
        }
        
        String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, type).toString();
                  
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
                return new TEntity(label, type, uri, nerdType.toString(), startchar, endchar,confidence,SOURCE);
            
        }catch(PatternSyntaxException eregex) {
            eregex.printStackTrace();
        }
        return null;
    }
    
    private String getAttributeFromTag(Element el, String tagName, String attribute) 
    {
        String value = null;
        NodeList nl = el.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element temp = (Element)nl.item(0);
            value = temp.getAttribute(attribute);
        }
        return value;
    }
    
    private String getTextValue(Element el, String tagName) {
        String textVal = null;
        NodeList nl = el.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element temp = (Element)nl.item(0);
            textVal = temp.getFirstChild().getNodeValue();
        }
        return textVal;
    } 
    
    private boolean containsAtIndex(List<TEntity> result, Integer startChar, Integer endChar) 
    {
        for (TEntity e : result) 
            if(e.getStartChar() == startChar || e.getEndChar() == endChar)
                return true;
        return false;
    } 
}
