package com.dialog.googletracks;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Math.*;

public class GoogleTracksCollectionList extends ArrayList<GoogleTracksCollection> {

	private static final long serialVersionUID = 1L;
	
	
/* a collection/list response will look something like this
 * 'nextId' will only be present if the list was too long, and represents the restart value
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
    ] 'nextId ' : '0291c259ca933c28'
}
*/	
	
	public void LoadFromTracksString(String tracksString) {

		StringBuffer sb = new StringBuffer(tracksString);
		int i = -1;
		i = sb.indexOf("{");
		if (i>-1) { // process the string
			i = sb.indexOf("collections");
			if (1 > -1) {
				sb.delete(0, i+11);
				i = sb.indexOf("["); // the list of collection entries
				while (i>-1) {
					sb.delete(0, i+1); // consume the [
					i = sb.indexOf("{"); // start of the collection
					i = sb.indexOf("entityIds");
					while (i>-1) { // process the embedded entities
						sb.delete(0, i+1);
						i = sb.indexOf("[");
						sb.delete(0, i+1);
						int j = sb.indexOf(",");
						int k = sb.indexOf("]");
						int m = java.lang.Math.min(j,k);
						
					//TODO
					}
					sb.delete(0, i+1);
					i = sb.indexOf("{");  // start of the collection
				}
				
				// TODO
				
			}
		}
	}
	
	/*
	 * creates a string that can be sent in a request body to 
	 * GoogleTracks to create a list of Collections
	 */
	public String StoreToTracksString() {
		
		StringBuffer sb = new StringBuffer("{ 'collections': [ ");
		GoogleTracksCollection gtColl = null;
		
		if (size()>0) {
			// store the List
			int iCount = 0;
			Iterator<GoogleTracksCollection> it = iterator();
			while (it.hasNext()) {
				gtColl = it.next();
				sb.append(gtColl.StoreToTracksString());
				if (iCount < (size() - 1)) {
					sb.append(", ");
				}
				iCount++;
			}
		}
		sb = sb.append(" ] }");
		return sb.toString();
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

	

}
