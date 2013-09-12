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

package fr.eurecom.nerd.db.table;

import java.util.List;

public class TAnnotation 
{
    private Integer idAnnotation;
    private Integer documentIdDocument;
    private Integer toolIdTool;
    private Integer userIdUser;
    private String timestamp;
    
    private List<TEntity> entities;
    
    public TAnnotation(int idAnnotation, List<TEntity> entities) 
    {
        this.setIdAnnotation(idAnnotation);
        this.setEntities(entities);
    }
    
    public TAnnotation( int idAnnotation, int idDocument, 
                        int idTool, int idUser, String timestamp) 
    {
        this.setIdAnnotation(idAnnotation);
        this.setDocumentIdDocument(idDocument);
        this.setToolIdTool(idTool);
        this.setUserIdUser(idUser);
        this.setTimestamp(timestamp);
    }

    public TAnnotation(int idAnnotation, int idDocument, int idTool, int idUser) 
    {
        this.setIdAnnotation(idAnnotation);

        this.setDocumentIdDocument(idDocument);
        this.setToolIdTool(idTool);
        this.setUserIdUser(idUser);
    }

    public Integer getIdAnnotation() {
        return idAnnotation;
    }

    public void setIdAnnotation(Integer idAnnotation) {
        this.idAnnotation = idAnnotation;
    }

    public Integer getToolIdTool() {
        return toolIdTool;
    }

    public void setToolIdTool(Integer toolIdTool) {
        this.toolIdTool = toolIdTool;
    }

    public Integer getUserIdUser() {
        return userIdUser;
    }

    public void setUserIdUser(Integer userIdUser) {
        this.userIdUser = userIdUser;
    }

    public List<TEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<TEntity> entities) {
        this.entities = entities;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getDocumentIdDocument() {
        return documentIdDocument;
    }

    public void setDocumentIdDocument(Integer documentIdDocument) {
        this.documentIdDocument = documentIdDocument;
    }
    
}
