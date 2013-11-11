package com.techventus.locations;


import android.app.Activity;
//import android.content.ComponentName;
//import android.content.Context;
import android.content.Intent;
//import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
//import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
//import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class NewLocation extends Activity{

	String TAG = "TECHVENTUS - NewLocation";
	
	 Button saveButton;
	 Button cancelButton;
	 EditText locationBox;
	 EditText metresBox;
	 


	 
	 
	@Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
//	    Intent hello_service = new Intent(this, LocationService.class);
//		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 
        setContentView(R.layout.namenewlocation);

       // LinearLayout layout = (LinearLayout) findViewById(R.id.namenewlocation);
        Log.e("TECHVENTUSPHONE","NEWLOCATION MENU - List Phone etc...");
//        SQLiteDatabase sql =  openOrCreateDatabase("db",0,null);
//        SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//        sql.close();
        
        saveButton = (Button)findViewById(R.id.NewLocationSaveButton);
        saveButton.setOnClickListener(saveButtonListener);
        
    	//ClickListener saveHandle = new ClickListener(){
        
        cancelButton= (Button)findViewById(R.id.NewLocationCancelButton);
        cancelButton.setOnClickListener(cancelButtonListener);
        
        
        locationBox = (EditText)findViewById(R.id.namenewlocationedittext);
        metresBox = (EditText)findViewById(R.id.newlocationmetresbox);
        
        
    	};
	
    OnClickListener cancelButtonListener = new OnClickListener(){

    		@Override
    		public void onClick(View arg0) {
    			try {
    				
					NewLocation.this.finish();
					
				} catch (Throwable e) {
					e.printStackTrace();
				}
    			
    		}
    	};

	OnClickListener saveButtonListener = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			if(!(locationBox.getText().toString()).equals("")){
				
				Intent i =new Intent(NewLocation.this,LocationMap.class);
				
				i.putExtra("isNew", true);
				
				if((metresBox.getText().toString()).equals("")){
					//Bundle bundle = new Bundle();
					i.putExtra(Settings.LOCATION_NAME_EXTRA/*"locationName"*/, locationBox.getText().toString().replace("'","" ));
					i.putExtra(Settings.RADIUS_EXTRA/*"radius"*/, 100);
					
					System.out.println(i.getStringExtra(Settings.LOCATION_NAME_EXTRA/*"locationName"*/));
					//i.putExtra("location", bundle);
				}else{
					//Bundle bundle = new Bundle();
					i.putExtra(Settings.LOCATION_NAME_EXTRA/*"locationName"*/, locationBox.getText().toString().replace("'","" ));
					
					i.putExtra(Settings.RADIUS_EXTRA/*"radius"*/, Integer.valueOf(metresBox.getText().toString()));
					//i.putExtra("location", bundle);
					System.out.println(i.getStringExtra(Settings.LOCATION_NAME_EXTRA));
				}
				startActivity(i);
				NewLocation.this.finish();
			}else{
				Toast.makeText(NewLocation.this,"Location Cannot Be Null",Toast.LENGTH_SHORT);
			}
		}
	};
	
}






//
//@Override 
//public void onResume(){
//	super.onResume();
//	
//    Intent hello_service = new Intent(this, LocationService.class);
//    
//	bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
//}

//@Override
//public void onPause(){
//	try{
//		unbindService(mConnection);
//	}catch(Exception e){
//		e.printStackTrace();
//	}
//	super.onPause();
//}

//@Override
//public void onDestroy(){
//	try{
//		unbindService(mConnection);
//	}catch(Exception e){
//		e.printStackTrace();
//	}
//	super.onDestroy();
//}		
//




//GVLServiceInterface mIRemoteService;
//private ServiceConnection mConnection = new ServiceConnection() {
//    // Called when the connection with the service is established
//    public void onServiceConnected(ComponentName className, IBinder service) {
//        // Following the example above for an AIDL interface,
//        // this gets an instance of the IRemoteInterface, which we can use to call on the service
//        mIRemoteService = GVLServiceInterface.Stub.asInterface(service);
//    }
//
//    // Called when the connection with the service disconnects unexpectedly
//    public void onServiceDisconnected(ComponentName className) {
//        Log.e(TAG, "Service has unexpectedly disconnected");
//        mIRemoteService = null;
//    }
//};


