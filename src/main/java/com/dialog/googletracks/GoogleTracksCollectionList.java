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
	
    // we track all the entities, which may be assigned to collections
	private List<GoogleTracksEntity> allEntities = new ArrayList<GoogleTracksEntity>() ;
	
	
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
                this.get(i).setID(collId);
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
	
	
	// Load entities from a list into the collection. 
	
	public void LoadAllEntitiesFromJSONObject(JSONObject json) {
        JSONArray jsonEntities = (JSONArray) json.get(GoogleTracksConstants.ENTITIES_LIT);
        for(int i=0; i<jsonEntities.size(); i++){
            JSONObject jEnt = (JSONObject) jsonEntities.get(i);
            GoogleTracksEntity gtEnt = new GoogleTracksEntity(jEnt);
            if (!allEntities.contains(gtEnt)) {
                allEntities.add(gtEnt);
            }
        }
        //  Google allows entities to belong to more than one collection
        //  We may have entities duplicated in our structure
        //  Replace any references that match ID with the ones in allEntities
        
	    for (int i=0;i<allEntities.size();i++) {
	        PutEntityIntoCollections((GoogleTracksEntity) allEntities.get(i));
	    }
	}
	
	public void LoadAllEntitiesFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            LoadAllEntitiesFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
	}
	
    /* check each collection for an entityID that matches
     *  
     */
	public void PutEntityIntoCollections(GoogleTracksEntity gtEnt) {
	    GoogleTracksCollection gtColl;
	    GoogleTracksEntity gteOther;
	    for (int i=0;i<this.size();i++) {
	        gtColl = (GoogleTracksCollection) this.get(i);
	        gteOther = gtColl.FindEntityById(gtEnt.getID()); 
	        if (gteOther != null) {
                if (gteOther != gtEnt) {
                    // duplicate - replace
                    int j=gtColl.getEntities().indexOf(gteOther);
                    gtColl.getEntities().remove(gteOther);
                    gtColl.getEntities().add(j, gtEnt);
                }
	        }
	    }
	}

	
	
	/**
     * @return the allEntities
     */
    public List<GoogleTracksEntity> getAllEntities() {
        return allEntities;
    }

    /**
     * @param allEntities the allEntities to set
     */
    public void setAllEntities(List<GoogleTracksEntity> allEntities) {
        this.allEntities = allEntities;
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
