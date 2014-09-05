package com.dialog.googletracks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.gtfs_realtime.visualizer.VisualizerService;

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
 
        // 1. get received JSON data from request
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String json = "";
        if(br != null){
            json = br.readLine();
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
        PrintWriter writer = response.getWriter();
        //writer.write(tracksData.writeCollectionsAsJSONString());
        writer.write(tracksString);

    }
}