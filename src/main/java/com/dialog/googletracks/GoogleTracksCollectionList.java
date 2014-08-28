package com.dialog.googletracks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.Math.*;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GoogleTracksCollectionList extends ArrayList<GoogleTracksCollection> {

	private static final long serialVersionUID = 1L;
	
	
/* a collection/list response will look something like this
 * 'nextId' will only be present if the list was too long, and represents the restart value
 * each collection object will instantiate itself from the appropriate entries
 * 	{
    'collections': [
        {
            'entityIds': [
                '1ff3a55f94e954ee',
                'ec6053f142ade5c9'
            ],
            'id': '0eb06d476f8a7486',
            'name': 'SEA rental fleet - compact'
        },
        {
            'entityIds': [
                '0ff3a55f94e954ee',
                'fc6053f142ade5c9'
            ],
            'id': '0179f339ec9232ef',
            'name': 'SEA rental fleet - midsize'
        }
    ] 'nextId' : '0291c259ca933c28'
}
*/	
	public void LoadFromJSONObject(JSONObject json) {
		
		JSONArray jsonCollections = (JSONArray) json.get(GoogleTracksConstants.COLLECTIONS_LIT);
		JSONObject nextId = (JSONObject) json.get("nextId"); // note will be none if no next
        // take the elements of the json array
        for(int i=0; i<jsonCollections.size(); i++){
            JSONObject jColl = (JSONObject) jsonCollections.get(i);
            this.add(new GoogleTracksCollection(jColl));
        }
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
	 * creates a string that can be sent in a request body to 
	 * GoogleTracks to create a list of Collections
	 * example format:
	 * 
	 *	   {
	 *		    "collections": [
	 *		        {
	 *		            "name": "SEA rental fleet - compact"
	 *		        },
	 *		        {
	 *		            "name": "SEA rental fleet - midsize"
	 *		        }
	 *		    ]
	 *		}
	 */
	
	public JSONObject StoreToJSONObject() {

	    JSONObject jObj = new JSONObject();
        JSONArray jArray = new JSONArray();
        
        for (int i=0; i<this.size(); i++) {
            jArray.add(this.get(i).StoreToJSONObject());
        }
	    jObj.put(GoogleTracksConstants.COLLECTIONS_LIT, jArray);
	    

	    return jObj;
	}
	public String StoreToTracksString() {
		return StoreToJSONObject().toJSONString();
	}

	/*
	 * For a new collection list, we need to load the Google generated IDs
	 *  format:
	 *  {
     *       "collectionIds": [
     *           "0eb06d476f8a7486",
     *           "0179f339ec9232ef"
     *       ]
        }
	 */
	        
	public void LoadCollectionIdsFromJSONObject(JSONObject json){
        JSONArray jsonCollections = (JSONArray) json.get(GoogleTracksConstants.COLLECTION_IDS);
        for(int i=0; i<jsonCollections.size(); i++){
            String collId = (String) jsonCollections.get(i);
            if (i<this.size()) {
                this.get(i).set_ID(collId);
            }
        }
	    
	}
	
	public void LoadCollectionIdsFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            LoadCollectionIdsFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("GoogleTracksCollectionList [size()=");
		sb.append(size());
		GoogleTracksCollection gtColl = null;
		
		if (size()>0) {
			// store the List
			int iCount = 0;
			Iterator<GoogleTracksCollection> it = iterator();
			while (it.hasNext()) {
				gtColl = it.next();
				sb.append(gtColl.toString());
				if (iCount < (size() - 1)) {
					sb.append(", ");
				}
				iCount++;
			}
		}
		sb = sb.append(" ]");
		
		
		return sb.toString();
	}

	public GoogleTracksCollectionList(String tracksString) {
		super();
		LoadFromTracksString(tracksString);
	}
	public GoogleTracksCollectionList(JSONObject json) {
		super();
		LoadFromJSONObject(json);
	}

    public GoogleTracksCollectionList() {
        super();
        // TODO Auto-generated constructor stub
    }

	

}
