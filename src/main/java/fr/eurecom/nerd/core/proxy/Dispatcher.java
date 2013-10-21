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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import fr.eurecom.nerd.core.db.SQL;
import fr.eurecom.nerd.core.db.table.TDocument;
import fr.eurecom.nerd.core.db.table.TEntity;
import fr.eurecom.nerd.core.exceptions.LanguageException;
import fr.eurecom.nerd.core.exceptions.NoContentException;
import fr.eurecom.nerd.core.exceptions.QuotaException;
import fr.eurecom.nerd.core.exceptions.RouterException;
import fr.eurecom.nerd.core.exceptions.TimeOutException;
import fr.eurecom.nerd.core.exceptions.TypeExpection;
import fr.eurecom.nerd.core.language.LangFactory;
import fr.eurecom.nerd.core.logging.LogFactory;
import fr.eurecom.nerd.core.ontology.Ontology;
import fr.eurecom.nerd.core.ontology.OntologyType;

public class Dispatcher {
    
    private SQL sql = new SQL();
    
    public Dispatcher () {}
    
    public int run (   int idUser, 
                       final String extractor, 
                       String ontology,
                       int idDocument, 
                       Long timeout,
                       Boolean force,
                       Boolean cache
                    ) 
    throws QuotaException, 
           TimeOutException, RouterException, 
           NoContentException, TypeExpection, 
           LanguageException 
    {
        LogFactory.logger.info("user= " + idUser + 
                               " requires to annotate the document= " + idDocument +
                               " with the extractor=" + extractor +
                               " normalizing the result according to the " + ontology + " ontology"
                              );
        
        //extractor is valid? throws the TypeException
        ExtractorType etype = null;
        try {
            etype = Extractor.getType(extractor);
        } catch (TypeExpection e) {
            throw e;
        }
                
        Integer idAnnotation = -1;
        Integer idTool = -1;
        List<TEntity> entities = new LinkedList<TEntity>(); //in case of timeout, the answer will be empty list
        TDocument document= null;
        
        OntologyType otype = Ontology.getType(ontology);
        
        try {
            //is language supported? it throws a LanguageException
            document = sql.selectDocument(idDocument);
            String language = document.getLanguage();
            if (!force) 
            {
                if(! LangFactory.detector.getExtractorLanguages().get(extractor).contains(language) )
                    throw new LanguageException("Extractor: " + extractor + 
                            " doesn't support the language: " + language);
            }
            
            idTool = sql.selectTool(extractor);
            if(cache) {
                //cache logic - we assume the extractor result set is time invariant
                if(sql.selectAnnotation(idDocument, idTool, Ontology.getName(otype))) {
                    entities = sql.getExtractionsAlreadyPerformed(idDocument,idTool);    
                    idAnnotation = sql.insertAnnotation(idDocument, idTool, idUser, Ontology.getName(otype));
                    sql.insertExtractions(entities, idAnnotation);
                    return idAnnotation;
                }
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
        
        //if extractor == combined, then launch the multithread combined strategy
        if(etype == ExtractorType.COMBINED) {
            DriverCombined driver = new DriverCombined();
            entities = driver.run(idUser, extractor, otype, document, timeout, idTool, force);
        }
        //for the other extractors, then launch the singlethread strategy
        else {
            DriverClient driver = new DriverClient();
            entities = driver.run(idUser, extractor, otype, document, timeout, idTool);
        }
        
        //save results
        try {
            idAnnotation = sql.insertAnnotation(idDocument, idTool, idUser, Ontology.getName(otype));
            sql.insertExtractions(entities, idAnnotation);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idAnnotation;
    }
}
