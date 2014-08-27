package com.dialog.googletracks;

import java.util.Iterator;
import java.util.List;

public class GoogleTracksCollection {

	private String _ID;
	private String _Name;
	private List<GoogleTracksEntity>   _Entities;
	
	
	public String get_ID() {
		return _ID;
	}
	public void set_ID(String _ID) {
		this._ID = _ID;
	}
	public String get_Name() {
		return _Name;
	}
	public void set_Name(String _Name) {
		this._Name = _Name;
	}
	public List<GoogleTracksEntity> get_Entities() {
		return _Entities;
	}
	public void set_Entities(List<GoogleTracksEntity> _Entities) {
		this._Entities = _Entities;
	}
	public GoogleTracksCollection() {
		super();
		// TODO Auto-generated constructor stub
	}
	public GoogleTracksCollection(String _ID, String _Name, List _Entities) {
		super();
		this._ID = _ID;
		this._Name = _Name;
		this._Entities = _Entities;
	}
	public GoogleTracksCollection(String tracksString) {
		super();
		// TODO Auto-generated constructor stub
		LoadFromTracksString(tracksString);
	}
	
	public void LoadFromTracksString(String tracksString) {
		// TODO
	}
	
	/*
	 * write the collection data out in a format for GoogleTracks create
	 * We do not include the entities. These must be created separately
	 * and added to the collection
	 */
	public String StoreToTracksString() {
		StringBuffer sb = new StringBuffer("{ ");
		sb.append("'name': '");
		sb.append(_Name);
		sb.append("' }");
		return sb.toString();
	}
	
	/*
	 * write out a GoogleTracks string for adding the entity list
	 */
	
	public String AddEntitiesToTracksString() {
		StringBuffer sb = new StringBuffer("{ 'collectionId': '");
		GoogleTracksEntity gtEnt = null;
		sb.append(_ID);
		sb.append("', 'entityIds': [");
		if (_Entities.size()>0) {
			// store the List - IDs only
			int iCount = 0;
			Iterator<GoogleTracksEntity> it = _Entities.iterator();
			while (it.hasNext()) {
				gtEnt = it.next();
				sb.append("'");
				sb.append(gtEnt.get_ID());
				sb.append("'");
				if (iCount < (_Entities.size() - 1)) {
					sb.append(", ");
				}
				iCount++;
			}
		}
		
		sb.append(" ] }");
		return sb.toString();
	}

	
	@Override
	public String toString() {
		return "GoogleTracksCollection [_ID=" + _ID + ", _Name=" + _Name
				+ ", _Entities=" + _Entities.toString() + "]";
	}
	
}
