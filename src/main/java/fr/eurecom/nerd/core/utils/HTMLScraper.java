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

package fr.eurecom.nerd.core.utils;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alchemyapi.api.AlchemyAPI;

public class HTMLScraper {
    
    private String text = null;
    private String URI;  
    
    public String run(String uri)
    {
        String result = null;
        //BasicConfigurator.configure();
        this.URI = uri; 
        try {
            result = getText();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

           
    private String getText()
    throws  IOException, 
            SAXException, 
            ParserConfigurationException, 
            XPathExpressionException
    {
        // Create an AlchemyAPI object.
        System.out.println(PropFactory.config.getProperty("fr.eurecom.nerd.extractor.alchemyapi.key"));
        AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromString("3420f889c255a95339bd0b5ee64d5f478b2c61bf");

        // Extract page text from a web URL. (ignoring ads, navigation links, and other content).
        Document doc = null; 
        try{ 
            doc = alchemyObj.URLGetText(this.URI);
            doc.getDocumentElement().normalize();
        } catch(Exception e) {      
            e.printStackTrace();
        }
        NodeList nList = doc.getElementsByTagName("text").item(0).getChildNodes();
        Node txtNode = nList.item(0);
        
        if(txtNode == null)
            return null;
        
        text = txtNode.getNodeValue();
        return text;
    }
}
