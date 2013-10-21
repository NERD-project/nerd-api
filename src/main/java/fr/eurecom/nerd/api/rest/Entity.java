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

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import fr.eurecom.nerd.api.authentication.NerdPrincipal;
import fr.eurecom.nerd.core.db.SQL;
import fr.eurecom.nerd.core.db.table.DocumentType;
import fr.eurecom.nerd.core.db.table.TEntity;
import fr.eurecom.nerd.core.gson.SRTAdapter;
import fr.eurecom.nerd.core.gson.TextAdapter;
import fr.eurecom.nerd.core.logging.LogFactory;

@Path("/entity")
@RolesAllowed({"user"})
public class Entity {  
    
    private SQL sql = new SQL();
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    
    @GET 
    @Path("/{idEntity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPostJSON( @Context SecurityContext context,
                                @PathParam("idEntity") int idEntity 
                              ) 
    {            
        NerdPrincipal user = ((NerdPrincipal)context.getUserPrincipal());
        
        try {
            LogFactory.logger.info("user:" + user.getId() + 
                    " requires to fetch entities for the extraction=" + idEntity);
        
            ResponseBuilder response = Response.status(Response.Status.OK);
            TEntity e = null;            
            e = sql.selectEntity(idEntity);
            String json = gson.toJson(e);  
       
            return response
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(json)
                    .build();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }   
    
    
    @GET 
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGetJSON( 
                                @Context SecurityContext context,
                                @QueryParam("idAnnotation") int idAnnotation,
                                @QueryParam("granularity") String granularity
                              ) 
    {  
        NerdPrincipal user = ((NerdPrincipal)context.getUserPrincipal());
        try {
            LogFactory.logger.info("user=" + user.getId() + 
                    " requires to fetch entities for the annotation=" + idAnnotation);
        
            ResponseBuilder response = Response.status(Response.Status.OK);
            List<TEntity> entities = sql.selectEntities(idAnnotation);
            
            if(granularity != null && granularity.equals("oed")) 
                entities = oed(entities);

//            if(grouped!=null && grouped) 
//                Collections.sort(extractions, TExtraction.NERDTYPE);
            
            String docuType = sql.selectDocumentTypeByAnnotation(idAnnotation);        
            
            GsonBuilder gsonbuilder = new GsonBuilder();
            String json = null;
            if(docuType.equals(DocumentType.TIMEDTEXTTYPE)) {
                Gson gson = gsonbuilder.registerTypeAdapter(TEntity.class, new SRTAdapter()).create();
                JsonElement itemsjson = gson.toJsonTree(entities);
                json = itemsjson.toString();//String json = gson.toJson(entities).concat("\n");  
            }
            else {
                Gson gson = gsonbuilder.registerTypeAdapter(TEntity.class, new TextAdapter()).create();
                JsonElement itemsjson = gson.toJsonTree(entities);
                json = itemsjson.toString();//String json = gson.toJson(entities).concat("\n");  
            }
                
            return response
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(json)
                    .build();
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private List<TEntity> oed(List<TEntity> entities) 
    {
        Iterator<TEntity> iterator = entities.iterator();
        Hashtable<String,TEntity> map = new Hashtable<String,TEntity>();
               
        while (iterator.hasNext()) 
        {
            TEntity element = iterator.next();
            
            // check first if the element has a NE equal to one more element in the map
            TEntity evaluate = map.get(element.getTriple());
            if(evaluate==null) 
                map.put(element.getTriple(), element);
        }

        List<TEntity> result = new LinkedList<TEntity>( map.values() );
        return result;
    }   
}
