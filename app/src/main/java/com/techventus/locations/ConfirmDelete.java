package com.techventus.locations;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ConfirmDelete extends Activity{

	String TAG = "TECHVENTUS - ConfirmDelete";
	
	Button confirmResetButton;
	Button cancelResetButton;
	String locationName = "";
	

	 SharedPreferences preferences ;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		 
		  preferences = getSharedPreferences(Settings.PREFERENCENAME, 0);
		
		if(preferences.getBoolean(Settings.SERVICE_ENABLED, true)){
		    Intent hello_service = new Intent(this, BackgroundService.class);
			bindService( hello_service, mConnection, Context.BIND_AUTO_CREATE);
		}
		
		
		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 
		setContentView(R.layout.resetconfirm);
		
		Bundle receivedBundle =getIntent().getExtras();
		
		locationName =receivedBundle.getString(Settings.LOCATION_NAME_EXTRA/*"locationName"*/);
		
		confirmResetButton = (Button)findViewById(R.id.confirmReset);
		cancelResetButton  = (Button)findViewById(R.id.cancelReset);
		
		confirmResetButton.setText("Confirm "+locationName+" Delete");
		
		confirmResetButton.setOnClickListener(confirmResetClick);
		cancelResetButton.setOnClickListener(cancelResetClick);
	}
	
	
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
	
	OnClickListener confirmResetClick = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			Toast.makeText(ConfirmDelete.this, "Deleting Location "+locationName+"...", Toast.LENGTH_LONG);
			
			SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
			sql.execSQL("DELETE FROM LOCATIONPHONEENABLE WHERE locationName = '"+locationName+"';");
			sql.close();
				
			if(mIRemoteService!=null)
			{
				try
				{
					mIRemoteService.restart();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		
			ConfirmDelete.this.finish();
		}
		
	};
	
	OnClickListener cancelResetClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			
			ConfirmDelete.this.finish();
		}
		
	};
	
	
	@Override 
	public void onResume(){
		super.onResume();
		if(preferences.getBoolean(Settings.SERVICE_ENABLED, true)){
		    Intent hello_service = new Intent(this, BackgroundService.class);
			bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
		}

	}

	@Override
	public void onPause(){
		try{
			unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		this.finish();
		super.onPause();
	}
	

	
}
