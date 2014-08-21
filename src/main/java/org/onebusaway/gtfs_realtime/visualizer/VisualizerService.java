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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
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
import javax.net.ssl.*;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
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

  public void setVehiclePositionsUri(URI uri) {
    _vehiclePositionsUri = uri;
  }

  @PostConstruct
  public void start() throws Exception {
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

  @PreDestroy
  public void stop() throws Exception {
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

    URL url = _vehiclePositionsUri.toURL();
    try {
	    // trust the translink site even though it has invalid certificate
	    disableCertificateValidation(); 
		HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
		int iSize = con.getContentLength();
		_log.info("data=["+iSize+" bytes]");
   
        //print_https_cert(con);
        //print_content(con);

		FeedMessage feed = FeedMessage.parseFrom(con.getInputStream());

		boolean hadUpdate = processDataset(feed);

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

    _executor.schedule(_refreshTask, _refreshInterval, TimeUnit.SECONDS);
  }

  private boolean processDataset(FeedMessage feed) {

    List<Vehicle> vehicles = new ArrayList<Vehicle>();
    boolean update = false;
    int vehiclePosnCount = 0;
    int tripUpdateCount = 0;
    int vehicleCount = 0;

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
      v.setLastUpdate(System.currentTimeMillis());
      v.setIsStationary(false);

      Vehicle existing = _vehiclesById.get(vehicleId);
      if (existing == null || existing.getLat() != v.getLat()
          || existing.getLon() != v.getLon()) {
        _vehiclesById.put(vehicleId, v);
      } else {
    	v.setIsStationary(true); // still update because we want to delete track tails  
        //v.setLastUpdate(existing.getLastUpdate());
      }
      update = true;

      vehicles.add(v);
    }

    if (update) {
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
      _refreshInterval = Math.max(10, refreshInterval);
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
