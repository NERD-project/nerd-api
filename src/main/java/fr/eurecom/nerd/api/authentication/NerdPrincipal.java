//   NERD - The Named Entity Recognition and Disambiguation framework.
//          It processes textual resources for extracting named entities
//          linked to Web resources.
//
//   Copyright 2011 Politecnico di Torino
//             2011 EURECOM
//             2012 Ghent University
//             2013 Universita' di Torino
//
//   Authors:
//      Giuseppe Rizzo <giuse.rizzo@gmail.com>
//      Ruben Verborgh <ruben.verborgh@ugent.be>
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

import java.security.Principal;

public class NerdPrincipal implements Principal {
    private final int id;
    private String nickname;
    private final String openid;
    private String email;
    private String token;
    
    /**
     * Creates a new NerdPrincipal.
     * @param id The ID.
     * @param name The OpenID name.
     */
    public NerdPrincipal(int id, String openid, String nickname, String email) {
        this.id = id;
        this.openid = openid;
        this.nickname = nickname;
        this.email = email;
    }
    
    /**
     * Gets the ID.
     * @return The ID.
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gets the OpenID name.
     * @return The OpenID name.
     */
    public String getName() {
        return openid;
    }
    
    /**
     * Gets the user's access token, if known.
     * @return The token.
     */
    public String getToken() {
        return token;
    }
    
    /**
     * Sets the user's access token.
     * @param The token.
     */
    public void setToken(String token) {
        this.token = token;
    }
    
    /**
     * The role of a NERD user is "user".
     * @return "user".
     */
    public String getRole() {
        return "user";
    }
    
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
