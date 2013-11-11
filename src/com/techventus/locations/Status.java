package com.techventus.locations;

import com.google.android.maps.GeoPoint;


public class Status {
	
//	private static Status status;
	
	//Voice voice;
	
	public static boolean isNetworkConnected;
	

//	private boolean isNetworkConnected = false;
	
//	private boolean isVoiceConnected = false;
	
//	private String googleConnectivity;
//	
//	private String networkConnectivity;
	
	public static GeoPoint locationGeoPoint = new GeoPoint(0,0);
	
	static public String  currentLocationString = "Elsewhere";
	
//	public Status(){
//		
//	}
	
	
//	/**
//	 * Get method for the singleton Settings object
//	 *
//	 * @return the settings
//	 */
//	public static Status getStatus(){
//		if(status==null){
//			status = new Status();
//		}
//		return status;
//	
//	}
	
	
	
	
	
//	public boolean isVoiceConnected() {
//		return isVoiceConnected;
//	}
//
//
//	public void setVoiceConnected(boolean isVoiceConnected) {
//		this.isVoiceConnected = isVoiceConnected;
//	}
	
	
//	public void setVoice(Voice voice){
//		this.voice = voice;
//	}
	
//	//TODO
//	private void load(){
//		
//	}
	
	
	
	public void reset(){
//		Status.status = null;
		locationGeoPoint = new GeoPoint(0,0);
		
		 currentLocationString = "Elsewhere";

//			 isNetworkConnected = false;
			
//			 isVoiceConnected = false;
	}
	
}
