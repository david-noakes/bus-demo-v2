package com.dialog.googletracks;

public class GoogleTracksEntity {

	private String _ID;
	private String _name;
	private String _Type;
	
	
	
    public GoogleTracksEntity() {
        super();
        // TODO Auto-generated constructor stub
    }
    public GoogleTracksEntity(String _name, String _Type) {
        super();
        this._name = _name;
        this._Type = _Type;
    }
    public GoogleTracksEntity(String _ID, String _name, String _Type) {
        super();
        this._ID = _ID;
        this._name = _name;
        this._Type = _Type;
    }
    public String get_ID() {
		return _ID;
	}
	public void set_ID(String _ID) {
		this._ID = _ID;
	}
	public String get_name() {
        return _name;
    }
    public void set_name(String _name) {
        this._name = _name;
    }
    public String get_Type() {
        return _Type;
    }
    public void set_Type(String _Type) {
        this._Type = _Type;
    }
    public void LoadFromTracksString(String tracksString) {
		// TODO
	}
	public String StoreToTracksString() {
		String tString = "";
		// TODO
		return tString;
	}
}
