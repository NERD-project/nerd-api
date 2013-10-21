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

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import fr.eurecom.nerd.api.authentication.NerdPrincipal;
import fr.eurecom.nerd.core.exceptions.LanguageException;
import fr.eurecom.nerd.core.exceptions.NoContentException;
import fr.eurecom.nerd.core.exceptions.QuotaException;
import fr.eurecom.nerd.core.exceptions.RouterException;
import fr.eurecom.nerd.core.exceptions.TimeOutException;
import fr.eurecom.nerd.core.exceptions.TypeExpection;
import fr.eurecom.nerd.core.logging.LogFactory;
import fr.eurecom.nerd.core.proxy.Dispatcher;

@Path("/annotation")
@RolesAllowed("user")
public class Annotation {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPostJSON( @Context UriInfo ui,
                                @Context SecurityContext context,
                                @FormParam("extractor") String extractor,
                                @FormParam("ontology") String ontology,
                                @FormParam("timeout") String stimeout,
                                @DefaultValue("-1") @FormParam("idDocument") int idDocument,
                                @DefaultValue("false") @FormParam("force") Boolean force,
                                @DefaultValue("true") @FormParam("cache") Boolean cache) 
    {
        int idUser = ((NerdPrincipal)context.getUserPrincipal()).getId();

        JSONObject jo = new JSONObject();
        URI uri=null;
        
        try {           
            LogFactory.logger.info("user=" + idUser + " requires to extract entities from the document=" + idDocument);

            Long timeout = (stimeout == null) ? 120L : Long.parseLong(stimeout);     
            
            Dispatcher router = new Dispatcher();     
            int idAnnotation = router.run(idUser, extractor, ontology, idDocument, timeout, force, cache);
            
            uri = new URI(ui.getBaseUri() + "annotation/" + idAnnotation);
            jo.put("idAnnotation", idAnnotation);
            
        } catch (TypeExpection e) {
            e.printStackTrace();
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        } catch (QuotaException e) {
            e.printStackTrace();
            return Response
                    .status(Status.FORBIDDEN)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        } catch (TimeOutException e) {
            e.printStackTrace();
            return Response
                    .status(204)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        } catch (NoContentException e) {
            e.printStackTrace();
            return Response
                    .status(204)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        } catch (LanguageException e) {
            e.printStackTrace();
            return Response.status(Response.Status.FORBIDDEN)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        } catch (RouterException e) {
            e.printStackTrace();
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return Response
                .created(uri)
                .header("Access-Control-Allow-Origin", "*")
                .entity(jo.toString() + "\n")
                .build();        
    }
}
