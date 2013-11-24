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
		public int[] getCurrentCoordinatesE6() throws RemoteException
		{
		
			int[] ret = {Status.locationGeoPoint.getLatitudeE6(),Status.locationGeoPoint.getLongitudeE6()};
			 
			return ret;
		}

		@Override
		public void reset() throws RemoteException {
			
			synchronized(BackgroundService.this){
				if(geoHandle!=null)	
				 try{locationManager.removeUpdates(geoHandle);locationManager = null;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","LocationManager Exception");}
				 try{geoHandle = new GeoUpdateHandler();}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","geohandle Exception");}
				 try{status.reset();}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","status Exception");}


				 try{VoiceSingleton.reset()/*voiceFact=null*/;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","voice Exception");}
				 try{startupStarted = false;startupComplete = false;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","Startup Flag Exception");}
				 SQLiteDatabase db = null;
				 try{
					 db= openOrCreateDatabase("db",0,null);
			    	SQLHelper.exec(db,SQLHelper.dropLocationPhoneEnable );
					if(SQLHelper.isTableExists("COMMAND",db) ||SQLHelper.isTableExists("PHONE",db) )
					{
						SQLHelper.exec(db,SQLHelper.dropPhone );
						SQLHelper.exec(db,SQLHelper.dropGoogle );
						SQLHelper.exec(db,SQLHelper.dropLocations );
						SQLHelper.exec(db,SQLHelper.dropLocation );
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
			BackgroundService.this.getStartupTask().execute();
		}

		@Override
		public void update() throws RemoteException {
			BackgroundService.this.startupStarted = false;
			getStartupTask().execute();
		}

	};

	Settings mSettings;
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override 
	public void onCreate() {
		  super.onCreate();

		mSettings = Settings.getInstance();

			  //Awareness of Network Change
				IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
				registerReceiver(networkStateReceiver, filter);
			  
			  preferences = getSharedPreferences(Settings.PREFERENCENAME, 0);
			  if(!preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
				  this.stopSelf();
				  return;
			  }else
			  if(!startupStarted){
				  startupStarted = true;
				  //Starting OnCreate BackgroundService
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
	
	

	/**
	 * Gets the startup task.
	 *
	 * @return the startup task
	 */
	AsyncTask<Void,Void,Void> getStartupTask(){
		AsyncTask<Void, Void, Void> startupTask = new AsyncTask<Void,Void,Void>(){
	
			@Override
			protected void onPreExecute(){
				mSettings.setRestartServiceFlag(false);
				
				if(timer!=null)
                {
					timer.cancel();
				}
				
				super.onPreExecute();
				if(preferences.getBoolean(Settings.SERVICE_ENABLED, false))
                {

					if(!BackgroundService.this.isNetworkConnected())
                    {
						startupStarted = false;
						this.cancel(true);
					}
				}
                else
                {
					this.cancel(true);
					BackgroundService.this.stopSelf();
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
						SQLHelper.exec(sql, SQLHelper.dropStatus);
						SQLHelper.exec(sql, SQLHelper.dropServiceStatus);
						SQLHelper.exec(sql, SQLHelper.dropSettings);
					}
					
					
					
					
					SQLHelper.createDatabases(sql);
					
					setLocationList();
						 
					reconnectToVoice();
					

	
				}catch(Exception e){
						
						Log.e("TECHVENTUS","Startup First EstablishTestDB exception"); 
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
	    			preferences.edit().remove("username").remove("password").apply();
	    			
	    		}catch(Exception f){
	    			f.printStackTrace();
					mSettings.setReconnectToVoiceFlag(true);
	    		}
				return null;
			}
			@Override
			protected void onPreExecute(){
				super.onPreExecute();
				if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
					
		    		   if(mSettings.getRestartServiceFlag()){
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
					
		    		   if(mSettings.getRestartServiceFlag())
				       {
		    			    try
					        {
		    				    this.cancel(true);
								BackgroundService.this.mBinder.restart();
								return;
							} catch (Exception e) {
								e.printStackTrace();
							}
		    		   }
					
					
					if(!BackgroundService.this.isNetworkConnected())
					{
						mSettings.setPhoneUpdateFlag(true);
						this.cancel(true);
					}
				}else
				{
					this.cancel(true);
					BackgroundService.this.stopSelf();
				}
			}
			
			@Override
			protected Void doInBackground(Void... params)
			{
				triggerLocationChange();
				
				return null;
			}
			
			/* (non-Javadoc)
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Void params)
			{

				if(!mSettings.getPhoneUpdateFlag() && ! mSettings.getReconnectToVoiceFlag() && mSettings.getLocationChanged() )
				{
					mSettings.setLocationChanged(false);
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
					mSettings.setPhoneUpdateFlag(true);
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

			if(preferences.getInt(Settings.LOCATION_FREQUENCY, LocationFrequencyToggle.hourly)!=-1)
				return true;
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

				if(runLocationCheckCondition()){
					geoHandle = new GeoUpdateHandler();
					locationManager.requestLocationUpdates( provider, preferences.getInt(Settings.LOCATION_FREQUENCY, LocationFrequencyToggle.fivemin)*60000 , 0, geoHandle);

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
			mSettings.setReconnectToVoiceFlag(true);
			return;
		}
		
	    	String username = preferences.getString("username", "");
	    	String password = preferences.getString("password", "");
	    	if(!username.equals("")&&!password.equals("")){
	    		VoiceSingleton.getOrCreateVoiceSingleton(username,Settings.decrypt(password, 10));
			    mSettings.setReconnectToVoiceFlag(false);
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
										mSettings.setLocationChanged(true);
									}
										
								}else if(lpe.enablePref==-1){
									if(!voiceSettings.isPhoneDisabled(phone.getId())){
										Log.e(TAG, "DISABLE "+phone.getId());
										mSettings.setLocationChanged(true);
										voice.phoneDisable(phone.getId());
									}
										
								}
							}
						}
					}
				}
				voice.getSettings(true);
				mSettings.setReconnectToVoiceFlag(false);
				mSettings.setPhoneUpdateFlag(false);
			} catch (Exception e) {
				mSettings.setReconnectToVoiceFlag(true);
				e.printStackTrace();
			}
		}
		else
		{
			mSettings.setPhoneUpdateFlag(true);
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
						break;
					}				
				}
			
			}
			if(!action && !Status.currentLocationString.equals("Elsewhere")){
				Log.e("TECHVENTUS","CHANGING LOCATION to ELSEWHERE from "+Status.currentLocationString);
				Status.currentLocationString = "Elsewhere";
				setLocation(Status.locationGeoPoint.getLatitudeE6(),Status.locationGeoPoint.getLongitudeE6(),"Elsewhere");
				LocationChangeTask().execute();
				Log.e("TECHVENTUS","LOCATION GEOPOINT SET "+Status.locationGeoPoint.getLatitudeE6());
			}
			
		}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
		 */
		@Override
		public void onProviderDisabled(String provider) {

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
		       boolean noConnectivity = extras.getBoolean(android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY );

		       if(!noConnectivity){
		    	   if(mSettings.getRestartServiceFlag() || mSettings.getReconnectToVoiceFlag() || mSettings.getPhoneUpdateFlag())
			    	   if(isNetworkConnected()){
			    		   if( mSettings.getRestartServiceFlag()){
			    			   try {
								    BackgroundService.this.mBinder.restart();
							    } catch (RemoteException e) {
								
								e.printStackTrace();
							}
			    		   }else if(mSettings.getReconnectToVoiceFlag() ){
			    			   reconnectToVoiceTask().execute();
			    		   }else if( mSettings.getPhoneUpdateFlag()){
			    			   LocationChangeTask().execute();
			    		   }
			    	   }
		       }
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




