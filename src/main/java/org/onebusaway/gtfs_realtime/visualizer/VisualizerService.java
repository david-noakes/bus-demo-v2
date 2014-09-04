/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_realtime.visualizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dialog.common.TimeUtility;
import com.dialog.googletracks.GoogleTracksCollection;
import com.dialog.googletracks.GoogleTracksCollectionList;
import com.dialog.googletracks.GoogleTracksConstants;
import com.dialog.googletracks.GoogleTracksCrumb;
import com.dialog.googletracks.GoogleTracksEntity;
import com.dialog.googletracks.TracksServiceRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

@Singleton
public class VisualizerService {

  private static final Logger _log = LoggerFactory.getLogger(VisualizerService.class);
  
  private URI _vehiclePositionsUri;

  private ScheduledExecutorService _executor;

  private WebSocketClientFactory _webSocketFactory;

  private WebSocketClient _webSocketClient;

  private IncrementalWebSocket _incrementalWebSocket;

  private Future<Connection> _webSocketConnection;

  private Map<String, String> _vehicleIdsByEntityIds = new HashMap<String, String>();

  private Map<String, Vehicle> _vehiclesById = new ConcurrentHashMap<String, Vehicle>();

  private List<VehicleListener> _listeners = new CopyOnWriteArrayList<VehicleListener>();

  private final RefreshTask _refreshTask = new RefreshTask();

  private int _refreshInterval = 20;

  private boolean _dynamicRefreshInterval = true;

  private long _mostRecentRefresh = -1;
  
  private boolean _tracksUpdate = false;
  
  private boolean _tracksSimulate = false;
 
  private GoogleTracksCollectionList gtcList = null;
  
  private String collectionPrefix ;
  
  private String tracksRoute = "60-470";
  
  private int tracksBurst = 17; // how many crumbs updates to run in a burst
                                // Empirical results equate to approx 9 mts for 23
                                //                                    7 mts for 17
                                //                                    5 mts for 11

  public void setVehiclePositionsUri(URI uri) {
    _vehiclePositionsUri = uri;
  }

  public boolean getTracksUpdate() {
	return _tracksUpdate;
}

public void setTracksUpdate(boolean _tracksUpdate) {
	this._tracksUpdate = _tracksUpdate;
}

/**
 * @return the _tracksSimulate
 */
public boolean getTracksSimulate() {
    return _tracksSimulate;
}

/**
 * @param _tracksSimulate the _tracksSimulate to set
 */
public void setTracksSimulate(boolean _tracksSimulate) {
    this._tracksSimulate = _tracksSimulate;
}

public GoogleTracksCollectionList getGtcList() {
    return gtcList;
}

public void setGtcList(GoogleTracksCollectionList gtcList) {
    this.gtcList = gtcList;
}

@PostConstruct
  public void start() throws Exception {
    Calendar currentDate = Calendar.getInstance();
    SimpleDateFormat sdFormat = new SimpleDateFormat(
            TimeUtility.DATE_FORMAT.toString());
    collectionPrefix = sdFormat.format(currentDate.getTime());
    
    String collectionName = collectionPrefix + " Route: " + tracksRoute;
    
    if (_tracksUpdate) {
    	String tracksString = 
    	  TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_LIST_COLLECTIONS, " ");
    	if (tracksString.trim().length() == 0) {
    	    initialiseCollectionlist(collectionName);
    	} else {
    	    gtcList = new GoogleTracksCollectionList(tracksString);
    	    gtcList.pruneCollection(collectionPrefix);
    	    if (gtcList.size() == 0) {
                initialiseCollectionlist(collectionName);
    	    }
    	}
    	
    	tracksString = 
      	  TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_LIST_ENTITIES, " ");
    	gtcList.loadAllEntitiesFromTracksString(tracksString);
    }
    
    String scheme = _vehiclePositionsUri.getScheme();
    _log.info("Scheme = " + scheme);
    if (scheme.equals("ws") || scheme.equals("wss")) {
      _webSocketFactory = new WebSocketClientFactory();
      _webSocketFactory.start();
      _webSocketClient = _webSocketFactory.newWebSocketClient();
      _webSocketClient.setMaxBinaryMessageSize(16384000); 
      _incrementalWebSocket = new IncrementalWebSocket();
      _webSocketConnection = _webSocketClient.open(_vehiclePositionsUri,
          _incrementalWebSocket);
    } else {
      _executor = Executors.newSingleThreadScheduledExecutor();
      _executor.schedule(_refreshTask, 0, TimeUnit.SECONDS);
    }
    
  }

  private void initialiseCollectionlist(String collectionName) throws IOException, GeneralSecurityException {
      String tracksString;
      gtcList = new GoogleTracksCollectionList();
      GoogleTracksCollection gtColl = new GoogleTracksCollection();
      gtColl.setName(collectionName);
      gtcList.add(gtColl);
      tracksString = TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_CREATE_COLLECTIONS, gtcList.storeToTracksString());
      gtcList.loadCollectionIdsFromTracksString(tracksString);
  }
  
  @PreDestroy
  public void stop() throws Exception {
      try {
          putTracksDataToGoogle(true); // force any updates out
      } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
      } catch (GeneralSecurityException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
      } 
      if (_webSocketConnection != null) {
          _webSocketConnection.cancel(false);
      }
      if (_webSocketClient != null) {
          _webSocketClient = null;
      }
      if (_webSocketFactory != null) {
          _webSocketFactory.stop();
          _webSocketFactory = null;
      }
      if (_executor != null) {
          _executor.shutdownNow();
      }
  }

  public List<Vehicle> getAllVehicles() {
    return new ArrayList<Vehicle>(_vehiclesById.values());
  }

  public void addListener(VehicleListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(VehicleListener listener) {
    _listeners.remove(listener);
  }

  private void print_content(HttpsURLConnection con){
	if(con!=null){
 
	try {
 
	   System.out.println("****** Content of the URL ********");			
	   BufferedReader br = 
		new BufferedReader(
			new InputStreamReader(con.getInputStream()));
 
	   String input;
 
	   while ((input = br.readLine()) != null){
	      System.out.println(input);
	   }
	   br.close();
 
	} catch (IOException e) {
	   e.printStackTrace();
	}
 
       }
 
   }

   private void print_https_cert(HttpsURLConnection con){
 
    if(con!=null){
 
      try {
 
	System.out.println("Response Code : " + con.getResponseCode());
	System.out.println("Cipher Suite : " + con.getCipherSuite());
	System.out.println("\n");
 
	Certificate[] certs = con.getServerCertificates();
	for(Certificate cert : certs){
	   System.out.println("Cert Type : " + cert.getType());
	   System.out.println("Cert Hash Code : " + cert.hashCode());
	   System.out.println("Cert Public Key Algorithm : " 
                                    + cert.getPublicKey().getAlgorithm());
	   System.out.println("Cert Public Key Format : " 
                                    + cert.getPublicKey().getFormat());
	   System.out.println("\n");
	}
 
	} catch (SSLPeerUnverifiedException e) {
		e.printStackTrace();
	} catch (IOException e){
		e.printStackTrace();
	}
 
     }
 
   }
    /*
    * Translink feed returns invalid certificate error
	* javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed' 
	*/
	public static void disableCertificateValidation() {
	  // Create a trust manager that does not validate certificate chains
	  TrustManager[] trustAllCerts = new TrustManager[] { 
		new X509TrustManager() {
		  public X509Certificate[] getAcceptedIssuers() { 
			return new X509Certificate[0]; 
		  }
		  public void checkClientTrusted(X509Certificate[] certs, String authType) {}
		  public void checkServerTrusted(X509Certificate[] certs, String authType) {}
	  }};

	  // Ignore differences between given hostname and certificate hostname
	  HostnameVerifier hv = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) { return true; }
	  };

	  // Install the all-trusting trust manager
	  try {
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	  } catch (Exception e) {}
	}

	private void refresh() throws IOException {

    _log.info("refreshing vehicle positions");
    String scheme = _vehiclePositionsUri.getScheme();
    URL url = _vehiclePositionsUri.toURL();
    boolean hadUpdate = false;
    
    if (scheme.contentEquals("https")) {
    try {
	    // trust the translink site even though it has invalid certificate
	    disableCertificateValidation(); 
		HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
		int iSize = con.getContentLength();
		_log.info("data=["+iSize+" bytes]");
   
        //print_https_cert(con);
        //print_content(con);

		FeedMessage feed = FeedMessage.parseFrom(con.getInputStream());

		hadUpdate = processDataset(feed);

		if (hadUpdate) {
			if (_dynamicRefreshInterval) {
				updateRefreshInterval();
			}
		}
    } catch (MalformedURLException e) {
	    e.printStackTrace();
    } catch (IOException e) {
       e.printStackTrace();
    }
    } else { // assume http NOT https
        FeedMessage feed = FeedMessage.parseFrom(url.openStream());

        hadUpdate = processDataset(feed);

        if (hadUpdate) {
          if (_dynamicRefreshInterval) {
            updateRefreshInterval();
          }
        }
    }

    _executor.schedule(_refreshTask, _refreshInterval, TimeUnit.SECONDS);
  }

  private boolean processDataset(FeedMessage feed) {

    List<Vehicle> vehicles = new ArrayList<Vehicle>();
    boolean update = false;
    boolean addVehicleIntoTracks = false;
    int vehiclePosnCount = 0;
    int tripUpdateCount = 0;
    int vehicleCount = 0;
    long updateTimestamp = System.currentTimeMillis() / 1000;
    java.util.Date time=new java.util.Date((long)updateTimestamp*1000);
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    System.out.println("timestamp = " + df.format(time));
    
	_log.info("Feed contains : " + feed.getEntityList().size() + " entities");
    for (FeedEntity entity : feed.getEntityList()) {
      if (entity.hasIsDeleted() && entity.getIsDeleted()) {
        String vehicleId = _vehicleIdsByEntityIds.get(entity.getId());
        if (vehicleId == null) {
          _log.warn("unknown entity id in deletion request: " + entity.getId());
          continue;
        }
        _vehiclesById.remove(vehicleId);
        continue;
      }
      vehicleCount++;
      if (!entity.hasVehicle()) {
    	//_log.info("no vehicle position for " + entity.getId());
    	if (entity.hasTripUpdate()) {
    		//_log.info("      Trip Update exists");
    		tripUpdateCount++;
    	}
        continue;
      }
      if (entity.hasTripUpdate()) {
		  tripUpdateCount++;
	  }
      VehiclePosition vehicle = entity.getVehicle();
      String vehicleId = getVehicleId(vehicle);
      if (vehicleId == null) {
        continue;
      }
      _vehicleIdsByEntityIds.put(entity.getId(), vehicleId);
      if (!vehicle.hasPosition()) {
        continue;
      }
      vehiclePosnCount++;
      TripDescriptor tripDesc = vehicle.getTrip();
      Position position = vehicle.getPosition();
      Vehicle v = new Vehicle();
      v.setId(vehicleId);
      v.setEntityId(entity.getId());
      if (vehicle.hasTrip() && tripDesc != null) {
    	  v.setRouteId(tripDesc.getRouteId());
    	  v.setTripId(tripDesc.getTripId());
      } else {
    	  v.setRouteId(" ");
    	  v.setTripId(" ");
      }
      v.setLat(position.getLatitude());
      v.setLon(position.getLongitude());
      v.setLastUpdate(updateTimestamp); // ensure all updates have the same timestamp
      v.setIsStationary(false);

      Vehicle existing = _vehiclesById.get(vehicleId);
      if (existing == null || existing.getLat() != v.getLat()
          || existing.getLon() != v.getLon()) {
        _vehiclesById.put(vehicleId, v);
        if (_tracksUpdate) {
            if (updateTracksInfo(v, existing)) {
                addVehicleIntoTracks = true;
            }
        }
        update = true;
      } else {
        // stationary is not required for Google tracks  
    	//v.setIsStationary(true); // still update because we want to delete track tails  
        v.setLastUpdate(existing.getLastUpdate());
      }

      vehicles.add(v);
    }

    if (update) {
        if (_tracksUpdate) {
            
           try {
               putTracksDataToGoogle(false); // batch them
           } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
           } catch (GeneralSecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
           } 
        }
      _log.info("vehicles updated: " + vehicles.size());
    } else {
    	_log.info("No Update - vehicles found: " + vehicleCount + 
    			  ", vehicle Posns found: " + vehiclePosnCount + 
    			  ", trip updates found: "+ tripUpdateCount);
    }

    for (VehicleListener listener : _listeners) {
      listener.handleVehicles(vehicles);
    }

    return update;
  }

  private boolean updateTracksInfo(Vehicle newData, Vehicle existingData) {
      // only update the specified route
      boolean created = false;
      GoogleTracksEntity gtEnt = null;
      if (newData.getRouteId().startsWith(tracksRoute)) {
          if (existingData != null) {
              newData.setTracksEntityId(existingData.getTracksEntityId());
              if (existingData.getTracksEntityId() == null) { 
                   gtEnt=gtcList.findEntityByName(existingData.generateTracksName());
              }
              if (gtEnt == null) {
                  addVehicletoTracks(newData);
                  created = true;
              } else {
                recordNewCrumb(gtEnt, newData);  
              }
          } else { //existingData is null, so doesn't exist
              created = true;
              addVehicletoTracks(newData);
          }
      }
      return created;
  }
  
  private void addVehicletoTracks(Vehicle newData) {
      /* create entity
       * add to collection
       * add crumb
       */
      GoogleTracksEntity gtEnt = new GoogleTracksEntity(newData.generateTracksName(), 
              GoogleTracksConstants.ENTITY_TYPE_AUTOMOBILE);
      if (!gtcList.getAllEntities().contains(gtEnt)) {
          gtcList.getAllEntities().add(gtEnt);
          gtcList.get(0).getEntities().add(gtEnt);
      } else {
          gtEnt=gtcList.findEntityByNameAndType(gtEnt);
      }
      recordNewCrumb(gtEnt, newData);
  }
  
  private void recordNewCrumb(GoogleTracksEntity gtEnt, Vehicle newData) {
      /* add the crumb only
       * NOTE - crumb userData is limited to 64 bytes
       *      - entity and ID have been removed
       *      - data names have been shortened 
       */
      //gtEnt.get_Crumbs().clear();
      GoogleTracksCrumb gtCrumb = new GoogleTracksCrumb();
      gtCrumb.setConfidenceRadius("3.4");
      JSONObject location = new JSONObject();
      location.put(GoogleTracksConstants.LATITUDE_LIT, newData.getLat());
      location.put(GoogleTracksConstants.LONGITUDE_LIT, newData.getLon());
      gtCrumb.setLocation(location);
      gtCrumb.setTimeStamp(Long.toString(newData.getLastUpdate()));
      JSONObject userData = new JSONObject();
      //userData.put(GoogleTracksConstants.USER_ENTITY,newData.getEntityId());
      //userData.put(GoogleTracksConstants.USER_V_ID,newData.getId());
      userData.put(GoogleTracksConstants.USER_ROUTE,newData.getRouteId());
      userData.put(GoogleTracksConstants.USER_TRIP,newData.getTripId());
                    
      gtCrumb.setUserData(userData);
      gtEnt.getCrumbs().add(gtCrumb);
      // check whether we need to add this entity to the collection
      GoogleTracksCollection gtColl = gtcList.get(0);
      if (!gtColl.getEntities().contains(gtEnt)) {
          gtColl.getEntities().add(gtEnt);
      }
  }
  
  private void putTracksDataToGoogle(boolean forceUpdate) throws IOException, GeneralSecurityException {
      String requestBody; 
      String tracksString;
      int increm = _refreshInterval / 10;
      if (increm == 0) {
          increm = 1;
      }
      gtcList.setCrumbsLength(gtcList.getCrumbsLength()+ increm);
      System.out.println(" crumbsLength = "+ gtcList.getCrumbsLength());
      // batch up the calls to google tracks to minimise our footprint on the daily quota
      if (forceUpdate || gtcList.getCrumbsLength() > tracksBurst) {
          gtcList.setCrumbsLength(0);
          if (gtcList.get(0).needToAddEntities()) {
              //1. add newly created entities (id is blank)
              //because of limits on entities, we may omit some, leading to an empty entity list
              requestBody = gtcList.storeNewEntitiesToTracksString();
              if (!requestBody.startsWith("{\"entities\":[]}")) { 
                  
                  tracksString = TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_CREATE_ENTITIES, requestBody);
                  gtcList.loadNewEntityIdsFromTracksString(tracksString);
                  // reuse the response to add the entities to the collection
                  JSONParser jsonParser=new JSONParser();
                  try {
                      JSONObject json = (JSONObject) jsonParser.parse( tracksString );
                      json.put(GoogleTracksConstants.COLLECTION_ID, gtcList.get(0).getID());
                      requestBody = json.toJSONString();
                  } catch (ParseException e) {
                      System.out.println("position: " + e.getPosition());
                      System.out.println(e);
                  }
                  tracksString = TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_ADD_ENTITIES_TO_COLLECTION, requestBody);
              }
              //2. add in existing entities not yet added to collection
              // this may have changed if we added the new ones
              if (gtcList.get(0).needToAddEntities()) {
                  requestBody = gtcList.get(0).addEntitiesToTracksString();
                  tracksString = TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_ADD_ENTITIES_TO_COLLECTION, requestBody);
                  gtcList.get(0).confirmAddEntitiesToCollection(requestBody);
              }
          }
          //This requires a loop - can add crumbs to 1 entity at a time
          for (int i=0; i<gtcList.getAllEntities().size(); i++) {
              GoogleTracksEntity gtEnt = gtcList.getAllEntities().get(i);
              if (gtEnt.getCrumbs().size()>0 && gtEnt.getID().trim().length() > 0) { // avoid google limit of 20 entities
                  gtcList.get(0).addEntityToCollection(gtEnt); // checks if already there
                  requestBody = gtEnt.storeCrumbsToTracksString();
                  tracksString = TracksServiceRequest.serviceRequest(GoogleTracksConstants.METHOD_CRUMBS_RECORD, requestBody);
              }
          }
      }
  }
  
  /**
   * @param vehicle
   * @return
   */
  private String getVehicleId(VehiclePosition vehicle) {
    if (!vehicle.hasVehicle()) {
      return null;
    }
    VehicleDescriptor desc = vehicle.getVehicle();
    if (!desc.hasId()) {
      return null;
    }
    return desc.getId();
  }

  private void updateRefreshInterval() {
    long t = System.currentTimeMillis();
    if (_mostRecentRefresh != -1) {
      int refreshInterval = (int) ((t - _mostRecentRefresh) / (2 * 1000));
      refreshInterval = Math.max(10, refreshInterval);
      _refreshInterval = Math.min(60, refreshInterval);
      _log.info("refresh interval: " + _refreshInterval);
    }
    _mostRecentRefresh = t;
  }

  private class RefreshTask implements Runnable {
    @Override
    public void run() {
      try {
        refresh();
      } catch (Exception ex) {
        _log.error("error refreshing GTFS-realtime data", ex);
      }
    }
  }

  private class IncrementalWebSocket implements OnBinaryMessage {

    @Override
    public void onOpen(Connection connection) {

    }

    @Override
    public void onMessage(byte[] buf, int offset, int length) {
      if (offset != 0 || buf.length != length) {
        byte trimmed[] = new byte[length];
        System.arraycopy(buf, offset, trimmed, 0, length);
        buf = trimmed;
      }
      FeedMessage message = parseMessage(buf);
      FeedHeader header = message.getHeader();
      switch (header.getIncrementality()) {
        case FULL_DATASET:
          processDataset(message);
          break;
        case DIFFERENTIAL:
          processDataset(message);
          break;
        default:
          _log.warn("unknown incrementality: " + header.getIncrementality());
      }
    }

    @Override
    public void onClose(int closeCode, String message) {

    }

    private FeedMessage parseMessage(byte[] buf) {
      try {
        return FeedMessage.parseFrom(buf);
      } catch (InvalidProtocolBufferException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }
}
