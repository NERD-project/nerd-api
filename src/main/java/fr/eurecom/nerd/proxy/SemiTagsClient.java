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

import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import fr.eurecom.nerd.db.table.DocumentType;
import fr.eurecom.nerd.db.table.TDocument;
import fr.eurecom.nerd.db.table.TEntity;
import fr.eurecom.nerd.logging.LogFactory;
import fr.eurecom.nerd.ontology.OntoFactory;
import fr.eurecom.nerd.ontology.OntologyType;
import fr.eurecom.nerd.srt.SRTMapper;

public class SemiTagsClient implements IClient {

    private static String ENDPOINT_SECURE = "https://ner.vse.cz/SemiTags/rest/v1/recognize";
    private static String SOURCE = Extractor.getName(ExtractorType.SEMITAGS);
    
    public List<TEntity> extract(TDocument document, String key, OntologyType otype) 
    {
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
        
        String xml = post(document.getText(), document.getLanguage());       

        List<TEntity> result = parse(xml, otype);
        
        //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
        if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
            SRTMapper srt = new SRTMapper();
            result = srt.run(document, result);
        }
        
        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    
    private String post(String text, String language) 
    {    
        SSLContext ssl;
        try {
            ssl = SSLContext.getInstance("SSL");
            TrustManager[ ] certs = new TrustManager[ ] {
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }
                    }
            };
            ssl.init(null, certs, new java.security.SecureRandom());
            
            Client client = ClientBuilder.newBuilder().sslContext(ssl).build();
            
            
            WebTarget target = client.target(ENDPOINT_SECURE);
            Form form = new Form();
            form.param("text", text)
                .param("language", language);

            Response response = target
                                .request(MediaType.APPLICATION_XML_TYPE)
                                .post(Entity.entity(
                                       form, 
                                       MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                                 );
            
            return response.readEntity(String.class);
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }     
        
        return null;
    }
    
    
    private List<TEntity> parse(String xml, OntologyType otype) 
    {
        List<TEntity> result = new LinkedList<TEntity>();
        
        Document document;
        try {
            document = loadXMLFromString(xml);
            Element docEle = document.getDocumentElement();

            NodeList nl = docEle.getElementsByTagName("namedEntity");
            if(nl != null && nl.getLength() > 0) {
                for(int i = 0 ; i < nl.getLength();i++) 
                {
                    Element el = (Element)nl.item(i);
                    result.addAll(getExtraction(el, otype));
                }
            }        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    
    private List<TEntity> getExtraction(Element el, OntologyType otype) 
    {    
        List<TEntity> result = new LinkedList<TEntity>();
        
        // entity value
        String label = getTextValue(el,"name");
        String type = getTextValue(el, "type");
        String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, type).toString();
        String uri = getTextValue(el, "dbpediaUri");
        Double confidence = getDoubleValue(el, "confidence");
        
        NodeList nl = el.getElementsByTagName("occurrence");
        for(int i=0; nl!=null && i < nl.getLength(); i++) {
            Element tmp = (Element)nl.item(i);
            Integer startChar = Integer.parseInt( tmp.getAttribute("start") );
            Integer endChar = Integer.parseInt( tmp.getAttribute("end") );
                        
            result.add(new TEntity(label, type, uri, nerdType, startChar, endChar + 1, confidence, SOURCE));
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
    
    private double getDoubleValue(Element el, String tagName) {
        return Double.parseDouble(getTextValue(el,tagName));
    }
    
    private Document loadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}
