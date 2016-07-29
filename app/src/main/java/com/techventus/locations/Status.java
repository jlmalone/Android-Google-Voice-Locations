package com.techventus.locations;


import com.google.android.gms.maps.model.LatLng;

public class Status {

	public static LatLng locationGeoPoint = new LatLng(0,0);
	
	static public String  currentLocationString = "Elsewhere";

	public void reset()
	{
		locationGeoPoint = new LatLng(0,0);
		
		currentLocationString = "Elsewhere";
	}
}
