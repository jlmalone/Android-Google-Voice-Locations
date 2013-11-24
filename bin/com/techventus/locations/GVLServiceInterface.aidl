package com.techventus.locations;

interface GVLServiceInterface{

//	int getFrequency();
	
	int[] getCurrentCoordinatesE6();
	
//	void setFrequency(int seconds);
	
	void restart();
	
	void reset();
	
//	void deleteLocation(String locationName);
	
//	void updateLoginCredentials(String login, String password);

//	void updateVoice();
	
	String getCurrentLocationString();
	
	void update();
	
//	String getPowerMode();
	
//	void setPowerMode(String powerMode);
	
//	String getAccuracy();
	
//	void setAccuracy(String accuracy);
	
//	boolean isVoiceConnected();
	
//	void dial(String phone, String number);
	
//	String[] getPhones();
	
//	String[] getLocations();
	
//	boolean startupComplete();
	
//	boolean hasCredentials();
	
//	void delete(String location);

//	String[] listProviders();
	
}