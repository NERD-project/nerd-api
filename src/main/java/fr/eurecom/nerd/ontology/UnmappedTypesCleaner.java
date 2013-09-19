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

package fr.eurecom.nerd.ontology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import fr.eurecom.nerd.api.PropFactory;

public class UnmappedTypesCleaner {
      
    private static HashMap<String, String> loadSchema() 
    {
        HashMap<String, String> logalignments = new HashMap<String, String>();
        String str = null;
        try {
            BufferedReader in = 
                new BufferedReader(
                    new FileReader(PropFactory.config.getProperty("fr.eurecom.nerd.ontology.log"))
                                   );
            while ((str = in.readLine()) != null) {
                if(!str.startsWith("#")) {
                    String[] tempMapping = str.split("-");
                    System.out.println(str);
                    // schema tool - quaero
                    logalignments.put(tempMapping[0], tempMapping[1]);
                }
            }
            in.close();            
        } catch (IOException e) {
            System.out.println(str);
            e.printStackTrace();
        }
        return logalignments;
    }
    public static void main(String[] args) {
         HashMap<String, String> nerdTypes = loadSchema();  
         FileWriter fw;
        try {
            fw = new FileWriter(new File(PropFactory.config.getProperty("fr.eurecom.nerd.ontology.log")));
            BufferedWriter out = new BufferedWriter(fw);
            for(Entry<String,String> e : nerdTypes.entrySet()) {
                out.write( e.getKey() + "-" + e.getValue() + "\n");
            }
            out.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
