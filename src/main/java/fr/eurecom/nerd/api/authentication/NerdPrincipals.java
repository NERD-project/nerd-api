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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import fr.eurecom.nerd.db.MySQL;

public class NerdPrincipals {
    private static MySQL database = new MySQL();
    private static SecureRandom random = new SecureRandom();
    
    /* The maximum validity time of a token in seconds. */
    private final static int TOKEN_VALID_SECONDS = 30 * 24 * 60 * 60;
    
    /**
     * Finds a user by his/her ID.
     * @param userId The ID of the user.
     * @return The user or null if not found.
     */
    public NerdPrincipal findUser(int userId) {
        NerdPrincipal user = null;
        
        Connection connection = database.connect();
        try {
            PreparedStatement findQuery = 
                    connection.prepareStatement("SELECT openid, nickname, email FROM user WHERE idUser = ?");
            findQuery.setInt(1, userId);
            ResultSet result = findQuery.executeQuery();
            if (result.next())
                user = new NerdPrincipal(userId, 
                                         result.getString("openid"), 
                                         result.getString("nickname"),
                                         result.getString("email"));
            result.close();
            findQuery.close();
        }
        catch (SQLException e) {
            user = null;
        }
        try {
            connection.close();
        }
        catch (SQLException e) { }
        
        return user;
    }
    
    /**
     * Finds a user by his/her OpenID name.
     * @param name The OpenID name.
     * @return The user or null if not found.
     */
    public NerdPrincipal findUser(String openid) 
    {
        NerdPrincipal user = null;
        
        Connection connection = database.connect();
        try {
            PreparedStatement findQuery = connection.
                    prepareStatement("SELECT idUser, openid, nickname, email FROM user WHERE openid = ?");
            findQuery.setString(1, openid);
            
            ResultSet result = findQuery.executeQuery();
            if (result.next())
                user = new NerdPrincipal(result.getInt("idUser"), 
                                         result.getString("openid"), 
                                         result.getString("nickname"),
                                         result.getString("email"));
            result.close();
            findQuery.close();
        }
        catch (SQLException e) {
            user = null;
        }
        try {
            connection.close();
        }
        catch (SQLException e) { }
        
        return user;
    }
    
    /**
     * Finds a user by his/her active token.
     * @param token The token.
     * @return The user or null if not found or token expired.
     */
    public NerdPrincipal findUserByToken(String token) {
        NerdPrincipal user = null;
        
        Connection connection = database.connect();
        try {
            PreparedStatement findQuery = connection.prepareStatement(
                    "SELECT idUser, openid, nickname, email " +
                    "FROM user WHERE tokenHash = SHA(?) AND NOW() < tokenTimeStamp");
            findQuery.setString(1, token);
            //findQuery.setInt(2, TOKEN_VALID_SECONDS);
            ResultSet result = findQuery.executeQuery();
            if (result.next())
                user = new NerdPrincipal(result.getInt("idUser"), 
                                         result.getString("openid"), 
                                         result.getString("nickname"),
                                         result.getString("email"));
            result.close();
            findQuery.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            user = null;
        }
        try {
            connection.close();
        }
        catch (SQLException e) { }
        
        return user;
    }
    
    /**
     * Finds a user by his/her key.
     * @param key The key.
     * @return The user or null if not found.
     */
    public NerdPrincipal findUserByKey(String key) 
    {
        NerdPrincipal user = null;
        
        Connection connection = database.connect();
        try {
            PreparedStatement findQuery
                = connection.prepareStatement("select idUser,openid,nickname,email from user u,service s,tool t where s.apikey=? and t.name='nerd' and s.toolIdTool=t.idTool and s.userIdUser=u.idUser");
                //= connection.prepareStatement("SELECT idUser, openid, nickname, email FROM user WHERE apikey = ?");
            findQuery.setString(1, key);
            ResultSet result = findQuery.executeQuery();
                        
            if (result.next())
                user = new NerdPrincipal(result.getInt("idUser"), 
                                         result.getString("openid"),
                                         result.getString("nickname"),
                                         result.getString("email"));
            result.close();
            findQuery.close();
        }
        catch (SQLException e) {
            user = null;
        }
        try {
            connection.close();
        }
        catch (SQLException e) { }
        
        return user;
    }
    
    /**
     * Creates a new user.
     * @param name The OpenID name.
     * @param email 
     * @return The user.
     */
    public NerdPrincipal addUser(String openid, String openidEmail) 
    {
        NerdPrincipal user = null;
        Connection connection = database.connect();
        
        try {
            String token = generateToken();
            PreparedStatement insertQuery
                = connection.prepareStatement(
                        "INSERT INTO user(openidemail, openid, tokenHash, tokenTimeStamp, registrationDate) " +
                        "VALUES( ?, ?, SHA(?), NOW() + INTERVAL ? SECOND, NOW())");
            insertQuery.setString(1, openidEmail);
            insertQuery.setString(2, openid);
            insertQuery.setString(3, token);
            insertQuery.setInt(4, TOKEN_VALID_SECONDS);
            insertQuery.executeUpdate();
            insertQuery.close();

            user = findUser(openid);
            user.setToken(token);
            user.setNickname("user" + user.getId());
            
            PreparedStatement updateQuery 
                = connection.prepareStatement("update user set nickName='" + user.getNickname() + "' where idUser="+user.getId());
            updateQuery.execute();
            updateQuery.close();
                
            connection.close();  
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        
        return user;
    }
    
    /**
     * Gives a new token to a user.
     * @param name The OpenID name.
     * @return the user.
     */
    public NerdPrincipal renewToken(String openid) {
        NerdPrincipal user = findUser(openid);
        if (user == null)
            return null;
        
        String token = generateToken();
        
        Connection connection = database.connect();
        try {
            PreparedStatement updateQuery
                = connection.prepareStatement("UPDATE user SET tokenHash = SHA(?), tokenTimeStamp = NOW() + INTERVAL ? SECOND WHERE openid = ?");
            updateQuery.setString(1, token);
            updateQuery.setInt(2, TOKEN_VALID_SECONDS);
            updateQuery.setString(3, openid);
            updateQuery.executeUpdate();
            updateQuery.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        }
        catch (SQLException e) { }
        
        user.setToken(token);
        return user;
    }
        
    /**
     * Generates a 32-character token.
     * @return The token.
     */
    private String generateToken() {
        return (new BigInteger(256, random).toString(32) + "00000000000000000000000000000000").substring(0, 32);
    }
}
