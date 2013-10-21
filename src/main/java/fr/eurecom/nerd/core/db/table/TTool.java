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

package fr.eurecom.nerd.core.db.table;

public class TTool {
    private int idTool;
    private String name;
    private String URIWebServer;
    private int extractions;
    
    public TTool(int idTool, String name, String URIWebServer) 
    {
        this.setIdTool(idTool);
        this.setName(name);
        this.setURIWebServer(URIWebServer);
    }
    
    public TTool(String name, int extractions) 
    {
        this.setName(name);
        this.setExtractions(extractions);
    }
    
    public int getIdTool() {
        return idTool;
    }
    public void setIdTool(int idTool) {
        this.idTool = idTool;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getURIWebServer() {
        return URIWebServer;
    }
    public void setURIWebServer(String uRIWebServer) {
        URIWebServer = uRIWebServer;
    }

    public int getExtractions() {
        return extractions;
    }

    public void setExtractions(int extractions) {
        this.extractions = extractions;
    }
}
