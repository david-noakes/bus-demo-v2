package com.dialog.googletracks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GoogleTracksCollection {

	private String _ID;
	private String _Name;
	private List<GoogleTracksEntity>   _Entities;
	private JSONObject belongsToCollection; // indicates true/false for entity id has been added to collection
	
	public String getID() {
		return _ID;
	}
	public void setID(String ID) {
		if (ID != null) {
			this._ID = ID;
		}
	}
	public String getName() {
		return _Name;
	}
	public void setName(String Name) {
		if (Name != null) {
			this._Name = Name;
		}
	}
	public List<GoogleTracksEntity> getEntities() {
		return _Entities;
	}
	public void setEntities(List<GoogleTracksEntity> Entities) {
		if (Entities != null) {
			this._Entities = Entities;
		}
	}
	
	
	/**
     * @return the belongsToCollection
     */
    public JSONObject getBelongsToCollection() {
        return belongsToCollection;
    }
    /**
     * @param belongsToCollection the belongsToCollection to set
     */
    public void setBelongsToCollection(JSONObject belongsToCollection) {
        this.belongsToCollection = belongsToCollection;
    }
    
    public boolean doesEntityBelongToCollection(String entId) {
        if (entId == null || entId.trim().length()==0) {
            return false;
        }
        if (!belongsToCollection.containsKey(entId)) {
            return false;
        }
        return ((Boolean) belongsToCollection.get(entId)).booleanValue();
    }
    
    public boolean needToAddEntities() {
        for (int i = 0; i< _Entities.size(); i++) {
            if (!doesEntityBelongToCollection(_Entities.get(i).getID())) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * Assumption. This entity has just been created  
     * We add it into the collection object if it hasn't been added already.
     * 
     * We do not set the belongsToCollection flag here,
     * is is done in confirmAddEntitiesToCollection
     *
     */
    
    
    public void addEntityToCollection(GoogleTracksEntity gtEnt) {
        if (!_Entities.contains(gtEnt)) {
            _Entities.add(gtEnt);
        }
        
    }
    
    //** constructors **//
    
    public GoogleTracksCollection() {
		super();
		_ID = "";
		_Name = "";
		_Entities = new ArrayList<GoogleTracksEntity>();
		belongsToCollection = new JSONObject();
	}

	public GoogleTracksCollection(JSONObject jsonColl) {
		super();
        _ID = "";
        _Name = "";
        _Entities = new ArrayList<GoogleTracksEntity>();
        belongsToCollection = new JSONObject();
		loadFromJSONObject(jsonColl);
	}
	public GoogleTracksCollection(String ID, String Name, List Entities) {
		super();
		setID(ID);
		setName(Name);
		setEntities(Entities);
        belongsToCollection = new JSONObject();
	}
	public GoogleTracksCollection(String tracksString) {
		super();
        _ID = "";
        _Name = "";
        _Entities = new ArrayList<GoogleTracksEntity>();
        belongsToCollection = new JSONObject();
		loadFromTracksString(tracksString);
	}

	/**
	 *  a json collection will look something like this
	 *  {
     *       "entityIds": [
     *           "0ff3a55f94e954ee",
     *           "fc6053f142ade5c9"
     *       ],
     *       "id": "0179f339ec9232ef",
     *       "name": "SEA rental fleet - midsize"
     *   }
     *
	 * @param jsonColl
	 */
	public void loadFromJSONObject(JSONObject jsonColl) {
        JSONArray  entities = (JSONArray) jsonColl.get(GoogleTracksConstants.ENTITY_IDS);
		List<GoogleTracksEntity> ents = new ArrayList();
        if (entities != null) {
        	for (int j=0; j< entities.size(); j++) {
        		String entId = (String) entities.get(j);
        		GoogleTracksEntity gtEntity = new GoogleTracksEntity();
        		gtEntity.setID(entId);
        		ents.add(gtEntity);
        		belongsToCollection.put(entId, true);
        	}
        }
        setName((String) jsonColl.get(GoogleTracksConstants.NAME_LIT));
        setID((String) jsonColl.get(GoogleTracksConstants.ID_LIT));
        setEntities(ents);

	}

	public void loadFromTracksString(String tracksString) {
	    JSONParser jsonParser=new JSONParser();
		try {
			JSONObject json = (JSONObject) jsonParser.parse( tracksString );
			loadFromJSONObject(json);
		} catch (ParseException e) {
		    System.out.println("position: " + e.getPosition());
		    System.out.println(e);
		}

	}
	
	/*
	 * write the collection data out in a format for GoogleTracks create
	 * We do not include the entities. These must be created separately
	 * and added to the collection
	 */
	public String storeToTracksString() {
		return storeToJSONObject().toJSONString();
	}
	
	/*
	 * write the collection data out in a format for GoogleTracks create
	 * We do not include the entities. These must be created separately
	 * and added to the collection
	 */
	public JSONObject storeToJSONObject() {
        JSONObject jObj = new JSONObject();
        jObj.put(GoogleTracksConstants.NAME_LIT, getName());
		return jObj;
	}
	
	
	public JSONObject storeEntireCollectionToJSONObject() {
	    JSONObject jObj = storeToJSONObject();
	    jObj.put(GoogleTracksConstants.ENTITIES_LIT, addAllEntitiesToJSONArray());
	    return jObj;
	}
	public String generateCollectionIdToTracksString() {
	    return generateCollectionIdToJSONObject().toJSONString();
	}
	
	public JSONObject generateCollectionIdToJSONObject() {
        JSONObject jObj = new JSONObject();
        jObj.put(GoogleTracksConstants.COLLECTION_ID, getID());
        return jObj;
	}
	
	
	/*
	 * write out a GoogleTracks string for adding the entity list
	 */
	
	public String addEntitiesToTracksString() {
		return addEntitiesToJSONObject().toJSONString();
	}
	
	/*
	 * write out a GoogleTracks string for adding the entity list
	 *   format is:
	 * 
     *	{
     *	    "collectionId": "0eb06d476f8a7486",
     *	    "entityIds": [
     *	        "1ff3a55f94e954ee",
     *	        "ec6053f142ade5c9"
     *	    ]
     *	}
     *
     * we ignore ones with empty ids - they need to be created first
     * 
	 */
	
	public JSONObject addEntitiesToJSONObject() {
        JSONObject jObj = new JSONObject();
        JSONArray jArray = new JSONArray();
		String sId;
        for (int i=0; i<_Entities.size(); i++) {
            sId = _Entities.get(i).getID();
            if (!doesEntityBelongToCollection(sId) && sId.trim().length()>0) {
                jArray.add(sId);
            }
        }
        jObj.put(GoogleTracksConstants.ENTITY_IDS, jArray);
        jObj.put(GoogleTracksConstants.COLLECTION_ID, _ID);
        
        return jObj;
	}
	
	/*
	 * generates entity list for appending to collection
	 *   [
     *     {
     *      "id": "003bc127f3e24591",
     *      "name": "VU-401030[401030]"
     *      "type": "AUTOMOBILE"
     *     },
     *     {
     *      "id": "10d087d7781c509d",
     *      "name": "VU-401047[401047]"
     *      "type": "AUTOMOBILE"
     *     },
     *
	 */
    public JSONArray addAllEntitiesToJSONArray() {
        JSONArray jArray = new JSONArray();
        for (int i=0; i<_Entities.size(); i++) {
            GoogleTracksEntity gtEnt = _Entities.get(i);
            jArray.add(gtEnt.storeToJSONObject());
        }
        return jArray;
    }
    
	public void confirmAddEntitiesToCollection(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            confirmAddEntitiesToCollection(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
	}

	// the collection/addentities request is parsed to confirm the added entities
	public void confirmAddEntitiesToCollection(JSONObject json) {
        JSONArray jsonEntities = (JSONArray) json.get(GoogleTracksConstants.ENTITY_IDS);
        for(int i=0; i<jsonEntities.size(); i++){
            belongsToCollection.put((String) jsonEntities.get(i), true);
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

	public JSONObject storeEntitiesToJSONObject() {
        JSONObject jObj = new JSONObject();
        JSONArray jArray = new JSONArray();
        
        for (int i=0;i<_Entities.size();i++) {
            jArray.add(_Entities.get(i).storeToJSONObject());
        }

        jObj.put(GoogleTracksConstants.ENTITIES_LIT, jArray);

        return jObj;
	}
	
	public String storeEntitiesToTracksString(){
	    return storeEntitiesToJSONObject().toJSONString();
	}

	/*
	 * Load the entity IDs from the Google tracks response:
	 * {
     *       "entityIds": [
     *           "ec6053f142ade5c9",
     *           "1ff3a55f94e954ee",
     *           "fb061e749fec1627"
     *       ]
     *   }
	 * 
	 *
	 */
	
	public void loadEntityIdsFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            loadEntityIdsFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
	}
	public void loadEntityIdsFromJSONObject(JSONObject json) {
        JSONArray jsonEntities = (JSONArray) json.get(GoogleTracksConstants.ENTITY_IDS);
        for(int i=0; i<jsonEntities.size(); i++){
            String entId = (String) jsonEntities.get(i);
            if (i<this._Entities.size()) {
                this._Entities.get(i).setID(entId);
            }
        }
	}
	
	/* we can't use contains, because it tests all the entity fields
	 * 
	 */
	public boolean listContainsEntityId(String entityId) {
	    for (int i=0;i<_Entities.size();i++) {
	        if (_Entities.get(i).getID().equals(entityId)) {
	            return true;
	        }    
	    }
	    return false;
	}
	public GoogleTracksEntity findEntityById(String entityId) {
        for (int i=0;i<_Entities.size();i++) {
            if (_Entities.get(i).getID().equals(entityId)) {
                return _Entities.get(i);
            }    
        }
        return null;
	}
	
	@Override
	public String toString() {
		return "GoogleTracksCollection [ ID=" + _ID + ", Name=" + _Name
				+ ", Entities=[" + _Entities.toString() + "]]";
	}
	
}
