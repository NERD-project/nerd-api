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

package fr.eurecom.nerd.core.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.glassfish.grizzly.utils.Pair;

import fr.eurecom.nerd.core.db.table.TAnnotation;
import fr.eurecom.nerd.core.db.table.TDocument;
import fr.eurecom.nerd.core.db.table.TEntity;
import fr.eurecom.nerd.core.db.table.TTool;
import fr.eurecom.nerd.core.db.table.TUser;
import fr.eurecom.nerd.core.db.table.VStream;
import fr.eurecom.nerd.core.db.table.VUsage;
import fr.eurecom.nerd.core.exceptions.LanguageException;

public class SQL {
       
    public static final Logger logger = Logger.getLogger(SQL.class.getName());

    private MySQL db = new MySQL();   
        
    /*
     * select
     */
    public TAnnotation selectAnnotation (int idAnnotation) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        String selectAnnotation = "SELECT documentIdDocument,toolIdTool,userIdUser,timestamp " +
                                  "FROM annotation " +
                                  "WHERE idAnnotation=?";
        
        PreparedStatement statement = conn.prepareStatement(selectAnnotation);
        statement.setInt(1, idAnnotation);

        ResultSet result = statement.executeQuery();
        TAnnotation annotation = null;
        while(result.next()) 
            annotation = new TAnnotation(   idAnnotation, 
                                            result.getInt("documentIdDocument"),
                                            result.getInt("toolIdTool"),
                                            result.getInt("userIdUser"),
                                            result.getString("timestamp"));   
        result.close();
        statement.close();
        conn.close();
        return annotation;
    }
    
    public List<TAnnotation> selectAnnotationByTool(int idTool) throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String selectAnnotation = "SELECT idAnnotation " +
        		                  "FROM annotation " +
                                  "WHERE toolIdTool=?";
        
        PreparedStatement ps = conn.prepareStatement(selectAnnotation);
        ps.setInt(1, idTool);
        ResultSet result = ps.executeQuery();
        
        List<TAnnotation> annotations = new LinkedList<TAnnotation>();
        while(result.next()) {
            int idAnnotation =  result.getInt("idAnnotation");
            TAnnotation temp = new TAnnotation(idAnnotation, null);
            annotations.add(temp);
        }
        
        result.close();
        ps.close();
        return annotations;
    }
    
    public int selectAnnotation(int idDocument, int idTool, int idUser, String ontology) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        String selectAnnotation = "SELECT idAnnotation " +
        		                  "FROM annotation " +
        		                  "WHERE documentIdDocument=? " +
        		                  "AND toolIdTool=? " +
        		                  "AND userIdUser=? " +
        		                  "AND ontology=?";
        
        PreparedStatement ps = conn.prepareStatement(selectAnnotation);
        ps.setInt(1, idDocument);
        ps.setInt(2, idTool);
        ps.setInt(3, idUser);
        ps.setString(4, ontology);
        ResultSet result = ps.executeQuery();
        int idAnnotation = -1;
        while(result.next()) 
            idAnnotation =  result.getInt("idAnnotation");
        
        result.close();
        ps.close();
        conn.close();
        return idAnnotation;
    }

    public boolean selectAnnotation(int idDocument, int idTool, String ontology) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String selectAnnotation = "SELECT idAnnotation " +
                                  "FROM annotation " +
                                  "WHERE documentIdDocument=? " +
                                  "AND toolIdTool=? " +
                                  "AND ontology=?";
        
        PreparedStatement ps = conn.prepareStatement(selectAnnotation);
        ps.setInt(1, idDocument);
        ps.setInt(2, idTool);
        ps.setString(3, ontology);
        ResultSet rs = ps.executeQuery();
        
        boolean result = rs.first();
        rs.close();
        ps.close();
        
        return result;
    }
    
    public List<TAnnotation> selectAnnotationWithExtraction() throws SQLException 
    {
        List<TAnnotation> result = new LinkedList<TAnnotation>();
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        Statement statement = conn.createStatement();
        ResultSet annotationexists= statement.
            executeQuery(   "SELECT * FROM annotation,extraction " +
            		        "where idAnnotation=annotationIdAnnotation;"
                        );
        while(annotationexists.next()) {
            int idAnnotation = annotationexists.getInt("idAnnotation");
            int idDocument = annotationexists.getInt("documentIdDocument");
            int idTool = annotationexists.getInt("toolIdTool");
            int idUser = annotationexists.getInt("userIdUser");
            String timestamp = annotationexists.getTimestamp("inserted_at").toGMTString();
            TAnnotation annotation = new TAnnotation(idAnnotation,idDocument,idTool,idUser,timestamp);
            result.add(annotation);
        }
        annotationexists.close();
        statement.close();
        conn.close();
        return result;
    }

    public List<Integer> selectAnnotationPerformedByUser(Integer user) throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        String selectAnnotation =   "SELECT idAnnotation " +
        		                    "FROM annotation " +
        		                    "WHERE userIdUser=?";
        PreparedStatement ps = conn.prepareStatement(selectAnnotation);
        
        ResultSet annotation= ps.executeQuery();
        List<Integer> result = new ArrayList<Integer>();
        while(annotation.next()) 
            result.add( annotation.getInt ("idAnnotation") );

        annotation.close();
        ps.close();
        conn.close();
        return result;        
    }

    public TDocument selectDocument(int idDocument)
    throws SQLException, LanguageException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        PreparedStatement statement = conn.prepareStatement(
                "SELECT text,timedtext,URI,type,language " +
                "FROM document " +
                "WHERE idDocument = ?"
        );
        statement.setInt(1, idDocument);
        ResultSet rs = statement.executeQuery();
        
        TDocument result = null;
        while(rs.next()) 
            result = new TDocument(
                    idDocument,
                    rs.getString("text"),
                    rs.getString("timedtext"),
                    rs.getString("URI"),
                    rs.getString("type"),
                    rs.getString("language")
                    );
                    
        return result;
    }

    public int selectDocument (TDocument document) 
    throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();

        PreparedStatement statement = conn.prepareStatement(
                                      "SELECT idDocument " +
                                      "FROM document " +
                                      "WHERE textHash=SHA(?)");
        statement.setString(1, document.getText());
        ResultSet result = statement.executeQuery();
        int id = -1;
        while(result.next()) 
            id = result.getInt ("idDocument");
        
        result.close();
        statement.close();
        conn.close();
        return id;
    }

    public int selectDocumentByUri (String URI) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        //Statement statement = conn.createStatement();
        PreparedStatement statement = 
                conn.prepareStatement("SELECT idDocument FROM document WHERE URI=?");
        statement.setString(1, URI);
        ResultSet result = statement.executeQuery();
        int id = -1;
        while(result.next()) 
            id = result.getInt ("idDocument");
        
        result.close();
        statement.close();
        conn.close();
        return id;
    }
    
    public String selectDocumentTypeByAnnotation(int idAnnotation) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        //Statement statement = conn.createStatement();
        PreparedStatement statement = 
                conn.prepareStatement("select type from document,annotation where idDocument=documentIdDocument and idAnnotation=?");
        statement.setInt(1, idAnnotation);
        ResultSet result = statement.executeQuery();
        String type = null;
        while(result.next()) 
            type = result.getString ("type");
        
        result.close();
        statement.close();
        conn.close();
        return type;
    }

    public List<Pair> selectArticles() throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        Statement statement = conn.createStatement();
        ResultSet articleexists= statement.
            executeQuery("SELECT URI,idDocument FROM document;");
        List<Pair> result = new LinkedList<Pair>();
        while(articleexists.next()) {
            Pair p = new Pair(articleexists.getString (1), articleexists.getInt(2));
            result.add(p);
        }
        articleexists.close();
        statement.close();
        conn.close();
        return result;
    }

    public List<Integer> selectArticlesInteger() throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        List<Integer> result = new ArrayList<Integer>();
        
        Statement statement = conn.createStatement();
        ResultSet articleexists= statement.
            executeQuery("SELECT idDocument FROM document;");
        while(articleexists.next()) {
           result.add(articleexists.getInt("idDocument"));
        }
        articleexists.close();
        statement.close();
        conn.close();
        return result;        
    }
    
    public List<String> selectArticleTexts() throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();

        Statement stmt = conn.createStatement();
        ResultSet articles = stmt.executeQuery("select text from article;");
        articles = stmt.getResultSet();
        
        //List<Integer> listIdAnalysis = new ArrayList<Integer>();
        List<String> result = new LinkedList<String>();
        while(articles.next()) {
            String text = articles.getString("text");
            result.add(text);
        }
        articles.close();
        stmt.close();
        conn.close();

        return result;
    }
    
    public List<String> selectArticleTextsGivenAuthority(String authority) 
    throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();

        String query = "select text from article where URI like ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, "%" + authority + "%");
        ResultSet articles = ps.executeQuery();
        articles = ps.getResultSet();
        
        //List<Integer> listIdAnalysis = new ArrayList<Integer>();
        List<String> result = new LinkedList<String>();
        while(articles.next()) {
            String text = articles.getString("text");
            result.add(text);
        }
        articles.close();
        ps.close();
        conn.close();

        return result;
    }


//    public String selectArticleURI(int idDocument) throws SQLException 
//    {
//        Connection conn = db.connect();
//        if(conn == null) throw new SQLException();
//
//        Statement stmt = conn.createStatement();
//        ResultSet articles = stmt.executeQuery( "select URI from document " +
//                                                "where idDocument=" + idDocument + ";");
//        articles = stmt.getResultSet();
//        
//        //List<Integer> listIdAnalysis = new ArrayList<Integer>();
//        String result = null;
//        while(articles.next()) {
//            result = articles.getString("URI");
//        }
//        
//        stmt.close();
//        articles.close();
//        conn.close();
//
//        return result;
//    }    
    
//    public String selectArticleText(int idDocument) throws SQLException 
//    {
//        Connection conn = db.connect();
//        if(conn == null) throw new SQLException();
//
//        Statement stmt = conn.createStatement();
//        ResultSet articles = stmt.executeQuery( "select text from document " +
//        		                                "where idDocument=" + idDocument + ";");
//        articles = stmt.getResultSet();
//        
//        //List<Integer> listIdAnalysis = new ArrayList<Integer>();
//        String result = null;
//        while(articles.next()) {
//            result = articles.getString("text");
//        }
//        
//        stmt.close();
//        articles.close();
//        conn.close();
//
//        return result;
//    }
     
    public TEntity selectEntity(int idEntity)
    throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql =  "SELECT idEntity,label,extractorType,uri,nerdType,startChar," +
                      "endChar,startNPT,endNPT,confidence,relevance,extractor " +
                      "FROM entity " +
                      "WHERE idEntity=?";
            
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1,Integer.toString(idEntity));
        ResultSet rs = ps.executeQuery();
    
        TEntity result = null;
        while(rs.next()) {
            String label = rs.getString("label");
            String extractorType = rs.getString("extractorType");
            String uri = rs.getString("uri");
            String nerdType = rs.getString("nerdType");
            Integer startChar = rs.getInt("startChar");
            Integer endChar = rs.getInt("endChar");
            Double startNPT = rs.getDouble("startNPT");
            Double endNPT = rs.getDouble("endNPT");
            double confidence = rs.getDouble("confidence");
            double relevance = rs.getDouble("relevance");
            String extractor = rs.getString("extractor");

            result = new TEntity(idEntity,label,extractorType,
                            uri,nerdType,startChar,endChar,confidence,relevance,
                            extractor,startNPT,endNPT);
        }
        rs.close();
        ps.close();
        conn.close();
        return result;
    }

    public List<TEntity> selectEntities (int idAnnotation) 
    throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql =  "SELECT idEntity,label,extractorType,uri,nerdType,startChar," +
        		      "endChar,startNPT,endNPT,confidence,relevance,extractor " +
        		      "FROM entity " +
        		      "WHERE annotationIdAnnotation=?";
            
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idAnnotation);
        ResultSet rs = ps.executeQuery();
    
        List<TEntity> result = new LinkedList<TEntity>();
        while(rs.next()) {
            int idEntity = rs.getInt("idEntity");
            String label = rs.getString("label");
            String extractorType = rs.getString("extractorType");
            String uri = rs.getString("uri");
            String nerdType = rs.getString("nerdType");
            Integer startChar = rs.getInt("startChar");
            Integer endChar = rs.getInt("endChar");
            Double startNPT = rs.getDouble("startNPT");
            Double endNPT = rs.getDouble("endNPT");
            double confidence = rs.getDouble("confidence");
            double relevance = rs.getDouble("relevance");
            String extractor = rs.getString("extractor");

            TEntity entity = new TEntity(idEntity,label,extractorType,
                            uri,nerdType,startChar,endChar,confidence,relevance,
                            extractor,startNPT,endNPT);
            result.add(entity);
        }
        rs.close();
        ps.close();
        conn.close();
        return result;
    }

    public TUser selectUser(int id) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
                
        String sql = "SELECT firstName,lastName,nickName,openidEmail,email," +
        		            "website,projectName,projectUri,organization," +
        		            "country,registrationDate,validity,dailyUsage,dailyQuota " +
                     "FROM user " +
                     "WHERE idUser=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet userexist= ps.executeQuery();

        TUser user = new TUser();
        while(userexist.next()) {
            user.setId(id);
            user.setFirstName( transformNullString(userexist.getString("firstName")) );
            user.setLastName(  transformNullString(userexist.getString("lastName")) ); 
            user.setNickName(  transformNullString(userexist.getString("nickName")) );
            user.setOpenidEmail( transformNullString(userexist.getString("openidEmail")) );
            user.setEmail( transformNullString(userexist.getString("email")) );
            user.setWebsite( transformNullString(userexist.getString("website")) );
            user.setProjectName( transformNullString(userexist.getString("projectName")) );
            user.setProjectUri( transformNullString(userexist.getString("projectUri")) );
            user.setOrganization( transformNullString(userexist.getString("organization")) );
            user.setCountry(transformNullString(userexist.getString("country")) );
            user.setRegistrationDate( userexist.getDate("registrationDate") );
            user.setValidity( userexist.getBoolean("validity") );
            user.setDailyUsage( userexist.getInt("dailyUsage") );
            user.setDailyQuota( userexist.getInt("dailyQuota") );
        }
        userexist.close();
        ps.close();
        conn.close();
        return user;
    }
    
    public TUser selectUser (String nickname) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "SELECT idUser,firstName,lastName,nickName,email,website,projectName," +
                "projectUri,organization,country,registrationDate,validity " +
                "FROM user " +
                "WHERE nickname=?";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, nickname);
        ResultSet userexist= ps.executeQuery();
            
        TUser user = null;
        while(userexist.next()) {
            user = new TUser(userexist.getInt("idUser"), 
                             transformNullString(userexist.getString("firstName")), 
                             transformNullString(userexist.getString("lastName")), 
                             transformNullString(userexist.getString("nickName")), 
                             transformNullString(userexist.getString("email")), 
                             transformNullString(userexist.getString("website")), 
                             transformNullString(userexist.getString("projectName")), 
                             transformNullString(userexist.getString("projectUri")), 
                             transformNullString(userexist.getString("organization")), 
                             transformNullString(userexist.getString("country")),
                             userexist.getDate("registrationDate"),
                             userexist.getBoolean("validity")
                             );
     
        }
        userexist.close();
        ps.close();
        conn.close();
        return user;
    }

    private static String transformNullString( String s )
    {
        return (s == null) ? "" : s;
    }

    public List<Integer> selectUserLike(String likeuser) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "SELECT idUser FROM user WHERE username LIKE ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + likeuser + "%");
        ResultSet articleexists= ps.executeQuery();
        
        List<Integer> result = new ArrayList<Integer>();
        while(articleexists.next())
            result.add(articleexists.getInt ("idUser"));
    
        articleexists.close();
        ps.close();
        conn.close();
        return result;        
    }


    public int selectUserDailyQuota(Integer idUser) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "SELECT dailyQuota FROM user WHERE idUser =?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUser);
        ResultSet articleexists= ps.executeQuery();
      
        Integer result = null;
        while(articleexists.next())
            result = articleexists.getInt ("dailyQuota");
    
        articleexists.close();
        ps.close();
        conn.close();
        return result; 
    }


    public int selectUserDailyCounter(Integer idUser) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "SELECT dailyUsage FROM user WHERE idUser=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUser);
        ResultSet rs= ps.executeQuery();
        
        Integer result = null;
        while(rs.next())
            result = rs.getInt ("dailyUsage");
    
        rs.close();
        ps.close();
        conn.close();
        return result; 
    }


    public List<Integer> selectUsers() throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        List<Integer> result = new ArrayList<Integer>(); 
        Statement statement = conn.createStatement();
        ResultSet rs= statement.
            executeQuery("SELECT idUser FROM user;");
    
        while(rs.next()) {
            result.add(rs.getInt ("idUser"));
        }
        
        rs.close();
        statement.close();
        conn.close();
        return result;
    }


    public int selectTool(String toolName) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        String sql = "SELECT idTool FROM tool WHERE name=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, toolName);
        ResultSet rs = ps.executeQuery();
        int idTool = -1;
        while(rs.next()) {
            idTool =  rs.getInt(1);
        }
        rs.close();
        ps.close();
        conn.close();
        return idTool;
    }

    public TTool selectTool(int idTool) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "SELECT * FROM tool WHERE idTool=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idTool);
        ResultSet rs = ps.executeQuery();
        TTool tool = null;
        while(rs.next()) {
            tool = new TTool( rs.getInt("idTool"), 
                              rs.getString("name"), 
                              rs.getString("uri")
                            );
        }
        rs.close();
        ps.close();
        conn.close();
        return tool;
    }

    public List<TTool> selectTool() throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        Statement statement = conn.createStatement();
        ResultSet rs= statement.
            executeQuery("SELECT idTool,name,uri FROM tool;");
        List<TTool> tools = new LinkedList<TTool>();
        while(rs.next()) {
            tools.add(new TTool(rs.getInt("idTool"), 
                             rs.getString("name"), 
                             rs.getString("uri")) 
                     );
        }
        statement.close();
        conn.close();
        return tools;
    }

    /*
     *  INSERT
     */
    // insert a document into the document table
    
    public int insertDocument (TDocument document)
    throws SQLException 
    {
        //check first if the article already exists
        int id = selectDocument(document);
        
        // if it already exists then we return the id
        if(id != -1) return id;

        // else we need to insert a new row
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO document(textHash,text,timedtext,URI,type,language) " +
                        "VALUES(SHA(?),?,?,?,?,?)");
        ps.setString(1, document.getText());
        ps.setString(2, document.getText());
        ps.setString(3, document.getTimedtext());
        ps.setString(4, document.getURI());
        ps.setString(5, document.getType());
        ps.setString(6, document.getLanguage());
        ps.execute();
        ps.close();
        conn.close();

        id = selectDocument(document);
        return id;
    }
    
    public int insertDocument (String text, String URI)
    throws SQLException, LanguageException 
    {   
        //check first if the article already exists
        int id = selectDocument(new TDocument(text, null, null));
        
        // if it already exists then we return the id
        if(id != -1) return id;

        // else we need to insert a new row
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String insertDocument = 
              "INSERT INTO document(text,textHash,URI,type) VALUES(?,SHA(?),?,?)";
        
        PreparedStatement ps = conn.prepareStatement(insertDocument);
        ps.setString(1, text);
        ps.setString(2, text);
        ps.setString(3, URI);
        ps.execute();
        ps.close();
        conn.close();

        id = selectDocument(new TDocument(text,null, null));
        return id;
    }    
        
     
    public int insertAnnotation(int idDocument, int idTool, int idUser, String ontology) 
    throws SQLException
    {       
        // else we need to insert a new row
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String insertAnnotation = 
              "INSERT INTO annotation(documentIdDocument,toolIdTool,userIdUser,ontology) " +
              "VALUES(?,?,?,?)";   

        PreparedStatement ps = conn.prepareStatement(insertAnnotation);
        ps.setInt(1, idDocument);
        ps.setInt(2, idTool);
        ps.setInt (3, idUser);
        ps.setString(4, ontology);
        ps.execute();
        ps.close();
        conn.close();
        
        int id = selectLastIndexAnnotation();        
        return id;
    }
    
    public void insertEntity(TEntity entity, int idAnnotation) 
    throws SQLException 
    {    
        // else we need to insert a new row
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        PreparedStatement ps = null;
               
        if (entity.getStartNPT() != null && entity.getEndNPT() != null) {
            String EXTRACTION_INSERT_NPT = 
                    "INSERT INTO entity(label,extractorType,uri,nerdType,startChar,endChar," +
                    "startNPT,endNPT,confidence,relevance,extractor,annotationIdAnnotation) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";   
            ps= conn.prepareStatement(EXTRACTION_INSERT_NPT);
            ps.setString(1, entity.getLabel());
            ps.setString(2, entity.getExtractorType());
            ps.setString(3, entity.getURI());
            ps.setString(4, entity.getNerdType());
            ps.setInt(5, entity.getStartChar());
            ps.setInt(6, entity.getEndChar());
            ps.setDouble(7, entity.getStartNPT());
            ps.setDouble(8, entity.getEndNPT());
            ps.setDouble(9, entity.getConfidence());
            ps.setDouble(10, entity.getRelevance());
            ps.setString(11, entity.getExtractor());
            ps.setInt(12, idAnnotation);
        }
        
        else {
            String EXTRACTION_INSERT = 
                    "INSERT INTO entity(label,extractorType,uri,nerdType,startChar,endChar," +
                    "confidence,relevance,extractor,annotationIdAnnotation) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?)";   
            ps= conn.prepareStatement(EXTRACTION_INSERT);
            ps.setString(1, entity.getLabel());
            ps.setString(2, entity.getExtractorType());
            ps.setString(3, entity.getURI());
            ps.setString(4, entity.getNerdType());
            ps.setInt(5, entity.getStartChar());
            ps.setInt(6, entity.getEndChar());
            ps.setDouble(7, entity.getConfidence());
            ps.setDouble(8, entity.getRelevance());
            ps.setString(9, entity.getExtractor());
            ps.setInt(10, idAnnotation);
        }
        ps.execute();
        ps.close();
        conn.close();
    }
    
    public void insertExtractions(List<TEntity> entities, int idAnnotation) throws SQLException 
    {
        for(TEntity entity : entities) 
            insertEntity(entity, idAnnotation);
    }
    
    /*
     *   COUNT
     */
    public int countAnnotations() throws SQLException 
    {
        int result=0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
    
    
        Statement statement = conn.createStatement();
        ResultSet analysis= statement.
                executeQuery("SELECT count(idAnnotation) FROM annotation");
    
        while(analysis.next()) {
            result = analysis.getInt("count(idAnnotation)");
        }
        analysis.close();
        statement.close();
        conn.close();
        
        return result;
    }

    public int countArticles() throws SQLException
    {
        int result=0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
 
        Statement statement = conn.createStatement();
        ResultSet articleexists= statement.
                executeQuery("SELECT COUNT(idDocument) FROM document");

        while(articleexists.next()) {
            result = articleexists.getInt("count(idDocument)");           
        }
        articleexists.close();
        statement.close();
        conn.close();
        return result;
    }
           
    public int countArticleGivenLikeAuthority(String authority) throws SQLException
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "select count(idDocument) from document where URI like ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + authority + "%");
        ResultSet rs = ps.executeQuery();
    
        while(rs.next()) {
            result = rs.getInt("count(idDocument)");           
        }
        rs.close();
        ps.close();
        conn.close();
        return result;
    }


    public int countArticleWords() throws SQLException 
    {
        int result = 0;
        List<String> list = selectArticleTexts();   
        for (int i=0; i<list.size(); i++) {
            if(list.get(i) != null)
                result += list.get(i).split(" ").length;
        }
        return result;
    }

    public int countArticleWordsGivenAuthority(String authority) throws SQLException 
    {
        int result = 0;
        List<String> list = selectArticleTextsGivenAuthority(authority);   
        for (int i=0; i<list.size(); i++) {
            if(list.get(i) != null)
                result += list.get(i).split(" ").length;
        }
        return result;
    }


    public int countEntities() throws SQLException 
    {
        int result = 0;
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
    
        Statement statement = conn.createStatement();

        ResultSet rs= statement.
                executeQuery("SELECT COUNT(idEntity) FROM entity ");

        while(rs.next()) 
            result = rs.getInt("count(idEntity)");
        
        rs.close();
        statement.close();
        conn.close();
        
        return result;
    }
    
    /*
     *  SELECT COUNT
     */
    public int countEntities(int tool) throws SQLException 
    {
        int result = 0;
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql = "select count(idExtraction) " +
                "from entity en,annotation an,evaluation ev " +
                "where an.toolIdTool=? and " +
                "en.idEntity=ev.entityIdEntity and " +
                "en.annotationIdAnnotation=an.idAnnotation;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, tool);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) 
            result = rs.getInt("count(idEntity)");
        
        rs.close();
        ps.close();
        conn.close();
        return result;
    }

    public int countExtractionTrue(int tool, String parameter) throws SQLException 
    {
        int result = 0;
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql = "select count(idExtraction) " +
                "from extraction ex,annotation an,evaluation ev " +
                "where an.toolIdTool=? and " +
                "ex.idExtraction=ev.extractionIdExtraction and " +
                "ex.annotationIdAnnotation=an.idAnnotation and " +
                "ev."+parameter+"="+true+";";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(tool, tool);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) 
            result = rs.getInt("count(idExtraction)");
        
        rs.close();
        ps.close();
        conn.close();
        return result;
    }
    
    public int countExtractionGivenTool(int idTool) throws SQLException 
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        Statement statement = conn.createStatement();
        String query =  "select count(idExtraction) as count " +
                        "from annotation an,document d,extraction ex " +
                        "where d.idDocument=an.documentIdDocument and " +
                        "an.idAnnotation=ex.annotationIdAnnotation and " +
                        "an.toolIdTool=?";
        
        ResultSet extractions = statement.executeQuery(query);
        while(extractions.next()) {
            result = extractions.getInt("count");
        }
        extractions.close();
        statement.close();
        conn.close();            
        return result;
    }


    public int countExtractionGivenAuthority(String authority) throws SQLException 
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select count(idExtraction) as count " +
        		        "from annotation an,document d,extraction ex " +
        		        "where d.idDocument=an.documentIdDocument and " +
        		        "an.idAnnotation=ex.annotationIdAnnotation and " +
        		        "ar.URI Like ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + authority + "%");
        ResultSet rs = ps.executeQuery(sql);
        while(rs.next()) 
            result = rs.getInt("count");
        
        rs.close();
        ps.close();
        conn.close();            
        return result;
    }
    
    public int countExtractionGivenAuthorityAndTool(String authority, int tool) 
            throws SQLException 
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select count(idExtraction) as count " +
                        "from annotation an,document d,extraction ex " +
                        "where d.document=an.documentIdDocument and " +
                        "an.idAnnotation=ex.annotationIdAnnotation and " +
                        "ar.URI Like ? and toolIdTool=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%"+authority+"%");
        ps.setInt(2, tool);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) 
            result = rs.getInt("count");
        
        rs.close();
        ps.close();
        conn.close();            
        return result;
    }
    

    public int countCategoryGivenTool(int idTool) throws SQLException
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        Statement statement = conn.createStatement();
        String query =  "select count(distinct(type)) as count " +
                        "from annotation an,document d,extraction ex " +
                        "where d.idDocument=an.documentIdDocument and " +
                        "an.idAnnotation=ex.annotationIdAnnotation and " +
                        "an.toolIdTool=?";
        
        ResultSet categories = statement.executeQuery(query);
        while(categories.next()) {
            result = categories.getInt("count");
        }
        categories.close();
        statement.close();
        conn.close();            
        return result;
    }


    public int countCategoryGivenAuthorityAndTool(String authority, int idTool) 
    throws SQLException
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select count(distinct(type)) as count " +
        		        "from annotation an,document d,extraction ex " +
        		        "where d.idDocument=an.documentIdDocument and " +
        		        "an.idAnnotation=ex.annotationIdAnnotation and " +
        		        "ar.URI like '%%' and toolIdTool=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) 
            result = rs.getInt("count");
        
        rs.close();
        ps.close();
        conn.close();            
        return result;
    }
    


    public int countURIGivenTool(int idTool) throws SQLException
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select count(distinct(ex.URI)) as count " +
                      "from annotation an,document d,extraction ex " +
                      "where d.idDocument=an.documentIdDocument and " +
                      "an.idAnnotation=ex.annotationIdAnnotation and " +
                      "toolIdTool=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idTool);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) 
            result = rs.getInt("count");
        
        rs.close();
        ps.close();
        conn.close();            
        return result;
    }


    public int countURIGivenAuthorityAndTool(String authority, int idTool) 
    throws SQLException
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select count(distinct(ex.URI)) as count " +
        		        "from annotation an,document d,extraction ex " +
        		        "where d.idDocument=an.documentIdDocument and " +
        		        "an.idAnnotation=ex.annotationIdAnnotation and " +
        		        "ar.URI like ? and toolIdTool=?";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + authority + "%");
        ps.setInt(2, idTool);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) 
            result = rs.getInt("count");
        
        rs.close();
        ps.close();
        conn.close();            
        return result;
    }

    public int countNERDTypeGivenTool(int idTool, String NERDType) 
    throws SQLException
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select count(idExtraction) as count " +
        "from annotation an,document d,extraction ex " +
        "where d.idDocument=an.documentIdDocument and " +
        "an.idAnnotation=ex.annotationIdAnnotation and " +
        "toolIdTool=? and ex.NERDType LIKE ?";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idTool);
        ps.setString(2, "%" + NERDType + "%");
        ResultSet rs = ps.executeQuery();
        while(rs.next()) 
        result = rs.getInt("count");
        
        rs.close();
        ps.close();
        conn.close();            
        return result;
    }
    
    
    public int countNERDTypeGivenAuthorityAndTool(  String authority, 
                                                    int idTool, 
                                                    String NERDType) 
    throws SQLException
    {
        int result = 0;
        
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select count(idExtraction) as count " +
        		        "from annotation an,document d,extraction ex " +
        		        "where d.idDocument=an.documentIdDocument and " +
        		        "an.idAnnotation=ex.annotationIdAnnotation and " +
        		        "ar.URI Like ? and " +
        		        "toolIdTool=? and ex.NERDType LIKE ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + authority + "%");
        ps.setInt(2, idTool);
        ps.setString(3, "%" + NERDType);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) 
            result = rs.getInt("count");
        
        rs.close();
        ps.close();
        conn.close();            
        return result;
    }


    public int countUsers() throws SQLException
    {
        int result = 0;
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
  
        Statement statement = conn.createStatement();

        ResultSet users= statement.
                executeQuery("select count(idUser) from user");

        while(users.next()) {
            result = users.getInt("count(idUser)");
        }
        users.close();
        statement.close();
        conn.close();
        
        return result;
    }

    /*
     *  read complex structure
     */
    public ArrayList<Pair> getTypeList(int article, int tool, Integer idUser) 
    throws SQLException 
    {
        // select URI,validityURI from entity,analysis,annotation where articleIdArticle=2 AND toolIdTool=1 AND userIduser=3 AND idEntity=entityIdEntity AND idAnalysis=analysisIdAnalysis;
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();

        String sql = "select NE,validityType from annotation,extraction,evaluation " +
                "where documentIdDocument=? AND toolIdTool=? AND userIduser=? " +
                "AND idAnnotation=annotationIdAnnotation and idExtraction=extractionIdExtraction;";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, article);
        ps.setInt(2, tool);
        ps.setInt(3, idUser);
        
        ResultSet rs = ps.executeQuery();
        ArrayList<Pair> result = new ArrayList<Pair>();
        while (rs.next())
            result.add( new Pair(rs.getString(1), rs.getBoolean(2)) );
        
        rs.close();
        ps.close();
        conn.close();
        
        return result;
    }    
    
    
//    public ArrayList<Triple> getNEList(   int article, 
//                                          int tool,
//                                          Integer idUser ) 
//    throws SQLException 
//    {
//        // select name,validityEntity,relevant from entity,analysis where articleIdArticle=2 AND toolIdTool=1 AND userIduser=3 AND idAnalysis=analysisIdAnalysis;
//        Connection conn = db.connect();
//        if(conn == null) throw new SQLException();
//        
//        String sql = "select NE,validityNE,relevant from evaluation,extraction,annotation " +
//        "where documentIdDocument=? AND toolIdTool=? AND userIduser=? AND idAnnotation=annotationIdAnnotation AND idExtraction=extractionIdExtraction;";
//        
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.setInt(1, article);
//        ps.setInt(2, tool);
//        ps.setInt(3, idUser);
//        ResultSet rs = ps.executeQuery();
//        
//        ArrayList<Triple> result = new ArrayList<Triple>();
//        while (rs.next())
//            result.add( new Triple(rs.getString(1), rs.getBoolean(2), rs.getBoolean(3)) );
//        
//        rs.close();
//        ps.close();
//        conn.close();
//        
//        return result;
//    }

    
    public ArrayList<Pair> getUriList(int article, int tool, Integer idUser) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();

        String sql = "select NE,validityURI from annotation,extraction,evaluation " +
                "where documentIdDocument=? AND toolIdTool=? AND userIduser=? " +
                "AND idAnnotation=annotationIdAnnotation AND idExtraction=extractionIdExtraction;";
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, article);
        ps.setInt(2, tool);
        ps.setInt(3, idUser);
        ResultSet rs = ps.executeQuery();
        ArrayList<Pair> result = new ArrayList<Pair>();
        while (rs.next()){
            result.add( new Pair(rs.getString(1), rs.getBoolean(2)) );
        }
        ps.close();
        conn.close();
        
        return result;
    }    
    
    public List<TEntity> getExtractionsAlreadyPerformed(int idDocument, int idTool) 
    throws SQLException 
    {
        // else we need to insert a new row
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select label,extractorType,uri,nerdType,startChar,endChar," +
        		        "startNPT,endNPT,confidence,relevance,extractor" +
                        " from entity where annotationIdAnnotation =" +
                        " (select idAnnotation from annotation where documentIdDocument=?"+
                        " and toolIdTool=? order by idAnnotation limit 1);";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idDocument);
        ps.setInt(2, idTool);
        ResultSet rs = ps.executeQuery();
        List<TEntity> result = new LinkedList<TEntity>();
        while(rs.next()) {
            String label = rs.getString("label");
            String extractorType = rs.getString("extractorType");
            String uri = rs.getString("uri");
            String nerdType = rs.getString("nerdType");
            int startChar = rs.getInt("startChar");
            int endChar = rs.getInt("endChar");
            Double startNPT = rs.getDouble("startNPT");
            Double endNPT = rs.getDouble("endNPT");
            Double confidence = rs.getDouble("confidence");
            Double relevance = rs.getDouble("relevance");
            String extractor = rs.getString("extractor");
            TEntity entity = new TEntity(label,extractorType,uri,nerdType,
                                                     startChar,endChar,confidence,
                                                     relevance,extractor,
                                                     startNPT,endNPT);
            result.add(entity);
        }
        rs.close();
        ps.close();
        conn.close();
        
        return result;
    }
 
    public List<Pair> getRankCategoriesGivenTool(int tool) throws SQLException
    {       
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql   =  "select type, count(*) as count " +
                        "from annotation an,document d,extraction ex " +
                        "where d.idDocument=an.documentIdDocument and " +
                        "an.idAnnotation=ex.annotationIdAnnotation and " +
                        "toolIdTool=? " +
                        "group by type order by count DESC;";  
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, tool);
        ResultSet rs = ps.executeQuery();
        List<Pair> result = new LinkedList<Pair>();
        while(rs.next()) {
            String category = rs.getString("type");
            int number = rs.getInt("count");
            Pair p = new Pair(category, number);
            result.add(p);
        }
        rs.close();
        ps.close();
        conn.close();
        return result;
    }    
    
    public List<Pair> getRankCategoriesGivenAuthorityAndTool(String authority, int tool) 
    throws SQLException
    {       
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();    
        
        String sql =  "select type, count(*) as count " +
        		        "from annotation an,document d,extraction ex " +
        		        "where d.idDocument=an.documentIdDocument and " +
        		        "an.idAnnotation=ex.annotationIdAnnotation and " +
        		        "ar.URI like ? and toolIdTool=? " +
        		        "group by type order by count DESC;";  
        
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + authority + "%");
        ps.setInt(2, tool);
        ResultSet rs = ps.executeQuery();
        List<Pair> result = new LinkedList<Pair>();
        while(rs.next()) {
            String category = rs.getString("type");
            int number = rs.getInt("count");
            Pair p = new Pair(category, number);
            result.add(p);
        }
        rs.close();
        ps.close();
        conn.close();
        return result;
    }
 
    /*
     *  internal function
     */
    private int selectLastIndexAnnotation() throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        Statement statement = conn.createStatement();
        ResultSet annotationexists= statement.
            executeQuery("select idAnnotation from annotation order by idAnnotation DESC limit 1");
        int idAnnotation= -1;
        while(annotationexists.next()) {
            idAnnotation =  annotationexists.getInt(1);
        }
        
        statement.close();
        conn.close();
        return idAnnotation;
    }


    public void deleteEvaluation(int idEvaluation) throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();

        String sql = "delete from evaluation where idEvaluation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idEvaluation);
        ps.execute();
        
        ps.close();
        conn.close();       
    }


    public void deleteAnnotation(Integer idAnnotation) throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "delete from annotation where idAnnotation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idAnnotation);
        ps.execute();

        ps.close();
        conn.close();               
    }


    public void deleteExtraction(int idExtraction) throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "delete from extraction where idExtraction=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idExtraction);
        ps.execute();

        ps.close();
        conn.close();       
    }


    public void deleteUser(Integer idUser) throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        String sql = "delete from user where idUser=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idUser);
        ps.execute();
        
        ps.close();
        conn.close();      
    }


    public void updateUser(TUser user) 
    throws SQLException
    {
        Connection conn = db.connect();
        
        String sql ="UPDATE user SET " +
                    "firstName=?,lastName=?,nickName=?,email=?,website=?,projectName=?,projectUri=?," +
                    "organization=?,country=? WHERE idUser=?";
        
        PreparedStatement ps = conn.prepareStatement(sql);   
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getNickName());
        ps.setString(4, user.getEmail());
        ps.setString(5, user.getWebsite());
        ps.setString(6, user.getProjectName());
        ps.setString(7, user.getProjectUri());
        ps.setString(8, user.getOrganization());
        ps.setString(9, user.getCountry());
        ps.setInt(10, user.getId());
        
        ps.executeUpdate();
        ps.close();
        conn.close();    
        
    }

    public void updateUser(int id, TUser user) 
    throws SQLException
    {
        Connection conn = db.connect();
                
        String sql ="UPDATE user SET " +
                    "firstName=?,lastName=?,email=?,website=?,projectName=?,projectUri=?," +
                    "organization=?,country=?,validity=?,registrationDate=NOW() " +
                    "WHERE idUser=?";
        
        PreparedStatement ps = conn.prepareStatement(sql);   
        ps.setString(1, user.getFirstName());
        ps.setString(2, user.getLastName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getWebsite());
        ps.setString(5, user.getProjectName());
        ps.setString(6, user.getProjectUri());
        ps.setString(7, user.getOrganization());
        ps.setString(8, user.getCountry());
        ps.setBoolean(9, user.getValidity());
        ps.setInt(10, id);
        
        ps.executeUpdate();
        ps.close();
        conn.close();
    }


    public String selectKey(int idUser, int idTool)
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        //Statement statement = conn.createStatement();
        PreparedStatement statement = conn.
                prepareStatement(   "SELECT apikey " +
                                    "FROM service " +
                                    "WHERE userIdUser = ? and toolIdTool= ?"
                                );
        statement.setInt(1, idUser);
        statement.setInt(2, idTool);
        
        ResultSet result = statement.executeQuery();
        String key = null;
        while(result.next()) 
            key = result.getString ("apikey");
        
        result.close();
        statement.close();
        conn.close();
        return key;
    }
    
    public String selectKeyByName(int idUser, String extractor) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        //Statement statement = conn.createStatement();
        PreparedStatement statement = conn.
                prepareStatement(   "SELECT apikey " +
                                    "FROM service s,tool t " +
                                    "WHERE s.userIdUser = ? and t.name=? and s.toolIdTool=t.idTool"
                                );
        statement.setInt(1, idUser);
        statement.setString(2, extractor);
        
        ResultSet result = statement.executeQuery();
        String key = null;
        while(result.next()) 
            key = result.getString ("apikey");
        
        result.close();
        statement.close();
        conn.close();
        return key;
    }

    public void insertKey(int idUser, int idTool, String key) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        PreparedStatement statement = conn.
                prepareStatement(   "insert into service (userIduser,toolIdTool,apikey) " +
                                    "values(?,?,?)");

        statement.setInt(1, idUser);
        statement.setInt(2, idTool);
        statement.setString(3, key);
        statement.execute();
        
        statement.close();
        conn.close();
    }

    public void updateKey(int idUser, int idTool, String key) 
    throws SQLException
    {

        Connection conn = db.connect();
        
        String sql ="UPDATE service SET " +
                    "apikey=? WHERE userIdUser=? and toolIdTool=?";
        
        PreparedStatement ps = conn.prepareStatement(sql);   
        ps.setString(1, key);
        ps.setInt(2, idUser);
        ps.setInt(3, idTool);
        ps.execute();
        
        ps.close();
        conn.close();
        
    }

    public Hashtable<String, String> selectAllKeys(int idUser) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        //Statement statement = conn.createStatement();
        PreparedStatement statement = conn.
                prepareStatement(   "SELECT toolIdTool, apikey " +
                                    "FROM service " +
                                    "WHERE userIdUser = ?"
                                );
        statement.setInt(1, idUser);
              
        ResultSet rs = statement.executeQuery();
        Hashtable<String, String> result = new Hashtable<String, String>();

        while(rs.next()) {
            String name = (selectTool(rs.getInt("toolIdTool"))).getName();
            result.put(name, rs.getString("apiKey"));
        }
            
        rs.close();
        statement.close();
        conn.close();
        return result;
    }

//    public void updateDailyCounter(Integer idUser) 
//    throws SQLException
//    {
//        Connection conn = db.connect();
//        
//        String sql =    "UPDATE user SET " +
//                        "dailyUsage = dailyUsage + 1 "+
//                        "WHERE idUser=?";
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.setInt(1, idUser);
//    
//        ps.executeUpdate();
//        ps.close();
//        conn.close();
//        
//    }
//
//    public void updateTotalCounter(int idUser) 
//    throws SQLException    
//    {
//        Connection conn = db.connect();
//        
//        String sql =    "UPDATE user SET " +
//                        "totalUsage = totalUsage + 1 "+
//                        "WHERE idUser=?";
//        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.setInt(1, idUser);
//
//        ps.executeUpdate();
//        ps.close();
//        conn.close();
//    }

    public List<TDocument> selectLastDocuments(int idUser, int lastRecentDocuments) 
    throws SQLException, LanguageException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
         
        //Statement statement = conn.createStatement();
        PreparedStatement statement = conn.
                prepareStatement(   "SELECT distinct(idDocument),text " +
                                    "FROM annotation a,document d " +
                                    "WHERE a.userIdUser = ? and a.documentIdDocument=d.idDocument " +
                                    "order by a.timestamp desc limit ?"
                                );
        statement.setInt(1, idUser);
        statement.setInt(2, lastRecentDocuments);
        ResultSet rs = statement.executeQuery();
        List<TDocument> docs = new LinkedList<TDocument>();
        while(rs.next()) {
            docs.add(   new TDocument(rs.getInt ("idDocument"), rs.getString ("text") )    
                    );
        }
            
        rs.close();
        statement.close();
        conn.close();
        return docs;
    }

    public List<VUsage> countUsage(int idUser, String extractorName, 
                                      Integer nDays, String start, String end) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        now.setDate(now.getDate()+1);
        Date before = new Date();
        before.setDate(before.getDate()-nDays);
        String snow = df.format(now);
        String sbefore = df.format(before);        
        
        PreparedStatement ps = null;
        if(extractorName == null) {
            ps = conn.prepareStatement( 
                             "SELECT date(timestamp) as d, count(*) as c " +
                             "FROM annotation " +
                             "WHERE userIdUser = ? and " +
                             "timestamp >= ? and timestamp <= ? "  +
                             "group by date(timestamp) desc"
                            );
            ps.setInt(1, idUser);
            ps.setString(2, sbefore);
            ps.setString(3, snow);
        }
        else {
            if( start != null && end != null ) 
            {
                ps = conn.prepareStatement( "SELECT date(timestamp) as d, count(*) as c " +
                                     "FROM tool, annotation " +
                                     "WHERE userIdUser = ? and name= ? and " +
                                     "tool.idTool=annotation.toolIdTool and " +
                                     "timestamp >= ? and timestamp <= ? " +
                                     "group by date(timestamp) desc"
                );
                
                ps.setInt(1, idUser);
                ps.setString(2, extractorName);
                ps.setString(3, start);
                ps.setString(4, end);
            }
            else { 
                //get last n statistics
                ps = conn.prepareStatement( 
                                    "SELECT date(timestamp) as d, count(*) as c " +
                                    "FROM tool, annotation " +
                                    "WHERE userIdUser = ? and name= ? and " +
                                    "timestamp >= ? and timestamp <= ? and "  +
                                    "tool.idTool=annotation.toolIdTool " +
                                    "group by date(timestamp) desc"
                                   );
                ps.setInt(1, idUser);
                ps.setString(2, extractorName);
                ps.setString(3, sbefore);
                ps.setString(4, snow);
            }
        }
        
        ResultSet rs = ps.executeQuery();
        HashMap<String, VUsage> map = new HashMap<String, VUsage>();
        while(rs.next()) map.put(rs.getString("d"), new VUsage(rs.getString("d"), rs.getInt("c")));
        rs.close();
        ps.close();
        conn.close();
        
        if(start!=null && end!=null) {
            Collection<VUsage> coll = map.values();
            List<VUsage> result = new LinkedList<VUsage>(coll);
            Collections.sort(result, VUsage.DESC);
            return result;
        }
        
        Vector<VUsage> window = new Vector<VUsage>(nDays);
        for(int i=0; i<=nDays; i++) {
            now.setDate(now.getDate()-1);
            String day = df.format(now);
            VUsage temp = ( map.get(day) != null) ? map.get(day) : new VUsage(day, 0);
            window.add(temp);
        }
        return window;
    }
    
//    public List<VUsage> countUsage(int idUser, String extractorName, 
//                                   Integer nDays, String start, String end) 
//    throws SQLException
//    {
//        Connection conn = db.connect();
//        if(conn == null) throw new SQLException();    
//        
//        PreparedStatement ps = null;
//        if(extractorName == null) {
//            ps = conn.prepareStatement( "SELECT date(timestamp) as d, count(*) as c " +
//                                        "FROM annotation " +
//                                        "WHERE userIdUser = ? " +
//                                        "group by date(timestamp) desc limit ?"
//                                       );
//            ps.setInt(1, idUser);
//            ps.setInt(2, nDays);
//        }
//        else {
//            if( start != null && end != null ) 
//            {
//                ps = conn.prepareStatement( "SELECT date(timestamp) as d, count(*) as c " +
//                                            "FROM tool, annotation " +
//                                            "WHERE userIdUser = ? and name= ? and " +
//                                            "tool.idTool=annotation.toolIdTool and " +
//                                            "timestamp >= ? and timestamp <= ? " +
//                                            "group by date(timestamp) asc"
//                      );
//               
//                ps.setInt(1, idUser);
//                ps.setString(2, extractorName);
//                ps.setString(3, start);
//                ps.setString(4, end);
//                
//            }
//            else { 
//                //get last n statistics
//                ps = conn.prepareStatement( "SELECT date(timestamp) as d, count(*) as c " +
//                                            "FROM tool, annotation " +
//                                            "WHERE userIdUser = ? and name= ? and " +
//                                                  "tool.idTool=annotation.toolIdTool " +
//                                            "group by date(timestamp) asc limit ?"
//                                          );
//                ps.setInt(1, idUser);
//                ps.setString(2, extractorName);
//                ps.setInt(3, nDays);
//            }
//        }
//            
//        ResultSet rs = ps.executeQuery();
//        Vector<VUsage> result = new Vector<VUsage>();
//        int i=0;
//        while(rs.next()) 
//            result.add(new VUsage(rs.getString("d"), rs.getInt("c")));
//        rs.close();
//        ps.close();
//        conn.close();
//        
//        if(extractorName==null)
//            while(i++<nDays) 
//                result.add(new VUsage(null, 0));
//            
//        return result;
//    }

    public List<TTool> countExtractorStatistics(int idUser) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();

        // select date(timestamp) from annotation group by date(timestamp) desc limit 30;
        PreparedStatement ps = conn.
                prepareStatement(   "SELECT name, count(*) as c " +
                                    "FROM annotation a,tool t " +
                                    "WHERE a.toolIdTool=t.idTool and userIdUser=? " +
                                    "group by name"
                                );
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        Vector<TTool> result = new Vector<TTool>();
        while(rs.next()) 
            result.add(new TTool(rs.getString("name"), rs.getInt("c")));
        rs.close();
        ps.close();
        
        return result;
    }

    public List<VStream> getStream(int idUser) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
              
        PreparedStatement ps = conn.
              prepareStatement( "select idAnnotation, ontology, timestamp, idDocument, name " +
              		            "from annotation a, document d, tool t " +
              		            "where userIdUser=? and d.idDocument=a.documentIdDocument " +
              		            "and t.idTool=a.toolIdTool order by timestamp desc limit 100"
                              );
        ps.setInt(1, idUser);
        
        ResultSet rs = ps.executeQuery();
        Vector<VStream> result = new Vector<VStream>();
        while(rs.next()) {
            PreparedStatement psTemp = 
                    conn.prepareStatement("select count(idEntity) as entityNumber, " +
                    		"count(distinct(nerdType)) as categoryNumber " +
                    		"from entity where annotationIdAnnotation=?" );
            psTemp.setInt(1, rs.getInt("idAnnotation"));
            ResultSet rsTemp = psTemp.executeQuery();
            int entityNumber=0, categoryNumber=0;
            while(rsTemp.next()) {
                entityNumber = rsTemp.getInt("entityNumber");
                categoryNumber = rsTemp.getInt("categoryNumber");
            }
                       
            result.add( new VStream(rs.getString("timestamp"), 
                                    "/document/" + rs.getString("idDocument"),
                                    "/annotation/" + rs.getString("idAnnotation"),
                                    rs.getString("name"),
                                    rs.getString("ontology"),
                                    entityNumber,
                                    categoryNumber) );
            
            rsTemp.close();
            psTemp.close();
        }
        rs.close();
        ps.close();
        return result;      
    }

    public void deleteToken(int idUser) 
    throws SQLException 
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
              
        PreparedStatement ps = conn.
              prepareStatement( "update user " +
              		            "set tokenTimeStamp = NOW() " +
              		            "where idUser = ?"
                              );
        ps.setInt(1, idUser);
        
        ps.executeUpdate();
        ps.close();        
    }

    public void updateDailyCounter(int idUser) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
              
        PreparedStatement ps = conn.
              prepareStatement( "update user " +
                                "set dailyUsage = dailyUsage + 1 " +
                                "where idUser = ?"
                              );
        ps.setInt(1, idUser);
        
        ps.executeUpdate();
        ps.close();        
    }

    public void updateRegistrationKeyDate(int idUser) 
    throws SQLException
    {
        Connection conn = db.connect();
        if(conn == null) throw new SQLException();
              
        PreparedStatement ps = conn.
              prepareStatement( "update user " +
                                "set registrationKeyDate = NOW()" +
                                "where idUser = ?"
                              );
        ps.setInt(1, idUser);
        
        ps.executeUpdate();
        ps.close(); 
        
    }    
}
