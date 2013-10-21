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

import java.util.Comparator;

import com.google.gson.annotations.Expose;

public class TEntity implements Comparable<TEntity> {
    @Expose private Integer idEntity;
    @Expose private String label;
    @Expose private Integer startChar;
    @Expose private Integer endChar;
    @Expose private String extractorType;
    @Expose private String nerdType;
    @Expose private String uri;
    @Expose private Double confidence = 0.5;
    @Expose private Double relevance = 0.5;
    @Expose private String extractor;
    @Expose private Double startNPT;
    @Expose private Double endNPT;
    
    public TEntity (     String label, 
                         String extractorType, 
                         String uri, 
                         String nerdtype, 
                         Double confidence,
                         String source
                   )
    {
        this.setLabel(label);
        this.setExtractorType(extractorType);
        this.setURI(uri);
        this.setNerdType(nerdtype);
        this.setConfidence(confidence);
        this.setExtractor(source);
    }
    
    public TEntity(     String label, 
                        String extractorType, 
                        String uri, 
                        String nerdType, 
                        Integer startchar, 
                        Integer endchar, 
                        Double confidence, 
                        Double relevance,
                        String source
                  ) 
    {
        this(label,extractorType,uri,nerdType,confidence, source); 
        this.setStartChar(startchar);
        this.setEndChar(endchar);
        this.setRelevance(relevance);
    }
    
    public TEntity(     String label, 
                        String extractorType, 
                        String uri, 
                        String nerdType, 
                        Integer startchar, 
                        Integer endchar, 
                        Double confidence, 
                        String source
                   ) 
    {
        this(label,extractorType,uri,nerdType,confidence, source); 
        this.setStartChar(startchar);
        this.setEndChar(endchar);
    }

    public TEntity(     String label, 
                        String extractorType, 
                        String uri, 
                        String nerdType, 
                        Integer startChar, 
                        Integer endChar, 
                        Double confidence,
                        Double relevance, 
                        String source,
                        Double startNPT, 
                        Double endNPT
                   ) 
    {
        this(label,extractorType,uri,nerdType, confidence, source); 
        this.setStartChar(startChar);
        this.setEndChar(endChar);
        this.setStartNPT(startNPT);
        this.setEndNPT(endNPT);
        this.setRelevance(relevance);
    }

    public TEntity(     int idEntity, 
                        String label, 
                        String extractorType,
                        String uri, 
                        String nerdType, 
                        Integer startChar,
                        Integer endChar, 
                        double confidence, 
                        double relevance,
                        String source, 
                        Double startNPT, 
                        Double endNPT
                    ) 
    {
        this(label,extractorType,uri,nerdType,startChar,
             endChar,confidence,relevance,source,startNPT,endNPT);
        this.setIdEntity(idEntity);
    }

    public TEntity() { }

    public int getIdEntity() {
        return idEntity;
    }

    public void setIdEntity(int idEntity) {
        this.idEntity = idEntity;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getExtractorType() {
        return extractorType;
    }

    public void setExtractorType(String extractorType) {
        this.extractorType = extractorType;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = (confidence==null) ? 0.0 : confidence;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }
    
    public String getNerdType() {
        return nerdType;
    }

    public void setNerdType(String nerdType) {
        this.nerdType = nerdType;
    }
    
    public String getTriple () 
    {
        return  getLabel() + ";" + getExtractorType() + ";" + getURI();
    }

    public int compareTo(TEntity extraction) 
    {
        return 0;
    }

    public Integer getEndChar() {
        return endChar;
    }

    public void setEndChar(Integer endchar) {
        this.endChar = endchar;
    }

    public Integer getStartChar() {
        return startChar;
    }

    public void setStartChar(Integer startchar) {
        this.startChar = startchar;
    }

    public Double getRelevance() {
        return relevance;
    }

    public void setRelevance(Double relevance) {
        this.relevance = relevance;
    }
    
    public String getExtractor() {
        return extractor;
    }

    public void setExtractor(String source) {
        this.extractor = source;
    }

    public Double getEndNPT() {
        return endNPT;
    }

    public void setEndNPT(Double endNPT) {
        this.endNPT = endNPT;
    }


    public Double getStartNPT() {
        return startNPT;
    }

    public void setStartNPT(Double startNPT) {
        this.startNPT = startNPT;
    }

    public String toString() 
    {
        return label.concat(";")
                .concat(nerdType)
                .concat(";")
                .concat(uri)
                .concat("-[")
                .concat(startChar.toString())
                .concat(",")
                .concat(endChar.toString())
                .concat("]");
    }


    /*
     *  Comparators
     */
    public static final Comparator<TEntity> NERDTYPE =
            new Comparator<TEntity>() 
            {
                public int compare(TEntity e1, TEntity e2) {
                    return e1.getNerdType().toString().compareToIgnoreCase(e2.getNerdType().toString());
            }
         };
                
               
    public static final Comparator<TEntity> LABEL =
        new Comparator<TEntity>() 
        {
             public int compare(TEntity e1, TEntity e2) {  
                 return e1.getLabel().compareTo(e2.getLabel());
             }
        };
        
    public static final Comparator<TEntity> EXTRACTORTYPE =
        new Comparator<TEntity>() 
        {
             public int compare(TEntity e1, TEntity e2) {  
                 return e1.getExtractorType().compareTo(e2.getExtractorType());
             }
        };
        
    public static final Comparator<TEntity> ENTITYPOSITION = 
        new Comparator<TEntity>() {
            public int compare(TEntity e1, TEntity e2) {  
                int position = e1.getStartChar().compareTo(e2.getStartChar());
                if(position == 0) 
                    position = e2.getEndChar().compareTo(e1.getEndChar());
                
                return position;
            }
        };
}
