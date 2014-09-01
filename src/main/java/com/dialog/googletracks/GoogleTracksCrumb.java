package com.dialog.googletracks;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GoogleTracksCrumb {
    private JSONObject location; // contains lat and lng
    private String timeStamp;     // POSIX time - seconds from midnight 01-jan-1970. fractions allowed
    private String confidenceRadius;
    private String heading;
    private JSONObject userData; // ad hoc data
    
    /* - example crumb:
    {
        "confidenceRadius": 3.14,
        "location": {
            "lat": -33.866495,
            "lng": 151.195446
        },
        "timestamp": 1341375062.19,
        "heading":   240,
        "userData": {
            "driver_name": "Joe",
            "measured_vehicle_speed": "110.2"
        }
    }
    */
    
    public void LoadFromJSONObject(JSONObject json) {
        setLocation((JSONObject) json.get(GoogleTracksConstants.LOCATION_LIT));
        setTimeStamp(((Double) json.get(GoogleTracksConstants.TIME_STAMP)).toString());
        if (json.get(GoogleTracksConstants.CONFIDENCE_RADIUS) != null) {
            setConfidenceRadius(((Double) json.get(GoogleTracksConstants.CONFIDENCE_RADIUS)).toString());
        }
        if (json.get(GoogleTracksConstants.HEADING_LIT) != null) {
            setHeading(((Double) json.get(GoogleTracksConstants.HEADING_LIT)).toString());
        }
        setUserData((JSONObject) json.get(GoogleTracksConstants.USER_DATA));
    }
    
    public void LoadFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            LoadFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
    }
    
    public String storeToTracksString() {
        return storeToJSONObject().toJSONString();
    }    
        
        
    public JSONObject storeToJSONObject() {
        JSONObject jObj = new JSONObject();
        jObj.put(GoogleTracksConstants.LOCATION_LIT, this.location);
        jObj.put(GoogleTracksConstants.TIME_STAMP, Double.parseDouble(this.timeStamp));
        jObj.put(GoogleTracksConstants.CONFIDENCE_RADIUS, this.getConfidenceRadius());
        if (this.heading != null && this.heading.trim().length()>0) {
            jObj.put(GoogleTracksConstants.HEADING_LIT, Double.parseDouble(this.heading));
        }
        jObj.put(GoogleTracksConstants.USER_DATA, this.userData);
        return jObj;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((confidenceRadius == null) ? 0 : confidenceRadius.hashCode());
        result = prime * result + ((heading == null) ? 0 : heading.hashCode());
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        result = prime * result
                + ((timeStamp == null) ? 0 : timeStamp.hashCode());
        result = prime * result
                + ((userData == null) ? 0 : userData.hashCode());
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GoogleTracksCrumb other = (GoogleTracksCrumb) obj;
        if (confidenceRadius == null) {
            if (other.confidenceRadius != null)
                return false;
        } else if (!confidenceRadius.equals(other.confidenceRadius))
            return false;
        if (heading == null) {
            if (other.heading != null)
                return false;
        } else if (!heading.equals(other.heading))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (timeStamp == null) {
            if (other.timeStamp != null)
                return false;
        } else if (!timeStamp.equals(other.timeStamp))
            return false;
        if (userData == null) {
            if (other.userData != null)
                return false;
        } else if (!userData.equals(other.userData))
            return false;
        return true;
    }
    /**
     * @return the location
     */
    public JSONObject getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(JSONObject location) {
        if (location == null) {
            this.location = new JSONObject();
        } else {
            this.location = location;
        }
    }
    /**
     * @return the timeStamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }
    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(String timeStamp) {
        if (timeStamp == null) {
            this.timeStamp = "";
        } else {
            this.timeStamp = timeStamp;
        }
    }
    /**
     * @return the confidenceRadius
     */
    public String getConfidenceRadius() {
        return confidenceRadius;
    }
    /**
     * @param confidenceRadius the confidenceRadius to set
     */
    public void setConfidenceRadius(String confidenceRadius) {
        if (confidenceRadius == null) {
            this.confidenceRadius = "";
        } else {
            this.confidenceRadius = confidenceRadius;
        }
    }
    /**
     * @return the heading
     */
    public String getHeading() {
        return heading;
    }
    /**
     * @param heading the heading to set
     */
    public void setHeading(String heading) {
        if (heading == null) {
            this.heading = "";
        } else {
            this.heading = heading;
        }
    }
    /**
     * @return the userData
     */
    public JSONObject getUserData() {
        return userData;
    }
    /**
     * @param userData the userData to set
     */
    public void setUserData(JSONObject userData) {
        if (userData == null) {
            this.userData = new JSONObject();
        } else {
            this.userData = userData;
        }
    }
    
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "GoogleTracksCrumb [location=" + location + ", timeStamp="
                + timeStamp + ", confidenceRadius=" + confidenceRadius
                + ", heading=" + heading + ", userData=" + userData + "]";
    }

    public GoogleTracksCrumb() {
        super();
        location = new JSONObject(); 
        timeStamp = "";     
        confidenceRadius = "";
        heading = "";
        userData = new JSONObject(); // ad hoc data
    }
    
    public GoogleTracksCrumb(JSONObject json) {
        super();
        location = new JSONObject(); 
        timeStamp = "";     
        confidenceRadius = "";
        heading = "";
        userData = new JSONObject(); // ad hoc data
        LoadFromJSONObject(json);
    }
}
