package com.techventus.locations;

interface GVLServiceInterface{

	int[] getCurrentCoordinatesE6();
	
	void restart();
	
	void reset();
	
	String getCurrentLocationString();
	
	void update();

}