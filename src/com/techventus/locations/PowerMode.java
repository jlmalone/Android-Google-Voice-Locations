package com.techventus.locations;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
@Deprecated
public class PowerMode extends Activity{
	
	String TAG = "TECHVENTUS - PowerMode";
	
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
	
	
	
	Button cancelButton;
	Button confirmButton;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
//	    
//	    Intent hello_service = new Intent(this, LocationService.class);
//	    
//		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
//		
//		
	    
	    
	    setContentView(R.layout.locationmode);
	    cancelButton = (Button) this.findViewById( R.id.cancelPowerChange);
	    cancelButton.setOnClickListener(closeClick);

	    Spinner powerSpin = (Spinner) findViewById(R.id.powerspinner);
	    ArrayAdapter powerAdapter = ArrayAdapter.createFromResource(
	            this, R.array.power, android.R.layout.simple_spinner_item);
	    powerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    powerSpin.setAdapter(powerAdapter);
	    
	    
	    Spinner accuracySpin = (Spinner) findViewById(R.id.accuracyspinner);
	    ArrayAdapter accuracyAdapter = ArrayAdapter.createFromResource(
	            this, R.array.accuracy, android.R.layout.simple_spinner_item);
	    accuracyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    accuracySpin.setAdapter(accuracyAdapter);
	    
	    
	    String[] powac = getPowerAccuracy();
	    
	    if(powac[0].equals("POWER_LOW")){
	    	powerSpin.setSelection(0);
	    }else if(powac[0].equals("POWER_MEDIUM")){
	    	powerSpin.setSelection(1);
	    }else if(powac[0].equals("POWER_HIGH")){
	    	powerSpin.setSelection(2);
	    }else if(powac[0].equals("NO_REQUIREMENT")){
	    	powerSpin.setSelection(3);
	    }
	    
	    
	    if(powac[1].equals("ACCURACY_COURSE")){
	    	accuracySpin.setSelection(0);
	    }else if(powac[1].equals("ACCURACY_FINE")){
	    	accuracySpin.setSelection(1);
	    }
	    
	    
	    confirmButton = (Button) this.findViewById( R.id.confirmPowerChange);
	    confirmButton.setOnClickListener(confirmClick(powerSpin,accuracySpin));
	}
	
	
	OnClickListener closeClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			PowerMode.this.finish();
		}
		
	};
	
	OnClickListener confirmClick(final Spinner powerSpin, final Spinner accuracySpin){
	
		OnClickListener ocl = new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				System.out.println("Confirm Button Click");
				System.out.println(powerSpin.getAdapter().getItem(powerSpin.getSelectedItemPosition()));
				System.out.println(accuracySpin.getAdapter().getItem(accuracySpin.getSelectedItemPosition()));
				try{
					
					//TODO SET PowerMode and Accuracy Through SharedPreferences
					
//					mIRemoteService.setPowerMode((String)(powerSpin.getAdapter().getItem(powerSpin.getSelectedItemPosition())));
//					mIRemoteService.setAccuracy((String)accuracySpin.getAdapter().getItem(accuracySpin.getSelectedItemPosition()));
					
					
//			       SQLiteDatabase sql =  openOrCreateDatabase("db",0,null);
//			        sql.execSQL("UPDATE STATUS SET value = '"+powerSpin.getAdapter().getItem(powerSpin.getSelectedItemPosition())+"' WHERE KEY = 'power';");
			        //SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//			        sql.execSQL("UPDATE STATUS SET value = '"+accuracySpin.getAdapter().getItem(accuracySpin.getSelectedItemPosition())+"' where key = 'accuracy';");
//			        sql.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				PowerMode.this.finish();
				
			}
			
		};
		return ocl;
	}
	
	
	String[] getPowerAccuracy(){
		String power = "";
		String accuracy = "";
		try{
			
			//TODO GET POWER MODE AND ACCURACY THROUGH SHARED PREFS
			
//			power = mIRemoteService.getPowerMode();
//			accuracy= mIRemoteService.getAccuracy();
//			SQLiteDatabase sql =  openOrCreateDatabase("db",0,null);
//			Cursor c = sql.rawQuery("SELECT * FROM STATUS WHERE key = 'power';", null);
//			while(c.moveToNext()){
//				 power = c.getString(2);
//				 System.out.println(power);
//			}
//			c.close();
//			c  = sql.rawQuery("SELECT * FROM STATUS WHERE key = 'accuracy';", null);
//			while(c.moveToNext()){
//				 accuracy = c.getString(2);
//				 System.out.println(accuracy);
//			}
//			c.close();
//			sql.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		String[] ret = {power,accuracy};
		return ret;
		 //  sql.execSQL("UPDATE SETTINGS SET VALUE = '"+powerSpin.getAdapter().getItem(powerSpin.getSelectedItemPosition())+"' WHERE KEY = 'POWER';");
	        //SQLHelper.listPhoneAndLocationPhoneEnable(sql);
	       // sql.close();
	}
	
	
	

	@Override 
	public void onResume(){
		super.onResume();
		
	    Intent hello_service = new Intent(this, BackgroundService.class);
	    
		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onPause(){
		try{
			unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		try{
			unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}		
	
}
