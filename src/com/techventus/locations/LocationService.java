package com.techventus.locations;

import gvjava.org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
//import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.maps.GeoPoint;
import com.techventus.locations.GVLServiceInterface;
//import com.techventus.locations.GVLServiceInterface.Stub;
import com.techventus.server.voice.Voice;
import com.techventus.server.voice.datatypes.AllSettings;
import com.techventus.server.voice.datatypes.Phone;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


public class LocationService extends Service {
	
	
	private String TAG = "TECHVENTUS - LocationService";
	
	Status status;
	Settings settings;
	private  Voice voice;
	

	// Implemented before understanding IBinder.  
	//Potentially can eliminate
    public class LocalBinder extends Binder {
    	LocationService getService() {
            return LocationService.this;

    	}
    }
    
    

	LocationManager locationManager;
	
	GeoUpdateHandler geoHandle = new GeoUpdateHandler();
	
	private List<Timer> timerList = new ArrayList<Timer>();
	
	Map<String,Integer[]> locationPhonePrefs = new HashMap<String,Integer[]>();



	//TODO ELIMINATE THIS ENTIRE TASK
	//1. Check Commands
	private TimerTask checkCommandTask(){
		TimerTask checkCommandTask = new TimerTask(){

			@Override
			public void run() {
				synchronized(this){
				try{
					Log.e("TECHVENTUS","RUNNING CHECK COMMAND TASK");
					SQLiteDatabase sql= openOrCreateDatabase("db",0,null);
					
					try{
						
						Cursor c =sql.rawQuery("SELECT command,id FROM COMMAND ORDER BY id ", null);
//					
//						if(c!=null)
						{
						   
						 // while( c.moveToNext())
						  {
						   
//								   Log.e("TECHVENTUS","COMMAND "+c.getString(0));
								   
//								   if( c.getString(0).equals("updateLoginCredentials")){
//									   
//									   sql.execSQL("DELETE FROM COMMAND WHERE id = '"+c.getString(1)+"';");
//									   updateLoginCredentials(sql);
//									  
//									  
//								   }
								   if(c.getString(0).equals("rectify")){
									   
								
									   sql.execSQL("DELETE FROM COMMAND WHERE id = '"+c.getString(1)+"';");
										
									   Log.e("TECHVENTUSPHONE","After RECTIFY COMMAND list Phone and Location");
//										SQLHelper.listPhoneAndLocationPhoneEnable(sql);
										Log.e("TECHVENTUSPHONE","Rectify Locations");
										rectifyLocationPhones();
										Log.e("TECHVENTUSPHONE","Now list Phone and Location after Rectify");
//										SQLHelper.listPhoneAndLocationPhoneEnable(sql);
								   }
								   
								   if(c.getString(0).equals("enforce")){
									   sql.execSQL("DELETE FROM COMMAND WHERE id = '"+c.getString(1)+"';");
									   Log.e("TECHVENTUSPHONE","ENFORCING VOICE PREFEreNCES");
									   enforceVoicePreferences();
								   }
								   
								   if(c.getString(0).equals("reset")){
									   sql.execSQL("DELETE FROM COMMAND WHERE id = '"+c.getString(1)+"';");
									   reset();
									  
//									  break;
								   }
								   
//								   if(c.getString(0).contains("call")){
//									   String callString = c.getString(0);
//									   sql.execSQL("DELETE FROM COMMAND WHERE id = '"+c.getString(1)+"';");
//									   String[] split = callString.split("bnmkj");
//									   String origin = split[1];
//									   String dest = split[2];
//									  
//									   
//									   AllSettings allsettings = voice.getSettings(false);
//									   
//									   for(Phone phone:allsettings.getPhones()){
//										   System.out.println(phone.getName()+" "+origin+" "+dest);
//										  
//										   if(phone.getName().equals(origin)){
//											   System.out.println("CALLING "+phone.getPhoneNumber()+" "+dest);
//											   voice.call(phone.getPhoneNumber(), dest, String.valueOf(phone.getType()));
//											   break;
//										   }
//									   }
//									   
//									   
//									   
//								   }
								   
								   
								   
//  if(c.getString(0).equals("locationUpdate")){
//	   sql.execSQL("DELETE FROM COMMAND WHERE id = '"+c.getString(1)+"';");
//	   locationUpdate();
									 
//  }
//   if(c.getString(0).equals("rectify")){
//	   sql.execSQL("DELETE FROM COMMAND WHERE id = '"+c.getString(1)+"';");
									 
//	  rectifyLocationPhones();
									 
//  }
							   }
						   }
//						else{					   
//							   Log.e("TECHVENTUS","NOT FIRST");					   
//						   }
						 c.close();
					}catch(Exception e){
						e.printStackTrace();
					}
					
					sql.close();
				}catch(Exception yt){
					 Log.e("TECHVENTUS","Exception in Command Timer Task");
					 yt.printStackTrace();
				}
			}
			
		}
			
		};
		return checkCommandTask;
		
	}
	

	
	private TimerTask checkPhoneEnabledTask(){
		TimerTask ret = new TimerTask(){
			@Override
			public void run() {
				synchronized(this){
					try{
						if(voice!= null && voice.isLoggedIn()/*status.isVoiceConnected()/*voice.isLoggedIn()*/){
							try {
								AllSettings allSettings = voice.getSettings(true);
								Phone[] phoneAr = allSettings.getPhones();
								
								Integer[] prefints =locationPhonePrefs.get(status.currentLocationString);
								if(prefints!=null){
									if(prefints.length==phoneAr.length/*settings.phoneList.size()*/){
										outer: for(int i=0;i<phoneAr.length/*settings.phoneList.size()*/;i++){
											inner: for(Phone phone:phoneAr){
										//		if(phone.getName().equals(settings.phoneList.get(i))){
													if(prefints[i]==1 && allSettings.isPhoneDisabled(phone.getId())){
														Log.e("TECHVENTUS","enforcing phones since not enabled, checkPhoneEnabledTask "+phone.getName());
														enforceVoicePreferences();
														break outer;
													}else if(prefints[i]==-1 && !allSettings.isPhoneDisabled(phone.getId())){
														Log.e("TECHVENTUS","enforcing phones since not disabled, checkPhoneEnabledTask "+phone.getName());
														enforceVoicePreferences();
														break outer;
													}
													break inner;
											//	}
											}
										}
									}else{
										Log.e("TECHVENTUS","ERROR IN SIZES checkPhoneEnabledTask");
									}
								}else{
									Log.e("TECHVENTUS","PrefInts Null checkPhoneEnabledTask");
									
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}catch(Exception y){
								y.printStackTrace();
							}
						}else{
							Log.e("TECHVENTUS","checkCommandPhoneEnable - voice object is null or not connected");
							
							
						}
					}catch(Exception h){
						Log.e("TECHVENTUS","Exception! checkCommandPhoneEnable -  voice object is null or not connected");
						
						h.printStackTrace();
					}
			}//synch
			}//run
		};
		return ret;
	}
	
//	private TimerTask checkGoogleAuthKeyTask(){
//		TimerTask checkGoogleAuthKeyTask =  new TimerTask(){
//			@Override
//			public void run() {
//				synchronized(this){
//				try{
//					Log.e("TECHVENTUS","RUNNING GOOG AUTH KEY  TASK");
//					SQLiteDatabase sql= openOrCreateDatabase("db",0,null);
//					try{
//						if(voice!=null){
//							if(voice.isLoggedIn()){
//								status.setVoiceConnected(true);
//							
//								//sql.execSQL(SQLHelper.updateGoogleConnectedStatement);
//							}else{
//								status.setVoiceConnected(false);
//								//Log In Again Based On Stored Credentials
//								
//								try{
//								
//									Cursor c = sql.rawQuery(SQLHelper.selectStoredGoogleCredentialsStatement, null);
//									if(c!=null){
//										if(c.moveToNext()){
//											//loginName = 
//											settings.setLoginName(c.getString(0));
//										 	//password = c.getString(1);
//										 	settings.setPassword(c.getString(1));
//										}
//										c.close();
//									}
//									if(settings.getLoginName()/*loginName*/!=null && settings.getPassword()/*password*/!=null){
//										try{
//											voice = new Voice(settings.getLoginName(),settings.getPassword());
//											if(voice.isLoggedIn()){
//												
////												sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
//												status.setVoiceConnected(true);
//												//sql.execSQL(SQLHelper.insertGoogleConnectedStatement);
//												Log.e("TECHVENTUSPHONE","Now list Phone and Location");
//												SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//												Log.e("TECHVENTUSPHONE","Rectify Locations");
//												rectifyLocationPhones();
//												Log.e("TECHVENTUSPHONE","Now list Phone and Location after Rectify");
//												SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//												
//											}else{
//												status.setVoiceConnected(false);
////												sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
////												sql.execSQL(SQLHelper.insertNoGoogleConnectionStatement);
//												
//											}
//										}catch(Exception voiceException){
//											voiceException.printStackTrace();
////											sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
//											status.setVoiceConnected(false);
////											sql.execSQL(SQLHelper.insertNoGoogleConnectionStatement);
//										}
//									}else{
////										sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
//										status.setVoiceConnected(false);
////										sql.execSQL(SQLHelper.insertNoGoogleConnectionStatement);
//									}
//	
//								}catch(Exception e){
//									
//									e.printStackTrace();
//								}
//								
//								
//								//If Login Failure Set Status to No Connection or Invalid Google Credentials
//								//sql.rawQuery(sql, null);
//								//	String updateString = "UPDATE STATUS SET values = 'disconnected' WHERE key = 'google';";
//							}
//		
//						}else{
//							if(settings.getLoginName()!=null && settings.getPassword()!=null){
//								try{
//									voice = new Voice(settings.getLoginName(), settings.getPassword());
//									if(voice.isLoggedIn()){
//										status.setVoiceConnected(true);
////										sql.execSQL(SQLHelper.updateGoogleConnectedStatement);
//										Log.e("TECHVENTUSPHONE","Now list Phone and Location");
//										SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//										Log.e("TECHVENTUSPHONE","Rectify Locations");
//										rectifyLocationPhones();
//										Log.e("TECHVENTUSPHONE","Now list Phone and Location after Rectify");
//										SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//									}else{
//										status.setVoiceConnected(false);
////										sql.execSQL(SQLHelper.updateNoGoogleConnectionStatement);
//										
//									}
//								}catch(Exception voiceException){
//									voiceException.printStackTrace();
//									status.setVoiceConnected(false);
////									sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
////									sql.execSQL(SQLHelper.insertNoGoogleConnectionStatement);
//								}
//							}else{
//								try{
//									//loginName = null;
//									//password = null;
//									settings.clearCredentials();
//									Cursor c = sql.rawQuery(SQLHelper.selectStoredGoogleCredentialsStatement, null);
//									if(c!=null){
//										
//										if(c.moveToNext()){
//											settings.setLoginName(c.getString(0));
//											settings.setPassword(c.getString(1));
//											//loginName = c.getString(0);
//										 	//password = c.getString(1);
//										}
//										c.close();
//									}
//									if(settings.getLoginName()!=null && settings.getPassword()!=null){
//										try{
//											voice = new Voice(settings.getLoginName(),settings.getPassword());
//											if(voice.isLoggedIn()){
//												status.setVoiceConnected(true);
////												sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
////												sql.execSQL(SQLHelper.insertGoogleConnectedStatement);
//												Log.e("TECHVENTUSPHONE","Now list Phone and Location");
//												SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//												Log.e("TECHVENTUSPHONE","Rectify Locations");
//												rectifyLocationPhones();
//												Log.e("TECHVENTUSPHONE","Now list Phone and Location after Rectify");
//												SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//											}else{
//												status.setVoiceConnected(false);
////												sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
////												sql.execSQL(SQLHelper.insertNoGoogleConnectionStatement);
//												
//											}
//										}catch(Exception voiceException){
//											voiceException.printStackTrace();
//											status.setVoiceConnected(false);
////											sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
////											sql.execSQL(SQLHelper.insertNoGoogleConnectionStatement);
//										}
//									}else{
//										status.setVoiceConnected(false);
////										sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
////										sql.execSQL(SQLHelper.insertNoGoogleConnectionStatement);
//									}
//	
//								}catch(Exception e){
//									
//									e.printStackTrace();
//								}
//							}
//						}
//	
//					}catch(Exception o){
//						o.printStackTrace();
//						try{
//							voice = new Voice(settings.getLoginName(),settings.getPassword());
//							if(voice.isLoggedIn()){
////								sql.execSQL(SQLHelper.updateGoogleConnectedStatement);
//								status.setVoiceConnected(true);
//								Log.e("TECHVENTUSPHONE","LOGIN Successful OCCURED AFTeR Error CATCH.");
//								Log.e("TECHVENTUSPHONE","Test Google Voice Listing.");
//								SQLHelper.listVoicePhoneList(voice);
//								Log.e("TECHVENTUSPHONE","Now list Phone and Location");
//								SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//								Log.e("TECHVENTUSPHONE","Rectify Locations");
//								rectifyLocationPhones();
//								Log.e("TECHVENTUSPHONE","Now list Phone and Location after Rectify");
//								SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//							}else{
////								sql.execSQL(SQLHelper.updateNoGoogleConnectionStatement);
//								status.setVoiceConnected(false);
//							}
//						}catch(Exception voiceException){
//							voiceException.printStackTrace();
//							status.setVoiceConnected(false);
////							sql.execSQL(SQLHelper.deleteGoogleConnectivityStatement);
////							sql.execSQL(SQLHelper.insertNoGoogleConnectionStatement);
//						}
//					}
//					sql.close();
//				}catch(Exception tr){
//					Log.e("TECHVENTUS","Exception in Auth Key Task");
//					tr.printStackTrace();
//				}
//				}
//			}
//		};
//		return checkGoogleAuthKeyTask;
//	}
//	
	@Override
	public IBinder onBind(Intent arg0) {
		
		return mBinder;
	}

	private final GVLServiceInterface.Stub mBinder = new GVLServiceInterface.Stub() {

//		@Override
//		public int getFrequency() throws RemoteException {
//			// TODO Auto-generated method stub
//			return 0;
//		}

		@Override
		public int[] getCurrentCoordinatesE6() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}



		@Override
		public void reset() throws RemoteException {

			
		}

//		@Override
//		public void setFrequency(int seconds) throws RemoteException {
//			
//		}

//		@Override
//		public void deleteLocation(String locationName) throws RemoteException {
//			try{
//			synchronized(LocationService.this){
//				SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
//				
//				sql.execSQL("DELETE FROM LOCATIONS WHERE locationName = '"+locationName+"';") ;
//				sql.execSQL("DELETE FROM LOCATIONPHONEENABLE WHERE locationName = '"+locationName+"';");
//				rectifyLocationPhones();
//				//sql.execSQL("INSERT INTO COMMAND (command) VALUES ('rectify');");
//				//rectify
//				sql.close();
//			}
//			}catch(Exception e){
//				Log.e(TAG, "ERROR IN DELETE LOCATION");
//				e.printStackTrace();
//			}
//		}

//		@Override
//		public void updateLoginCredentials(String login, String password)
//				throws RemoteException {
//			synchronized(LocationService.this){
//	
//				
//				Log.e("TECHVENTUS","SAVING CREDENTIALS TO DATABASE");
//				SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
//				try{
//					sql.execSQL(SQLHelper.createGoogle);
//					sql.execSQL("DELETE FROM GOOGLE;");
//					
//					Log.e("TECHVENTUS","INSERTING CREDENTIALS");
//					
//					if(login.contains("@")){
//						
//						settings.setLoginName(login);
//						settings.setPassword(password);
//						
//						sql.execSQL("INSERT INTO GOOGLE (loginName, password) VALUES ('"+login+"','"+password+"');");
//
//						//sql.execSQL("INSERT INTO COMMAND (command) VALUES ('updateLoginCredentials');");
//						
//					} else{
//						
//						settings.setLoginName(login.replace("@gmail.com","").replace("@googlemail.com", "")+"@gmail.com");
//						settings.setPassword(password);
//						
//						
//						sql.execSQL("INSERT INTO GOOGLE (loginName, password) VALUES ('"+login.replace("@gmail.com","").replace("@googlemail.com", "")+"@gmail.com','"+password+"');");
//						//sql.execSQL("INSERT INTO COMMAND (command) VALUES ('updateLoginCredentials');");
//					}
//					Log.e(TAG, "LOGIN CREDENTIAL INSERTED FOR "+settings.getLoginName());
//					//LocationService.this.updateLoginCredentials(sql);
//					System.out.println("login credentials inserted + "+login);
//				}catch(Exception u){
//					u.printStackTrace();
//				}finally{
//					sql.close();
//				}
//
//			
//			}
//			
//			LocationService.this.reconnectToVoice();
//			
//		}

		@Override
		public String getCurrentLocationString() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

//		@Override
//		public String getPowerMode() throws RemoteException {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public void setPowerMode(String powerMode) throws RemoteException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public String getAccuracy() throws RemoteException {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public void setAccuracy(String accuracy) throws RemoteException {
//			// TODO Auto-generated method stub
//			
//		}

//		@Override
//		public boolean isVoiceConnected() throws RemoteException {
//			// TODO Auto-generated method stub
//			return voice.isLoggedIn();
////			return status.isVoiceConnected();
//		}

//		@Override
//		public void dial(String phone, String number) throws RemoteException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public String[] getPhones() throws RemoteException {
//			// TODO Auto-generated method stub
//			return null;
//		}

//		@Override
//		public boolean startupComplete() throws RemoteException {
//			// TODO Auto-generated method stub
//			return false;
//		}

//		@Override
//		public boolean hasCredentials() throws RemoteException {
//			// TODO Auto-generated method stub
//			return false;
//		}

//		@Override
//		public String[] getLocations() throws RemoteException {
//			// TODO Auto-generated method stub
//			return null;
//		}

//		@Override
//		public void delete(String location) throws RemoteException {
//			// TODO Auto-generated method stub
//			
//		}



//		@Override
//		public void updateVoice() throws RemoteException {
//			// TODO Auto-generated method stub
//			
//		}



		@Override
		public void restart() throws RemoteException {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void update() throws RemoteException {
			// TODO Auto-generated method stub
			
		}




//		public String[] listProviders() throws RemoteException {
//			// TODO Auto-generated method stub
//			return null;
//		}
	    
	    
	};
	
	
	
	
	
	void startup(){
		try{
//			 settings = Settings.getSettings();
//			 status = Status.getStatus();
		 // timer.schedule(locationManagerTask, 10,180000);
		Log.e("TECHVENTUS","SERVICE START UP");  
		
		synchronized(this){
		 SQLiteDatabase  sql = openOrCreateDatabase("db",0,null);


		 SQLHelper.establishTestDBSettings(sql);
		 
//		 SQLHelper.createElsewhere(sql, null);
		 
		 SQLHelper.reportTable(sql, "SELECT * FROM LOCATIONS;", 4);
		
		 Cursor c = sql.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;", null);
		  
		 try{
	           if(c!=null){
				   c.moveToNext();
				   if(c.isFirst()){
					   do{
						   Log.e("TECHVENTUS","RESULT "+c.getString(0));
					   }while(c.moveToNext());
				   }else{
					   Log.e("TECHVENTUS","NOT FIRST");
					   
				   }
			   }
			  
			  
		 }catch(Exception h){
			 h.printStackTrace();
		 }finally{
			 c.close();
		 }
		  


		   updateLoginCredentials(sql);
		   sql.close();
		}
		  // init the service here
		 // _startService();

		 // if (MAIN_ACTIVITY != null) AppUtils.showToastShort(MAIN_ACTIVITY, "MyService started");
		   
		   
			  try{

					locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					
					
					  Criteria criteria= new Criteria();
						criteria.setCostAllowed(false);
						criteria.setPowerRequirement(Criteria.POWER_LOW);
					
					String provider = locationManager.getBestProvider(criteria, true);
					
					locationManager.requestLocationUpdates(provider, 180000,
							0, geoHandle);
				  
				  
					//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 90000,
					//		50, new GeoUpdateHandler());
					
			  }catch(Exception e){
				  Log.e("Techventus","No Location Service Available");
				  
			  }
			  
			  if(timerList.size()>0){
				 for(Timer timer:timerList){
					 timer.cancel();
					 timerList.remove(timer);
				 }
			  }
			  Timer timer = new Timer();
			 

			  Log.e("TECHVENTUS","Scheduling Timers");
//			  timer.schedule(checkGoogleAuthKeyTask(), 30000,1800000);
//			  timer.schedule(statuspingTask(),29000, 100000);
			 // timer.schedule(getLocationUpdate, 60000, 90000);
	//		  timer.schedule(locationUpdateTask(), 60000, 95000);
			  timer.schedule(checkCommandTask(), 15000,15000);
			  timer.schedule(checkPhoneEnabledTask(), 60000,300000);
			  timerList.add(timer);
			  Log.e("TECHVENTUS","Done Scheduling Timers");
		}catch(Exception ty){
			ty.printStackTrace();
		}
	}
//	
//	protected void reconnectToVoice() {
//		// TODO Auto-generated method stub
//		
//		
//		try{
//			//go with current credentials
//			if(settings.hasCredentials){
//				voice = new Voice(settings.getLoginName(),settings.getPassword());
//			//set the credentials from the database and try again
//			}else{
//				settings.setVoiceSettingsFromDB(this);
//				if(settings.hasCredentials){
//					voice = new Voice(settings.getLoginName(),settings.getPassword());
//				}else{
//					Log.e(TAG, "NO CREDENTIALS ARE AVAILABLE FOR GOOGLE VOICE CONNECTION");
//					Toast.makeText(this, "GVL: Reconnect to Voice ERROR\n\r<br>NO CREDENTIALS", Toast.LENGTH_LONG);
//					//TODO CONSIDER LAUNCHING CREDENTIALS ACTIVITY OR AT LEAST RAISING A TOAST
//					voice =null;
//				}
//				//TODO = What get credentials from database and set them to voice object (seperate method)
//				
//			}
//			status.setVoiceConnected(voice.isLoggedIn());
//		}catch(Exception e){
//			Log.e(TAG, "EXCEPTION IN RECONNECT TO VOICE");
//			e.printStackTrace();
//		}
//	}
//	
	




	@Override
	public void onLowMemory(){
		// Log.e("TECHVENTUS", "On Low Memory Call");
		//this.setForeground(true);
	}
	
	@Override
	public void onDestroy(){
		// Log.e("DESTROY", "SERVICE DESTROY CALL");
		 
		//this.setForeground(true);
		//this.stopSelf();
		//TODO
		locationManager.removeUpdates(geoHandle);
		for(Timer timer:timerList){
			timer.cancel();
		}
		//Intent i = new Intent(this,LocationService.class);
		//startService(i);
		
		this.stopSelf();
	}
	
	@Override public void onCreate() {
		  super.onCreate();
		  Log.e("TECHVENTUS", "Starting OnCreate Location Service");
		//  this.setForeground(true);

		   startup();
		}
	
	/*
	@Override
	public int onStartCommand(Intent intent,int flags, int startId){
		
		Log.e("TECHVENTUS", "Start Command Location Service");
		//intent.getStringExtra(name)
		
		return 1;
	}
	*/
/*
	@Override public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
	}

	public GeoPoint getCurrentGeoPoint(){
//sdf
		GeoPoint ret = locationGeoPoint;
		
		try{
		
			if(locationManager == null){
				Criteria criteria= new Criteria();
				criteria.setCostAllowed(false);
				criteria.setPowerRequirement(Criteria.POWER_LOW);
				
				
				
				Log.e("TECHVENTUS", "Location WAS Null");
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				
				String provider = locationManager.getBestProvider(criteria, true);
				
				locationManager.requestLocationUpdates(provider, 30000,
						50, new GeoUpdateHandler());
				Log.e("TECHVENTUS", "Location NOLONGER Null");
			}else{
				Log.e("TECHVENTUS", "Location Manager Not Null");
			}
			Location l =locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(l!=null){
				int lat = (int) (l.getLatitude() * 1E6);
				int lng = (int) (l.getLongitude() * 1E6);
				//System.out.println(lat+" "+lng);
				 ret = new GeoPoint(lat, lng);
			}
		}catch(Exception r){
			Log.e("TECHVENTUS", "Location exception"+r.getLocalizedMessage());
			r.printStackTrace();
		}
		return ret;
	}
		*/

	
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
	 
	 
	 private void enforceVoicePreferences(){
		 synchronized(this){
		 try{
			 Log.e("TECHVENTUS","ENFORCING PREFERENCES");
			 if(voice!=null && voice.isLoggedIn()){
				 Log.e("TECHVENTUS","ENFORCING - Logged In Not Null");
				 SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
				 
				 Cursor c =sql.rawQuery("SELECT phoneName, phoneEnable FROM LOCATIONPHONEENABLE WHERE locationName = '"+status.currentLocationString+"'", null);
				 
				 if(c!=null){
					 while(c.moveToNext()){
						 for(Phone phone:voice.getSettings(false).getPhones()){
							 if(phone.getName().equals(c.getString(0))){
								 if(c.getInt(1)==1){
									 Log.e("TECHVENTUS","ENABLING PHONE "+phone.getName()+" at location: "+status.currentLocationString);
									 
									 String res = voice.phoneEnable(phone.getId());
									 Log.e("TECHVENTUS","RESULT "+res);
								 }else if(c.getInt(1)==-1){
									 Log.e("TECHVENTUS","DISABLING PHONE "+phone.getName()+" at location: "+status.currentLocationString);
									 String res =voice.phoneDisable(phone.getId());
									 Log.e("TECHVENTUS","RESULT "+res);
								 }
							 }
						 }
					 }
					 c.close();
				 }
				 sql.close();
				 
				 
				 
				 /*
//				Integer[] prefs = locationPhonePrefs.get(currentLocationString);
				int i=0;
				 Log.e("TECHVENTUS","ENFORCING - PhoneList is Null? "+(voice.phoneList==null));
				 for(Phone phone:voice.phoneList){
					 Log.e("TECHVENTUS","Enforce Phone: "+phone.name+" Location: "+currentLocationString);
					 if(prefs[i].intValue()==1){
						 voice.phoneEnable(Integer.parseInt(phone.id));
						 Log.e("TECHVENTUS","Enabling Phone: "+phone.name);
					 }if(prefs[i].intValue()==-1){
						 voice.phoneDisable(Integer.parseInt(phone.id));
						 Log.e("TECHVENTUS","Disabling Phone: "+phone.name);
					 }
					 i++;
				 }
				 */
				 
				 
				 
				 
			 }else{
				 Log.e("TECHVENTUS","VOICE IS NULL OR NOT LOGGED IN");
				 SQLiteDatabase sqlite = openOrCreateDatabase("db",0,null);
				 updateLoginCredentials(sqlite);
				 sqlite.close();
			 }
		 }catch(Exception u){
			 Log.e("TECHVENTUS","Exception in ENFORCEPHONEPREFERENCES");
			 u.printStackTrace();
		 }
		 }
	 }
	 
	 
	 
	 private void setLocation(int lat , int lon, String loc){
		 synchronized(this){
		 try{
			 Log.e("TECHVENTUS","SET LOCATION");
			 
			 //Adjust voice Location

			 status.currentLocationString = loc;

			status.locationGeoPoint = new GeoPoint(lat,lon);
			 
			 SQLiteDatabase  sql = openOrCreateDatabase("db",0,null);
			 
			 try{

//				 String deleteStringloc = "DELETE FROM STATUS WHERE key = 'location';";
//				 
//				 String deleteStringlat = "DELETE FROM STATUS WHERE  key = 'latitude';";
//				 
//				 String deleteStringlon = "DELETE FROM STATUS WHERE  key = 'longitude';";
//				 
//				 String insertStringloc = "INSERT INTO STATUS (key , status) VALUES ('location','"+loc+"') ;";
//				 
//				 String insertStringlat = "INSERT INTO STATUS (key , status) VALUES  ('latitude','"+lat+"') ;";
//
//				 String insertStringlon = "INSERT INTO STATUS (key , status) VALUES ('longitude','"+lon+"');";
				 
//				 sql.execSQL(deleteStringloc);
//				 sql.execSQL(deleteStringlat);
//				 sql.execSQL(deleteStringlon);
//				 
//				 sql.execSQL(insertStringloc);
//				 sql.execSQL(insertStringlat);
//				 sql.execSQL(insertStringlon);

			 }catch(Exception y){
				 y.printStackTrace();
			 }
			 
			 sql.close();
			 
		 }catch(Exception u){
			 u.printStackTrace();
		 }
		 }
	 }
	 

	 public synchronized void updateLoginCredentials(SQLiteDatabase sql) {
		 
		 //Get Google Credentials from Database
		 //Create a New Voice Object
		 //Check if Logged In
		 //SET DATABASE WITH CONNECTION STATUS

		 Cursor c = sql.rawQuery("select loginName, password from GOOGLE;",
				 null);
		 try {
			 // sql= openOrCreateDatabase("db",0,null);


			 if(c!=null){
				 c.moveToNext();
				 if(c.isFirst()){


//					 settings.setLoginName(c.getString(0));
//					 settings.setPassword( c.getString(1));
//					 System.out.println("loginName , password" + settings.getLoginName() );
//					 voice = new Voice(settings.getLoginName(),settings.getPassword() );



					 if(voice==null){
						 Log.e(TAG, "GOOGLE VOICE OBJECT IS NULL");
						 Toast.makeText(this, "Location Service - Null Google Voice Object", Toast.LENGTH_LONG);
					 }else{
//							 status.setVoiceConnected(voice.isLoggedIn());
//							 if(voice.isLoggedIn()){
//							// System.out
//							// .println("IN THE SERVICE Auth token = Present but not " + voice.);
//								 System.out
//									 .println("Logged into voice");
//								 status.setVoiceConnected(true);
//								 sql.execSQL(SQLHelper.updateGoogleConnectedStatement);
//						
//							 }else{
//								 status.setVoiceConnected(false);
		//						 sql.execSQL(SQLHelper.updateNoGoogleConnectionStatement);
		//					
		//					 }
							 Phone[] phoneList = voice.getSettings(false).getPhones();
							 if(phoneList.length>0){
								 sql.execSQL("DELETE FROM PHONE;");
								 
								 for (Phone p : phoneList){
									 Log.e("TECHVENTUSPHONE","Adding Phone"+p.getName());
									 sql.execSQL("INSERT INTO PHONE (phoneName) VALUES ('"+p.getName()+"');");
									 System.out.println("Adding Phone"+p.getName());
								 }
									Log.e("TECHVENTUSPHONE","Now list Phone and Location");
//									SQLHelper.listPhoneAndLocationPhoneEnable(sql);
									Log.e("TECHVENTUSPHONE","Rectify Locations");
									rectifyLocationPhones();
									Log.e("TECHVENTUSPHONE","Now list Phone and Location after Rectify");
//									SQLHelper.listPhoneAndLocationPhoneEnable(sql);
							 }
						
				 }
					 c.close();
				 }
			 }else{
//				 status.setVoiceConnected(false);
//				 SQLHelper.exec(sql, "UPDATE STATUS SET status = 'No Connexion' WHERE key = 'google';");
			 }

			 // sql.close();
		 } catch (Exception r) {
//			 status.setVoiceConnected(false);
//			 SQLHelper.exec(sql, "UPDATE STATUS SET status = 'No Connexion' WHERE key = 'google';");
			 r.printStackTrace();
		 }finally{
			 c.close();
		 }

	 }

	 
	 
	 synchronized void rectifyLocationPhones(){
		 Log.e("TECHVENTUS","rectifyLocationPhones");
		 SQLiteDatabase sql= openOrCreateDatabase("db",0,null);
		
		 
		 //GET LOCATION LIST
		 
		 
		 try{
			 Cursor c = sql.rawQuery(SQLHelper.selectLocationsStatement,
					 null);
			 if(c!=null){
				 
//				 settings.locationMap.clear();
//				 settings.locationRadiusMap.clear();
//				 settings.locationEnabledMap.clear();
//				 settings.locationList.clear();
				 
				 while(c.moveToNext()){
					 
					 	GeoPoint g = new GeoPoint(c.getInt(1), c.getInt(2));
					 
//					 	settings.locationMap.put(c.getString(0), g);
//						
//					 	settings.locationRadiusMap.put(c.getString(0), Double.valueOf(c.getInt(3)));
						
//					 	settings.locationEnabledMap.put(c.getString(0), Boolean.parseBoolean(c.getString(4)));
						
//					 	settings.locationList.add( c.getString(0));
				 }
				 
				 c.close();
			 }
			 
		 }catch(Exception e){
			e.printStackTrace();
		 }
		 
		 
		 
		 //GET PHONE LIST
//		 settings.phoneList.clear();
		 try{
			 Cursor c = sql.rawQuery("SELECT DISTINCT phoneName FROM PHONE;",
					 null);
			 if(c!=null){
				 while(c.moveToNext()){
//					 settings.phoneList.add( c.getString(0));
				 }
				 c.close();
			 }
			
		 }catch(Exception e){
			e.printStackTrace();
		 }
		 
		 
		 //CLEAN PHONES FROM LOCATIONPHONEENABLE BY PHONENAME

		 try{
//				 if(settings.phoneList!=null && settings.phoneList.size()>0){
					 String deletePhoneString = "";
					 boolean first = true;
//					 for(@SuppressWarnings("unused") String phone:settings.phoneList){
//						 if(first){
//							 deletePhoneString += " phoneName != ?";
//							 first = false;
//						 }else{
//							 deletePhoneString+=" AND  phoneName != ?";
//						 }
//					 }
					 Log.e("TECHVENTUS","GREAT TRY LOCATIONPHONEENABLE DELETE");
//					int delrows =  sql.delete("LOCATIONPHONEENABLE", deletePhoneString,(String[])settings.phoneList.toArray(new String[settings.phoneList.size()]));
//					System.out.println("delrows "+delrows);
					// String finaldelps = "DELETE FROM LOCATIONPHONEENABLE "+deletePhoneString+");";
					 //Log.e("TECHVENTUS",finaldelps);
					//sql.execSQL(deletePhoneString);
			//	 }
			//QUERY EXCESS 
			Cursor c = sql.rawQuery("SELECT DISTINCT phoneName FROM LOCATIONPHONEENABLE;",null);
//			List<String> addList = new ArrayList<String>();
//			for(String p:settings.phoneList) addList.add(new String(p));
//			
//			 if(c!=null){
//				 while(c.moveToNext()){
//					 addList.remove(c.getString(0));
//				 }
//				 
//				 c.close();
//			 }
			
			 /*
			 //ADD EXTRA PHONES
			 if(addList.size()>0 && locationList.size()>0){
				// String ValuesString = "";
				 for(String phone:addList){
					 for(String loc:locationList){
						 sql.execSQL("INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('"+loc+"','"+phone+"',-2);");
						//ValuesString +=",  ";
					 }
				 }
				 
			 }
			 */
			 
//			 for(String loc:settings.locationList){
//				Cursor d =sql.rawQuery("SELECT * FROM LOCATIONPHONEENABLE WHERE locationName = '"+loc+"';",null);
//				 List<String> tmplist = new ArrayList<String>();
//				 Map<String,Integer> enableMap = new HashMap<String,Integer>();
////				 for(String phone:settings.phoneList){
////					tmplist.add(phone) ;
////				 }
//			//	 List<String> locPhones = new ArrayList<String>();
//				try{
//					if(d!=null){
//						while(d.moveToNext()){
//							enableMap.put(d.getString(1),d.getInt(2));
//							for(int i=0;i<tmplist.size();i++){
//								if(tmplist.get(i).equals(d.getString(1))){
//									tmplist.remove(i);
//									break;
//								}
//							}
//						}
//						}else{
//							Log.e("TECHVENTUSPHONE","c was null");
//						}
//					if(tmplist.size()>0){
//						Log.e("TECHVENTUSPHONE","NEED TO REDO for LOCAtION "+loc);
////						int delrows = sql.delete("LOCATIONPHONEENABLE", "locationName = ?", new String[] {loc});
////						Log.e("TECHVENTUSPHONE","DELETING ROWS in "+loc+":"+delrows);
////						for(String phone:settings.phoneList){
////							Integer k;
////							if((k=enableMap.get(phone))!=null){
////								sql.execSQL("INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('"+loc+"','"+phone+"',"+k.intValue()+");");
////							}else{
////								sql.execSQL("INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('"+loc+"','"+phone+"',-2);");
////							}
////						}
//					}else{
//						Log.e("TECHVENTUSPHONE","Looks OK for  "+loc+" in LOCATIONPHONEENABLE");
//					}
//					if(d!=null)
//						d.close();
//				}catch(Exception u){
//					u.printStackTrace();
//				}
//				 	 
//			 }//for
			 
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 
		 //CLEAN LOCATIONS FROM LOCATIONPHONEENABLE

		 try{
//			 if(settings.locationList!=null && settings.locationList.size()>0){
				 String deletelocString = "";
				 boolean first = true;
//				 for(String loc:settings.locationList){
//					 if(first){
//						 deletelocString+="WHERE locationName <> '"+loc+"'";
//						 first = false;
//					 }else{
//						 deletelocString+=" AND locationName <> '"+loc+"'";
//					 }
//				 }
				 String finaldelstring = "DELETE FROM LOCATIONPHONEENABLE "+deletelocString+";";
				 System.out.println(finaldelstring);
				 sql.execSQL(finaldelstring);
//			 }
			 
				
				
				Cursor c = sql.rawQuery("SELECT DISTINCT locationName FROM LOCATIONPHONEENABLE;",null);
				List<String> addLocList = new ArrayList<String>();
//				for(String loc:settings.locationList) addLocList.add(new String(loc));
				
				 if(c!=null){
					 while(c.moveToNext()){
						 addLocList.remove(c.getString(0));
					 }
					 c.close();
				 }
				
				 
//				 if(addLocList.size()>0 && settings.phoneList.size()>0){
//					// String ValuesString = "";
//					 for(String loc:addLocList){
//						 for(String phone:settings.phoneList){
//							 sql.execSQL("INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('"+loc+"','"+phone+"',-2);");
//							//ValuesString +=", ('"+loc+"','"+phone+"',-2) ";
//						 }
//					 }
//
//				 }
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			 
			 
			 
			 
//
//			 try{
//					
//				 locationPhonePrefs.clear();
//				 if(settings.locationList!=null && settings.locationList.size()>0){
//					 for(String loc:settings.locationList){
//							Cursor c = sql.rawQuery("SELECT locationName,phoneName,phoneEnable FROM LOCATIONPHONEENABLE WHERE locationName = '"+loc+"';",null);
//							List<Integer> phoneEnableList = new ArrayList<Integer>();
//							 if(c!=null){
//								 while(c.moveToNext()){
//										phoneEnableList.add(c.getInt(2));
//								 }
//								 c.close();
//							 }
//							 
//							 if(phoneEnableList.size()>0){
//								Integer[] prefAr = new Integer[phoneEnableList.size()];
//								for(int i=0;i<prefAr.length;i++){
//									prefAr[i] = phoneEnableList.get(i);
//								}
//								locationPhonePrefs.put(loc, prefAr);
//							 }
//					 	}
//				 }
//			 }catch(Exception e){
//				 e.printStackTrace();
//			 }
			 
			 
			 
		 
		 sql.close();
		 
	 }
	 
	 synchronized void reset(){
//		 SQLiteDatabase db= openOrCreateDatabase("db",0,null);
		 
			//private Timer timer = new Timer();
		// locationUpdateTask.cancel();
		// checkCommandTask.cancel();
		// checkGoogleAuthKeyTask.cancel();
		
		 
		  if(timerList.size()>0){
				 for(Timer timer:timerList){
					 timer.cancel();
					 timerList.remove(timer);
				 }
		  }
		  
		  
		 //timer.cancel();
			
		  //timer = new Timer();
		 
			status.locationGeoPoint = new GeoPoint(0,0);
			
			 status.currentLocationString = "Unknown";
			
//			settings.locationMap = new HashMap<String,GeoPoint>();
//			
//			 settings.locationRadiusMap = new HashMap<String,Double>();
//			
//			 settings.locationEnabledMap = new HashMap<String,Boolean>();
			
			locationPhonePrefs = new HashMap<String,Integer[]>();
			
//			settings.phoneList.clear();
//			settings.phoneList = new ArrayList<String>();
//			
//			settings.locationList = new ArrayList<String>();
			
//			settings.clearCredentials();
			//settings.getLoginName()loginName = null;
			//password = null;
			
			voice = null;
		 
		 
//	    	SQLHelper.exec(db,SQLHelper.dropLocations );
//	    	SQLHelper.exec(db,SQLHelper.dropGoogle );
//	    	SQLHelper.exec(db,SQLHelper.dropPhone );
//	    	SQLHelper.exec(db,SQLHelper.dropLocationPhoneEnable );
//	    	SQLHelper.exec(db,SQLHelper.dropCommand );
//	    	SQLHelper.exec(db,SQLHelper.dropStatus );
	    //	SQLHelper.exec(db,SQLHelper.dropSettings );
	      
	    	
	    	//SQLHelper.exec(db,SQLHelper.createLocations );
	    	//SQLHelper.exec(db,SQLHelper.createLocationPhoneEnable );
	    	//SQLHelper.exec(db,SQLHelper.createPhone );
//	    	SQLHelper.exec(db,SQLHelper.createGoogle );
	    	//SQLHelper.exec(db,SQLHelper.createCommand );
	    	//SQLHelper.exec(db,SQLHelper.createStatus );
	    	//SQLHelper.exec(db,SQLHelper.createSettings );
	    	//Log.e("TECHVENTUS", "INSERT ELSEWHERE");
	    	//SQLHelper.exec(db, SQLHelper.insertElsewhereLocation);
	    	
	    	//SQLHelper.exec(db, SQLHelper.initStatus);

//	    	db.close();
		 
	    	startup();
	 }
	 
	 
	 //TODO - MAKE SURE NO BOUNCING BETWEEN NEARBY LOCATIONS - NEED TO BREAK
		public class GeoUpdateHandler implements LocationListener {

			@Override
			public void onLocationChanged(Location location) {
				String locOrig = status.currentLocationString;
				Log.e(TAG,"Geopoint Update Handler - updating location GeoPoint from "+location.getProvider());
				Log.e(TAG,"Accuracy"+location.getAccuracy());
				int lat = (int) (location.getLatitude() * 1E6);
				int lng = (int) (location.getLongitude() * 1E6);
				
				status.locationGeoPoint = new GeoPoint(lat, lng);
				setLocation(lat,lng,status.currentLocationString);
				boolean action = false;
//				for(String loc:settings.locationMap.keySet()){
//					if(loc.equals("Elsewhere"))
//						continue;
//					if(distInMetres(settings.locationMap.get(loc),status.locationGeoPoint) < settings.locationRadiusMap.get(loc) + location.getAccuracy()){
//						Log.e("TECHVENTUS","CHANGING LOCATION from "+status.currentLocationString+" to "+loc);
//						action = true;
//						status.currentLocationString = loc;
//						Log.e("TECHVENTUS","CHANGING LOCATION to "+loc);
//						setLocation(status.locationGeoPoint.getLatitudeE6(),status.locationGeoPoint.getLongitudeE6(),loc);
//
//
//						break;
//					}
//				}
				if(!action && !status.currentLocationString.equals("Elsewhere")){
					Log.e("TECHVENTUS","CHANGING LOCATION to ELSEWHERE from "+status.currentLocationString);
					status.currentLocationString = "Elsewhere";
					setLocation(status.locationGeoPoint.getLatitudeE6(),status.locationGeoPoint.getLongitudeE6(),"Elsewhere");
					Log.e("TECHVENTUS","LOCATION GEOPOINT SET "+status.locationGeoPoint.getLatitudeE6());
				}
				
				 if(!locOrig.equals(status.currentLocationString)){
					 Log.e("TECHVENTUS","Enforcing Voice Preferences "+status.currentLocationString);
					 enforceVoicePreferences();
				 }
				//mapController.setCenter(point);
//				setContentView(mapView);
			}

			@Override
			public void onProviderDisabled(String provider) {
				
				
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
		}	 
		
		
		
		
		
}







//   try{ sql.execSQL("DROP TABLE locationdatabase");}catch(Exception e){e.printStackTrace();}
//  try{ sql.execSQL("DROP TABLE LOCATIONS;");}catch(Exception e){e.printStackTrace();}
// try{ sql.execSQL("DROP TABLE GOOGLE;");}catch(Exception e){e.printStackTrace();}
// try{ sql.execSQL("DROP TABLE LOCATIONS;");}catch(Exception e){e.printStackTrace();}

/* 
void locationChange(){
	 try{
		 SQLiteDatabase  sql = openOrCreateDatabase("db",0,null);
		 
	 }catch(Exception e){
		 Log.e("TECHVENTUS","Exception in Location Change Process");
		 e.printStackTrace();
	 }
}
*/

/*
public void updateLocationMap(){
	 SQLiteDatabase  sql = openOrCreateDatabase("db",0,null);
	 
	// SELECT locationName,locationLatitude,locationLongitude,locationRadius, enabled FROM LOCATIONS";
	
	 try{
		 
		 Cursor c =  sql.rawQuery(SQLHelper.selectLocationsStatement, null);
		 c.moveToNext();
	 
		 if(c!=null){
			   
			   c.moveToNext();
			   
			   if(c.isFirst()){
				   locationMap.clear();
				   locationRadiusMap.clear();
				   locationEnabledMap.clear();

				   
				   do{
					   
					   Log.e("TECHVENTUS","LOCATION "+c.getString(0));

					   
					   
					   
					  
					   
					//	Map<String,GeoPoint> locationMap = new HashMap<String,GeoPoint>();
						
						//Map<String,Double> locationRadiusMap = new HashMap<String,Double>();
					   

						
						//Map<String,Boolean> locationEnabledMap = new HashMap<String,Boolean>();
						
						//Map<String,Integer[]> locationPhonePrefs = new HashMap<String,Integer[]>();
					   
					   
				   }while(c.moveToNext());
				   
			   }else{
				   
				   Log.e("TECHVENTUS","NOT FIRST");
				   
			   }
		}
		
		
	 
	 }catch(Exception e){
		 e.printStackTrace();
	 }
	 sql.close();
}
	// public void update(){
		 
	//		SharedPreferences settings = getSharedPreferences("Locations", 0);
		 
	 //}
	 
*/



//SQLiteDatabase sql ;
/*
private TimerTask locationUpdateTask(){
	 TimerTask locationUpdateTask = new TimerTask(){
		@Override
		public void run() {
			try{
				locationUpdate();
			}catch(Exception e){
				Log.e("TECHVENTUS","EXCEPTION IN LOCATION UPDATE TIMERTASK");
				e.printStackTrace();
			}
		}
	};
	return locationUpdateTask;
}


private void locationUpdate(){
	try{

		Log.e("TECHVENTUS","HERRO LOATION UPDATE");
		GeoPoint cur = getCurrentGeoPoint();
		if(distInMetres(cur,locationGeoPoint)>200){
			locationGeoPoint = cur;
			
			boolean action = false;
			for(String s:locationMap.keySet()){
				if(s.equals("Elsewhere"))
					continue;
				if(distInMetres(locationMap.get(s),locationGeoPoint)<locationRadiusMap.get(s)){
					action = true;
					
					setLocation(locationGeoPoint.getLatitudeE6(),locationGeoPoint.getLongitudeE6(),s);
					
					break;
				}
			}
			if(!action){
				setLocation(locationGeoPoint.getLatitudeE6(),locationGeoPoint.getLongitudeE6(),"Elsewhere");
				Log.e("TECHVENTUS","LOCATION GEOPOINT SET "+locationGeoPoint.getLatitudeE6());
			}
		}
		
	//	boolean action = false;

	}catch(Exception e){
		Log.e("TECHVENTUS","Location Exception");
		if(locationGeoPoint==null){
			Log.e("TECHVENTUS","LocationGeoPoint was NULL");
		}else{
			Log.e("TECHVENTUS","LocationGeoPoint wasnt NULL");
		}
		e.printStackTrace();
	}
}
	*/




//
//private TimerTask statuspingTask(){
//	TimerTask statuspingTask = new TimerTask(){
//
//		@Override
//		public void run() {
//			synchronized(this){
//				try{
//					Date d = new Date();
//					String time = String.valueOf(d.getTime());
//					SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
//					sql.execSQL("DELETE FROM STATUS WHERE key = 'service';");
//					sql.execSQL("INSERT INTO STATUS (key,status) VALUES ('service','"+time+"');");
//					sql.close();
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}
//		}
//		
//	};
//	return statuspingTask;
//}
//




/**
 * Returns availability of a data connection
 * @param mContext
 *            Context of app
 * @return True is data connection is available , false otherwise
 */
//public static boolean isDataConnectionOn(Context mContext) {
//	ConnectivityManager connectionManager = (ConnectivityManager) mContext
//			.getSystemService(Context.CONNECTIVITY_SERVICE);
//	try {
//		if (connectionManager.getActiveNetworkInfo().isConnected()) {
//			Log.d("ConStatus", "Data Connection On");
//			return true;
//		} else {
//			Log.d("ConStatus", "Data Connection off");
//			return false;
//		}
//	} catch (NullPointerException e) {
//		// No Active Connection
//		Log.d("ConStatus", "No Active Connection");
//		return false;
//	}
//}