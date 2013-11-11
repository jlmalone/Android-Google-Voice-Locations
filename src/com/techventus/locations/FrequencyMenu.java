package com.techventus.locations;




//import android.content.ServiceConnection;
//import android.os.IBinder;
//import android.content.ComponentName;
//import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FrequencyMenu extends Activity{

	
	String PREFERENCENAME = "TECHVENTUS";
	SharedPreferences settings;
	String TAG = "TECHVENTUS - FrequencyMenu";
	
	TextView locationmethodvalue;
	TextView locationfreqvalue;
	TextView powerlevelvalue;
	TextView googlesyncvalue;
	
	Button locationfreqbutton;
	Button locationmethodbutton;
	Button googlesyncbutton;
	Button powerlevelbutton;

	
	
	//REQUEST CODES (may be extraneous)
	int LOCATION_METHOD = 723;
	int LOCATION_FREQUENCY = 724;
	int GOOGLE_SYNC = 725;
	int POWER_LEVEL = 726;
	

//	GVLServiceInterface mIRemoteService;
	
	boolean RESET_SERVICE_FLAG =false;
	
	
//	private ServiceConnection mConnection = new ServiceConnection() {
//	    // Called when the connection with the service is established
//	    public void onServiceConnected(ComponentName className, IBinder service) {
//	        // Following the example above for an AIDL interface,
//	        // this gets an instance of the IRemoteInterface, which we can use to call on the service
//	        mIRemoteService = GVLServiceInterface.Stub.asInterface(service);
//	   
//	    }
//
//	    // Called when the connection with the service disconnects unexpectedly
//	    public void onServiceDisconnected(ComponentName className) {
//	        Log.e(TAG, "Service has unexpectedly disconnected");
//	        mIRemoteService = null;
//	    }
//	};

	
	
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
      //  if (requestCode == LOCATION_METHOD) {
	      //  }
            if (resultCode == RESULT_OK) {
            	displayValues();
            	setResult(999);
            	//RESET BACKGROUND SERVICE TO REFLECT NEW VALUES
         		RESET_SERVICE_FLAG = true;
            }
    }
	
	
	private void displayValues(){
		
		int location_frequency = settings.getInt("LOCATION_FREQUENCY", 5);

		if(location_frequency==-1){
			locationfreqvalue.setText("OFF");
		}else{
			locationfreqvalue.setText(location_frequency+"m");
		}
		
		
		 locationmethodvalue.setText(String.valueOf(settings.getString("LOCATION_METHOD", "NETWORK")));
		 powerlevelvalue.setText(String.valueOf(settings.getString("POWER_LEVEL", "LOW")));

		int google_sync =  settings.getInt("GOOGLE_SYNC", 60);
		 if(google_sync==-1){
			 googlesyncvalue.setText("On Startup");
		 }else{
			 googlesyncvalue.setText(google_sync+"m");
		 }
		 

	}
	
	
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		this.setContentView(R.layout.frequencymenu);

		settings = this.getSharedPreferences(PREFERENCENAME, 0);

		 locationfreqvalue = (TextView)findViewById(R.id.locationfreqvalue);
		 locationmethodvalue= (TextView)findViewById(R.id.locationmethodvalue);
		 powerlevelvalue= (TextView)findViewById(R.id.powerlevelvalue);
		 googlesyncvalue= (TextView)findViewById(R.id.googlesyncvalue);
		 
		 locationfreqbutton = (Button)findViewById(R.id.locationfreqbutton);
		 locationmethodbutton = (Button)findViewById(R.id.locationmethodbutton);
		 powerlevelbutton = (Button)findViewById(R.id.powerlevelbutton);
		 googlesyncbutton = (Button)findViewById(R.id.googlesyncbutton);
		

		 //ADD CLICK LISTENERS
		 locationfreqbutton.setOnClickListener(this.clicklistener);
		 locationmethodbutton.setOnClickListener(this.clicklistener);
		 powerlevelbutton.setOnClickListener(this.clicklistener);
		 googlesyncbutton.setOnClickListener(this.clicklistener);
		 displayValues();
	}
	
	OnClickListener closeClick = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			FrequencyMenu.this.finish();
		}
	};
	
	OnClickListener clicklistener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			int id = view.getId();
			switch(id) {
				case R.id.locationfreqbutton:{
					Intent i = new Intent(FrequencyMenu.this,LocationFrequencyToggle.class);
					startActivityForResult(i,LOCATION_FREQUENCY);
					break;
				}
				case R.id.powerlevelbutton:
					
					break;
				case R.id.locationmethodbutton:
					
					break;
				case R.id.googlesyncbutton:{
					Intent i = new Intent(FrequencyMenu.this,GoogleFrequencyToggle.class);
					startActivityForResult(i,GOOGLE_SYNC);
					break;
				}
				default:
					Log.e(TAG, "Error: DEFAULT Click switch ought not be called!!");
			}
		}
		
	};
	
	

//	
//	@Override 
//	public void onResume(){
//		super.onResume();
//		

//	}

//	@Override
//	public void onPause(){
//		try{
//			unbindService(mConnection);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		super.onPause();
//	}
//	
	@Override
	public void onDestroy(){
		try{
			if(this.RESET_SERVICE_FLAG){
				//Intent intent = new Intent();
				setResult(999);
				finish();
//				setResult()
//			    Intent hello_service = new Intent(this, LocationService.class);
//			    
//				bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
//				if(settings.getBoolean("ENABLED", true))
//					mIRemoteService.reset();
			}
		//	unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}		
}






//
////
//OnClickListener destroyClick(final String locationName){
//	
//	OnClickListener ret = new OnClickListener(){
//	
//
//		@Override
//		public void onClick(View v) {
//			try{
//				
//				Bundle b = new Bundle();
//				b.putString("locationName", locationName);
//				Intent i = new Intent(FrequencyMenu.this,ConfirmDelete.class);
//				i.putExtras( b);
//				FrequencyMenu.this.startActivity(i);
//			
//				FrequencyMenu.this.finish();
//
//				
//				
//				//SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
//				//sql.execSQL("DELETE FROM LOCATIONS WHERE locationName = '"+locationName+"';");
//				//sql.close();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			//ADD MORE SHIT LATER
//			FrequencyMenu.this.finish();
//		}
//		
//	};
//	
//	return ret;
//}
//
//private String[] getStringArSQLVertical(String query, SQLiteDatabase sql){
//	String[] ret = new String[0];
//	try{
//		Cursor c = sql.rawQuery(query, null);
//		List<String> list = new ArrayList<String>();
//		if(c!=null){
//			while(c.moveToNext()){
//				list.add(c.getString(0));
//				Log.e("TECHVENTUS","DIRECT FROM QUERY +"+c.getString(0));
//			}
//			if(list.size()>0){
//				ret = list.toArray(new String[list.size()]);
//				//for(int i=0;i<list.size();i++){
//				//	ret[i] = list.get(i);
//				//}
//			}
//			c.close();
//		}
//		
//
//	}catch(Exception o){
//		o.printStackTrace();
//	}
//	return ret;
//}
