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

public class Vehicle {
  private String vId;
  
  private String routeId;
  
  private String tripId;
  
  private String entityId;
  
  private boolean isStationary;

  private double lat;

  private double lon;
  
  private long lastUpdate;  
  
  private String tracksEntityId;
  
  public String generateTracksName() {
      return getEntityId()+"[" + getId() + "]";
  }

  /**
 * @return the tracksEntityId
 */
public String getTracksEntityId() {
    return tracksEntityId;
}

/**
 * @param tracksEntityId the tracksEntityId to set
 */
public void setTracksEntityId(String tracksEntityId) {
    this.tracksEntityId = tracksEntityId;
}

/**
 * @param isStationary the isStationary to set
 */
public void setStationary(boolean isStationary) {
    this.isStationary = isStationary;
}

public String getId() {
    return vId;
  }

  public void setId(String id) {
    this.vId = id;
  }

  public String getRouteId() {
	return routeId;
}

public void setRouteId(String routeId) {
	this.routeId = routeId;
}

public String getTripId() {
	return tripId;
}

public void setTripId(String tripId) {
	this.tripId = tripId;
}

public String getEntityId() {
	return entityId;
}

public void setEntityId(String entityId) {
	this.entityId = entityId;
}

public boolean getIsStationary() {
	return isStationary;
}

public void setIsStationary(boolean isStationary) {
	this.isStationary = isStationary;
}

public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
}
