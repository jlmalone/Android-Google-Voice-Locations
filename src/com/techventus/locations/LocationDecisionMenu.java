package com.techventus.locations;

//import java.util.Set;


import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
//import android.content.ComponentName;
//import android.content.Context;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
//import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
//import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//TODO REVIEW THIS ACTIVITY. IT IS ANTIQUATED AND NEEDS UPDATING
/**
 * The Class LocationDecisionMenu.
 */
public class LocationDecisionMenu extends Activity{
	
	/** The TAG. */
	String TAG = "TECHVENTUS - LocationDecistionMenu";
	

	GVLServiceInterface mIRemoteService;
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // Following the example above for an AIDL interface,
	        // this gets an instance of the IRemoteInterface, which we can use to call on the service
	        mIRemoteService = GVLServiceInterface.Stub.asInterface(service);
	    }

	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "Service has unexpectedly disconnected");
	        mIRemoteService = null;
	    }
	};
	
	
	/** The location name text view. */
	TextView locationNameTextView;
	
	/** The gpscoords text view. */
	TextView gpscoordsTextView;
	
	/** The phone prefs status text view. */
	TextView phonePrefsStatusTextView;
	
	/** The awareness text view. */
	TextView awarenessTextView;
	
	/** The view edit location button. */
	Button viewEditLocationButton;
	
	/** The view edit phone button. */
	Button viewEditPhoneButton;
	
	/** The enable disable button. */
	Button enableDisableButton;
	
	
	/** The location name. */
	String locationName;
	
	/** The gpscoords. */
	int[] gpscoords = new int[] {0,0};
	
	/** The radius. */
	int radius = 100;
	
	/** The awareness. */
	boolean awareness;
	
	/** The phone prefs status. */
	int phonePrefsStatus;
	
	 /** The preferences. */
	SharedPreferences preferences ;
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		 preferences = getSharedPreferences(Settings.PREFERENCENAME, 0);
//  	Intent hello_service = new Intent(this, LocationService.class);


        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 
        setContentView(R.layout.locationdecisionmenu);
        
        locationNameTextView = (TextView)findViewById(R.id.locationdecisionlocation);
        gpscoordsTextView = (TextView)findViewById(R.id.locationdecisiongpscoords);
        awarenessTextView = (TextView)findViewById(R.id.locationawarenessstatus);
        viewEditLocationButton = (Button)findViewById(R.id.locationdecisionlocationbutton); 
        phonePrefsStatusTextView = (TextView)findViewById(R.id.phoneprefsstatus);
        
        Bundle bundle = getIntent().getExtras();
//        Set<String> keys =bundle.keySet();
//        Log.e("TECHVENTUS",String.valueOf(keys.size()));
        locationName = bundle.getString(Settings.LOCATION_NAME_EXTRA);
        
        
        
        if(locationName.equals("Elsewhere")){
        	viewEditLocationButton.setVisibility(View.GONE);
        }else{
        	viewEditLocationButton.setVisibility(View.VISIBLE);
        	viewEditLocationButton.setOnClickListener(locationClick);
        }
        
        getLocationVariables();
        
        if(gpscoords[0]!=0||gpscoords[1]!=0)
        	gpscoordsTextView.setText("LAT: "+String.valueOf(((double)gpscoords[0]/(double)1E6))
        			+" LON: "+String.valueOf(((double)gpscoords[1]/(double)1E6)));
        else
        	gpscoordsTextView.setText("LAT: -XXX.XXXXX LON: -XXX.XXXXX");
        
        awarenessTextView .setText(String.valueOf(awareness));
        
        /*
        try{
        	
        	int lat = bundle.getInt("locationLatitude");
        	int lon = bundle.getInt("locationLongitude");
        	System.out.println("lat"+lat);
        	System.out.println("lon"+lon);
        	gpscoords = new int[] {lat,lon};

        }catch(Exception e){
        	e.printStackTrace();
        }
        */
        if(locationName!=null)
        	locationNameTextView.setText(locationName);
        

        viewEditPhoneButton = (Button)findViewById(R.id.locationdecisionphonebutton); 
        
        
        viewEditPhoneButton.setOnClickListener(phoneClick);
       
        establishPhonePrefsStatus();
	}
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.location_edit_menu, menu);
	    return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	
	    	case R.id.preferences:{
	    		Intent i = new Intent(LocationDecisionMenu.this,PhonePreference.class);
	    		i.putExtra(Settings.LOCATION_NAME_EXTRA, locationName);
	    		i.putExtra(Settings.RADIUS_EXTRA, radius);
	    		startActivity(i);
	    		return true;
	    	}
		    case R.id.radius:{
		    	Toast.makeText(getApplicationContext(), "Editing of Radius Coming In the Next Version.", Toast.LENGTH_LONG).show();
//				Intent i = new Intent(LocationDecisionMenu.this, LocationsMenu.class);
//				startActivity(i);
		        return true;
		    }
		    case R.id.delete:{
		    	
		    	if(locationName.equals("Elsewhere")){
		    		Toast.makeText(LocationDecisionMenu.this, "You Cannot Delete the Default Location", Toast.LENGTH_LONG).show();
		    		return true;
		    	}
		    	
		    	
		           AlertDialog.Builder builder = new AlertDialog.Builder(LocationDecisionMenu.this)
                   .setTitle("DELETE LOCATION")
                   .setMessage("ARE YOU SURE YOU WANT TO DELETE "+locationName+"?")
                   .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
       						SQLiteDatabase db= openOrCreateDatabase("db",0,null);
       						db.delete("LOCATIONPHONEENABLE", "locationName = ?", new String[]{locationName});
                    	   db.close();
       						if(preferences.getBoolean(Settings.SERVICE_ENABLED, false))
	       						if(mIRemoteService!=null){
	       							try {
										mIRemoteService.restart();
									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
	       						}
       						LocationDecisionMenu.this.finish();
                    	   try{
                    		   dialogInterface.dismiss();
                    	   }catch(Exception e){
                    		   e.printStackTrace();
                    	   }
                           
                           
                       }
                   })
                   .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                    	   try{
                    		   dialog.dismiss();
                    	   }catch(Exception e){
                    		   e.printStackTrace();
                    	   }
                       }

                   });
           builder.create().show();
		    	
		  
		        return true;
		    }
		    default:{
		        return super.onOptionsItemSelected(item);
		    }
	    }
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume(){
		
		super.onResume();
        if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
    	    Intent hello_service = new Intent(this, BackgroundService.class);  
    		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
            
        }
		
	  //  Intent hello_service = new Intent(this, LocationService.class);
//	    Intent hello_service = new Intent(this, BackgroundService.class);
//		   
//		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
//		
		Log.e("TECHVENTUS", "RESUMING Location Decision Menu");
		
		if(!phonePrefsStatusTextView.getText().equals("SET")){
				
			  	establishPhonePrefsStatus();
			  	
		}
	}
	
	/** The location click. */
	OnClickListener locationClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			Intent i = new Intent(LocationDecisionMenu.this,LocationMap.class);
			Bundle b = new Bundle();
			b.putString(Settings.LOCATION_NAME_EXTRA/*"locationName"*/, locationName);
			b.putBoolean("isNew", false);

//			SQLiteDatabase sql= openOrCreateDatabase("db",0,null);
			List<LPEPref> prefList = LocationPhoneEnablePreference.loadListFromDB(LocationDecisionMenu.this, " locationName = '"+locationName+"' ");
			if(prefList!=null && prefList.size()>0){
				b.putInt(Settings.LATITUDE_EXTRA, prefList.get(0).latitude);
				b.putInt(Settings.LONGITUDE_EXTRA, prefList.get(0).longitude);
//				b.putInt("radius", prefList.get(0).radius);
				b.putInt(Settings.RADIUS_EXTRA, prefList.get(0).radius);
				if(prefList.get(0).latitude!=-1 && prefList.get(0).longitude!=1){
					i.putExtras(b);
					startActivity(i);
				}else{
					Toast.makeText(getApplicationContext(), "ERROR: LOCATION NOT IN DATABASE!!!", Toast.LENGTH_LONG).show();
					LocationDecisionMenu.this.finish();
				}
			}else{
				Toast.makeText(getApplicationContext(), "ERROR: LOCATION NOT IN DATABASE!!!", Toast.LENGTH_LONG).show();
				LocationDecisionMenu.this.finish();
			}
			/*
			Cursor c =sql.rawQuery("SELECT locationLatitude,locationLongitude,locationRadius, enabled FROM LOCATIONS where locationName = '"+locationName+"';", null);
			
			   if(c!=null){
				   c.moveToNext();
				   if(c.isFirst()){
					   //do{
						   b.putInt("latitude", c.getInt(0));
						   b.putInt("longitude", c.getInt(1));
						   b.putInt("radius", c.getInt(2));
						 // b.putString(c.getString(0), c.getString(1)) ;
						   		
			//			   Log.e("TECHVENTUS","Phone added to bundle "+c.getString(0));
						   		
						   //if( c.getString(0).equals("updateLoginCredentials")){}
						   
					  // }while(c.moveToNext());
				   }else{
					   Log.e("TECHVENTUS","NOT FIRST");
				   }
			   }
			c.close();
			sql.close();
			*/

		}
		
	};
	
	/**
	 * Gets the location variables.
	 *
	 * @return the location variables
	 */
	void getLocationVariables(){
		SQLiteDatabase sql = null;
		
		try{
		
		if(!locationName.equals("Elsewhere")){

			sql= openOrCreateDatabase("db",0,null);
			
			Cursor c =sql.rawQuery("SELECT locationLatitude,locationLongitude,radius, phoneEnable FROM LOCATIONPHONEENABLE where locationName = '"+locationName+"';", null);
			
			   if(c!=null){
				   c.moveToNext();
				   if(c.isFirst()){
					  // do{
						   gpscoords[0] = c.getInt(0);
						   gpscoords[1] =  c.getInt(1);
						   radius= c.getInt(2);
						   awareness= Boolean.valueOf(c.getString(3));
						 // b.putString(c.getString(0), c.getString(1)) ;
						   		
			//			   Log.e("TECHVENTUS","Phone added to bundle "+c.getString(0));
						   		
						   //if( c.getString(0).equals("updateLoginCredentials")){}

					   //}while(c.moveToNext());
				   }else{
					   Log.e("TECHVENTUS","NOT FIRST");
				   }
			   }
			c.close();
			
			
		}	
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(sql!=null)
				sql.close();
		}
	}
	
	/**
	 * Establish phone prefs status.
	 * 
	 * TODO This is a ridiculous method, although it does get the job done
	 * Think about replacing with something more elegant.
	 */
	private void establishPhonePrefsStatus(){
		Log.e("TECHVENTUS","Establish Phone Prefs Status");
		try{
			SQLiteDatabase sql =  openOrCreateDatabase("db",0,null);
			
			Cursor c = sql.rawQuery("SELECT phoneEnable FROM LOCATIONPHONEENABLE WHERE locationName = '"+locationName+"';", null);
			
			if(c!=null){
				Log.e("TECHVENTUS","C not null");
				boolean valid = false;
				while(c.moveToNext()){
					Log.e("TECHVENTUS","In the while Check...");
					Log.e("TECHVENTUS","In the while Check phone "+c.getInt(0));
					if(c.getInt(0)!=-2){
						valid = true;
						phonePrefsStatusTextView.setText("SET");
						break;
					}else if(c.getInt(0)==-2){
						valid = true;
					}
				}
				if(!phonePrefsStatusTextView.getText().equals("SET") && valid){
					phonePrefsStatusTextView.setText("NOT SET");
				}else if (!valid){
					phonePrefsStatusTextView.setText("UNKNOWN");
				}
				c.close();
			}else{
				phonePrefsStatusTextView.setText("UNKNOWN");
			}
			sql.close();
		
		}catch(Exception u){
			u.printStackTrace();
		}
	}
	
	
	/** The phone click. */
	OnClickListener phoneClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			Intent i = new Intent(LocationDecisionMenu.this,PhonePreference.class);
			Bundle b = new Bundle();
			b.putString(Settings.LOCATION_NAME_EXTRA, locationName);
			
//			SQLiteDatabase sql= openOrCreateDatabase("db",0,null);
//			
//			Cursor c =sql.rawQuery("SELECT phoneName, phoneEnable FROM LOCATIONPHONEENABLE where locationName = '"+locationName+"';", null);
//			
//			   if(c!=null){
//				   while(c.moveToNext()){
//						  b.putInt(c.getString(0), c.getInt(1)) ;
//					   		
//						   Log.e("TECHVENTUS","Phone added to bundle "+c.getString(0));
//				   }
//				   c.close();
//			   }
//			
//			sql.close();
			i.putExtras(b);
			startActivity(i);

		}
		
	};
	
	
	@Override
	public void onPause(){
		try{
			unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onPause();
	}
	
//	@Override
//	public void onDestroy(){
//		try{
//			unbindService(mConnection);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		super.onDestroy();
//	}		
//	

}
