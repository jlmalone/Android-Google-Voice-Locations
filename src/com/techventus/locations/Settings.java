package com.techventus.locations;

//import java.util.HashMap;
//import java.util.Map;
import com.google.android.maps.GeoPoint;

/**
 * The Class Settings.
 */
public class Settings {


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
	
	final protected static String LOCATION_ARRAY_EXTRA = "LOCATION_ARRAY_EXTRA";
	final protected static String LOCATION_NAME_EXTRA = "LOCATION_NAME_EXTRA";
	
	final protected static String RADIUS_EXTRA = "RADIUS_EXTRA";
	final protected static String LATITUDE_EXTRA = "LATITUDE_EXTRA";
	final protected static String LONGITUDE_EXTRA = "LONGITUDE_EXTRA";
	public static final String LOCATION_PROVIDER_SETTING = "LOCATION_PROVIDER_SETTING";

	
	
	// DELETE
//	final public static Map<String, GeoPoint> locationMap = new HashMap<String, GeoPoint>();
//	final public static Map<String, Double> locationRadiusMap = new HashMap<String, Double>();

	

	
	
	public static boolean RESTART_SERVICE_FLAG = false;
	public static boolean RECONNECT_TO_VOICE_FLAG = false;
	public static boolean PHONE_UPDATE_FLAG = false;
	public static boolean LOCATION_CHANGED = false;
	
	


	final private static String basis = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String GOOGLE_SYNC_FREQUENCY = "GOOGLE_SYNC_FREQUENCY";
	
	public static String decrypt(String inputString, int shift) {
		return encrypt(inputString, 52 - shift);
	}

	public static String encrypt(String inputString, int shift) {
		StringBuffer sb = new StringBuffer();

		for (char c : inputString.toCharArray()) {
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



	

	 
	 public static double distInMetres(GeoPoint point1, GeoPoint point2) {
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


//
///** The settings. */
//private static Settings settings;


//	public String getLoginName() {
//		return loginName;
//	}
//
//	public String getPassword() {
//		return password;
//	}
//
//	public void clearCredentials() {
//		this.loginName = null;
//		this.password = null;
//		hasCredentials = false;
//	}

//	public int getPower_criteria() {
//		return power_criteria;
//	}
//
//	public void setPower_criteria(int power_criteria) {
//		this.power_criteria = power_criteria;
//	}

//	public int getAccuracy_criteria() {
//		return accuracy_criteria;
//	}
//
//	public void setAccuracy_criteria(int accuracy_criteria) {
//		this.accuracy_criteria = accuracy_criteria;
//	}


//
// public void setVoiceSettingsFromDB(Context context){
// System.out.println("TRY TO SET VOICE SETTINGS FROM DB");
// SQLiteDatabase sql= context.openOrCreateDatabase("db",0,null);
// try{
//
// //settings.clearCredentials();
// Cursor c = sql.rawQuery(SQLHelper.selectStoredGoogleCredentialsStatement,
// null);
// if(c!=null){
// c.moveToNext();
// if(c.isFirst()){
// settings.setLoginName(c.getString(0));
// settings.setPassword(c.getString(1));
// //loginName = c.getString(0);
// //password = c.getString(1);
// System.out.println("got settings "+settings.getLoginName());
// }
// c.close();
// }else{
// Log.e("TECHVENTUS - SETTINGS",
// "ERROR IN setVoiceSettingsFromDB.  Cursor is NULL");
// // Toast.makeText(context,
// "ERROR IN setVoiceSettingsFromDB.  Cursor is NULL", 5000);
// }
// }catch(Exception e){
// e.printStackTrace();
// }finally{
// sql.close();
// }
// }

// public void setPassword(String password) {
// this.password = password;
// if(loginName!=null)
// hasCredentials = true;
//
// }

// public void setLoginName(String loginName) {
// this.loginName = loginName;
// if(password!=null)
// hasCredentials = true;
// }





// List<String> phoneList = new ArrayList<String>();
// DELETE
//protected List<String> phoneList = null;
//Map<String, Boolean> locationEnabledMap = new HashMap<String, Boolean>();
//List<String> locationList = new ArrayList<String>();


//private int power_criteria = Criteria.POWER_LOW;
//private int accuracy_criteria = Criteria.ACCURACY_COARSE;

//private String loginName;
//private String password;

//boolean hasCredentials = false;

//SharedPreferences preferences;



///**
// * Instantiates a new settings object which is Singleton.
// */
//private Settings() {
//
//}
//
///**
// * Get method for the singleton Settings object
// * 
// * @return the settings
// */
//public static Settings getSettings() {
//	if (settings == null) {
//		settings = new Settings();
//	}
//	return settings;
//
//}



//
//
//	public void reset() {
//
//		// DELETE
////		locationMap = new HashMap<String, GeoPoint>();
////		locationRadiusMap = new HashMap<String, Double>();
//		// List<String> phoneList = new ArrayList<String>();
//		// DELETE
////		phoneList = null;
////		locationEnabledMap = new HashMap<String, Boolean>();
//
////		power_criteria = Criteria.POWER_LOW;
////
////		accuracy_criteria = Criteria.ACCURACY_COARSE;
//
////		loginName = null;
////		password = null;
//
////		hasCredentials = false;
////		locationList = new ArrayList<String>();
//		Settings.settings = null;
//
//
//
//
//	}
