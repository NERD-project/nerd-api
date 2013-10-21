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

package fr.eurecom.nerd.core.ontology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import fr.eurecom.nerd.core.utils.PropFactory;

public class Mapper {

    private HashMap<String, String> core;
    private HashMap<String, String> extended;
  
    public static Mapper getMapper () {
        return new Mapper();
    }
      
    public void init(String fcore, String fextended) 
    {
        core = new HashMap<String, String>();
        extended = new HashMap<String, String>();
        String str;  
        
        try {
            /* read core ontology */
            BufferedReader inputCore = 
                new BufferedReader( new FileReader(fcore) );
          
            while ((str = inputCore.readLine()) != null) {
                if(!str.startsWith("#")) {
                    String[] match = str.split(";");
                    core.put(match[0].toLowerCase(), match[1]);
                }
            }
            inputCore.close();      
            
            /* read extended ontology */
            BufferedReader inputExtended = 
                    new BufferedReader( new FileReader(fextended) );         
            while ((str = inputExtended.readLine()) != null) {
                if(!str.startsWith("#")) {
                    String[] match = str.split(";");
                    extended.put(match[0].toLowerCase(), match[1]);
                }
            }
            inputExtended.close();      
                            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public URI getNerdType (OntologyType otype, String entity, String extractor, String type) 
    {
        String baseuri = "http://nerd.eurecom.fr/ontology#";
        
        // no type provided, then we assume it inherits from Thing
        if(type == null) 
            return URI.create(baseuri + "Thing");
                 
        
        HashMap<String, String> ontology = null;
        switch(otype) {
        case CORE:
            ontology = core;
            break;
        case EXTENDED:
            ontology = extended;
            break;
        }
        
        String normalizedType = type.toLowerCase();
        String normalizedDataset = ( extractor.equals("dbspotlight") || 
                                     extractor.equals("lupedia") ||
                                     extractor.equals("textrazor") ||
                                     extractor.equals("thd")
                                   ) 
                                   ? "dbpedia" : extractor;

        if(!ontology.containsKey(normalizedDataset.concat(":").concat(normalizedType)) )
        {
            FileWriter fstream;
            try {
                fstream = new FileWriter(
                    PropFactory.config.getProperty("fr.eurecom.nerd.ontology.log"),
                    true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(  normalizedDataset + ":" + type + 
                            " - [" + entity + "]" + 
                            " - (" + Ontology.getName(otype) + ")\n"
                         );
                out.close();
                fstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return URI.create(baseuri + "Thing");
        }
       
        return URI.create( baseuri + ontology.get(normalizedDataset.concat(":").concat(normalizedType)) );
    }
}
