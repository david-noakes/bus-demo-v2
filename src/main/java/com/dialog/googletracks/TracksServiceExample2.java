package com.dialog.googletracks;

/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;

/**
 * @author Yaniv Inbar
 */
public class TracksServiceExample2 {


  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "Bus Ferry GTFS Realtime Tracks API Project";
  
  /** E-mail address of the service account. */
  private static final String SERVICE_ACCOUNT_EMAIL = "182726294251-r30936t0d6lusl56t9okve6bijpjq7c5@developer.gserviceaccount.com";

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** Global configuration for location of private key file. */
  private static final String PRIVATE_KEY = "target/classes/com/dialog/googletracks/2d4511f818fee924312c4ae34518de0dc37c018d-privatekey.p12";

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  
  /** Global configuration of OAuth 2.0 scope. */
  private static final String TRACKS_SCOPE = "https://www.googleapis.com/auth/tracks"; 


  public static void main(String[] args) {
	    String userDir = System.getProperty("user.dir");
	    String p12FileName = userDir+"/"+PRIVATE_KEY;

   try {
      try {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        // service account credential (uncomment setServiceAccountUser for domain-wide delegation)
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
            .setServiceAccountScopes(Collections.singleton(TRACKS_SCOPE))
            .setClientAuthentication(null)
            .setServiceAccountPrivateKeyFromP12File(new File(p12FileName))
            // .setServiceAccountUser("SERVICE_ACCOUNT_EMAIL")
            .build();
        // Set up and execute Tracks API Request.
        String method = args[0];
        String URI = "https://www.googleapis.com/tracks/v1/" + method;
        String requestBody = args[1];
        HttpRequestFactory requestFactory =
        		httpTransport.createRequestFactory(credential);
        GenericUrl url = new GenericUrl(URI);
        HttpRequest request =
            requestFactory.buildPostRequest(url,ByteArrayContent.fromString(null, requestBody));
        request.getHeaders().setContentType("application/json");
        System.out.println(request.getHeaders());
        System.out.println(request.getContent());
        HttpResponse shortUrl = request.execute();

        // Print response.
        BufferedReader output = new BufferedReader(new InputStreamReader(shortUrl.getContent()));
        for (String line = output.readLine(); line != null; line = output.readLine()) {
          System.out.println(line);
        }
        return;
      } catch (IOException e) {
        System.err.println(e.getMessage());
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }

}