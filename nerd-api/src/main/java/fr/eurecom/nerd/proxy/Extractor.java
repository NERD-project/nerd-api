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

import fr.eurecom.nerd.exceptions.TypeExpection;

public class Extractor {

    protected static ExtractorType getType(String extractor) throws TypeExpection 
    {    
        if(extractor.equals("alchemyapi")) 
            return ExtractorType.ALCHEMYAPI;
        
        else if (extractor.equals("combined"))
            return ExtractorType.COMBINED;
        
        else if (extractor.equals("dbspotlight"))
            return ExtractorType.DBSPOTLIGHT;
        
//        else if (extractor.equals("evri"))
//            return ExtractorType.EVRI;
        
        else if (extractor.equals("extractiv"))
            return ExtractorType.EXTRACTIV;
        
        else if (extractor.equals("lupedia"))
            return ExtractorType.LUPEDIA;
        
        else if (extractor.equals("opencalais"))
            return ExtractorType.OPENCALAIS;
        
        else if (extractor.equals("saplo"))
            return ExtractorType.SAPLO;        
                
        else if (extractor.equals("semitags"))
            return ExtractorType.SEMITAGS;
        
        else if (extractor.equals("textrazor"))
            return ExtractorType.TEXTRAZOR;
        
        else if (extractor.equals("wikimeta"))
            return ExtractorType.WIKIMETA;
        
        else if(extractor.equals("yahoo"))
            return ExtractorType.YAHOO;
        
        else if(extractor.equals("zemanta"))
            return ExtractorType.ZEMANTA;
        else
            throw new TypeExpection(extractor + " is not supported by the NERD platform yet. " +
            		"If you are interested to use this extractor through NERD, please send an " +
            		"email to giuseppe.rizzo@eurecom.fr\n");
        
    }
    
    protected static String getName(ExtractorType extractor) 
    {
        switch(extractor) 
        {
        case ALCHEMYAPI: 
            return "alchemyapi";
        case DBSPOTLIGHT:
            return "dbspotlight";
        case EVRI:
            return "evri";
        case EXTRACTIV:
            return "extractiv"; 
        case OPENCALAIS:
            return "opencalais";  
        case LUPEDIA:
            return "lupedia";
        case SAPLO:
            return "saplo";
        case SEMITAGS:
            return "semitags";
        case TEXTRAZOR:
            return "textrazor";
        case WIKIMETA: 
            return "wikimeta";
        case YAHOO:
            return "yahoo";
        case ZEMANTA:
            return "zemanta";
        case COMBINED:
            return "combined";
        default:
            break;        
        }
        
        return null;
    }
}
