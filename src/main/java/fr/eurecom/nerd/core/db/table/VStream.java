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

public class VStream {
    private String date;
    private String documentUri;
    private String annotationUri;
    private String extractorName;
    private String ontology;
    private Integer entityNumber;
    private Integer categoryNumber;
    
    public VStream (String date, 
                    String documentUri, 
                    String annotationUri,
                    String extractorName, 
                    String ontology,
                    Integer entityNumber,
                    Integer categoryNumber)
    {
        this.date = date;
        this.documentUri = documentUri;
        this.annotationUri = annotationUri;
        this.extractorName = extractorName;
        this.ontology = ontology;
        this.entityNumber = entityNumber;
        this.categoryNumber = categoryNumber;
    }
    
    public Integer getEntityNumber() {
        return entityNumber;
    }
    public void setEntityNumber(Integer entityNumber) {
        this.entityNumber = entityNumber;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getExtractorName() {
        return extractorName;
    }
    public void setExtractorName(String extractorName) {
        this.extractorName = extractorName;
    }
    public Integer getCategoryNumber() {
        return categoryNumber;
    }
    public void setCategoryNumber(Integer categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    public String getAnnotationUri() {
        return annotationUri;
    }

    public void setAnnotationUri(String annotationUri) {
        this.annotationUri = annotationUri;
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    public String getOntology() {
        return ontology;
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }
    
}
