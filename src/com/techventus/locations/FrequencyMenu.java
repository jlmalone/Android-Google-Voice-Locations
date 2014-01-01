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
	
//	TextView locationmethodvalue;
//	TextView locationfreqvalue;
//	TextView powerlevelvalue;
	TextView googlesyncvalue;

//	Button locationfreqbutton;
//	Button locationmethodbutton;
	Button googlesyncbutton;
//	Button powerlevelbutton;

	
	
	//REQUEST CODES (may be extraneous)

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
		
//		int location_frequency = settings.getInt("LOCATION_FREQUENCY", 5);



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


		 googlesyncvalue= (TextView)findViewById(R.id.googlesyncvalue);
		 

		 googlesyncbutton = (Button)findViewById(R.id.googlesyncbutton);
		

		 //ADD CLICK LISTENERS
		 googlesyncbutton.setOnClickListener(this.clicklistener);
		 displayValues();
	}
	
	OnClickListener clicklistener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			int id = view.getId();
			switch(id)
			{


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