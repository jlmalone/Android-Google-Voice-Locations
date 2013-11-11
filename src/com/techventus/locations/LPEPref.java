package com.techventus.locations;

import com.google.android.maps.GeoPoint;

/**
 * The Class LPEPref. Location Phone Enabled Preference
 */
public class LPEPref {
	
	/** The location. */
	protected  String location;
	
	/** The phone string. */
	protected  String phoneString;
	
	/** The radius. */
	protected int radius = 100;
	
	/** The enable pref. */
	protected  int enablePref = 0;
	
	
	protected int latitude = -1;
	protected int longitude = -1;
	
	/**
	 * Instantiates a new lPE pref.
	 *
	 * @param location the location
	 * @param phoneString the phone string
	 * @param radius the radius
	 * @param enablePref the enable pref
	 */
	public LPEPref(String location, String phoneString,int radius, int enablePref, int lat, int lon){
		this.location = location;
		this.phoneString = phoneString;
		this.enablePref = enablePref;
		this.radius = radius;
		this.latitude = lat;
		this.longitude = lon;
	}
	
	
	public GeoPoint getGeoPoint(){
		GeoPoint point = new GeoPoint(latitude, longitude);
		return point;
	}
	
	
	/**
	 * Sets the enable preference.
	 *
	 * @param enablePref the new enable preference
	 */
	public void setEnablePreference(int enablePref){
		this.enablePref = enablePref;
	}

	
	/**
	 * Sets the radius.
	 *
	 * @param enablePref the new radius
	 */
	public void setRadius(int enablePref){
		this.radius = radius;
	}


	
	
}
