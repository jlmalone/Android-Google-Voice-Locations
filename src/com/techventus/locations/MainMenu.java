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


/**
 * The Class MainMenu.
 */
public class MainMenu extends Activity {
	
    /**
     * The current context.
     */

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
    
 /**
 * Checks if EULA has been accepted.  If not, It shows the EULA Alert Dialog.
 */
    private void checkShowEULA() {
       versionInfo = getPackageInfo();

       final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
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
    	}
    }

	/** The m i remote service. */
	GVLServiceInterface mIRemoteService;
	
	/** The m connection. */
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {

	    	  Log.e(TAG, "Service has connected");
	 			System.out.println("SERVICE STARTED");


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
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mainmenu, menu);
	    return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
     * Initialise the Activity.
     */
    private void init(){
        //IF SERVICE_ENABLED, LAUNCH IT ON A PERSISTENT BASIS
    	if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
    		Intent hello_service = new Intent(this, BackgroundService.class);
    		startService(hello_service);		
    	}

 		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 

 		warningPanel = (LinearLayout)this.findViewById(R.id.WarningPanel) ;  
 	    Button locationsButton = (Button)findViewById(R.id.mainlocationsbutton);
 	    Button settingsButton = (Button)findViewById(R.id.mainsettingsbutton);
 	    ImageButton dialerButton = (ImageButton)findViewById(R.id.DialerButton);
 	    gpsTextView = (TextView)findViewById(R.id.maingpscoords);
 	    settingsButton.setOnClickListener(settingsClick);
 	    locationsButton.setOnClickListener(locationsClick);
 	    dialerButton.setOnClickListener(dialerClick);
 	    locationNameView = (TextView)findViewById(R.id.currentlocationname);

    }


	@Override 
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		
		preferences = getSharedPreferences(Settings.PREFERENCENAME, 0);
		
		checkShowEULA();

		init();
	       
	}
	
	OnClickListener settingsClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			 Intent i = new Intent(MainMenu.this, SettingsMenu.class);
			startActivity(i);
		}
		
	};
	
	OnClickListener dialerClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			 Intent i = new Intent(MainMenu.this, Dialer.class);
			startActivity(i);
		}
		
	};
	

	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.techventus.locations.BackgroundService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
    OnClickListener locationsClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {

			Intent i = new Intent(MainMenu.this, LocationsMenu.class);
			
			startActivity(i);
			
		}
	};
	
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
	}
	
	

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

			if(mIRemoteService!=null){
			
					int[] coords;
					try {
						coords = mIRemoteService.getCurrentCoordinatesE6();
						lat = coords[0];
						lon = coords[1];
					} catch (Exception e1) {
					
						e1.printStackTrace();
					}

					try {
						locationString = mIRemoteService.getCurrentLocationString();
					} catch (RemoteException e) {
						locationString = "ERROR";

						e.printStackTrace();
					}
			}		
			}catch(Exception r){
				r.printStackTrace();
				Log.e(TAG,"SetDisplay Exception");
			}
					
	}
	
	
	
	/**
	 * Sets the display.
	 */
	private void setDisplay(){
				
		if(lat!=0&&lon!=0 && lat!=-1 && lon!=-1){
			DecimalFormat nf = new DecimalFormat("###.00000");
			gpsTextView.setText("LAT: "+nf.format((double)lat/(1E6)) +" LON: "+nf.format((double)lon/1E6));
			locationNameView.setText(locationString);
		}else{	
			gpsTextView.setText("LAT: -XXX.XXXXX LON: -XXX.XXXXX");
			locationNameView.setText("Unknown");

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

			        super.onPreExecute();
			        if(!mActive){
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

             InputStreamReader inputReader = new InputStreamReader(inputStream);
             BufferedReader buffReader = new BufferedReader(inputReader);
              String line;
              StringBuilder text = new StringBuilder();

              try {
                while (( line = buffReader.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                  }
            } catch (IOException e) {
                return null;
            }
              return text.toString();
     }
	 

	}

