/*
 * 
 */
package com.techventus.locations;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.widget.Toast;

//import android.widget.Toast;


/**
 * The Class MainMenu.
 */
public class MainMenu extends Activity {
	
    /**
     * The current context.
     */
//    private Context mContext = this;
	
	String TAG = "TECHVENTUS - MAINMENU";
	
	/** The lat. */
	int lat = 0;
	
	/** The lon. */
	int lon = 0;
	
	/** The location string. */
	String locationString = "Unknown";
	
	/** The updated. */
	String updated = "--:--";
	
	Timer timer = new Timer();;


//	String PREFERENCENAME = "TECHVENTUS";
	//String RINGPREFENABLED = "RINGPREFENABLED";
	/** The version info. */
	PackageInfo versionInfo;
	
	/** The warning panel. */
	LinearLayout warningPanel;
	
    /** The EUL a_ prefix. */
    private String EULA_PREFIX = "eula_";
    
    /** The m activity. */
    private Activity mActivity = this;
     
     /** The preferences. */
     SharedPreferences preferences ;
     
     
     /** The INTERVAL. */
     long INTERVAL = 5000;
     
 	
 	
 	/** The gps text view. */
	 TextView gpsTextView  ;
 	
	 /** The location name view. */
	 TextView locationNameView  ;
 	
 	
 	/** The m active. */
	 boolean mActive = false;
    
     
//     int LOGIN_CREDENTIALS_REQUEST =94;
	
//	boolean mIsBound = false;
//	String authToken;
//	String source = "a";
	//String urlString = "https://www.google.com/accounts/ManageAccount";
//	String urlString = "https://www.google.com/voice/";
	
//	Timer timer =new Timer();
    
    
    //0. Check EULA, if not, launch.  TODO Fix EULA to show full text
    //1. CHECK SharedPreferences if Login Credentials have been set, if not launch activity
    //2. Start Background Service if Active
    
    
  

    
    
    
 /**
 * Checks if EULA has been accepted.  If not, It shows the EULA Alert Dialog.
 */
    private void checkShowEULA() {
       versionInfo = getPackageInfo();

       // the eulaKey changes every time you increment the version number in the AndroidManifest.xml
       final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
     //  final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
     //  final SharedPreferences prefs = settings;
       final SharedPreferences settings = this.preferences;
       boolean hasBeenShown = settings.getBoolean(eulaKey, false);
       if(hasBeenShown == false){

           // Show the Eula
           String title = mActivity.getString(R.string.app_name) + " v" + versionInfo.versionName;

           //Includes the updates as well so users know what changed.
           String message = mActivity.getString(R.string.updates) + "\n\n" + mActivity.getString(R.string.eula);
           View v = LayoutInflater.from(this).inflate(R.layout.eulalayout,null);
          TextView eula =  (TextView)v.findViewById(R.id.TextView01);
          eula.setText(readRawTextFile(MainMenu.this, R.raw.gpl3));
           AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                   .setTitle(title)
                   //.setMessage(message)
                   .setView(v)

                   .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           // Mark this version as read.
                           SharedPreferences.Editor editor = settings.edit();
                           editor.putBoolean(eulaKey, true);
                           editor.commit();
                           try{
                        	   dialogInterface.dismiss();
                           }catch(Exception e){
                        	   e.printStackTrace();
                           }

                           //SET Background Service Enabled Preference
	            				Editor edit = preferences.edit();
	            				edit.putBoolean(Settings.SERVICE_ENABLED, true);
	            				edit.commit();
	            			//TODO - Launch Background Service.
	            			checkShowCredentials();
            			
                           
                           
                       }
                   })
                   .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           // Close the activity as they have declined the EULA
                           mActivity.finish();
                       }

                   });
           builder.create().show();
       }else{
    	   checkShowCredentials();
       }
   }
    
    /**
     * Check show credentials.
     */
    private void checkShowCredentials(){
    	String username = preferences.getString("username", "");
    	String password = preferences.getString("password", "");
    	if(username.equals("")||password.equals("")){
    		Intent i = new Intent(MainMenu.this,LoginCredentials.class);
    		startActivity(i);
//    		startActivityForResult(i, );
    	}
//	 	startActivity(i);
    }

	/** The m i remote service. */
	GVLServiceInterface mIRemoteService;
	
	/** The m connection. */
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {

	    	  Log.e(TAG, "Service has connected");
//	    	  Intent hello_service = new Intent(MainMenu.this, BackgroundService.class);
//	 		 startService(hello_service);
	 			System.out.println("SERVICE STARTED");

//	    	 launchCredentials().execute();
	    	
	    	// Following the example above for an AIDL interface,
	        // this gets an instance of the IRemoteInterface, which we can use to call on the service
	        mIRemoteService = GVLServiceInterface.Stub.asInterface(service);
		    //MAKE THIS A BACKGROUND TASK
	        DisplayTask().execute();
	    }

	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "Service has unexpectedly disconnected");
	        mIRemoteService = null;
	    }
	};
	
	
	/**
	 * Check service enabled.
	 */
	public void checkServiceEnabled(){
		 
		
        
        
		boolean isEnabled = preferences.getBoolean(Settings.SERVICE_ENABLED, false);
		
		if(!isEnabled){
			warningPanel.setVisibility(View.VISIBLE);
		}else{
			warningPanel.setVisibility(View.GONE);
		}
	
	}
	
	
	

	

	

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.mainmenu, menu);
	    return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.quickpreferences:{
	    		Intent i = new Intent(MainMenu.this,QuickRingPreferences.class);
	    		startActivity(i);
	    		return true;
	    	}
		    case R.id.locations:{
				Intent i = new Intent(MainMenu.this, LocationsMenu.class);
				startActivity(i);
		        return true;
		    }
		    case R.id.settings:{
	
				Intent intent = new Intent(MainMenu.this, SettingsMenu.class);
				
	//			try {
	//				String[] LOCATIONS = null;
	//				LOCATIONS = mIRemoteService.getLocations();
	//				if(LOCATIONS!=null)
	//					intent.putExtra("locations",LOCATIONS);
	//			} catch (RemoteException e) {
	//				e.printStackTrace();
	//			}
				
				startActivity(intent);
		        return true;
		    }
		    default:{
		        return super.onOptionsItemSelected(item);
		    }
	    }
	}
	
    /**
     * Gets the package info.
     *
     * @return the package info
     */
    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
             pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }
    
    
    /**
     * Inits the.
     */
    private void init(){
        //IF SERVICE_ENABLED, LAUNCH IT ON A PERSISTENT BASIS
    	if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
    		Intent hello_service = new Intent(this, BackgroundService.class);
    		startService(hello_service);		
    	}

 		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 
// 		Intent serviceIntent = new Intent(MainMenu.this,LocationService.class);
// 		bindService( serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
// 	    timer.schedule(startServiceTask, 7000);
 	        /*
 	        Log.e("TECHVENTUS","STARTING SERVICE");
 	        Intent serviceIntent = new Intent(this,LocationService.class);
 	        startService(serviceIntent);
 	        */
 	      //  bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
//	 	Intent i = new Intent(MainMenu.this,LoginCredentials.class);
//	 	startActivity(i);

 		warningPanel = (LinearLayout)this.findViewById(R.id.WarningPanel) ;  
 	    Button locationsButton = (Button)findViewById(R.id.mainlocationsbutton);
 	    Button settingsButton = (Button)findViewById(R.id.mainsettingsbutton);
 	    ImageButton dialerButton = (ImageButton)findViewById(R.id.DialerButton);
 	    gpsTextView = (TextView)findViewById(R.id.maingpscoords);
 	    settingsButton.setOnClickListener(settingsClick);
 	    locationsButton.setOnClickListener(locationsClick);
 	    dialerButton.setOnClickListener(dialerClick);
 	    //closeButton.setOnClickListener(closeClick);
 	    locationNameView = (TextView)findViewById(R.id.currentlocationname);

// 	    try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			//  Auto-generated catch block
//			e.printStackTrace();
//		}

// 	   checkServiceEnabled();
    }
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override 
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		
		preferences = getSharedPreferences(Settings.PREFERENCENAME, 0);
		
		checkShowEULA();
		
		
		//new SimpleEula(this).show();
		init();
	       
	}
	
	/** The settings click. */
	OnClickListener settingsClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			 Intent i = new Intent(MainMenu.this, SettingsMenu.class);
			startActivity(i);
		}
		
	};
	
	/** The dialer click. */
	OnClickListener dialerClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			 Intent i = new Intent(MainMenu.this, Dialer.class);
			startActivity(i);
		}
		
	};
	
//	OnClickListener closeClick = new OnClickListener(){
//
//		@Override
//		public void onClick(View arg0) {
//			MainMenu.this.finish();
//			
//		}
//		
//	};
	
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.techventus.locations.BackgroundService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
	/** The locations click. */
OnClickListener locationsClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {

			Intent i = new Intent(MainMenu.this, LocationsMenu.class);
			
//			try {
//				String[] LOCATIONS = null;
//				LOCATIONS =LocationPhoneEnablePreference.getLocations(MainMenu.this);
////				LOCATIONS = mIRemoteService.getLocations();
//				if(LOCATIONS!=null && LOCATIONS.length>0)
//					i.putExtra(Settings.LOCATION_ARRAY_EXTRA/*"locations"*/,LOCATIONS);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
			
			startActivity(i);
			
		}
	};
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume(){
		super.onResume();
		this.mActive = true;
		
		
		
	       versionInfo = getPackageInfo();
	       // the eulaKey changes every time you increment the version number in the AndroidManifest.xml
	       final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
	       boolean hasBeenShown = preferences.getBoolean(eulaKey, false);
	       if(hasBeenShown){
	    	   
	    	   
	    	   
	    	Intent hello_service = new Intent(this, BackgroundService.class);
	   	   	if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
	   	   		
	   	   		if(!isMyServiceRunning()){
		    		
		    		startService(hello_service);	
	   	   		}
	    	}else{
	    		stopService(hello_service);
	    	}
		   	
		 	   if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
		 		   Intent hello_service2 = new Intent(this, BackgroundService.class);
		 		   bindService( hello_service2, mConnection,Context.BIND_AUTO_CREATE);
		 	   }
	    	   
	    	   
		 	   
		 	   
		    	checkServiceEnabled();
		    	
		    	
		    	
		     	  INTERVAL = 5000;
		    	  
		      	
		    	  TimerTask tt = new TimerTask(){

					@Override
					public void run() {
						if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
							try{
								INTERVAL = INTERVAL+(INTERVAL/2) - 2000;
								if(mActive)
								  DisplayTask().execute();
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}
		    		  
		    	  };
		    	  if(timer!=null){
		    		  timer.cancel();
		    	  }
		    	  timer = new Timer();
		    	  //ARBITRARY - CONSIDER ADJUSTMENT
		    	  if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
		    		  timer.schedule(tt, (long)5000, 14000+INTERVAL);
		    	  }else{
		  			gpsTextView.setText("LAT: -XXX.XXXXX LON: -XXX.XXXXX");
					locationNameView.setText("Unknown");
		    	  }

		 	   
	       }

		
	 	   

//		System.out.println("On Resume");
		
 
	    // Intent hello_service = new Intent(this, LocationService.class);
	   
		//if(mIRemoteService==null){

		//}
		
//		System.out.println("On Resume BinderService");
//		DisplayTask.execute(null);
		//setDisplay();
//		System.out.println("DisplayTask Launched ");
        //timer.schedule(statuscheck)
        //timer.schedule(statuscheck(), 3000);
	}
	
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause(){
		super.onPause();
		this.mActive = false;
		if(timer!=null)
			timer.cancel();
		
		try{
			unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Gets the display values.
	 *
	 * @return the display values
	 */
	private void getDisplayValues(){
		if(!preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
			 return;
		}
		
		try{
//			int lat=0;
//			int lon=0;
//			String locationString = "Unknown";

			if(mIRemoteService!=null){
			
					int[] coords;
					try {
						coords = mIRemoteService.getCurrentCoordinatesE6();
						lat = coords[0];
						lon = coords[1];
					} catch (Exception e1) {
					
						//Toast.makeText(this, "RemoteException - ERROR Obtaining Coordinates", Toast.LENGTH_LONG);
						e1.printStackTrace();
					}

					try {
						locationString = mIRemoteService.getCurrentLocationString();
					} catch (RemoteException e) {
						locationString = "ERROR";
						//Toast.makeText(this, "RemoteException - ERROR Obtaining LocationString", Toast.LENGTH_LONG);
					
						e.printStackTrace();
					}
			}		
			}catch(Exception r){
				r.printStackTrace();
				Log.e(TAG,"SetDisplay Exception");
				//Toast.makeText(this, TAG+" SetDisplay Exception", Toast.LENGTH_LONG);
			}
					
	}
	
	
	
	/**
	 * Sets the display.
	 */
	private void setDisplay(){
				
		if(lat!=0&&lon!=0 && lat!=-1 && lon!=-1){
			DecimalFormat nf = new DecimalFormat("###.00000");
			gpsTextView.setText("LAT: "+nf.format((double)lat/((double)1E6)) +" LON: "+nf.format((double)lon/(double)1E6));
			locationNameView.setText(locationString);
		}else{	
		//	Toast.makeText(this, "CHECKING Status", Toast.LENGTH_SHORT).show();
			gpsTextView.setText("LAT: -XXX.XXXXX LON: -XXX.XXXXX");
			locationNameView.setText("Unknown");
//			Timer timer = new Timer();
//			TimerTask tt = new TimerTask(){
//				@Override
//				public void run() {
//					if(mActive)
//						DisplayTask().execute();
//				}
//			};
//			INTERVAL+=(long)(INTERVAL/2);
//			if(preferences.getBoolean(Settings.SERVICE_ENABLED, false) && mActive){
//				timer.schedule(tt, INTERVAL);
//			}
				
		}

	}
	
	
	/**
	 * Display task.
	 *
	 * @return the async task
	 */
	AsyncTask<Void,Void,Void> DisplayTask(){
		AsyncTask<Void,Void,Void> DisplayTask = new AsyncTask<Void,Void,Void>(){
			 @Override
			    protected void onPreExecute() {
//				 Toast.makeText(MainMenu.this, "DEBUG: Display Task Called", Toast.LENGTH_LONG).show();
		        	
			        super.onPreExecute();
			        if(!mActive){
//			        	Toast.makeText(MainMenu.this, "DEBUG: CANCELLING MAIN MENU DISPLAY TASK", Toast.LENGTH_LONG).show();
			        	this.cancel(true);
			        }
			 }
			
			@Override
			protected Void doInBackground(Void... params) {
				try{
				if(mActive)
					getDisplayValues();
				
				}catch(Exception e){
					e.printStackTrace();
				}
				return null;
			}
				
			@Override
			protected void onPostExecute(Void result){
				 try{
				if(mActive){
//					Toast.makeText(MainMenu.this, "DEBUG: Post Execute - Active - Set Display coords:"+lat+","+lon, Toast.LENGTH_LONG).show();
			        
					setDisplay();
					System.out.println("DisplayTask Finished");
				}
				 }catch(Exception e){
					 e.printStackTrace();
				 }
			}
		};
		return DisplayTask;
		}
		
	
	
	
	
	 public static String readRawTextFile(Context ctx, int resId)
     {
          InputStream inputStream = ctx.getResources().openRawResource(resId);

             InputStreamReader inputreader = new InputStreamReader(inputStream);
             BufferedReader buffreader = new BufferedReader(inputreader);
              String line;
              StringBuilder text = new StringBuilder();

              try {
                while (( line = buffreader.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                  }
            } catch (IOException e) {
                return null;
            }
              return text.toString();
     }
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	}



//
//	private String[] getStringArSQLVertical(String query, SQLiteDatabase sql){
//		String[] ret = new String[0];
//		try{
//			Cursor c = sql.rawQuery(query, null);
//			try{
//				List<String> list = new ArrayList<String>();
//				if(c!=null){
//					while(c.moveToNext()){
//						list.add(c.getString(0));
//						Log.e("TECHVENTUS",c.getString(0));
//						
//					}
//					if(list.size()>0){
//						ret = new String[list.size()];
//						for(int i=0;i<list.size();i++){
//							ret[i] = list.get(i);
//						}
//					}
//					//c.close();
//				}
//			}catch(Exception o){
//				Log.e("TECHVENTUS","EXCEPTION IN getStringArSQLVertical "+query);
//				o.printStackTrace();
//			}finally{
//				c.close();
//			}
//
//			//c.getString(0);
//			
//		}catch(Exception o){
//			
//			o.printStackTrace();
//		}
//		return ret;
//	}
//	

//	@Override
//	public void onDestroy(){
//		try{
//			unbindService(mConnection);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		super.onDestroy();
//	}		
	





//synchronized(this){
//
//		String queryString = "SELECT status FROM STATUS WHERE key = 'latitude';";
//		
//		Cursor c = sql.rawQuery(queryString, null);
//		
//		try{
//			if(c!=null){
//					if(c.moveToNext()){
//						lat = Integer.parseInt(c.getString(0));
//						System.out.println(lat);
//					}
//					//c.close();
//			}
//		}catch(Exception y){
//			y.printStackTrace();
//		}finally{
//			c.close();
//		}
//	
//}
//synchronized(this){
//	String queryString = "SELECT  status FROM STATUS WHERE key = 'longitude';";
//	
//	 Cursor c = sql.rawQuery(queryString, null);
//	try{
//		 if(c!=null){
//			 if(c.moveToNext()){
//				 lon = Integer.parseInt(c.getString(0));
//				 System.out.println(lon);
//			 }
//			 //c.close();
//		 }
//	}catch(Exception y){
//		y.printStackTrace();
//	}finally{
//		c.close();
//	}
//}
//
//synchronized(this){
//
//	String queryString = "SELECT  status FROM STATUS WHERE key = 'location';";
//	
//	Cursor c = sql.rawQuery(queryString, null);
//	try{
//		 if(c!=null){
//			 if(c.moveToNext()){
//				 place = c.getString(0);
//				 System.out.println(place);
//			 }
//			
//		 }
//	}catch(Exception y){
//		y.printStackTrace();
//	}finally{
//		c.close();
//	}
//}
//
//}catch(Exception e){
//e.printStackTrace();
//}finally{
//
//sql.close();
//}

//c.moveToNext();

//place = c.getString(0);

//c.close();


//}catch(Exception e){
//Log.e("TECHVENTUS","Exception in MainMenu Lat Lon ping");
//System.out.println("");
//e.printStackTrace();
//lat = 0;
//lon = 0;
//
//}












/*
public void login() throws IOException{

	String data = URLEncoder.encode("accountType", "UTF-8") + "="
			+ URLEncoder.encode("GOOGLE", "UTF-8");
	data += "&" + URLEncoder.encode("Email", "UTF-8") + "="
			+ URLEncoder.encode(user, "UTF-8");
	data += "&" + URLEncoder.encode("Passwd", "UTF-8") + "="
			+ URLEncoder.encode(pass, "UTF-8");
	data += "&" + URLEncoder.encode("service", "UTF-8") + "="
			+ URLEncoder.encode("grandcentral", "UTF-8");
	data += "&" + URLEncoder.encode("source", "UTF-8") + "="
			+ URLEncoder.encode(source, "UTF-8");

	// Send data
	URL url = new URL("https://www.google.com/accounts/ClientLogin");
	URLConnection conn = url.openConnection();
	conn.setDoOutput(true);
	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	wr.write(data);
	wr.flush();

	// Get the response
	BufferedReader rd = new BufferedReader(new InputStreamReader(conn
			.getInputStream()),1024);
	String line;

	//String AuthToken = null;
	while ((line = rd.readLine()) != null) {
		// System.out.println(line);
		if (line.contains("Auth=")) {
			this.authToken = line.split("=", 2)[1].trim();
			System.out.println("AUTH TOKEN =" + this.authToken);
		}
	}
	wr.close();
	rd.close();

	if (this.authToken == null) {
		throw new IOException("No Authorisation Received.");
	}
}


void openNext(){
	try{
		URL url = new URL(urlString+"?auth="+URLEncoder.encode(authToken,"UTF-8"));
		URLConnection conn = url.openConnection ();
		//Log.e("TECHVENTUS",conn.getRequestProperty("User-agent"));
		conn.setRequestProperty("User-agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");
//"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13"
		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()),1024);
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null)
		{
			sb.append(line+"\n\r");
		}
		rd.close();
		String result = sb.toString();
		Log.e("TECHVENTUS",result);
		System.out.println(result);
	}catch(Exception e){
		e.printStackTrace();
	}
}
*/
/*
void doUnbindService() {
    if (mIsBound) {
        // Detach our existing connection.
        unbindService(mConnection);
        mIsBound = false;
    }
}
*/
/*
void doBindService() {
    // Establish a connection with the service.  We use an explicit
    // class name because we want a specific service implementation that
    // we know will be running in our own process (and thus won't be
    // supporting component replacement by other applications).
	System.out.println("****************************");
	Log.e("TECHVENTUS","BINDING NOW");
    bindService(new Intent(MainMenu.this, 
    		//?????LocationService
            LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
    mIsBound = true;
    Log.e("TECHVENTUS","misBound "+mIsBound);
}
*/



/*	
	TimerTask startLoginProcedureTask = new TimerTask(){

		@Override
		public void run() {
			 
		       // setDisplay();
		        boolean startLoginProcedure = false;
		        
		        	try{
		        		synchronized(this){
				        	 SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
				        	 Cursor c = sql.rawQuery("SELECT * FROM GOOGLE;", null);
				        	 try{
					        	 
					        	 
					        	 if(c!=null){
						        	 if(!c.moveToNext()){
						        		 startLoginProcedure = true;
				
						        	 }else{
						        		 startLoginProcedure = false;
						        	 }
						        	 
						        	 //c.close();
						        	 
					        	 }else{
					        		 startLoginProcedure = true;
					        	 }																	
				        	 }catch(Exception u){
				        		 u.printStackTrace();
				        	 }finally{
				        		 c.close();
				        		 sql.close();
				        	 }
			        	// sql.close();
		        		}//synch
		        	 
		        	 if(startLoginProcedure){
		        		 Intent i = new Intent(MainMenu.this,LoginCredentials.class);
		        		 startActivity(i);
		        	 }
		        		
		        }catch(Exception e){
		        	 Log.e("TECHVENTUS","Exception in MainMenu determining credentials - starting LoginCredentials intent");
		        	 e.printStackTrace();
		       		 Intent i = new Intent(MainMenu.this,LoginCredentials.class);
		    		 startActivity(i);
		        }

		}
		
	};
	*/
	


/*
private TimerTask statuscheck(){
	TimerTask statuscheck = new TimerTask(){

		@Override
		public void run() {
			
			setDisplay();
			
		}
		
	};
	return statuscheck;
}
*/
//private LocationService mBoundService;
//
///** The m connection. */
//private ServiceConnection mConnection = new ServiceConnection() {
//	@Override
//    public void onServiceConnected(ComponentName className, IBinder service) {
//        // This is called when the connection with the service has been
//        // established, giving us the service object we can use to
//        // interact with the service.  Because we have bound to a explicit
//        // service that we know is running in our own process, we can
//        // cast its IBinder to a concrete class and directly access it.
//        mBoundService = ((LocationService.LocalBinder)service).getService();
//
//        // Tell the user about this for our demo.
//        Toast.makeText(MainMenu.this, "Connected",
//                Toast.LENGTH_SHORT).show();
//    }
//	@Override
//    public void onServiceDisconnected(ComponentName className) {
//        // This is called when the connection with the service has been
//        // unexpectedly disconnected -- that is, its process crashed.
//        // Because it is running in our same process, we should never
//        // see this happen.
//        mBoundService = null;
//        Toast.makeText(MainMenu.this, "Disconn",
//                Toast.LENGTH_SHORT).show();
//    }
//
//
//};
//
//




//TimerTask startServiceTask = new TimerTask(){
//	
//	@Override
//	public void run() {
//		
//		try{
//	        Log.e("TECHVENTUS","STARTING SERVICE");
//	       // Intent serviceIntent = new Intent(MainMenu.this,LocationService.class);
//	        Intent serviceIntent = new Intent(MainMenu.this,BackgroundService.class);
//	        
//		       
//	       
//	        startService(serviceIntent);
//		}catch(Exception o){
//			Log.e("TECHVENTUS","EXCEPTION IN MainMenu.startServiceTask");
//			o.printStackTrace();
//		}
//	}
//	
//};

//AsyncTask<Void,Boolean,Boolean> launchCredentials(){
//	AsyncTask<Void,Boolean,Boolean> task = new AsyncTask<Void,Boolean,Boolean>(){
//		
//		
//		@Override
//		protected void onPostExecute(Boolean result){
//			if(result){
//			 	Intent i = new Intent(MainMenu.this,LoginCredentials.class);
//			 	startActivity(i);
//			 	System.out.println("LaunchCredential Task Finished Attempting to Start Credential Activity");
//			}
//		}
//		
//		@Override
//		protected Boolean doInBackground(Void... params) {
//			
//		    try { 
//		    	//Thread.sleep(2000);
//			    while(mIRemoteService==null || !mIRemoteService.startupComplete()){
//			    	System.out.println("while null...");
//			    	Thread.sleep(2000);
//			    }
//		    
//				if(!mIRemoteService.hasCredentials()){
//					return true;
//
//				}
//			} catch (RemoteException e) {

//				e.printStackTrace();
//			} catch (InterruptedException e) {

//				e.printStackTrace();
//			}
//			
//			return false;
//		}
//		
//	};
//	return task;
//}
//
