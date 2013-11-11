package com.techventus.locations;

import android.app.ListActivity;
//import android.content.ComponentName;
//import android.content.Context;
import android.content.Intent;
//import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.ToggleButton;


public class SettingsMenu extends ListActivity{
	

	String TAG = "TECHVENTUS - SETTINGSMENU";
	ToggleButton serviceEnableToggle ;
	ToggleButton startupEnableToggle ;
	SharedPreferences preferences;
	

	boolean isServiceEnabled;
	boolean isStartupEnabled;
//	
//	GVLServiceInterface mIRemoteService;
//	private ServiceConnection mConnection = new ServiceConnection() {
//	    // Called when the connection with the service is established
//	    public void onServiceConnected(ComponentName className, IBinder service) {
//	        // Following the example above for an AIDL interface,
//	        // this gets an instance of the IRemoteInterface, which we can use to call on the service
//	        mIRemoteService = GVLServiceInterface.Stub.asInterface(service);
//	        if(Settings.RESET_SERVICE_FLAG){
//	        	try {
//					mIRemoteService.reset();
//					Settings.RESET_SERVICE_FLAG = false;
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//	        }
//	    }
//
//	    // Called when the connection with the service disconnects unexpectedly
//	    public void onServiceDisconnected(ComponentName className) {
//	        Log.e(TAG, "Service has unexpectedly disconnected");
//	        mIRemoteService = null;
//	    }
//	};
	

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.settingsmenu);
		
		preferences   = SettingsMenu.this.getSharedPreferences(Settings.PREFERENCENAME, 0);
        
		isServiceEnabled = preferences.getBoolean(Settings.SERVICE_ENABLED, false);
		isStartupEnabled = preferences.getBoolean(Settings.STARTUP_ENABLED, false);
//		Intent hello_service = new Intent(this, LocationService.class);
//		Intent hello_service = new Intent(this, BackgroundService.class);
//		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
	
		serviceEnableToggle = (ToggleButton)findViewById(R.id.serviceEnableToggle);
		startupEnableToggle = (ToggleButton)findViewById(R.id.serviceStartupToggle);
		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 
		
		//SET Toggle to Appropriate Setting.
		serviceEnableToggle.setChecked(preferences.getBoolean(Settings.SERVICE_ENABLED, false));
		serviceEnableToggle.setOnClickListener(click);

		startupEnableToggle.setChecked(preferences.getBoolean(Settings.STARTUP_ENABLED, false));
		startupEnableToggle.setOnClickListener(click);
		initListView();
	}
	
	

	
	private void initListView()
	{


	    final String[] matrix  = { "_id", "name", "value" };
	    final String[] columns = { "name", "value" };
	    final int[]    layouts = { R.id.title, R.id.subtitle };

	    MatrixCursor  cursor = new MatrixCursor(matrix);
	    int key=-1;
	   // DecimalFormat formatter = new DecimalFormat("##,##0.00");
	    //0
	    cursor.addRow(new Object[] { key++, "Google Connectivity", "Edit Credentials" });
	    //1
	    cursor.addRow(new Object[] { key++, "Power, Frequency, Accuracy", "Adjust Battery and Data Performance" });
		  
//	    cursor.addRow(new Object[] { key++, "Google Updates", "Updates and " });
//	    cursor.addRow(new Object[] { key++, "Location Detection", "Every 5 minutes" });
	    //2
	    cursor.addRow(new Object[] { key++, "Notifications", "Set Alerts..." });
	    //3
	    cursor.addRow(new Object[] { key++, "Locations", "Edit Location Profiles" });
	    //4
	    cursor.addRow(new Object[] { key++, "Reset", "Reset all Settings to Default" });
//	    cursor.addRow(new Object[] { key++, PriceName,
//	            "$" + formatter.format(mPrice) });

	    SimpleCursorAdapter data =
	        new SimpleCursorAdapter(this,
	                R.layout.rowlayout,
	                cursor,
	                columns,
	                layouts);

	    setListAdapter( data );

	}   // end of initListView()
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
	//	String item = (String) getListAdapter().getItem(position);
		//Adjust Login Credentials...
		
		
		switch(position){
			//Google Connectivity, Login
			case 0:
			{
				Intent i = new Intent(SettingsMenu.this,LoginCredentials.class);
				startActivityForResult(i,0);
				break;
			}
			//Power Settings, Update Frequencies
			case 1:
			{
				Intent i = new Intent(SettingsMenu.this,FrequencyMenu.class);
				startActivityForResult(i,1);
				break;
			}
			//Notification Preferences
			case 2:
			{
				Intent i = new Intent(SettingsMenu.this,NotificationPreferences.class);
				startActivityForResult(i,2);
				break;
			}
			case 3:
			{
				Intent i = new Intent(SettingsMenu.this,LocationsMenu.class);
				startActivity(i);
				this.finish();
				break;
			}
			case 4:
			{
				Intent i = new Intent(SettingsMenu.this,ResetConfirm.class);
				startActivityForResult(i,4);
				break;
				//this.finish();
			}
			default:
				break;
		}
		
		
		
		
		
//		Toast.makeText(this, position + " selected", Toast.LENGTH_LONG).show();
//		if(position==2){
//			temp = "secondary";
//			initListView();
//		}
	}
	
	
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
      //  if (requestCode == LOCATION_METHOD) {
	      //  }
		if(resultCode==999){
			Settings.RESTART_SERVICE_FLAG = true;
//			try {
//				mIRemoteService.reset();
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
		}
    }
	
	
			
			@Override
			protected void onResume(){
				super.onResume();
				
//				if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
//				
//				     Intent hello_service = new Intent(this, BackgroundService.class);
//				      
//				     bindService( hello_service, mConnection,
//									 Context.BIND_AUTO_CREATE);
//				}
			}



//			@Override
//			public void onPause(){
//				try{
//					unbindService(mConnection);
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//				super.onPause();
//			}


			
			OnClickListener click = new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					switch(arg0.getId()){
						case R.id.serviceEnableToggle:
						{
							serviceToggle();
							break;
						}
						case R.id.serviceStartupToggle:
						{
							startupToggle();
							break;
						}
					}
				}
			};
			
			
			void serviceToggle(){
				Editor edit = preferences.edit();
				edit.putBoolean(Settings.SERVICE_ENABLED, serviceEnableToggle.isChecked());
				edit.commit();
				
				
				if(serviceEnableToggle.isChecked()){
					Settings.RESTART_SERVICE_FLAG = true;
					Intent serviceIntent = new Intent(this,BackgroundService.class);
					startService(serviceIntent);
				}else{
					//STOP SERVICE EVERY WAY IMAGINABLE
//					try{
//						unbindService(mConnection);
//					}catch(Exception e){
//						e.printStackTrace();
//					}
					
					Intent serviceIntent = new Intent(this,BackgroundService.class);
					stopService(serviceIntent);
				}
				
			}
			
			void startupToggle(){
				Editor edit = preferences.edit();
				edit.putBoolean(Settings.STARTUP_ENABLED, startupEnableToggle.isChecked());
				edit.commit();
			}
	
}








//private void resetServiceWhenReady(){
//	
//}



//OnClickListener clickReset = new OnClickListener(){
//	@Override
//	public void onClick(View arg0) {
//		System.out.println("Reset Confirm");
//		Intent i = new Intent(SettingsMenu.this,ResetConfirm.class);
//		startActivityForResult(i,0);
//		//startActivity(i);
//		
//	}
//};



//
//OnClickListener clickPower = new OnClickListener(){
//	@Override
//	public void onClick(View arg0) {
//		System.out.println("Power Menu");
//		Intent i = new Intent(SettingsMenu.this,PowerMode.class);
//	//	startActivity(i);
//		startActivityForResult(i,0);
//	}
//};
//
//OnClickListener clickDelete = new OnClickListener(){
//
//	@Override
//	public void onClick(View arg0) {
//		System.out.println("Delete Menu");
//		Intent i = new Intent(SettingsMenu.this,DeleteLocation.class);
//		//startActivity(i);
//		startActivityForResult(i,0);
//	}};
//
//OnClickListener clickGoogle = new OnClickListener(){
//
//	@Override
//	public void onClick(View arg0) {	
//	}};
//	
//OnClickListener clickFrequency = new OnClickListener(){
//
//		@Override
//		public void onClick(View arg0) {
//			frequencyLayout.setBackgroundColor(Color.BLUE);
//			SettingsMenu.this.frequencyTitle.setBackgroundColor(Color.BLUE);
//			Intent i = new Intent(SettingsMenu.this,FrequencyMenu.class);
//			startActivityForResult(i,0);
//			try {
//				Thread.sleep(2000);
//				frequencyLayout.setBackgroundColor(Color.BLACK);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//		}};
		
//OnClickListener clickService = new OnClickListener(){
//
//			@Override
//			public void onClick(View arg0) {
//				//Intent i = new Intent(SettingsMenu.this,LocationService.class);
//				Intent i = new Intent(SettingsMenu.this,BackgroundService.class);
//				startActivityForResult(i,0);
//				//startService(i);
//				
//		}};	



//@Override 
//public void onResume(){
//	super.onResume();
//	
//    //Intent hello_service = new Intent(this, LocationService.class);
//    Intent hello_service = new Intent(this, BackgroundService.class);
//    
//	bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
//	
//	try{
//		//SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
//		
//		//Cursor c = sql.rawQuery("SELECT status FROM STATUS WHERE key = 'google';", null);
////		
////		if(mIRemoteService!=null){
////			serviceStatus.setText("Service Functioning");
////			if(mIRemoteService.isVoiceConnected()){
////				googlesubTitle.setText("Connected");
////			}else{
////				googlesubTitle.setText("No Connexion");
////			}
////			
////		}else{
////			serviceStatus.setText("Service Error");
////			googlesubTitle.setText("Service Error");
////		}
//		
////		
////		if(c!=null){
////			if(c.moveToNext()){
////				
////				googlesubTitle.setText(c.getString(0));
////			}
////			
////
////			c.close();
////		}
//			
//		
//		
////		
////		 c = sql.rawQuery("SELECT status FROM STATUS WHERE key = 'service';", null);
////			if(c!=null){
////				if(c.moveToNext()){
////					long time =(new Date()).getTime();
////					
////					long servicetime = Long.parseLong(c.getString(0));
////					
////					if(time-servicetime<300000){
////						serviceStatus.setText("Service Functioning");
////					}else{
////						serviceStatus.setText("Service Error");
////					}
////				}
////				c.close();
////			}
////				
//		
//
//		/*
//		 c = sql.rawQuery("SELECT status FROM STATUS WHERE key = 'service';", null);
//			if(c!=null){
//				if(c.moveToNext()){
//					googlesubTitle.setText(c.getString(0));
//				}
//				c.close();
//			}*/
//		//sql.close();
//	}catch(Exception e){
//		e.printStackTrace();
//	}finally{
//		//sql.close();
//	}
//}






//Layout googleItem ;//= (Layout)findLayoutById(R.id.settingsmenugooglelayout);
/*		
serviceLayout = (LinearLayout)findViewById(R.id.settingsmenubackgroundlayout);

 googleTitle = (TextView)findViewById(R.id.settingsmenugoogletitle);
 googlesubTitle = (TextView)findViewById(R.id.settingsmenugooglesubtext);
 googleLayout = (LinearLayout)findViewById(R.id.settingsmenugooglelayout);
//googleTitle.setOnClickListener(clickGoogle);
googlesubTitle.setOnClickListener(clickGoogle);
googleLayout.setOnClickListener(clickGoogle);

 frequencyTitle = (TextView)findViewById(R.id.settingsmenufrequenciestitle);
 frequencysubTitle = (TextView)findViewById(R.id.settingsmenufrequenciessubtext);
frequencyTitle.setOnClickListener(clickFrequency);
frequencysubTitle.setOnClickListener(clickFrequency);
frequencyLayout = (LinearLayout)findViewById(R.id.settingsmenufrequencieslayout);
frequencyLayout.setOnClickListener(clickFrequency);


deleteTitle = (TextView)findViewById(R.id.settingsmenudeletetitle);
//deleteTitle.setOnClickListener(clickDelete);
deleteLayout = (LinearLayout)findViewById(R.id.settingsmenudeletelayout);
deleteLayout.setOnClickListener(clickDelete);
resetLayout = (LinearLayout)findViewById(R.id.settingsmenuresetlayout);
resetLayout.setOnClickListener(clickReset);

powerLayout = (LinearLayout)findViewById(R.id.settingsmenupowerlayout);
powerLayout.setOnClickListener(clickPower);


serviceLayout.setOnClickListener(clickService);
//BackgroundService
//OK, No Connectivity
serviceStatus = (TextView)findViewById(R.id.settingsmenubackgroundservicesubtext);

*/
//Google Account
//OK, Log In Again

//
//SQLiteDatabase sql = openOrCreateDatabase("db",0,null);

//try{
//	
//	if(mIRemoteService!=null){
//		serviceStatus.setText("Service Functioning");
//		if(mIRemoteService.isVoiceConnected()){
//			serviceStatus.setText("Running");
//			googlesubTitle.setText("Connected");
//		}else{
//			serviceStatus.setText("Running");
//			googlesubTitle.setText("Disconnected");
//		}
//	}else{
//		serviceStatus.setText("Service Error");
//		googlesubTitle.setText("No Connexion");
//	}
//	
//	
	
	
//	
//	Cursor c = sql.rawQuery("SELECT status FROM STATUS WHERE key = 'google';", null);
//	
//	if(c!=null){
//		if(c.moveToNext()){
//			googlesubTitle.setText(c.getString(0));
//		}
//		c.close();
//	}
//	
//	 c = sql.rawQuery("SELECT status FROM STATUS WHERE key = 'service';", null);
//		if(c!=null){
//			if(c.moveToNext()){
//				long time =(new Date()).getTime();
//				
//				long servicetime = Long.parseLong(c.getString(0));
//				
//				if(time-servicetime<300000){
//					serviceStatus.setText("Service Functioning");
//				}else{
//					serviceStatus.setText("Service Error");
//				}
//			}
//			c.close();
//		}
//	sql.close();
//}catch(Exception e){
//	Log.e(TAG,"On Create");
//	e.printStackTrace();
//}finally{
//	sql.close();
//}

//String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
//		"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//		"Linux", "OS/2" };
//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//		android.R.layout.simple_list_item_1, values);
//setListAdapter(adapter);
////setListAdapter()

//String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
//		"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//		"Linux", "OS/2" };
// Use your own layout
//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//		R.layout.rowlayout, R.id.title, values);
//setListAdapter(adapter);


//TextView googleTitle;
//TextView googlesubTitle;
//LinearLayout googleLayout;
//TextView frequencyTitle;
//TextView frequencysubTitle;
//LinearLayout frequencyLayout;
//TextView deleteTitle;
//TextView deletesubTitle;
//LinearLayout deleteLayout;
//LinearLayout serviceLayout;
//
//LinearLayout resetLayout;
//LinearLayout powerLayout;
//
//TextView serviceStatus;

