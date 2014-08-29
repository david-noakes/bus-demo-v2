package com.dialog.googletracks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GoogleTracksCollection {

	private String _ID;
	private String _Name;
	private List<GoogleTracksEntity>   _Entities;
	
	
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
	
	
	public GoogleTracksCollection() {
		super();
		_ID = "";
		_Name = "";
		_Entities = new ArrayList<GoogleTracksEntity>();
	}

	public GoogleTracksCollection(JSONObject jsonColl) {
		super();
		LoadFromJSONObject(jsonColl);
	}
	public GoogleTracksCollection(String ID, String Name, List Entities) {
		super();
		setID(ID);
		setName(Name);
		setEntities(Entities);
	}
	public GoogleTracksCollection(String tracksString) {
		super();
		LoadFromTracksString(tracksString);
	}

	/**
	 *  a json collection will look something like this
	 *         {
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
	public void LoadFromJSONObject(JSONObject jsonColl) {
        JSONArray  entities = (JSONArray) jsonColl.get(GoogleTracksConstants.ENTITY_IDS);
		List<GoogleTracksEntity> ents = new ArrayList();
        if (entities != null) {
        	for (int j=0; j< entities.size(); j++) {
        		String entId = (String) entities.get(j);
        		GoogleTracksEntity gtEntity = new GoogleTracksEntity();
        		gtEntity.setID(entId);
        		ents.add(gtEntity);
        	}
        }
        setName((String) jsonColl.get(GoogleTracksConstants.NAME_LIT));
        setID((String) jsonColl.get(GoogleTracksConstants.ID_LIT));
        setEntities(ents);

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
	
	/*
	 * write the collection data out in a format for GoogleTracks create
	 * We do not include the entities. These must be created separately
	 * and added to the collection
	 */
	public String StoreToTracksString() {
		return StoreToJSONObject().toJSONString();
	}
	
	/*
	 * write the collection data out in a format for GoogleTracks create
	 * We do not include the entities. These must be created separately
	 * and added to the collection
	 */
	public JSONObject StoreToJSONObject() {
        JSONObject jObj = new JSONObject();
        jObj.put(GoogleTracksConstants.NAME_LIT, getName());
		return jObj;
	}
	
	/*
	 * write out a GoogleTracks string for adding the entity list
	 */
	
	public String AddEntitiesToTracksString() {
		return AddEntitiesToJSONObject().toJSONString();
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
	 */
	
	public JSONObject AddEntitiesToJSONObject() {
        JSONObject jObj = new JSONObject();
        JSONArray jArray = new JSONArray();
		
        for (int i=0; i<_Entities.size(); i++) {
        	jArray.add(_Entities.get(i).getID());
        }
        jObj.put(GoogleTracksConstants.ENTITY_IDS, jArray);
        jObj.put(GoogleTracksConstants.COLLECTION_ID, _ID);
        
        return jObj;
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

	public JSONObject StoreEntitiesToJSONObject() {
        JSONObject jObj = new JSONObject();
        JSONArray jArray = new JSONArray();
        
        for (int i=0;i<_Entities.size();i++) {
            jArray.add(_Entities.get(i).StoreToJSONObject());
        }

        jObj.put(GoogleTracksConstants.ENTITIES_LIT, jArray);

        return jObj;
	}
	
	public String StoreEntitiesToTracksString(){
	    return StoreEntitiesToJSONObject().toJSONString();
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
	
	public void LoadEntityIdsFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            LoadEntityIdsFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
	}
	public void LoadEntityIdsFromJSONObject(JSONObject json) {
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
	public boolean ListContainsEntityId(String entityId) {
	    for (int i=0;i<_Entities.size();i++) {
	        if (_Entities.get(i).getID().equals(entityId)) {
	            return true;
	        }    
	    }
	    return false;
	}
	public GoogleTracksEntity FindEntityById(String entityId) {
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
