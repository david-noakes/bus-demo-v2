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
	
	
	public String get_ID() {
		return _ID;
	}
	public void set_ID(String _ID) {
		if (_ID != null) {
			this._ID = _ID;
		}
	}
	public String get_Name() {
		return _Name;
	}
	public void set_Name(String _Name) {
		if (_Name != null) {
			this._Name = _Name;
		}
	}
	public List<GoogleTracksEntity> get_Entities() {
		return _Entities;
	}
	public void set_Entities(List<GoogleTracksEntity> _Entities) {
		if (_Entities != null) {
			this._Entities = _Entities;
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
	public GoogleTracksCollection(String _ID, String _Name, List _Entities) {
		super();
		set_ID(_ID);
		set_Name(_Name);
		set_Entities(_Entities);
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
        		gtEntity.set_ID(entId);
        		ents.add(gtEntity);
        	}
        }
        set_Name((String) jsonColl.get(GoogleTracksConstants.NAME_LIT));
        set_ID((String) jsonColl.get(GoogleTracksConstants.ID_LIT));
        set_Entities(ents);

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
        jObj.put(GoogleTracksConstants.NAME_LIT, get_Name());
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
        	jArray.add(_Entities.get(i).get_ID());
        }
        jObj.put(GoogleTracksConstants.ENTITY_IDS, jArray);
        jObj.put(GoogleTracksConstants.COLLECTION_ID, _ID);
        
        return jObj;
	}


	
	@Override
	public String toString() {
		return "GoogleTracksCollection [ ID=" + _ID + ", Name=" + _Name
				+ ", Entities=[" + _Entities.toString() + "]]";
	}
	
}
