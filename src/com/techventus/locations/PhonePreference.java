package com.techventus.locations;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import gvjava.org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.techventus.server.voice.Voice;
import com.techventus.server.voice.datatypes.AllSettings;
import com.techventus.server.voice.datatypes.Phone;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


/**
 * The Class PhonePreference.  This class uses a bespoke mimicry of the radioGroup functionality since
 * actual RadioGroups cannot be properly set inside of TableRows.
 * 
 */
public class PhonePreference extends Activity{
	
	/** The TAG. */
	String TAG = "TECHVENTUS - PhonePreference";

	/** The location name. */
	String locationName ;
	
	/** The radius. */
	int radius=100;
	
	/** The latitude. */
	int latitude=-1;
	
	/** The longitude. */
	int longitude=-1;
	
	/** The cancel button. */
	Button cancelButton;
	
	/** The save button. */
	Button saveButton;
	
	/** The voice. */
	Voice voice;

	/** The phones. */
	Phone[] phones;
	
	/** The table. */
	TableLayout table;

	/** The preferences. */
	SharedPreferences preferences;
	

	/** The m i remote service. */
	GVLServiceInterface mIRemoteService;
	
	/** The m connection. */
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


    AdView mAdView;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.phonepreferences);

        mAdView= (AdView)this.findViewById(R.id.ad);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        mAdView.loadAd(adRequest);

		preferences = getSharedPreferences(Settings.PREFERENCENAME, 0);
		
		try{
			Bundle receivedBundle = getIntent().getExtras();
			locationName =receivedBundle.getString(Settings.LOCATION_NAME_EXTRA/*"locationName"*/);
			radius = receivedBundle.getInt(Settings.RADIUS_EXTRA);
			latitude = receivedBundle.getInt(Settings.LATITUDE_EXTRA);
			longitude = receivedBundle.getInt(Settings.LONGITUDE_EXTRA);	
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), "EXCEPTION - DATA NOT IN BUNDLE", Toast.LENGTH_LONG);
			this.finish();
		}

		saveButton = (Button)findViewById(R.id.save);
		cancelButton = (Button)findViewById(R.id.cancelButton);
		table = (TableLayout )findViewById(R.id.table);
		saveButton.setOnClickListener(saveClick);
		cancelButton.setOnClickListener(cancelClick);
	}
	
	
	/** The save click. */
	OnClickListener saveClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			
			//TODO - REFACTOR TO HANDLE EXCEPTION IN INSERT
			SQLiteDatabase db = null;
			try{
				
				db=  openOrCreateDatabase("db",0,null);
				SQLHelper.exec(db, SQLHelper.createLocationPhoneEnable);
				SQLHelper.exec(db, "DELETE FROM LOCATIONPHONEENABLE WHERE locationName = '"+locationName+"'");
				String[] commands = prepareInsertArray();
				for(String command:commands)
					SQLHelper.exec(db,command );	
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(db!=null)
					db.close();
			}

			
			if(mIRemoteService!=null){
				try {
					mIRemoteService.restart();
				} catch (RemoteException e) {
					//TODO Maybe Handle
					e.printStackTrace();
				}
			}

			PhonePreference.this.finish();
			
		}
	};
	

    
    /** The cancel click. */
    OnClickListener cancelClick = new OnClickListener(){
		@Override
		public void onClick(View v) {
			
			finish();	
		}
    };
	

    

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override 
	public void onResume(){
		super.onResume();
		
 	   if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
 		   Intent hello_service2 = new Intent(this, BackgroundService.class);
 		   bindService( hello_service2, mConnection,Context.BIND_DEBUG_UNBIND);
 	   }
 	   
 	   setVoiceTask().execute();

	}
	

	/**
	 * Sets the phones task.
	 *
	 * @param update the update
	 * @return the async task
	 */
	AsyncTask<Void, Void, Boolean> setPhonesVariablesTask(final boolean update){ 
		AsyncTask<Void,Void,Boolean> ret= new AsyncTask<Void,Void,Boolean>(){
			@Override
			protected void onPreExecute() {
			
				if(isNetworkConnected())
					return;
				else{
					networkExit();
					this.cancel(true);
				}
			}
			@Override
			protected Boolean doInBackground(Void... arg0) {	
				return setPhones(update);
			}
			@Override
			protected void onPostExecute(Boolean result){
				if(result){
					setPhoneChoiceDisplay();
				}else{
					Toast.makeText(getApplicationContext(), "ERROR UPDATING PHONES. CHECK NETWORK AND TRY AGAIN ", Toast.LENGTH_LONG);
					PhonePreference.this.finish();
				}
			}
		};
		return ret;
	}
	
	
	 /** The phone rows. */
 	private List<TableRowPref> phoneRows = new ArrayList<TableRowPref>();
	
 	
 	
 	private Map<String, Integer> getPrefs(){
 		Map<String,Integer> ret = new HashMap<String,Integer>();
 		try{
		SQLiteDatabase db =  openOrCreateDatabase("db",0,null);
		SQLHelper.exec(db, SQLHelper.createLocationPhoneEnable);
		if(locationName!=null){
			Cursor c = db.query("LOCATIONPHONEENABLE", new String[]{"phoneName","phoneEnable" }
	        , "locationName = ?" ,new String[]{locationName}, null, null, null);
		if(c!=null)
			while(c.moveToNext())
		        {
					try{
						String phoneName = c.getString(c.getColumnIndex("phoneName"));
						Integer pref = c.getInt(c.getColumnIndex("phoneEnable"));
						ret.put(phoneName, pref);
					}catch(Exception e){
						e.printStackTrace();
					}
		        }
			c.close();
			
		}
		db.close();
 		}catch(Exception adsf){
 			adsf.printStackTrace();
 		}
		return ret;
 	}
 	
 	
	/**
	 * Sets the phone choice display.
	 */
	private void setPhoneChoiceDisplay(){
		for(TableRowPref row:phoneRows){
			table.removeView(row.row);
		}
		phoneRows.clear();
		Map<String,Integer> prefMap = getPrefs();
		if(phones!=null && phones.length>0){
			for(Phone phone:phones){
				
				TableRow row = (TableRow)LayoutInflater.from(getBaseContext()).inflate(R.layout.phonepreferencetablerow,
						null);
				TextView nameText = (TextView)row.findViewById(R.id.phoneName);
				final RadioButton enableButton = (RadioButton)row.findViewById(R.id.enableButton);
				final RadioButton disableButton = (RadioButton)row.findViewById(R.id.disableButton);
				final RadioButton neutralButton = (RadioButton)row.findViewById(R.id.neutralButton);

				nameText.setText(phone.getName());
				
				OnClickListener radioClick = new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						if(arg0.getId()==enableButton.getId()){
							enableButton.setChecked(true);
							disableButton.setChecked(false);
							neutralButton.setChecked(false);
						}else if(arg0.getId()==disableButton.getId()){
							enableButton.setChecked(false);
							disableButton.setChecked(true);
							neutralButton.setChecked(false);
						}else if(arg0.getId()==neutralButton.getId()){
							enableButton.setChecked(false);
							disableButton.setChecked(false);
							neutralButton.setChecked(true);
						}
					}
				};
				
				enableButton.setOnClickListener(radioClick);
				disableButton.setOnClickListener(radioClick);
				neutralButton.setOnClickListener(radioClick);
				TableRowPref rowpref = new TableRowPref(row,phone, enableButton, disableButton, neutralButton);
				if(prefMap.containsKey(rowpref.phone.getName())   ){
					rowpref.setEnableDisableValue(prefMap.get(rowpref.phone.getName()));
				}
				phoneRows.add(rowpref);
			}
		}
		for(TableRowPref row:phoneRows){
			table.addView(row.row);
		}
	}

	
	

	
	/**
	 * The Inner Class TableRowPref.  This class helps group Preferences
	 */
	private class TableRowPref{
		
		/** The row. */
		private TableRow row;
		
		/** The enable. */
		private RadioButton enable;
		
		/** The disable. */
		private RadioButton disable;
		
		/** The neutral. */
		private RadioButton neutral;
		
		/** The phone. */
		protected Phone phone;
		
		/**
		 * Instantiates a new table row pref.
		 *
		 * @param row the row
		 * @param phone the phone
		 * @param enable the enable
		 * @param disable the disable
		 * @param neutral the neutral
		 */
		public TableRowPref(TableRow row, Phone phone,  RadioButton enable, RadioButton disable, RadioButton neutral){
			this.row = row;
			this.enable = enable;
			this.disable = disable;
			this.neutral = neutral;
			this.phone = phone;
		}
		
		/**
		 * Gets the phone.
		 *
		 * @return the phone
		 */
		public Phone getPhone(){
			return phone;
		}
		
		/**
		 * Gets the enable disable value.
		 *
		 * @return the enable disable value
		 */
		public int getEnableDisableValue(){
			if(enable.isChecked()){
				return 1;
			}if(disable.isChecked()){
				return -1;
			}
			return 0;
		}
		
		public void setEnableDisableValue(int value){
			if(value==1){
				enable.setChecked(true);
				disable.setChecked(false);
				neutral.setChecked(false);
			}else if(value==-1){
				enable.setChecked(false);
				disable.setChecked(true);
				neutral.setChecked(false);
			}else if(value==0){
				enable.setChecked(false);
				disable.setChecked(false);
				neutral.setChecked(true);
			}else{
				enable.setChecked(false);
				disable.setChecked(false);
				neutral.setChecked(false);
			}
		}
		
	}
	
	
	
	/**
	 * Network exit.
	 */
	private void networkExit(){
		if(!isNetworkConnected()){
			Toast.makeText(getApplicationContext(), "Network Not Connected\nReestablish and Try Again\nExiting Activity...", Toast.LENGTH_LONG).show();
			this.finish();
			Log.e(TAG, "Exiting owing to Bad Network Connection!");
		}
	}
	

	/**
	 * Instantiates the phones Array object and the phoneStrings Array.
	 * Radio Buttons are populated with the values from Google Voice
	 *
	 * @param update the update
	 * @return true, if successful
	 */
	private boolean setPhones(boolean update){
		boolean ret;
		if(voice!=null){
			try {
				AllSettings settings = voice.getSettings(update);
				phones = settings.getPhones();
				if(phones!=null && phones.length>0){
					ret = true;
				}else{
					ret = false;
				}
			} catch (JSONException e) {
				ret = false;
				e.printStackTrace();
			} catch (IOException e) {
				ret = false;
				e.printStackTrace();
			} catch(Exception e){
				ret = false;
				e.printStackTrace();
			}
		}else{
			ret = false;
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause(){
		try{
			unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onPause();
	}
	
	
	/**
	 * Checks if is network connected.
	 *
	 * @return true, if is network connected
	 */
	private boolean isNetworkConnected(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
	
		if (ni!=null && ni.isConnected()){
			return true;
		}else{
			return false;
		}
	}
	

	/**
	 * Sets the voice.  This Method should only be called inside a 
	 * BackgroundTask since it makes HTTP calls.
	 *
	 * @return true, if successful
	 */
	private boolean setVoice(){
		
		voice=null;
		try {
			voice = VoiceSingleton.getVoiceSingleton().getVoice();
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
			if(voice==null){
				VoiceSingleton.reset();
				SharedPreferences preferences = this.getSharedPreferences(Settings.PREFERENCENAME, 0);
				String username = preferences.getString("username", "");
		    	String password = preferences.getString("password", "");
		    	if(!username.equals("")&&!password.equals("")){
		    		try {
						VoiceSingleton vs = VoiceSingleton.getOrCreateVoiceSingleton(username,Settings.decrypt(password, 10));
						voice = vs.getVoice();
		    		}catch (com.techventus.server.voice.exception.BadAuthenticationException ba){
			    			Toast.makeText(getApplicationContext(), "Google Credentials Authentication Error.", Toast.LENGTH_LONG).show();
			    			Intent i = new Intent(PhonePreference.this, LoginCredentials.class);
			    			preferences.edit().remove("username").remove("password").apply();
			    			startActivity(i);
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
		    		return true;
		    	}else{
		    		return false;
		    	}
			}
		}
		return false;
	}
	

	/**
	 * AsyncTask Background Task to Set the local Voice Object
	 * and check Google Voice connectivity. It is meant to be fired
	 * when the Activity Resumes (or is Created) after checking the Network
	 * State is Active and Connectable.
	 * If the successful Voice object is connectable and instantiated, the setPhonesTask,
	 * another AsyncTask meant to populate the Phones choices, 
	 * is launched onPostExecute.  If the Network is Connectable, but the 
	 * Google Voice cannot Log In, the Credentials Activity is Launched
	 *
	 * @return the AsyncTask
	 */
	AsyncTask<Void, Void, Boolean> setVoiceTask(){ 
		AsyncTask<Void,Void,Boolean> ret= new AsyncTask<Void,Void,Boolean>(){
			@Override
			protected void onPreExecute() {
			
				if(isNetworkConnected())
					return;
				else{
					Toast.makeText(getApplicationContext(), "NETWORK NOT CONNECTED - TRY AGAIN LATER", Toast.LENGTH_LONG).show();
					this.cancel(true);
					PhonePreference.this.finish();
				}
				
			}
			@Override
			protected Boolean doInBackground(Void... arg0) {	
				return setVoice();
			}
			@Override
			protected void onPostExecute(Boolean result){
				if(!result){
					Toast.makeText(getApplicationContext(), "No Voice Connection - CHECK NETWORK CONNECTIVITY - Exiting...", Toast.LENGTH_LONG);
		    		PhonePreference.this.finish();
				}else{
					setPhonesVariablesTask(false).execute();
				}

			}
		};
		return ret;
		
	}
	

	/**
	 * Prepare insert Array.  This method was originally written as a multiline insert but that was changed owing to
	 * indications that only single line inserts were supported.
	 *
	 * @return the string Array
	 */
	String[] prepareInsertArray(){
		String[] ret = new String[phoneRows.size()];
		String base = "INSERT OR REPLACE INTO LOCATIONPHONEENABLE (locationName , phoneName , phoneEnable , locationLatitudeE6 , locationLongitudeE6 , radius ) VALUES " ;
		int i=0;
		for(TableRowPref row:phoneRows){
			String stat = base;
			stat += "('"+locationName+"' , ";
			stat += "'"+row.getPhone().getName()+"' ," ;
			stat += row.getEnableDisableValue() +" , ";
			stat += this.latitude +" , ";
			stat += this.longitude +" , ";
			stat += this.radius +" ); ";
			ret[i]=stat;
			i++;
			
		}
		return ret;
	}

}
