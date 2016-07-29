package com.techventus.locations;


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
	int LOCATION_FREQUENCY = 724;
	int GOOGLE_SYNC = 725;


	boolean RESET_SERVICE_FLAG =false;
	
	
	
	protected void onActivityResult(int requestCode, int resultCode,Intent data)
	{
            if (resultCode == RESULT_OK) {
            	displayValues();
            	setResult(999);
            	//RESET BACKGROUND SERVICE TO REFLECT NEW VALUES
         		RESET_SERVICE_FLAG = true;
            }
    }
	
	
	private void displayValues(){
		
		int location_frequency = settings.getInt("LOCATION_FREQUENCY", 5);

		if(location_frequency==-1)
		{
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
	

	@Override
	public void onDestroy(){
		try{
			if(this.RESET_SERVICE_FLAG){
				setResult(999);
				finish();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}		
}