package com.techventus.locations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.techventus.server.voice.Voice;
import com.techventus.server.voice.datatypes.AllSettings;
import com.techventus.server.voice.datatypes.Phone;

/**
 * The Class BackgroundService.
 * 
 * Tasks:
 * -1. Create Single Table Database
 * 			0. (location, phoneName,phoneEnable

 * 0. Ensure Background Service is Enabled - Stop Self Otherwise.
 * 1. On a Frequency, update Google Settings
 * 2. On a Frequency, enforce Phone Preferences
 * 3. On a Location Change, enforce Phone Preferences
 * 4. Create Task FLAGS - If no Connectivity, Raise Task Flag
 * 5. Upon Reconnection Execute Flagged Tasks

 */
public class BackgroundService extends Service{
	
	/** The Constant TAG. */
	final static String TAG = "TECHVENTUS - BackgroundService";
	
	Timer timer = new Timer();
	
	/** The status. */
	Status status;
	
	/** The settings. */
	Settings settings;
	
	/** The startup started. */
	boolean startupStarted = false;
	
	/** The startup complete. */
	boolean startupComplete = false;
	
	/** The location manager. */
	LocationManager locationManager;
	
	/** The geo handle. */
	public static  GeoUpdateHandler geoHandle;// = new GeoUpdateHandler();

	 /** The preferences. */
 	SharedPreferences preferences ;
	 
	 /** The UPDAT e_ flag. */
 	boolean UPDATE_FLAG = false;
	 
	 /** The pref list. */
 	List<LPEPref> prefList = new ArrayList<LPEPref>();
	 
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		
		return mBinder;
	}

	/** The Binder */
	private final GVLServiceInterface.Stub mBinder = new GVLServiceInterface.Stub() {

		@Override
		public int[] getCurrentCoordinatesE6() throws RemoteException {
		
			int[] ret = {Status.locationGeoPoint.getLatitudeE6(),Status.locationGeoPoint.getLongitudeE6()};
			 
			return ret;	//return null;
		}

		@Override
		public void reset() throws RemoteException {
			
			synchronized(BackgroundService.this){
				if(geoHandle!=null)	
				 try{locationManager.removeUpdates(geoHandle);locationManager = null;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","LocationManager Exception");}
				 try{geoHandle = new GeoUpdateHandler();}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","geohandle Exception");}
				 try{status.reset();}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","status Exception");}

//				 try{settings.reset();}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","settings Exception");}

				 try{VoiceSingleton.reset()/*voiceFact=null*/;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","voice Exception");}
//				 try{locationPreferences.reset(); }catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","preferences Exception");}
				 try{startupStarted = false;startupComplete = false;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","Startup Flag Exception");}
				 SQLiteDatabase db = null;
				 try{
					 db= openOrCreateDatabase("db",0,null);
			    	SQLHelper.exec(db,SQLHelper.dropLocationPhoneEnable );
					if(SQLHelper.isTableExists("COMMAND",db) ||SQLHelper.isTableExists("PHONE",db) ){
//						SQLHelper.exec(db, SQLHelper.dropLocationPhoneEnable);
						SQLHelper.exec(db,SQLHelper.dropPhone );
						SQLHelper.exec(db,SQLHelper.dropGoogle );
						SQLHelper.exec(db,SQLHelper.dropLocations );
						SQLHelper.exec(db,SQLHelper.dropLocation );
						SQLHelper.exec(db, SQLHelper.dropCommand);
						SQLHelper.exec(db, SQLHelper.dropStatus);
						SQLHelper.exec(db, SQLHelper.dropServiceStatus);
						SQLHelper.exec(db, SQLHelper.dropSettings);
					}


			    	
				 }catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","LocationManager Exception");}
				 finally{
					 if(db!=null)
						 db.close();
				 }
					
			}//synch
	    	getStartupTask().execute();
		}

		@Override
		public String getCurrentLocationString() throws RemoteException {
			return Status.currentLocationString;
		}

		@Override
		public void restart() throws RemoteException {
			BackgroundService.this.startupStarted = false;
			//Settings.PHONE_UPDATE_FLAG=true;
			BackgroundService.this.getStartupTask().execute();
		}

		@Override
		public void update() throws RemoteException {
			BackgroundService.this.startupStarted = false;
			//Settings.PHONE_UPDATE_FLAG=true;
			getStartupTask().execute();
		}
		
		@Override
		public String[] listProviders() throws RemoteException {
			if(locationManager!=null){
				return  locationManager.getAllProviders().toArray(new String[locationManager.getAllProviders().size()]);
			}else{
				return new String[0];
			}
		}
		
		
	};
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override 
	public void onCreate() {
		  super.onCreate();

			  Log.e(TAG, "STARTING "+TAG+" onCreate");
			  //Awareness of Network Change
				IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
				registerReceiver(networkStateReceiver, filter);
			  
			  preferences = getSharedPreferences(Settings.PREFERENCENAME, 0);
			  if(!preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
				  this.stopSelf();
				  return;
			  }else
			  if(!startupStarted){
	//		  this.notifyUserLocationChange("STARTINGtest");
				  startupStarted = true;
				  Log.e("TECHVENTUS", "Starting OnCreate BackgroundService");
				  getStartupTask().execute();
			  }

	}
	
	/**
	 * Sets the location list.
	 */
	void setLocationList(){
		List<LPEPref> ret = new ArrayList<LPEPref>();
		try{
			SQLiteDatabase db =  openOrCreateDatabase("db",0,null);
			SQLHelper.exec(db, SQLHelper.createLocationPhoneEnable);
//			if(locationName!=null){
			Cursor c = db.query("LOCATIONPHONEENABLE", null
	        , null ,null, null, null, null);
			if(c!=null){
				while(c.moveToNext())
			        {
						try{
							String locationName = c.getString(c.getColumnIndex("locationName"));
							String phoneName = c.getString(c.getColumnIndex("phoneName"));
							Integer pref = c.getInt(c.getColumnIndex("phoneEnable"));
							Integer latitude = c.getInt(c.getColumnIndex("locationLatitudeE6"));
							Integer longitude = c.getInt(c.getColumnIndex("locationLongitudeE6"));
							Integer radius = c.getInt(c.getColumnIndex("radius"));
							LPEPref lpe = new LPEPref(locationName, phoneName, radius, pref, latitude, longitude);
							ret.add(lpe);
//							ret.put(phoneName, pref);
						}catch(Exception e){
							e.printStackTrace();
						}
			        }
				c.close();
			}
			
			db.close();
	 		}catch(Exception adsf){
	 			adsf.printStackTrace();
	 		}
		prefList = ret;
	}
	
	
	//AsyncTasks
	
	/**
	 * Gets the startup task.
	 *
	 * @return the startup task
	 */
	AsyncTask<Void,Void,Void> getStartupTask(){
		AsyncTask<Void, Void, Void> startupTask = new AsyncTask<Void,Void,Void>(){
	
			@Override
			protected void onPreExecute(){
				Settings.RESTART_SERVICE_FLAG = false;
				
				if(timer!=null){
					timer.cancel();
				}
				
				super.onPreExecute();
				if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){

					
					
					if(!BackgroundService.this.isNetworkConnected()){
						startupStarted = false;
						this.cancel(true);
					}
				}else{
					this.cancel(true);
					BackgroundService.this.stopSelf();
//					cancel();
				}
			}
			
			@Override
			protected Void doInBackground(Void... arg0) {
				startupStarted = true;//trt
				SQLiteDatabase  sql = null;
				
				try{
				
					sql = openOrCreateDatabase("db",0,null);
					 
					
					if(SQLHelper.isTableExists("COMMAND",sql) ||SQLHelper.isTableExists("PHONE",sql) ){
						SQLHelper.exec(sql, SQLHelper.dropLocationPhoneEnable);
						SQLHelper.exec(sql,SQLHelper.dropPhone );
						SQLHelper.exec(sql,SQLHelper.dropGoogle );
						SQLHelper.exec(sql,SQLHelper.dropLocations );
						SQLHelper.exec(sql,SQLHelper.dropLocation );
						SQLHelper.exec(sql, SQLHelper.dropCommand);
						SQLHelper.exec(sql, SQLHelper.dropStatus);
						SQLHelper.exec(sql, SQLHelper.dropServiceStatus);
						SQLHelper.exec(sql, SQLHelper.dropSettings);
					}
					
					
					
					
					SQLHelper.createDatabases(sql);
					
					setLocationList();
						 
					reconnectToVoice();
					
//					BackgroundService.this.
					
	
				}catch(Exception e){
						
						Log.e("TECHVENTUS","Startup First EstablishTestDB exception"); 
	//					Toast.makeText(BackgroundService.this, "GVL BACKGROUND SERVICE Excpetion Establish DBSettings", Toast.LENGTH_SHORT);
						e.printStackTrace();
				}finally{
					if(sql!=null)
						sql.close();
				}
			
				return null;
			}
			@Override
			protected void onPostExecute(Void result){
				 setLocationUpdates();
				  	
				  setUpdateTimers();
				  startupComplete = true;
			}
		};
		return startupTask;
	}
	

	/**
	 * Reconnect to voice task.
	 *
	 * @return the async task
	 */
	AsyncTask<Void,Void,Void> reconnectToVoiceTask(){
		AsyncTask<Void,Void,Void> ret = new AsyncTask<Void,Void,Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				try{
					reconnectToVoice();
				}catch (com.techventus.server.voice.exception.BadAuthenticationException ba){
//	    			Toast.makeText(getApplicationContext(), "Google Voice Locations - Google Credentials Authentication Error.", Toast.LENGTH_LONG).show();
	    			preferences.edit().remove("username").remove("password").apply();
	    			
	    		}catch(Exception f){
	    			f.printStackTrace();
	    			Settings.RECONNECT_TO_VOICE_FLAG = true;
	    		}
				return null;
			}
			@Override
			protected void onPreExecute(){
				super.onPreExecute();
				if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
					
		    		   if(Settings.RESTART_SERVICE_FLAG){
		    			   try {
		    				   this.cancel(true);
							BackgroundService.this.mBinder.restart();
							return;
							} catch (Exception e) {
								
								e.printStackTrace();
							}
		    		   }
					
					
					if(!BackgroundService.this.isNetworkConnected()){
						//startupStarted = false;
						this.cancel(true);
					}
				}else{
					this.cancel(true);
					BackgroundService.this.stopSelf();
//					cancel();
				}
			}
		};
		return ret;
	}
	
	
	@Override 
	public void onDestroy(){
		try{
			timer.cancel();
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			locationManager.removeUpdates(geoHandle);
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			this.unregisterReceiver(networkStateReceiver);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}
	
	

	/**
	 * Location change task. Called By GeoHandler when a change in User-Set Locations is 
	 * Detected.
	 *
	 * @return the async task
	 */
	AsyncTask<Void,Void,Void> LocationChangeTask(){
		AsyncTask<Void,Void,Void> ret = new AsyncTask<Void,Void,Void>(){
			@Override
			protected void onPreExecute(){
				
				super.onPreExecute();

				if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
					
		    		   if(Settings.RESTART_SERVICE_FLAG){
		    			   try {
		    				   this.cancel(true);
							BackgroundService.this.mBinder.restart();
							return;
						} catch (Exception e) {
							e.printStackTrace();
						}
		    		   }
					
					
					if(!BackgroundService.this.isNetworkConnected()){
						Settings.PHONE_UPDATE_FLAG = true;
//						startupStarted = false;
						this.cancel(true);
					}
				}else{
					this.cancel(true);
					BackgroundService.this.stopSelf();
//					cancel();
				}
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				triggerLocationChange();
				
				return null;
			}
			
			/* (non-Javadoc)
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Void params){
				if(!Settings.RECONNECT_TO_VOICE_FLAG && !Settings.PHONE_UPDATE_FLAG && Settings.LOCATION_CHANGED){
					Settings.LOCATION_CHANGED = false;
					notifyUserLocationChange(com.techventus.locations.Status.currentLocationString);
					
				}
					
			}
		};
		return ret;
	}
	
	//TODO EXPERIMENTAL
	TimerTask phoneUpdateTask(){
		TimerTask phoneUpdateTask = new TimerTask(){
			@Override
			public void run() {
				Log.e(TAG, "PHONE UPDATE TASK");
				if(isNetworkConnected())
					LocationChangeTask().execute();
				else{
					Settings.PHONE_UPDATE_FLAG = true;
	
				}
			}
		};
		return phoneUpdateTask;
	}
	
	//TODO EXPERIMENTAL
	void setUpdateTimers(){
		
		try{
			int phoneUpdateFreq = preferences.getInt(Settings.GOOGLE_SYNC_FREQUENCY, 30)*60000;
			if(timer!=null)
				timer.cancel();
			timer = new Timer();
			if(phoneUpdateFreq>30000)
				timer.schedule(phoneUpdateTask(),20000, phoneUpdateFreq);
			else
				Log.e(TAG,"Error In Set Update Timers");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Run location check condition.
	 *
	 * @return true, if successful
	 */
	//ELIMINATE PERHAPS
	boolean runLocationCheckCondition(){
//		return true;
//		if(LocationPhoneEnablePreference.getLocations(BackgroundService.this).length>1)
	//	if(locationPreferences.getLocations().length>1)
//		boolean cond = false;
//		for(LPEPref pref:prefList){
//			if(!pref.location.equals("Elsewhere"))
//				cond =  true;
//		}
//		
//		
//		if(cond)
			if(preferences.getInt(Settings.LOCATION_FREQUENCY, LocationFrequencyToggle.hourly)!=-1)
				return true;
//		
		return false;
	}
	

	//TODO CHANGE CRItERIA TO SETTINGS VALUES
	synchronized void setLocationUpdates(){
		  try{
			  	
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				int locationpowercriteria = preferences.getInt(Settings.POWER_SETTING, Criteria.POWER_LOW);
				int locationaccuracycriteria = preferences.getInt(Settings.ACCURACY_SETTING, Criteria.NO_REQUIREMENT);
				Criteria criteria= new Criteria();
				criteria.setCostAllowed(false);
			    criteria.setAltitudeRequired(false);
			    criteria.setBearingRequired(false);
				criteria.setPowerRequirement(locationpowercriteria);
				criteria.setAccuracy(locationaccuracycriteria);
				String provider = "NETWORK";
				if(preferences.getString(Settings.LOCATION_PROVIDER_SETTING, "BEST_PROVIDER").equals("BEST_PROVIDER")){
					 provider = locationManager.getBestProvider(criteria, true);
				}else{
					provider = preferences.getString(Settings.LOCATION_PROVIDER_SETTING, "BEST_PROVIDER");
				}
				if(geoHandle!=null){
					locationManager.removeUpdates(geoHandle);
					Log.e(TAG, "**********GEOHANDLE REMOVED HOPEFULLY**********");
				}
//				locationManager.
				//geoHandle.
				//if(locationManager.)
//				locationManager.
				if(runLocationCheckCondition()){
					geoHandle = new GeoUpdateHandler();
					locationManager.requestLocationUpdates( provider, preferences.getInt(Settings.LOCATION_FREQUENCY, LocationFrequencyToggle.fivemin)*60000 , 0, geoHandle);
//					Toast.makeText(BackgroundService.this, "Location Manager ("+provider +") engaged on a frequency of " +preferences.getInt(Settings.LOCATION_FREQUENCY, 5) +" minutes." , Toast.LENGTH_LONG).show();
					
				}
			}catch(Exception e){
			  e.printStackTrace();
			  Log.e("Techventus","No Location Service Available");
			  
			}
	}
	

	/**
	 * Reconnect to voice.
	 */
	protected void reconnectToVoice() throws IOException{
		if(!isNetworkConnected()){
			Settings.RECONNECT_TO_VOICE_FLAG = true;
			return;
		}
		
	    	String username = preferences.getString("username", "");
	    	String password = preferences.getString("password", "");
	    	if(!username.equals("")&&!password.equals("")){
	    		VoiceSingleton.getOrCreateVoiceSingleton(username,Settings.decrypt(password, 10));
	    		Settings.RECONNECT_TO_VOICE_FLAG = false;
//	    		voice = VoiceSingleton.getOrCreateVoiceSingleton(username,Settings.decrypt(password, 10)).getVoice();
//	    		voice = new Voice(username,Settings.decrypt(password, 10));
	    	}else{

	    		VoiceSingleton.reset();
	    	}


	}
	

	
	
	/**
	 * Trigger location change.
	 */
	synchronized void triggerLocationChange(){
		
		if(isNetworkConnected()){
			try {
				Voice voice = VoiceSingleton.getVoiceSingleton().getVoice();
				AllSettings voiceSettings =voice.getSettings(false);
				Phone[] phoneAr = voiceSettings.getPhones();
				for(LPEPref lpe:prefList){
					if(lpe.location.equals(Status.currentLocationString)){
						for(Phone phone:phoneAr){
							if(phone.getName().equals(lpe.phoneString)){
								System.out.println(""+phone.getName()+" "+lpe.enablePref);
								if(lpe.enablePref==1){
									if(voiceSettings.isPhoneDisabled(phone.getId())){
										voice.phoneEnable(phone.getId());
										Settings.LOCATION_CHANGED = true;
									}
										
								}else if(lpe.enablePref==-1){
									if(!voiceSettings.isPhoneDisabled(phone.getId())){
										Log.e(TAG, "DISABLE "+phone.getId());
										Settings.LOCATION_CHANGED = true;
										voice.phoneDisable(phone.getId());
									}
										
								}
							}
						}
					}
				}
				voice.getSettings(true);
				Settings.RECONNECT_TO_VOICE_FLAG = false;
				Settings.PHONE_UPDATE_FLAG = false;
			} catch (Exception e) {
				Settings.RECONNECT_TO_VOICE_FLAG = true;
				e.printStackTrace();
			}
		}else{
			Settings.PHONE_UPDATE_FLAG = true;
		}
	}
	

	
	
	
	/**
	 * The Class GeoUpdateHandler.
	 */
	public class GeoUpdateHandler implements LocationListener {

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
		 */
		@Override
		public void onLocationChanged(Location location) {
			//Do Not Execute if Service Disabled
			if(!preferences.getBoolean(Settings.SERVICE_ENABLED, true)){
				BackgroundService.this.stopSelf();
				return;
			}
			
//			String locOrig = Status.currentLocationString;
			
			Log.e(TAG,"Geopoint Update Handler - updating location GeoPoint from "+location.getProvider());
			Log.e(TAG,"Accuracy"+location.getAccuracy());
			
			
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			
			setLocation(lat,lng,Status.currentLocationString);
			boolean action = false;
			for(LPEPref lpe:prefList){
				if(lpe.location.equals("Elsewhere")){
					continue;
				}
				if(Settings.distInMetres(lpe.getGeoPoint(), Status.locationGeoPoint)<lpe.radius+location.getAccuracy()){

					Log.e("TECHVENTUS","CHANGING LOCATION from "+Status.currentLocationString+" to "+lpe.location);
					action = true;
					if(!lpe.location.equals(Status.currentLocationString)){
						Status.currentLocationString = lpe.location;
						Log.e("TECHVENTUS","CHANGING LOCATION to "+lpe.location);
						LocationChangeTask().execute();
//						triggerLocationChange();
						break;
					}				
//					setLocation(status.locationGeoPoint.getLatitudeE6(),status.locationGeoPoint.getLongitudeE6(),loc);

				}
			
			}
//			for(String loc:Settings.locationMap.keySet()){
//					if(loc.equals("Elsewhere"))
//						continue;
//					if(Settings.distInMetres(Settings.locationMap.get(loc),Status.locationGeoPoint) < Settings.locationRadiusMap.get(loc) + location.getAccuracy()){

//			}
			if(!action && !Status.currentLocationString.equals("Elsewhere")){
				Log.e("TECHVENTUS","CHANGING LOCATION to ELSEWHERE from "+Status.currentLocationString);
				Status.currentLocationString = "Elsewhere";
				setLocation(Status.locationGeoPoint.getLatitudeE6(),Status.locationGeoPoint.getLongitudeE6(),"Elsewhere");
				LocationChangeTask().execute();
				Log.e("TECHVENTUS","LOCATION GEOPOINT SET "+Status.locationGeoPoint.getLatitudeE6());
			}
			
//			 if(!locOrig.equals(Status.currentLocationString)){
//				 Log.e("TECHVENTUS","Enforcing Voice Preferences "+Status.currentLocationString);
////				 enforceVoicePreferences();
//			 }
			//mapController.setCenter(point);
//			setContentView(mapView);
		}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
		 */
		@Override
		public void onProviderDisabled(String provider) {
			// Raise Notification Maybe???
			
		}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
		 */
		@Override
		public void onProviderEnabled(String provider) {
		}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
		 */
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			//TODO Raise Notification
		}
	}	 
	
	 
	 /**
 	 * Sets the location class variables for locationName and coordinates.
 	 *
 	 * @param lat the lat
 	 * @param lon the lon
 	 * @param loc the loc
 	 */
 	private void setLocation(int lat , int lon, String loc){
		if(!preferences.getBoolean(Settings.SERVICE_ENABLED, true)){
				BackgroundService.this.stopSelf();
				return;
		}
		 synchronized(this){
			 try{
				 Log.e("TECHVENTUS","SET LOCATION");
				 
				 Status.currentLocationString = loc;
				Status.locationGeoPoint = new GeoPoint(lat,lon);
				 
	
			 }catch(Exception u){
				 u.printStackTrace();
			 }
		 }
	 }

 	
 	//TODO Consider Launching FLAGGED TASKS FROM OTHER METHODS
	 /** The network state receiver.  This allows immediate response to network change events . */
	BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

		    @Override
		    public void onReceive(Context context, Intent intent) {
		        Log.w("Network Listener", "Network Type Changed");
		       Bundle extras =  intent.getExtras();
//		      boolean hasKey =  extras.containsKey(android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY);
		       boolean noConnectivity = extras.getBoolean(android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY );

		       if(!Status.isNetworkConnected && !noConnectivity){
		    	   if(Settings.RESTART_SERVICE_FLAG || Settings.RECONNECT_TO_VOICE_FLAG || Settings.PHONE_UPDATE_FLAG)
			    	   if(isNetworkConnected()){
			    		   if(Settings.RESTART_SERVICE_FLAG){
			    			   try {
								BackgroundService.this.mBinder.restart();
							} catch (RemoteException e) {
								
								e.printStackTrace();
							}
			    		   }else if( Settings.RECONNECT_TO_VOICE_FLAG ){
			    			   reconnectToVoiceTask().execute();
			    		   }else if(Settings.PHONE_UPDATE_FLAG){
			    			   LocationChangeTask().execute();
			    		   }
			    	   }
		       }
//		       Toast.makeText(getApplicationContext(), "Network Change. HasKey "+hasKey, Toast.LENGTH_LONG).show() ;
//		       Toast.makeText(getApplicationContext(), "Network Change. noConnectivity "+noConnectivity, Toast.LENGTH_LONG).show() ;
//				
//		      Toast.makeText(getApplicationContext(), "Network Change.  Is Connected "+isNetworkConnected(), Toast.LENGTH_LONG).show() ;
		    }
		};


	 
	//SET NOTIFICATION WHEN LOCATION IS CHANGED
	 /**
	 * Notify user of location change.
	 *
	 * @param location the location
	 */
	private void notifyUserLocationChange(String location){
		if(!preferences.getBoolean(Settings.SERVICE_ENABLED, true)){
			BackgroundService.this.stopSelf();
			return;
		}
		if(preferences.getBoolean(Settings.NOTIFICATION_ACTIVE, false)){
									
			
			 int icon = R.drawable.globesextanticon;        // icon from resources
			 CharSequence tickerText = "GV Location "+location;              // ticker-text
			 long when = System.currentTimeMillis();         // notification time
			 Context context = getApplicationContext();      // application Context
			 CharSequence contentTitle = "GV Location Changed";  // message title
			 CharSequence contentText = "Current Location: "+location;      // message text
	
			 Intent notificationIntent;
			 
			 if(preferences.getBoolean(Settings.NOTIFICATION_APP_LAUNCH, false))
				 notificationIntent = new Intent(this, MainMenu.class);
			 else
				 notificationIntent= new Intent(this, BlankIntent.class);
			 
			 PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,0);
	
			 
			 
			 // the next two lines initialize the Notification, using the configurations above
			 Notification notification = new Notification(icon, tickerText, when);
			 notification.flags |= Notification.FLAG_AUTO_CANCEL;
			 
			 if(preferences.getBoolean(Settings.SOUND_ACTIVE,false )){
				 notification.defaults |= Notification.DEFAULT_SOUND;
			 }
			 
			 notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			 String ns = Context.NOTIFICATION_SERVICE;
			 NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			 mNotificationManager.notify(434, notification);
		}
	 }
	 
	
	
	
	 /**
 	 * Checks if is network connected.
 	 *
 	 * @return true, if is network connected
 	 */
 	private boolean isNetworkConnected(){
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
		
			if (ni!=null && ni.isConnected()){
				return true;
			}else{
				return false;
			}
		}
	 
}







//
//protected void reconnectToVoice() {
//	Log.e(TAG, "START RECONNECT TO VOICE "+status.isVoiceConnected());
//	try{
//		
//		//go with current credentials
//		if(settings.hasCredentials){
//			System.out.println("YES WE HAVE CREDENTIALS");
//			voice = new Voice(settings.getLoginName(),settings.getPassword());
//		//set the credentials from the database and try again
//		}else{
//			System.out.println("NOT WE DO NOT HAVE CREDENTIALS");
//			settings.setVoiceSettingsFromDB(this);
//			
//			if(settings.hasCredentials){
//				System.out.println("we now have Credentials");
//				voice = new Voice(settings.getLoginName(),settings.getPassword());
//			}else{
//				Log.e(TAG, "NO CREDENTIALS ARE AVAILABLE FOR GOOGLE VOICE CONNECTION");
////				Toast.makeText(this, "GVL: Reconnect to Voice ERROR\n\r<br>NO CREDENTIALS", Toast.LENGTH_LONG);
//				//TO_DO CONSIDER LAUNCHING CREDENTIALS ACTIVITY OR AT LEAST RAISING A TOAST
//				voice =null;
//			}
//			//TO_DO = What get credentials from database and set them to voice object (seperate method)
//			
//		}
//		if(status!=null && voice!=null){
//			 boolean res = voice.isLoggedIn();
//			 Log.e(TAG, "voice.isLoggedIn? "+res);
//			status.setVoiceConnected(res);
//			Log.e(TAG, "STATUS JUST SET TO voice.isLoggedIn? "+status.isVoiceConnected());
//		}else if(status==null){
//			System.out.println("null status");
//		}else{
//			System.out.println("null voice object");
//		}
//	}catch(Exception e){
//		Log.e(TAG, "EXCEPTION IN RECONNECT TO VOICE");
////		Toast.makeText(this, "GVL: Reconnect to Voice ERROR\n\r<br>NO CREDENTIALS", Toast.LENGTH_LONG);
//		
//		e.printStackTrace();
//	}
//}
//






//ELIMINATE
/*		@Override
public void updateLoginCredentials(String login, String password)
		throws RemoteException {
	
	synchronized(BackgroundService.this){

		
		Log.e("TECHVENTUS","SAVING CREDENTIALS TO DATABASE");
		SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
		try{
			sql.execSQL(SQLHelper.createGoogle);
			sql.execSQL("DELETE FROM GOOGLE;");
			
			Log.e("TECHVENTUS","INSERTING CREDENTIALS");
			
			if(login.contains("@")){
				
				settings.setLoginName(login);
				settings.setPassword(password);
				
				sql.execSQL("INSERT INTO GOOGLE (loginName, password) VALUES ('"+login+"','"+password+"');");

				//sql.execSQL("INSERT INTO COMMAND (command) VALUES ('updateLoginCredentials');");
				
			} else{
				
				settings.setLoginName(login.replace("@gmail.com","").replace("@googlemail.com", "")+"@gmail.com");
				settings.setPassword(password);
				
				
				sql.execSQL("INSERT INTO GOOGLE (loginName, password) VALUES ('"+login.replace("@gmail.com","").replace("@googlemail.com", "")+"@gmail.com','"+password+"');");
				//sql.execSQL("INSERT INTO COMMAND (command) VALUES ('updateLoginCredentials');");
			}
			Log.e(TAG, "LOGIN CREDENTIAL INSERTED FOR "+settings.getLoginName());
			//LocationService.this.updateLoginCredentials(sql);
			System.out.println("login credentials inserted + "+login);
		}catch(Exception u){
			u.printStackTrace();
		}finally{
			sql.close();
		}
	}
	BackgroundService.this.reconnectToVoice();

}
*/



// MAKE THIS FUNCTIONAL, Redo this method - perhaps take into account Network Connectivity
//@Override
//public boolean isVoiceConnected() throws RemoteException {
//	Voice voice;
//	try {
//		voice = VoiceSingleton.getVoiceSingleton().getVoice();
//		if(voice!=null/*voice!=nullstatus!=null*/)
//			return true;
////			return status.isVoiceConnected();
//		else{
//			return false;
//		}
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	return false;
//
//}

//@Override
//public boolean startupComplete() throws RemoteException {
//	return startupComplete;
//
//}

//@Override
//public String[] getLocations() throws RemoteException {
//	return locationPreferences.getLocations();
//}

//@Override
//public void delete(String location) throws RemoteException {
//	//  Auto-generated method stub
//	
//}

//@Override
//public void updateVoice() throws RemoteException {
//	
//	// 
//}





//
//// CHECK NETWORK CONNECTIVITY FIRST
//protected void getPhonesFromVoice() throws Exception{
//	Voice voice = VoiceSingleton.getVoiceSingleton().getVoice();
//	if(voice!=null /*status.isVoiceConnected()*/){
//		try{
//				AllSettings allSettings =voice.getSettings(false);
//				Phone[] phones = allSettings.getPhones();
//				List<String> phoneList = new ArrayList<String>();
//				for(Phone phone:phones){
//					phoneList.add(phone.getName());
//				}
//				LocationPhoneEnablePreference.updatePhoneSet(phoneList, BackgroundService.this);
//
//		}catch(Exception e){
//			e.printStackTrace();
//			Log.e("TECHVENTUS","getPhonesFromVoice exception"); 
////			Toast.makeText(this, "GVL BACKGROUND SERVICE Excpetion getPhonesFromVoice", Toast.LENGTH_SHORT);
//			
////			status.setVoiceConnected(voice.isLoggedIn());
//			Log.e("TECHVENTUS","voice.isLoggedIn() "+voice.isLoggedIn()); 
//			
//		}
//	}
//}
//



//
//AsyncTask<Void,Void,Void> checkPhoneEnabledTask = new AsyncTask<Void,Void,Void>(){
//
//	@Override
//	protected Void doInBackground(Void... params) {
//		return null;
//	}
//};
