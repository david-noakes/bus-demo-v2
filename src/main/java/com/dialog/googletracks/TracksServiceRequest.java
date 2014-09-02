package com.dialog.googletracks;

import com.fasterxml.jackson.core.*;
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

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;


public class TracksServiceRequest { 

	  /** E-mail address of the service account. */
	  private static final String SERVICE_ACCOUNT_EMAIL =
	          "182726294251-r30936t0d6lusl56t9okve6bijpjq7c5@developer.gserviceaccount.com";
	  /** Global configuration of OAuth 2.0 scope. */
	  private static final String TRACKS_SCOPE= "https://www.googleapis.com/auth/tracks";

	  /** Global configuration for location of private key file. */
	  private static final String PRIVATE_KEY = "target/classes/com/dialog/googletracks/2d4511f818fee924312c4ae34518de0dc37c018d-privatekey.p12";

	  public static String getPrivateKey() {
		return PRIVATE_KEY;
	  }
	  
	  public static GoogleCredential credential = null;

	/** Global instance of the HTTP transport. */
	  
	  private static HttpTransport HTTP_TRANSPORT = null;
	  
	  private static boolean _SimulateTracks = false;

	  /**
     * @return the _SimulateTracks
     */
    public static boolean getSimulateTracks() {
        return _SimulateTracks;
    }

    /**
     * @param _SimulateTracks the _SimulateTracks to set
     */
    public static void setSimulateTracks(boolean _SimulateTracks) {
        TracksServiceRequest._SimulateTracks = _SimulateTracks;
    }

    /** Global instance of the JSON factory. */
	  private static JsonFactory JSON_FACTORY = new JacksonFactory();

	  public static String serviceRequest(String method, String requestBody) throws IOException, GeneralSecurityException {

	     if (HTTP_TRANSPORT == null) { 
	         HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	     } 
	    String userDir = System.getProperty("user.dir");
	    String p12FileName = userDir+"/"+PRIVATE_KEY;
	    String URI = "";;
	    String response = "";
	    
	    File p12File = new File(p12FileName);
	    
	    // partial fix for testing when daily limits exceeded
	    // this simulates the google response.
	    if (_SimulateTracks) {
            System.out.println(method);
            System.out.println(requestBody);
    	    if (method.equals(GoogleTracksConstants.METHOD_LIST_COLLECTIONS)) {
    	        String collFileName = userDir + "/target/classes/com/dialog/googletracks/collections_List.txt";
    	        File collFile = new File(collFileName);
    	        response = Files.toString(collFile, Charset.defaultCharset());
    	        System.out.println(response);
    	        return response;
    
    	    }
    	    if (method.equals(GoogleTracksConstants.METHOD_LIST_ENTITIES)) {
    	        String entfileName = userDir + "/target/classes/com/dialog/googletracks/entities_List.txt";
    	        File entFile = new File(entfileName);
    	        response = Files.toString(entFile, Charset.defaultCharset());
                System.out.println(response);
    	        return response;
    	    } 
   	        return "{\"message\": \"Daily Limit Exceeded\"}";
	    }
	    // Build service account credential.
	    if (credential==null) {
    	    try {
    	    	credential=
    	        new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
    	            .setJsonFactory(JSON_FACTORY)
    	            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
    	            .setServiceAccountScopes(Collections.singleton(TRACKS_SCOPE))
    	            .setServiceAccountPrivateKeyFromP12File(new File(p12FileName))
    	            .build();
    	    }
    	    catch (GeneralSecurityException e) {
    	    	e.printStackTrace();
    	    }
	    }
	    // Set up and execute Tracks API Request.
	    URI = GoogleTracksConstants.GOOGLE_TRACKS_API + method;
	    HttpRequestFactory requestFactory =
	        HTTP_TRANSPORT.createRequestFactory(credential);
	    GenericUrl url = new GenericUrl(URI);
	    HttpRequest request =
	        requestFactory.buildPostRequest(url,ByteArrayContent.fromString(null, requestBody));
	    request.getHeaders().setContentType("application/json");
        // Print Request
        System.out.println(method + " " + requestBody);
        System.out.println(request.getHeaders());
        System.out.println(request.getContent());
        
	    HttpResponse shortUrl = request.execute();

	    // Print response.
	    BufferedReader output = new BufferedReader(new InputStreamReader(shortUrl.getContent()));
	    for (String line = output.readLine(); line != null; line = output.readLine()) {
	      System.out.println(line);
          response = response + line;
	    }
	    
	    return response;
	  }


}
