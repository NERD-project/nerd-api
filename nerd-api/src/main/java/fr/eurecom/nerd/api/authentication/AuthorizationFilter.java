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

package fr.eurecom.nerd.api.authentication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.google.common.base.Splitter;

public class AuthorizationFilter implements ContainerRequestFilter{

    private static NerdPrincipals principals = new NerdPrincipals();

    public void filter(ContainerRequestContext cx) throws IOException 
    {       
        NerdPrincipal user = null;
        byte[] content = null;
        String key= null;
        
        if(cx.getMethod().equals(HttpMethod.GET)) {
            UriInfo ui = cx.getUriInfo();
            MultivaluedMap<String, String> map = ui.getQueryParameters();
            key = map.getFirst("key");
        }
        else {  //let's assume GET,POST,PUT encapsulate key in the entities
            if (cx.hasEntity()) {           
                InputStream is = cx.getEntityStream() ;
                
                // deep copy of the input stream
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while ((n = is.read(buf)) >= 0)
                    baos.write(buf, 0, n);
                content = baos.toByteArray();
                          
                String writer = new String(content, "UTF-8");
                Map<String,String> map = Splitter
                                          .on("&")
                                          .withKeyValueSeparator("=")
                                          .split(writer);
                key = map.get("key");
                
                cx.setEntityStream(new ByteArrayInputStream(content));
            }
        }
        
        if (key != null) user = principals.findUserByKey(key);
        cx.setSecurityContext(new Authorizer(user));
    }
}
