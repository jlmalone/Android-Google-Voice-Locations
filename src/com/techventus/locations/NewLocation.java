package com.techventus.locations;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class NewLocation extends Activity{

	String TAG = "TECHVENTUS - NewLocation";
	
	 Button saveButton;
	 Button cancelButton;
	 EditText locationBox;
	 EditText metresBox;
	AdView mAdView;


	@Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 
        setContentView(R.layout.namenewlocation);


        mAdView= (AdView)this.findViewById(R.id.ad);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        mAdView.loadAd(adRequest);

        Log.e("TECHVENTUSPHONE","NEWLOCATION MENU - List Phone etc...");

        
        saveButton = (Button)findViewById(R.id.NewLocationSaveButton);
        saveButton.setOnClickListener(saveButtonListener);
        
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
					i.putExtra(Settings.BundleKey.LOCATION_NAME_EXTRA/*"locationName"*/, locationBox.getText().toString().replace("'","" ));
					i.putExtra(Settings.BundleKey.RADIUS_EXTRA/*"radius"*/, 100);
					
					System.out.println(i.getStringExtra(Settings.BundleKey.LOCATION_NAME_EXTRA/*"locationName"*/));
					//i.putExtra("location", bundle);
				}else{
					//Bundle bundle = new Bundle();
					i.putExtra(Settings.BundleKey.LOCATION_NAME_EXTRA/*"locationName"*/, locationBox.getText().toString().replace("'","" ));
					
					i.putExtra(Settings.BundleKey.RADIUS_EXTRA/*"radius"*/, Integer.valueOf(metresBox.getText().toString()));
					//i.putExtra("location", bundle);
					System.out.println(i.getStringExtra(Settings.BundleKey.LOCATION_NAME_EXTRA));
				}
				startActivity(i);
				NewLocation.this.finish();
			}else{
				Toast.makeText(NewLocation.this,"Location Cannot Be Null",Toast.LENGTH_SHORT);
			}
		}
	};
}