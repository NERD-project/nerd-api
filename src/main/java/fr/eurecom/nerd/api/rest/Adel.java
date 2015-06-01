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

package fr.eurecom.nerd.api.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.eurecom.adel.okechallenge.OKE;

@Path("/adel")
public class Adel {
              
    @POST
    @Consumes("application/x-turtle")
    @Produces("application/x-turtle")
    public Response doPostJSON( String nif )             
    {                    
        System.out.println(nif +"\n");

//        String nif2 = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
//                "      @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
//                "      @prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
//                "      @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
//                "      @prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
//                "      @prefix d0: <http://ontologydesignpatterns.org/ont/wikipedia/d0.owl#> .\n" +
//                "      @prefix dul: <http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#> .\n" +
//                "      @prefix oke: <http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/> .\n" +
//                "      @prefix dbpedia: <http://dbpedia.org/resource/> .\n" +
//                "      @prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
//                "\n" +
//                "      <http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-1#char=0,146>\n" +
//                "              a                     nif:RFC5147String , nif:String , nif:Context ;\n" +
//                "              nif:beginIndex        \"0\"^^xsd:nonNegativeInteger ;\n" +
//                "              nif:endIndex          \"146\"^^xsd:nonNegativeInteger ;\n" +
//                "              nif:isString          \"Florence May Harding studied at a school in Sydney, and " +
//                "with Douglas Robert Dundas , but in effect had no formal training in either botany or art.\"@en .";
        
        return Response.status(Status.OK)
        				.header("Access-Control-Allow-Origin", "*")
                        .entity(OKE.run(nif) + "\n")
                        .build();
    }
       
}
