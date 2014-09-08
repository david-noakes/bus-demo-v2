package com.dialog.googletracks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
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
        if (json.startsWith(GoogleTracksConstants.JSONSERVLET_GET_ENTITY_CRUMBS)) {
            String tracksString = json.substring(GoogleTracksConstants.JSONSERVLET_GET_ENTITY_CRUMBS.length());
            // expect collectionId and entityIds []
            // use collectionId to determine the posix date
            JSONObject jObj ;
            try {
              jObj = (JSONObject) jsonParser.parse( tracksString );
              String collectionId = (String) jObj.get(GoogleTracksConstants.COLLECTION_ID);
              JSONArray jsonEntities = (JSONArray) jObj.get(GoogleTracksConstants.ENTITY_IDS);
              if (collectionId != null && collectionId.trim().length() > 0 &&
                      jsonEntities != null && jsonEntities.size() > 0 ) {
                  writer.write(getEntityHistories(collectionId, jsonEntities));
                  return;
              }
              
            } catch (ParseException e) {
                System.out.println("position: " + e.getPosition());
                System.out.println(e);
            }
            
            writer.write("{\"error\": \"bad request \", " + json + " }");
             
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
    
    private String getEntityHistories(String collectionId, JSONArray jsonEntities) {
        // we allow multiple entities
        // package as {entityId: xxx crumbs: []}, ... 
        // {get_entity_crumbs: [] }
        String response = "";
        JSONParser jsonParser=new JSONParser();
        JSONArray jArray = new JSONArray();
        
        GoogleTracksCollection gtColl = tracksData.findCollectionById(collectionId);
        String sDate = gtColl.getName().substring(0, 8);
        int year = Integer.parseInt(sDate.substring(0, 4));
        int month = Integer.parseInt(sDate.substring(4, 6));
        int day = Integer.parseInt(sDate.substring(6, 8));
        Date tStamp = new Date(year, month, day, 22, 0)  ; // make it in the evening
        int timestamp = (int) (tStamp.getTime() / 1000L);
        
        for (int i = 0;i<jsonEntities.size();i++) {
            GoogleTracksEntity gtEnt = gtColl.findEntityById((String) jsonEntities.get(i));
            try {
                response = TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_CRUMBS_GET_HISTORY, gtEnt.generateCrumbsHistoryRequest(timestamp, -160));
                try { // add the entityId to the crumbs array
                    JSONObject jObj = (JSONObject) jsonParser.parse( response );
                    jObj.put(GoogleTracksConstants.ENTITY_ID, gtEnt.getID());
                    jArray.add(jObj);
                  } catch (ParseException e) {
                      System.out.println("position: " + e.getPosition());
                      System.out.println(e);
                  }
               
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                
            } catch (GeneralSecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        JSONObject jObj = new JSONObject();
        jObj.put(GoogleTracksConstants.JSONSERVLET_GET_ENTITY_CRUMBS, jArray);
        return jObj.toJSONString();
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