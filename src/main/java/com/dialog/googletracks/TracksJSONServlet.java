package com.dialog.googletracks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.io.Files;
 
public class TracksJSONServlet extends HttpServlet {
 
    private static final long serialVersionUID = 1L;

    @PostConstruct
    public void start() {
        tracksData = new GoogleTracksCollectionList();
    }
 
    @PreDestroy
    public void stop() {
        // nothing to do yet
    }
    
    // This will store all received articles
    GoogleTracksCollectionList tracksData = new GoogleTracksCollectionList();
 
    /***************************************************
     * URL: /tracksJSONServlet
     * doPost(): receives JSON data, parse it,
     * request information from google tracks if we don't currently have it,
     * send back as JSON
     ****************************************************/
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
 
        PrintWriter writer = response.getWriter();
       // 1. get received JSON data from request
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String json = "";
        if(br != null){
            json = br.readLine();
        }
        JSONParser jsonParser=new JSONParser();

//        try {
//            JSONObject jObj = (JSONObject) jsonParser.parse( json );
//        } catch (ParseException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        if (json.equals(GoogleTracksConstants.METHOD_LIST_COLLECTIONS) || 
            json.equals(GoogleTracksConstants.JSONSERVLET_GET_COLLECTIONS) ||
            json.length() == 0 ||
            tracksData.size()== 0) {
            loadCollectionsAndEntities();
            writer.write(tracksData.writeCollectionsAsJSONString());
            return;
        }
        // temporary - read collection list from disk
        String userDir = System.getProperty("user.dir");
        String collFileName = userDir + "/target/classes/com/dialog/googletracks/collections_List.txt";
        File collFile = new File(collFileName);
        String tracksString = Files.toString(collFile, Charset.defaultCharset());

        
        
//        // 2. initiate jackson mapper
//        ObjectMapper mapper = new ObjectMapper();
// 
//        // 3. Convert received JSON to Article
//        Article article = mapper.readValue(json, Article.class);
// 
//        // 4. Set response type to JSON
//        response.setContentType("application/json");            
// 
//        // 5. Add article to List<Article>
//        tracksData.add(article);
 
        // 6. Send List<Article> as JSON to client
        //mapper.writeValue(response.getOutputStream(), tracksData);
        //writer.write(tracksData.writeCollectionsAsJSONString());
        writer.write(tracksString);

    }
    
    private void loadCollectionsAndEntities() {
        String tracksString="";
        try {
            tracksString = TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_LIST_COLLECTIONS, " ");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (tracksString.trim().length() == 0) {
            initialiseCollectionlist("*** NO DATA FOUND ***");
            return;
        } else {
            tracksData = new GoogleTracksCollectionList(tracksString);
            if (tracksData.size() == 0) {
                initialiseCollectionlist("*** NO DATA FOUND ***");
                return;
            }
        }
      
        try {
            tracksString = "";
            tracksString = 
               TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_LIST_ENTITIES, " ");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (tracksString.length()>0) {
            tracksData.loadAllEntitiesFromTracksString(tracksString);
            GoogleTracksCollection gtColl = new GoogleTracksCollection("orphan", "Orphaned Entities", new ArrayList<GoogleTracksEntity>());
            for (int i = 0; i < tracksData.getAllEntities().size(); i++) {
                GoogleTracksEntity gtEnt = tracksData.getAllEntities().get(i);
                if (tracksData.isThisEntityOrphaned(gtEnt)) {
                    gtColl.addEntityToCollection(gtEnt);
                }
            }
        }
        
    }
    
    private void initialiseCollectionlist(String collectionName){
        String tracksString;
        tracksData = new GoogleTracksCollectionList();
        GoogleTracksCollection gtColl = new GoogleTracksCollection();
        gtColl.setName(collectionName);
        tracksData.add(gtColl);
        // DUMMY DATA FOR MAKING ERROR HANDLING EASY
    }

}