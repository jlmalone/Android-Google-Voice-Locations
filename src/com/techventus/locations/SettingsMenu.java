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
	

	private static final String TAG = "TECHVENTUS - SETTINGSMENU";

	ToggleButton serviceEnableToggle ;
	ToggleButton startupEnableToggle ;
	SharedPreferences preferences;
	

	boolean isServiceEnabled;
	boolean isStartupEnabled;

	Settings mSettings;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		mSettings = Settings.getInstance();
		setContentView(R.layout.settingsmenu);
		
		preferences   = SettingsMenu.this.getSharedPreferences(Settings.PREFERENCENAME, 0);
        
		isServiceEnabled = preferences.getBoolean(Settings.SERVICE_ENABLED, false);
		isStartupEnabled = preferences.getBoolean(Settings.STARTUP_ENABLED, false);

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

	    SimpleCursorAdapter data =
	        new SimpleCursorAdapter(this,
	                R.layout.rowlayout,
	                cursor,
	                columns,
	                layouts);
        Log.v(TAG, "SET LIST ADAPTER");

	    setListAdapter( data );
        Log.v(TAG, "SET LIST ADAPTER Length "+data.getCount());

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
    {
		
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
			}
			default:
				break;
		}

	}
	
	
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
		if(resultCode==999){
			mSettings.setRestartServiceFlag(true);

		}
    }
			@Override
			protected void onResume(){
				super.onResume();
				
			}

			
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
					mSettings.setRestartServiceFlag(true);
					Intent serviceIntent = new Intent(this,BackgroundService.class);
					startService(serviceIntent);
				}else{
					//STOP SERVICE EVERY WAY IMAGINABLE

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



