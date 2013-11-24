package com.techventus.locations;

import com.google.android.maps.GeoPoint;


public class Status {

	public static GeoPoint locationGeoPoint = new GeoPoint(0,0);
	
	static public String  currentLocationString = "Elsewhere";

	public void reset()
	{
		locationGeoPoint = new GeoPoint(0,0);
		
		currentLocationString = "Elsewhere";
	}
}
