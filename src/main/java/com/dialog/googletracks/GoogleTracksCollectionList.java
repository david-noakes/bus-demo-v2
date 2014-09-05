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
	private List<GoogleTracksEntity> allEntities;
	
	private int crumbsLength = 0;
	
	
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
	public void loadFromJSONObject(JSONObject json) {
		
		JSONArray jsonCollections = (JSONArray) json.get(GoogleTracksConstants.COLLECTIONS_LIT);
		JSONObject nextId = (JSONObject) json.get("nextId"); // note will be none if no next
        // take the elements of the json array
        for(int i=0; i<jsonCollections.size(); i++){
            JSONObject jColl = (JSONObject) jsonCollections.get(i);
            this.add(new GoogleTracksCollection(jColl));
        }
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
	
	public JSONObject storeToJSONObject() {

	    JSONObject jObj = new JSONObject();
        JSONArray jArray = new JSONArray();
        
        for (int i=0; i<this.size(); i++) {
            jArray.add(this.get(i).storeToJSONObject());
        }
	    jObj.put(GoogleTracksConstants.COLLECTIONS_LIT, jArray);
	    

	    return jObj;
	}
	public String storeToTracksString() {
		return storeToJSONObject().toJSONString();
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
	        
	public void loadCollectionIdsFromJSONObject(JSONObject json){
        JSONArray jsonCollections = (JSONArray) json.get(GoogleTracksConstants.COLLECTION_IDS);
        for(int i=0; i<jsonCollections.size(); i++){
            String collId = (String) jsonCollections.get(i);
            if (i<this.size()) {
                this.get(i).setID(collId);
            }
        }
	}
	
	public void loadCollectionIdsFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            loadCollectionIdsFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
	}
	
	
	// Load entities from a list into the collection. 
	
	public void loadAllEntitiesFromJSONObject(JSONObject json) {
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
	        putEntityIntoCollections((GoogleTracksEntity) allEntities.get(i));
	    }
	}
	
	public void loadAllEntitiesFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            loadAllEntitiesFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
	}
	
    /* check each collection for an entityID that matches
     * 
     * Assumption - we have loaded a collection, with a list of entity ids
     * 
     * We are now adding the entities. 
     * This is why we ignore the ones that are not in the list
     *  
     */
	public void putEntityIntoCollections(GoogleTracksEntity gtEnt) {
	    GoogleTracksCollection gtColl;
	    GoogleTracksEntity gteOther;
	    for (int i=0;i<this.size();i++) {
	        gtColl = (GoogleTracksCollection) this.get(i);
	        gteOther = gtColl.findEntityById(gtEnt.getID()); 
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

    public GoogleTracksEntity findEntityById(String entityId) {
        for (int i=0;i<allEntities.size();i++) {
            if (allEntities.get(i).getID().equals(entityId)) {
                return allEntities.get(i);
            }    
        }
        return null;
    }
    
    public GoogleTracksEntity findEntityByName(String entityName) {
        for (int i=0;i<allEntities.size();i++) {
            if (allEntities.get(i).getName().equals(entityName)) {
                return allEntities.get(i);
            }    
        }
        return null;
    }
    
    public GoogleTracksEntity findEntityByNameAndType(GoogleTracksEntity gtOther) {
        for (int i=0;i<allEntities.size();i++) {
            // Note equals must test TO the list to be able to handle
            // blank ID on the new item
            if (gtOther.equals(allEntities.get(i))) {
                return allEntities.get(i);
            }    
        }
        return null;
    }
    
	public String storeNewEntitiesToTracksString() {
	       return storeNewEntitiesToJSONObject().toJSONString();
	}
	
	public JSONObject storeNewEntitiesToJSONObject(){
        JSONObject jObj = new JSONObject();
        JSONArray jArray = new JSONArray();
        
        for (int i=0;i<allEntities.size();i++) {
            //TODO - Remove limit on entities
            if (i<15) { // avoid google entity limit of 20
            if (allEntities.get(i).getID().trim().length()==0) {
                jArray.add(allEntities.get(i).storeToJSONObject());
            }
            }
        }

        jObj.put(GoogleTracksConstants.ENTITIES_LIT, jArray);

        return jObj;
	    
	}
	
    // Load entities from a list into the collection. 
    
    public void loadNewEntityIdsFromJSONObject(JSONObject json) {
        JSONArray jsonEntities = (JSONArray) json.get(GoogleTracksConstants.ENTITY_IDS);
        if (jsonEntities == null) {
            // Google tracks hickup
            return;
        }
        GoogleTracksEntity gtEnt=null;
        for(int i=0; i<jsonEntities.size(); i++){
            String entID = (String) jsonEntities.get(i);
            for (int j=0;j<allEntities.size();j++) {
                gtEnt = allEntities.get(j);
                if (gtEnt.getID().trim().length()==0) {
                    gtEnt.setID(entID);
                    break;
                }
                putEntityIntoCollections(gtEnt);
            }
        }
    }
    
    public void loadNewEntityIdsFromTracksString(String tracksString) {
        JSONParser jsonParser=new JSONParser();
        try {
            JSONObject json = (JSONObject) jsonParser.parse( tracksString );
            loadNewEntityIdsFromJSONObject(json);
        } catch (ParseException e) {
            System.out.println("position: " + e.getPosition());
            System.out.println(e);
        }
    }
    
    public String storeAllCrumbsToTracksString() {
        return storeAllCrumbsToJSONObject().toJSONString();
    }
 
    public JSONObject storeAllCrumbsToJSONObject(){
         JSONObject jObj = new JSONObject();
         JSONArray jArray = new JSONArray();
         
         for (int i=0;i<allEntities.size();i++) {
             if (allEntities.get(i).getID().trim().length()==0) {
                 jArray.add(allEntities.get(i).storeCrumbsToJSONObject());
             }
         }
    
         jObj.put(GoogleTracksConstants.MULTI_ENTITY_CRUMBS, jArray);
    
         return jObj;
         
    }
     
     /*
      * is this entity not in any collection (= orphaned)
      */
     
    public boolean isThisEntityOrphaned(GoogleTracksEntity gtEnt) {
        for (int i = 0; i < size(); i++) {
            GoogleTracksCollection gtColl = this.get(i);
            if (gtColl.getEntities().contains(gtEnt)) {
                return false;
            }
        }
        return true;
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

    /**
     * @return the crumbsLength
     */
    public int getCrumbsLength() {
        return crumbsLength;
    }

    /**
     * @param crumbsLength the crumbsLength to set
     */
    public void setCrumbsLength(int crumbsLength) {
        this.crumbsLength = crumbsLength;
    }
    
    public void updateCrumbsLength(int crumbsLength) {
        if (crumbsLength>this.crumbsLength) {
            this.crumbsLength = crumbsLength;
        }
    }
    public void incCrumbsLength() {
        this.crumbsLength++;
    }

    /*
     * write out the collections list and their entities as response
     */
    public String writeCollectionsAsJSONString() {
        return writeCollectionsAsJSONObject().toJSONString();
    }
    
    public JSONObject writeCollectionsAsJSONObject() {
        JSONObject jObj = new JSONObject();
        JSONArray jArray = new JSONArray();
        for (int i = 0; i < size(); i++) {
            GoogleTracksCollection gtColl = this.get(i);
            jArray.add(gtColl.storeEntireCollectionToJSONObject());
        }

        jObj.put(GoogleTracksConstants.JSON_COLLECTION_SET, jArray);
        return jObj;
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

    /**
     * removes collections that do not have a name starting with the prefix
     */
    public void pruneCollection(String collectionPrefix) {
        // work down, remove shifts left
        for (int i=this.size()-1;i>=0;i--) {
            if (!this.get(i).getName().startsWith(collectionPrefix)) {
                this.remove(i);
            }
        }
    }
	
	
	public GoogleTracksCollectionList(String tracksString) {
		super();
        allEntities = new ArrayList<GoogleTracksEntity>();
        crumbsLength = 0;
		loadFromTracksString(tracksString);
	}
	public GoogleTracksCollectionList(JSONObject json) {
		super();
        allEntities = new ArrayList<GoogleTracksEntity>();
        crumbsLength = 0;
		loadFromJSONObject(json);
	}

    public GoogleTracksCollectionList() {
        super();
        allEntities = new ArrayList<GoogleTracksEntity>();
        crumbsLength = 0;
    }

	

}
