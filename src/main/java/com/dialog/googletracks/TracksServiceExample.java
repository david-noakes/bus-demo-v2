package com.dialog.googletracks;


import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Make a request to Tracks API.
 * Command Line Usage Examples (with Maven)
 * <pre>{@code
 * mvn -q exec:java -Dexec.args="METHOD JSON_REQUEST_BODY"
 * mvn -q exec:java -Dexec.args="crumbs/getlocationinfo {'entityId':'280415822391405995','timestamp':'1334643465000000'}"
 * mvn -q exec:java -Dexec.args="entities/list ''"
 * mvn -q exec:java -Dexec.args="entities/create {'entities':[{'name':'auto001','type':'AUTOMOBILE'}]}"}</pre>
 * "crumbs/record {'crumbs': [{'confidenceRadius': 3.14,'location': {'lat': -33.866495,'lng': 151.195446},'timestamp': 1341375062.19,'userData': {'driver_name': 'Joe','measured_vehicle_speed': '110.2'}}],'entityId': '1ff3a55f94e954ee'}"
 */
public class TracksServiceExample {

  /** E-mail address of the service account. */
  private static final String SERVICE_ACCOUNT_EMAIL =
      "182726294251-r30936t0d6lusl56t9okve6bijpjq7c5@developer.gserviceaccount.com";
  
  /** Global configuration of OAuth 2.0 scope. */
  private static final String TRACKS_SCOPE = "https://www.googleapis.com/auth/tracks"; 

  /** Global configuration for location of private key file. */
  private static final String PRIVATE_KEY = "target/classes/com/dialog/googletracks/2d4511f818fee924312c4ae34518de0dc37c018d-privatekey.p12";

  /** client secrets */
  private static final String CLIENT_SECRETS_FILE = 
		  "target/classes/com/dialog/googletracks/client_secrets (1).json";
  
  /** Global instance of the HTTP transport. */
  private static final HttpTransport NET_HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  public static void main(String[] args) throws IOException, GeneralSecurityException {
	  HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	  // Check for valid setup.
    String userDir = System.getProperty("user.dir");
    String p12FileName = userDir+"/"+PRIVATE_KEY;


     
   // Build service account credential.
    GoogleCredential credential =
        new GoogleCredential.Builder().setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
            .setServiceAccountScopes(Collections.singleton(TRACKS_SCOPE))
            .setServiceAccountPrivateKeyFromP12File(new File(p12FileName))
            .build();

    // Set up and execute Tracks API Request.
    
    GoogleTracksCollectionList gtCollList = new GoogleTracksCollectionList();
    GoogleTracksCollection gtColl = new GoogleTracksCollection("", "SEA rental fleet - compact", new ArrayList<GoogleTracksEntity>());
    gtCollList.add(gtColl);
    gtColl = new GoogleTracksCollection("", "SEA rental fleet - midsize", new ArrayList<GoogleTracksEntity>());
    gtCollList.add(gtColl);
    
    String method = "collections/create";
    String URI = "https://www.googleapis.com/tracks/v1/" + method;
    String requestBody = gtCollList.StoreToTracksString();

    HttpRequestFactory requestFactory =
      httpTransport.createRequestFactory(credential);  //1409033790941
    GenericUrl url = new GenericUrl(URI);
    HttpRequest request =
      requestFactory.buildPostRequest(url,ByteArrayContent.fromString(null, requestBody));
    request.getHeaders().setContentType("application/json");
    System.out.println(request.getHeaders());
    System.out.println(request.getContent());
    HttpResponse shortUrl = request.execute();
    
    // Print response.
    String response = "";
    BufferedReader output = new BufferedReader(new InputStreamReader(shortUrl.getContent()));
    for (String line = output.readLine(); line != null; line = output.readLine()) {
        System.out.println(line);
        response = response + line;
    }

//    String response = "{ " + "\"collectionIds\": [ " +
//                      "  \"0eb06d476f8a7486\",   " +
//                      " \"0179f339ec9232ef\" ] }";
    gtCollList.LoadCollectionIdsFromTracksString(response);
    GoogleTracksEntity gtEnt = new GoogleTracksEntity("eb27bbe02496d603","Bus001","AUTOMOBILE");
    gtCollList.get(0).get_Entities().add(gtEnt);
    gtEnt = new GoogleTracksEntity("174919bd798ce5cb","Bus002","AUTOMOBILE");
    gtCollList.get(0).get_Entities().add(gtEnt);
    gtEnt = new GoogleTracksEntity("eb27bbe02496d603","Bus003","AUTOMOBILE");
    gtCollList.get(1).get_Entities().add(gtEnt);
    gtEnt = new GoogleTracksEntity("174919bd798ce5cb","Bus004","AUTOMOBILE");
    gtCollList.get(1).get_Entities().add(gtEnt);
    
    
    method = "collections/addentities";
    URI = "https://www.googleapis.com/tracks/v1/" + method;
    for (int i=0;i<gtCollList.size();i++) {
       requestBody = gtCollList.get(i).AddEntitiesToTracksString();

    //HttpRequestFactory 
    requestFactory = httpTransport.createRequestFactory(credential);  //1409033790941
    //GenericUrl
    url = new GenericUrl(URI);
    //HttpRequest 
    request =
      requestFactory.buildPostRequest(url,ByteArrayContent.fromString(null, requestBody));
    request.getHeaders().setContentType("application/json");
    System.out.println(request.getHeaders());
    System.out.println(request.getContent());
    //HttpResponse 
    shortUrl = request.execute();
    
    // Print response.
    //BufferedReader 
    output = new BufferedReader(new InputStreamReader(shortUrl.getContent()));
    for (String line = output.readLine(); line != null; line = output.readLine()) {
        System.out.println(line);
    }
    
//       System.out.println(requestBody);
    }
    
    method = "collections/list";
    URI = "https://www.googleapis.com/tracks/v1/" + method;
//    String method = args[0];
//    method = "crumbs/record";
//    method = "crumbs/gethistory";
//    String URI = "https://www.googleapis.com/tracks/v1/" + method;
//    String requestBody = args[1];
//    requestBody = "{'crumbs': [ { 'location': {'lat': -27.48196792602539, 'lng': 153.03427124023438},'timestamp': " 
//    		        + (int) (System.currentTimeMillis() / 1000L) + ",'userData': {'driver_name': 'Joe','measured_vehicle_speed': '110.2'}}], 'entityId': 'eb27bbe02496d603' }";
//    requestBody = "{'entityId': 'eb27bbe02496d603',  'timestamp': 1409032121892, 'countAfter': 25 }";
    requestFactory =
    		httpTransport.createRequestFactory(credential);  //1409033790941
    url = new GenericUrl(URI);
    request =
        requestFactory.buildPostRequest(url,ByteArrayContent.fromString(null, requestBody));
    request.getHeaders().setContentType("application/json");
    System.out.println(request.getHeaders());
    System.out.println(request.getContent());
    shortUrl = request.execute();

    // Print response.
    output = new BufferedReader(new InputStreamReader(shortUrl.getContent()));
    for (String line = output.readLine(); line != null; line = output.readLine()) {
      System.out.println(line);
    }
//    String jsonTxt = "   	{ " +
//				   " 'collections': [" +
//				   "     { " +
//				   "         'entityIds': [ " +
//				   "             '1ff3a55f94e954ee', " +
//				   "             'ec6053f142ade5c9' " +
//				   "         ], " +
//				   "         'id': '0eb06d476f8a7486', " +
//				   "         'name': 'SEA rental fleet - compact' " +
//				   "     }, " +
//				   "     { " +
//				   "         'entityIds': [ " +
//				   "             '0ff3a55f94e954ee', " +
//				   "             'fc6053f142ade5c9' " +
//				   "         ], " +
//				   "         'id': '0179f339ec9232ef', " +
//				   "         'name': 'SEA rental fleet - midsize' " +
//				   "     } " +
//				   " ] 'nextId ' : '0291c259ca933c28' " +
//  				   "	} " 
//  				   ;
//    
//    jsonTxt = jsonTxt.replace("'", "\"");
//    JSONParser jsonParser=new JSONParser();
//    try {
//		JSONObject json = (JSONObject) jsonParser.parse( jsonTxt );
//		JSONArray jsonCollections = (JSONArray) json.get("collections");
//		String collName = "";
//		String collId   = "";
//		JSONObject nextId = (JSONObject) json.get("nextId"); // note should be "nextId " so we receive null
//        // take the elements of the json array
//        for(int i=0; i<jsonCollections.size(); i++){
//            JSONObject coll = (JSONObject) jsonCollections.get(i);
//            JSONArray  entities = (JSONArray) coll.get("entityIds");
//    		List<String> ents = new ArrayList();
//            if (entities != null) {
//            	for (int j=0; j< entities.size(); j++) {
//            		String entId = (String) entities.get(j);
//            		ents.add(entId);
//            	}
//            }
//            collName = (String) coll.get("name");
//            collId   = (String) coll.get("id");
//           System.out.println("collName="+collName+", collId="+collId);
//           for (int j=0; j< ents.size(); j++) {
//        	   System.out.println("  entity:"+ents.get(j));
//           }
//        }
//        if (nextId != null) {
//        	System.out.println("nextId:"+nextId);
//        } else {
//        	System.out.println("nextId:null");
//        }
//		
//	} catch (ParseException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//
    
  }
}
