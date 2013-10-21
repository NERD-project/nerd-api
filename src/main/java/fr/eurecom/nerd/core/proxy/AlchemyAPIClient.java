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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;
import com.alchemyapi.api.AlchemyAPI_NamedEntityParams;

import fr.eurecom.nerd.core.db.table.DocumentType;
import fr.eurecom.nerd.core.db.table.TDocument;
import fr.eurecom.nerd.core.db.table.TEntity;
import fr.eurecom.nerd.core.logging.LogFactory;
import fr.eurecom.nerd.core.ontology.OntoFactory;
import fr.eurecom.nerd.core.ontology.OntologyType;
import fr.eurecom.nerd.core.srt.SRTMapper;

public class AlchemyAPIClient implements IClient {

    private static String SOURCE = Extractor.getName(ExtractorType.ALCHEMYAPI);
    
    public List<TEntity> extract ( TDocument document, String key, OntologyType otype ) 
    {
        if (document.getText() == null) return null;
        
        //posted content is normalized / cleaned (removing ads, navigation links, 
        //and other unimportant content), the primary document language is 
        //detected, and named entities are extracted automatically.
        //language==null
        LogFactory.logger.info(SOURCE + " is going to extract entities from the document: " + document.getIdDocument());
       
        AlchemyAPI object = AlchemyAPI.GetInstanceFromString( key );        
        List<TEntity> result = null;
        try {
            AlchemyAPI_NamedEntityParams params = new AlchemyAPI_NamedEntityParams();
            params.setMaxRetrieve(1000);
            Document doc = object.TextGetRankedNamedEntities(document.getText(), params);
            
            //parse extractor result to extract TExtractor instances
            result = parse(doc, document.getText(), otype);
            
            //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
            if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
                SRTMapper srt = new SRTMapper();
                result = srt.run(document, result);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        
        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;        
    }
    
    private List<TEntity> parse(Document document, String text, OntologyType otype)
    {
        List<TEntity> result = new LinkedList<TEntity>();
        //get the root element
        Element docEle = document.getDocumentElement();

        NodeList nl = docEle.getElementsByTagName("entity");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) 
            {
                Element el = (Element)nl.item(i);
                
                for(TEntity extraction : getEntities(el, text, otype)) 
                    result.add( extraction );
            }
        }
        return result;
    } 
    
    private List<TEntity> getEntities(Element el, String text, OntologyType otype) 
    {    
        List<TEntity> result = new LinkedList<TEntity>();
        
        // entity value
        String label = getTextValue(el, "text");
        String type = getTextValue(el, "type");
        String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, type).toString();
        double confidence = getDoubleValue(el,"relevance");      
        
        //get disambiguated node
        String uri = null;
        NodeList nl = el.getElementsByTagName("disambiguated");
        if(nl != null && nl.getLength() > 0) {
            Element element = (Element)nl.item(0);
            uri = getTextValue(element, "website");
            if(uri==null) uri = getTextValue(element, "dbpedia");
            if(uri==null) uri = getTextValue(element, "yago");
        }

        // we are generating different version of NE
        int count = getIntValue(el, "count");
        //System.out.println("warn:" + entity);
        
        // exception handling because
        // some entities are wrong defined. For instance:
        // "commission mixte parit(aire" -> it's missing a parenthesis
        Map<String, Integer> map = new HashMap<String, Integer>();
        try {
            Pattern p = Pattern.compile ("\\b" + label + "\\b") ; 
            Matcher m = p.matcher (text); 
          
            // count is wrong output from AlchemyAPI
            for(int i=0; i<count && m.find(); i++) 
            {   
                //logic to compute the startchar and endchar of the entity within the text
                Integer startchar=null, endchar=null;
                if(map.containsKey(label)) {
                    int value = map.get(label);
                    map.remove(label);
                    map.put(label, new Integer(value+1));
                }
                else 
                    map.put(label, new Integer(1));
    
                for(int j=0; j < map.get(label); j++) {
                    startchar = m.start ( 0 );
                    endchar = m.end ( 0 ); 
                    if ( containsAtIndex(result, startchar, endchar) ) j--;
                }
                            
                result.add( new TEntity(label, type, uri, nerdType, startchar,
                                            endchar, confidence, SOURCE) 
                          );
            }
        }catch(RuntimeException e) {
            LogFactory.logger.log(Level.WARNING,"Regex failed for the entity:'" + label + "'");
        }
    
        return result;
    }
    
    private String getTextValue(Element ele, String tagName) 
    {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName) 
    {
        return Integer.parseInt(getTextValue(ele,tagName));
    }
    
    private double getDoubleValue(Element el, String tagName) {
        return Double.parseDouble(getTextValue(el,tagName));
    }
    
    private boolean containsAtIndex(List<TEntity> result, Integer startChar, Integer endChar) 
    {
        for (TEntity e : result) 
            if(e.getStartChar() == startChar || e.getEndChar() == endChar)
                return true;
        return false;
    }       
    
    @SuppressWarnings("unused")
    private String convertXMLToString(org.w3c.dom.Document doc)
    {
      try{
        StringWriter stw = new StringWriter();
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.transform(new DOMSource(doc), new StreamResult(stw));
        return stw.toString();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
        return null;
    }    
}
