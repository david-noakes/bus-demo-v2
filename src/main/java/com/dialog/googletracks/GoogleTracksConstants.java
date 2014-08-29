package com.dialog.googletracks;

public class GoogleTracksConstants {

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
	
	// allowable Entity Types:
	public static String ENTITY_TYPE_AUTOMOBILE = "AUTOMOBILE"; // car or bus
    public static String ENTITY_TYPE_TRUCK      = "TRUCK";      // cargo vehicle
    public static String ENTITY_TYPE_WATERCRAFT = "WATERCRAFT"; // Ferry
    public static String ENTITY_TYPE_PERSON     = "PERSON";     //
    
    // methods
    
    public static final String METHOD_LIST_COLLECTIONS = "collections/list";
    public static final String METHOD_LIST_ENTITIES    = "entities/list";
    public static final String METHOD_CRUMBS_GET_HISTORY = "crumbs/gethistory";

    
    public static final String GOOGLE_TRACKS_API       = "https://www.googleapis.com/tracks/v1/";
}
