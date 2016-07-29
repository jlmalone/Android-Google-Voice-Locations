package com.techventus.locations;

import com.google.android.maps.GeoPoint;

/**
 * The Class Settings.
 */
public class Settings
{

	/** The Constant PREFERENCENAME. */
	final protected static String PREFERENCENAME = "TECHVENTUS";
	final protected static String SERVICE_ENABLED = "SERVICE_ENABLED";
	final protected static String STARTUP_ENABLED = "STARTUP_ENABLED";
	final protected static String NOTIFICATION_ACTIVE = "NOTIFICATION_ACTIVE";
	final protected static String SOUND_ACTIVE = "SOUND_ACTIVE";
	final protected static String NOTIFICATION_APP_LAUNCH = "NOTIFICATION_APP_LAUNCH";
	final protected static String POWER_SETTING = "POWER_SETTING";
	final protected static String LOCATION_FREQUENCY = "LOCATION_FREQUENCY";

	final protected static  String ACCURACY_SETTING = "ACCURACY_SETTING";
	
	final protected static String LOCATION_NAME_EXTRA = "LOCATION_NAME_EXTRA";
	
	final protected static String RADIUS_EXTRA = "RADIUS_EXTRA";
	final protected static String LATITUDE_EXTRA = "LATITUDE_EXTRA";
	final protected static String LONGITUDE_EXTRA = "LONGITUDE_EXTRA";
	public static final String LOCATION_PROVIDER_SETTING = "LOCATION_PROVIDER_SETTING";
	public static final String GOOGLE_SYNC_FREQUENCY = "GOOGLE_SYNC_FREQUENCY";

	final private static String basis = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";


	private  boolean restartServiceFlag = false;
	private  boolean reconnectToVoiceFlag = false;
	private  boolean phoneUpdateFlag = false;
	private  boolean locationChanged = false;

	//SETTINGS
	private static Settings instance;
	private Settings(){}

	public static Settings getInstance()
	{
		if( instance==null)
		{
			 instance = new Settings();
		}
		return instance;
	}


	public boolean getRestartServiceFlag()
	{
		return restartServiceFlag;
	}

	public void setRestartServiceFlag(boolean RESTART_SERVICE_FLAG)
	{
		this.restartServiceFlag = RESTART_SERVICE_FLAG;
	}

	public boolean getReconnectToVoiceFlag()
	{
		return reconnectToVoiceFlag;
	}

	public void setReconnectToVoiceFlag(boolean RECONNECT_TO_VOICE_FLAG)
	{
		this.reconnectToVoiceFlag = RECONNECT_TO_VOICE_FLAG;
	}

	public boolean getPhoneUpdateFlag()
	{
		return phoneUpdateFlag;
	}

	public void setPhoneUpdateFlag(boolean PHONE_UPDATE_FLAG)
	{
		this.phoneUpdateFlag = PHONE_UPDATE_FLAG;
	}

	public boolean getLocationChanged()
	{
		return locationChanged;
	}

	public void setLocationChanged(boolean LOCATION_CHANGED)
	{
		this.locationChanged = LOCATION_CHANGED;
	}

	public static String decrypt(String inputString, int shift)
    {
		return encrypt(inputString, 52 - shift);
	}

	public static String encrypt(String inputString, int shift)
    {
		StringBuffer sb = new StringBuffer();

		for (char c : inputString.toCharArray())
        {
			int i = basis.indexOf(c);
			int j = (i + shift) % 52;
			if (j < 0)
				j += 52;
			if (i == -1)
				sb.append(c);
			else
				sb.append(basis.charAt(j));

		}
		return sb.toString();

	}
	 
	 public static double distInMetres(GeoPoint point1, GeoPoint point2)
     {
		    double earthRadius = 6378140;
		    
		    double lat1 =((double)point1.getLatitudeE6()/(double)1E6);
		    double lat2 =((double)point2.getLatitudeE6()/(double)1E6);
		   double lng1 = ((double)point1.getLongitudeE6()/(double)1E6);
		    double lng2 =((double)point2.getLongitudeE6()/(double)1E6);
		    
		    double dLat = Math.toRadians(lat2-lat1);
		    double dLng = Math.toRadians(lng2- lng1);
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		               Math.sin(dLng/2) * Math.sin(dLng/2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    double dist = earthRadius * c;

		    return new Double(dist ).doubleValue();
	}
	
	
}
