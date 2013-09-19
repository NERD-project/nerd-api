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

package fr.eurecom.nerd.api;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.Priorities;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import fr.eurecom.nerd.api.authentication.AuthorizationFilter;

public class Server {
    
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8888/api/";
    private static volatile Boolean running = true;
    
    public static HttpServer startServer() 
    {
        // create a resource config that scans for JAX-RS resources and providers
        // in fr.eurecom.nerd package
        final ResourceConfig rc = new ResourceConfig().packages("fr.eurecom.nerd.api.rest");
        rc.register(AuthorizationFilter.class, Priorities.AUTHENTICATION);
        rc.register(RolesAllowedDynamicFeature.class);
        rc.register(LoggingFilter.class);
        
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer server =  GrizzlyHttpServerFactory.
                             createHttpServer(URI.create(BASE_URI), rc);
        
        
        return server;
    }

    public static void main(String[] args) throws IOException 
    {
        final HttpServer server = startServer();
        
        System.out.println(
                String.format("NERD API started with WADL available at " + 
                              "%sapplication.wadl", 
                BASE_URI)
         );
        
        Thread warmUp = new Thread() {
            public void run() {}
        };
        warmUp.start();
        while(running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.in.read();
        server.stop();        
    }
}

