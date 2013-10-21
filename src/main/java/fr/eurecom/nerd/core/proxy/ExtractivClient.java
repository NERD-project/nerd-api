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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartBase;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.extractiv.Document;
import com.extractiv.ExtractivJSONParser;

import fr.eurecom.nerd.core.db.table.DocumentType;
import fr.eurecom.nerd.core.db.table.TDocument;
import fr.eurecom.nerd.core.db.table.TEntity;
import fr.eurecom.nerd.core.logging.LogFactory;
import fr.eurecom.nerd.core.ontology.OntoFactory;
import fr.eurecom.nerd.core.ontology.OntologyType;
import fr.eurecom.nerd.core.srt.SRTMapper;
import fr.eurecom.nerd.core.utils.PropFactory;

public class ExtractivClient implements IClient {
    
    private static HttpClient httpClient;
    private static final String EXTRACTIV_SERVER_LOCATION = 
                            "http://rest.extractiv.com/extractiv/json/";
    
    private static String SOURCE = Extractor.getName(ExtractorType.EXTRACTIV);
    
    public List<TEntity> extract(TDocument document, String key, OntologyType otype) 
    {
        if(document.getText()==null) return null;
        
        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
        
        String serviceKey = (key!=null) ? key : 
            PropFactory.config.getProperty("fr.eurecom.nerd.extractor.extractiv.key");
        
        //it works only with english documents
        //language field discarded
        List<TEntity> result = parse (document.getText(), serviceKey, otype);
        
        //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
        if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
            SRTMapper srt = new SRTMapper();
            result = srt.run(document, result);
        }
                
        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
        return result;
    }
    private List<TEntity> parse (String text, String serviceKey, OntologyType otype)
    {
        List<TEntity> result = new LinkedList<TEntity>();
        URI endpoint;
        try {
            endpoint = new URI(EXTRACTIV_SERVER_LOCATION);
            HttpMethodBase extractivRequest = getExtractivProcessString(endpoint, text, serviceKey);
            InputStream extractivResults = fetchHttpRequest(extractivRequest);
            Readable jsonReadable = new InputStreamReader(extractivResults);
            ExtractivJSONParser jsonParser = new ExtractivJSONParser(jsonReadable);

            Map<String, Integer> map = new HashMap<String, Integer>();
            for(Document document : jsonParser)
                for(com.extractiv.Entity item : document.getEntities()) 
                {
                    String label = item.asString();
                    String type = item.getType();
                    String nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, type).toString();
                    String uri = (item.getLinks().size() > 0) ? item.getLinks().get(0) : "null";
//                    Integer startChar = item.getOffset();
//                    Integer endChar = startChar + item.getCharLength();
//                    TEntity extraction = new TEntity(label, type, uri, nerdType, 
//                    startChar, endChar, confidence, SOURCE); 
//                    result.add(extraction);
                    
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
                        
                        Double confidence = 0.5;
                      
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
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (BadInputException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
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
    
    private static PostMethod getExtractivProcessString(
                                final URI extractivURI, 
                                final String content,
                                final String serviceKey) throws FileNotFoundException 
    {

        final PartBase filePart = new StringPart("content", content, null);

        // bytes to upload
        final ArrayList<Part> message = new ArrayList<Part>();
        message.add(filePart);
        message.add(new StringPart("formids", "content"));
        message.add(new StringPart("output_format", "JSON"));
        message.add(new StringPart("api_key", serviceKey));
   
        final Part[] messageArray = message.toArray(new Part[0]);

        // Use a Post for the file upload
        final PostMethod postMethod = new PostMethod(extractivURI.toString());
        postMethod.setRequestEntity(new MultipartRequestEntity(messageArray, postMethod.getParams()));
        postMethod.addRequestHeader("Accept-Encoding", "gzip"); // Request the response be compressed (this is highly recommended)

        return postMethod;
    }

    /**
     * Get the output from the REST server
     */
    private static InputStream fetchHttpRequest(final HttpMethodBase method) throws BadInputException {
        try {

            final int statusCode = getClient().executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                throw new BadInputException("webpage status " + HttpStatus.getStatusText(statusCode) + "(" + statusCode + ")");
            }

            final InputStream is = new ByteArrayInputStream(method.getResponseBody());
            final GZIPInputStream zippedInputStream = new GZIPInputStream(is);
            return zippedInputStream;
        }
        catch (HttpException e) {
            throw new BadInputException("Fatal protocol violation " + e.getMessage(), e);
        }
        catch (IOException e) {
            //e.g. www.google.assadsaddsa
            throw new BadInputException("Fatal transport error " + e.getMessage(), e);
        }
        finally {
            method.releaseConnection();
        }
    }

    private static HttpClient getClient() {
        if (httpClient == null) {
            // snuff the getBody() HTTPClient warnings
            final Logger logger = Logger.getLogger("org.apache.commons.httpclient");
            logger.setLevel(Level.SEVERE);
            httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        }
        return httpClient;
    }

    public static class BadInputException extends Exception 
    {
        private static final long serialVersionUID = 1L;
        private Exception e;

        public BadInputException(final String msg) {
            super(msg);
        }

        public BadInputException(final String msg, Exception e) {
            super(msg, e);
            this.e = e;
        }

        public Exception getException() {
            return e;
        }
    }   
}
