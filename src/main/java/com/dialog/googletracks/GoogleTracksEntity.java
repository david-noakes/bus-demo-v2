package com.dialog.googletracks;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;

public class GoogleTracksEntity {

	private String _ID;
	private String _Name;
	private String _Type;
	private List<GoogleTracksCrumb> _Crumbs;
	
	
	
    public GoogleTracksEntity() {
        super();
        this._ID = "";
        this._Name = "";
        this._Type = "";
        this._Crumbs = new ArrayList<GoogleTracksCrumb>();
    }
    public GoogleTracksEntity(String Name, String Type) {
        super();
        this._ID = "";
        setName(Name);
        setType(Type);
        this._Crumbs = new ArrayList<GoogleTracksCrumb>();
    }
    public GoogleTracksEntity(String ID, String Name, String Type) {
        super();
        setID(ID);
        setName(Name);
        setType(Type);
        this._Crumbs = new ArrayList<GoogleTracksCrumb>();
    }
    
    public GoogleTracksEntity(JSONObject json) {
        super();
        this._ID = "";
        this._Name = "";
        this._Type = "";
        this._Crumbs = new ArrayList<GoogleTracksCrumb>();
        LoadFromJSONObject(json);
    }
    
    public String getID() {
		return this._ID;
	}
	public void setID(String ID) {
	    if (ID != null) {
	        this._ID = ID;    
	    }
	}
	public String getName() {
        return this._Name;
    }
    public void setName(String Name) {
        if (Name != null) {
            this._Name = Name;
        }
    }
    public String getType() {
        return _Type;
    }
    public void setType(String Type) {
        if (Type != null) {
            this._Type = Type;
        }
    }
    
    
    /**
     * @return the _Crumbs
     */
    public List<GoogleTracksCrumb> get_Crumbs() {
        return _Crumbs;
    }
    /**
     * @param _Crumbs the _Crumbs to set
     */
    public void set_Crumbs(List<GoogleTracksCrumb> _Crumbs) {
        if (_Crumbs == null) {
            this._Crumbs = new ArrayList<GoogleTracksCrumb>();
        } else {
            this._Crumbs = _Crumbs;
        }    
    }
    
    
    /* The entities will be listed as: 
     * {
     *       "entities": [
     *           {
     *               "id": "1ff3a55f94e954ee",
     *               "name": "Chevrolet Volt 001",
     *               "type": "AUTOMOBILE"
     *           },
     *           {
     *               "id": "ec6053f142ade5c9",
     *               "name": "Ford Fiesta 001",
     *               "type": "AUTOMOBILE"
     *           }
     *       ]
     *   }
     * 
     * We load the single entity from this response
     */
    public void LoadFromJSONObject(JSONObject json) {
        setID((String) json.get(GoogleTracksConstants.ID_LIT));
        setName((String) json.get(GoogleTracksConstants.NAME_LIT));
        setType((String) json.get(GoogleTracksConstants.TYPE_LIT));
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
    
    /* Create entity. They are created as a group:
     * {
     *       "entities": [
     *           {
     *               "name": "Ford Fiesta 001",
     *               "type": "AUTOMOBILE"
     *           },
     *           {
     *               "name": "Chevrolet Volt 001",
     *               "type": "AUTOMOBILE"
     *           },
     *           {
     *               "name": "Chevrolet Volt 002",
     *           }
     *       ]
     *   }
     * 
     * 
     *
     */
    
	public String StoreToTracksString() {
		return StoreToJSONObject().toJSONString();
	}
	
	public JSONObject StoreToJSONObject() {
        JSONObject jObj = new JSONObject();
        jObj.put(GoogleTracksConstants.NAME_LIT, _Name);
        if (_ID.trim().length()>0) {
            jObj.put(GoogleTracksConstants.TYPE_LIT, _Type);
        }
        return jObj;
	    
	}
	
	public String storeCrumbsToTracksString() {
	    return storeCrumbsToJSONObject().toJSONString();
	}
	
	public JSONObject storeCrumbsToJSONObject() {
        JSONObject jObj = new JSONObject();
        jObj.put(GoogleTracksConstants.ENTITY_ID, getID());
        JSONArray jArray = new JSONArray();
        
        for (int i=0;i<_Crumbs.size();i++) {
            jArray.add(_Crumbs.get(i).storeToJSONObject());
        }
   
        jObj.put(GoogleTracksConstants.CRUMBS_LIT, jArray);

        return jObj;
	}
	
	/* example:
	 *  {
     *       "entityId": "1ff3a55f94e954ee",
     *       "timestamp": 1341500000,
     *       "countBefore": 25
     *   }
	 */
	
	/**
	 * 
	 * @param timeStamp if 0 - use current time
	 * @param countAfter positive for count after, negative for countbefore
	 * @return JSONString
	 */
	public String GenerateCrumbsHistoryRequest(int timeStamp, int countAfter) {
        JSONObject jObj = new JSONObject();
        jObj.put(GoogleTracksConstants.ENTITY_ID, _ID);
        if (timeStamp == 0) {
            jObj.put(GoogleTracksConstants.TIME_STAMP, (int) (System.currentTimeMillis() / 1000L));
            jObj.put(GoogleTracksConstants.COUNT_BEFORE, java.lang.Math.abs(countAfter));
        } else {
            jObj.put(GoogleTracksConstants.TIME_STAMP, timeStamp);
            if (countAfter < 0) {
                jObj.put(GoogleTracksConstants.COUNT_BEFORE, java.lang.Math.abs(countAfter));
            } else {
                jObj.put(GoogleTracksConstants.COUNT_AFTER, countAfter);
            }
        }
            
        return jObj.toJSONString();
	}
	
	public void LoadCrumbsFromJSONObject(JSONObject json) {
	    JSONArray jArray = (JSONArray) json.get(GoogleTracksConstants.CRUMBS_LIT);
	    if (jArray != null && jArray.size()>0) {
	        for (int i=0;i<jArray.size();i++) {
	            GoogleTracksCrumb gtCrumb = new GoogleTracksCrumb((JSONObject) jArray.get(i));
	            _Crumbs.add(gtCrumb);
	        }
	    }
	    
	}
	public void LoadCrumbsFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            LoadCrumbsFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
	}
	
	
	
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_ID == null) ? 0 : _ID.hashCode());
        result = prime * result + ((_Name == null) ? 0 : _Name.hashCode());
        result = prime * result + ((_Type == null) ? 0 : _Type.hashCode());
        return result;
    }
    /* 
     * equals tests equality on _name and _type only
     * to allow matching of blank ID on new entity
     * 
     * use identical if you want to include ID in equality
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GoogleTracksEntity other = (GoogleTracksEntity) obj;
        // do not compare ID to allow new entities matching existing ones
        if (_Name == null) {
            if (other._Name != null)
                return false;
        } else if (!_Name.equals(other._Name))
            return false;
        if (_Type == null) {
            if (other._Type != null)
                return false;
        } else if (!_Type.equals(other._Type))
            return false;
        return true;
    }

    /* tests equality on the _ID, _name and _type
     * 
     */
	public boolean identical(Object obj) {
	    if (equals(obj)) {
	        GoogleTracksEntity other = (GoogleTracksEntity) obj;
	        if (this._ID == null && other._ID == null) {
	            return true;
	        }
            if (this._ID != null && other._ID != null) {
                return this._ID.equals(other._ID);
            }
	    }
	    return false;
	}
	
}
