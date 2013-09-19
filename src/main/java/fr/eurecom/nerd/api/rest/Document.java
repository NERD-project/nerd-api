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
import java.sql.SQLException;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import fr.eurecom.nerd.api.authentication.NerdPrincipal;
import fr.eurecom.nerd.db.SQL;
import fr.eurecom.nerd.db.table.TDocument;
import fr.eurecom.nerd.exceptions.LanguageException;
import fr.eurecom.nerd.logging.LogFactory;

@Path("/document")
public class Document {
          
    private SQL sql = new SQL();
    private Gson gson = new Gson();
    
    @POST
    @RolesAllowed({"user"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPostJSON(   @Context UriInfo ui,
                                  @Context SecurityContext context,
                                  @FormParam("text") String text,
                                  @FormParam("timedtext") String timedtext,
                                  @FormParam("uri") String uri
                               )             
    {        
        NerdPrincipal user = ((NerdPrincipal)context.getUserPrincipal());
        
        ResponseBuilder response = null;
        JSONObject jo = new JSONObject(); 
               
        try {          
            LogFactory.logger.info("user:" + user.getId() + " requires to store a resource");
           
            if(text==null && timedtext==null && uri==null )
                return Response.status(Status.NOT_ACCEPTABLE)
                                .header("Access-Control-Allow-Origin", "*")
                                .entity("Wrong parameters")
                                .build();
       
            TDocument document = new TDocument(text, timedtext, uri);
            int idDocument = sql.selectDocument(document);
            if (idDocument != -1) 
                response = Response.status(Status.OK);
            else {
                idDocument = sql.insertDocument(document);
                //201 OK resource created
                String resource = ui.getAbsolutePath().toString();
                response = Response.created(new URI(resource + "/" + idDocument));
                }
            jo.put("idDocument", idDocument);  
        }catch(SQLException e){
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (LanguageException e) {
            return Response.status(Status.NOT_ACCEPTABLE)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }

        return response.header("Access-Control-Allow-Origin", "*")
                       .entity(jo.toString() + "\n")
                       .build();
    }
       
    @GET
    @RolesAllowed({"user"})
    @Path("/{idDocument}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGetJSON(@PathParam("idDocument") int idDocument) 
    { 
        LogFactory.logger.info("plain text of article id=" + idDocument + " is required");
        
        ResponseBuilder response = Response.status(Response.Status.OK);
        String json = null;
        try {
            //check first if this uri already exist in our storage
            TDocument article = sql.selectDocument(idDocument);
            json = gson.toJson(article);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (LanguageException e) {
            return Response.status(Status.NOT_ACCEPTABLE)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }
        
        response.header("Access-Control-Allow-Origin", "*");
        response.entity(json + "\n");
        return response.build();
    }
}
