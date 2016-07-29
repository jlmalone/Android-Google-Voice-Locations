package com.techventus.locations;

interface GVLServiceInterface{

	double[] getCurrentCoordinatesE6();
	
	void restart();
	
	void reset();
	
	String getCurrentLocationString();
	
	void update();

}