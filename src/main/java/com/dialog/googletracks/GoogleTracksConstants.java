package com.dialog.googletracks;

public class GoogleTracksConstants {

    // values for tracks
	public static final String COLLECTION_ID = "collectionId";
    public static final String COLLECTION_IDS = "collectionIds";
	public static final String COLLECTIONS_LIT = "collections";
	public static final String CONFIDENCE_RADIUS = "confidenceRadius";
	public static final String COUNT_AFTER = "countAfter";
    public static final String COUNT_BEFORE = "countBefore";
    public static final String CRUMBS_LIT = "crumbs";
	public static final String ENTITY_ID = "entityId";
    public static final String ENTITY_IDS = "entityIds";
	public static final String ENTITIES_LIT = "entities";
	public static final String HEADING_LIT = "heading";
	public static final String ID_LIT     = "id";
	public static final String LATITUDE_LIT = "lat";
	public static final String LOCATION_LIT = "location";
	public static final String LONGITUDE_LIT = "lng";
	public static final String NAME_LIT   = "name";
	public static final String TIME_STAMP = "timestamp";
	public static final String TYPE_LIT   = "type";
	public static final String USER_DATA = "userData";
	public static final String MULTI_ENTITY_CRUMBS = "  ";
	
	// allowable Entity Types:
	public static String ENTITY_TYPE_AUTOMOBILE = "AUTOMOBILE"; // car or bus
    public static String ENTITY_TYPE_TRUCK      = "TRUCK";      // cargo vehicle
    public static String ENTITY_TYPE_WATERCRAFT = "WATERCRAFT"; // Ferry
    public static String ENTITY_TYPE_PERSON     = "PERSON";     //
    
    // methods
    
    public static final String METHOD_ADD_ENTITIES_TO_COLLECTION = "collections/addentities";
    public static final String METHOD_CREATE_COLLECTIONS = "collections/create";
    public static final String METHOD_CREATE_ENTITIES = "entities/create";
    public static final String METHOD_LIST_COLLECTIONS = "collections/list";
    public static final String METHOD_LIST_ENTITIES    = "entities/list";
    public static final String METHOD_CRUMBS_GET_HISTORY = "crumbs/gethistory";
    public static final String METHOD_CRUMBS_GET_RECENT = "crumbs/getrecent";
    public static final String METHOD_CRUMBS_RECORD = "crumbs/record";

    // user data - note short names.
    // total user data is limited to 64 chars
    public static final String USER_ENTITY = "ent";
    public static final String USER_V_ID   = "id";
    public static final String USER_ROUTE  = "rte";
    public static final String USER_STATIONARY = "st";
    public static final String USER_TRIP   = "trip";

    
    
    public static final String GOOGLE_TRACKS_API       = "https://www.googleapis.com/tracks/v1/";
    
    // javascript calls to tracksJSONServlet. Note these are embedded in quotes
    public static final String JSONSERVLET_GET_COLLECTIONS = "\"get_collections\"";
    public static final String JSONSERVLET_GET_ENTITY_CRUMBS = "\"get_entity_crumbs\"";
    public static final String JSONSERVLET_GET_COLLECTION_CRUMBS = "\"get_collection_crumbs\"";
    
    // values for the json object passed from tracksJSONServlet
    public static final String JSON_COLLECTION_SET = "collectionSet";
    
}
