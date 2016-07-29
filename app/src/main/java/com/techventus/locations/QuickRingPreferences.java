package com.techventus.locations;

import gvjava.org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.techventus.server.voice.Voice;
import com.techventus.server.voice.datatypes.AllSettings;
import com.techventus.server.voice.datatypes.Phone;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ProgressBar;

/**
 * The Class QuickRingPreferences. This Class allows instant manual updating of Ring Settings.
 * 
 */
public class QuickRingPreferences extends Activity{

	/** The TAG. */
	String TAG = "TECHVENTUS - QuickRingPreference";

	/** The voice. */
	Voice voice;

	/** The phones. */
	Phone[] phones;

	List<View> phoneViewList = new ArrayList<View>();

	/** The phone strings. */
//	String[] phoneStrings;
	
	
	/** The Outer Layout is a LinearLayout which is the container for Inflated Layouts to
	 * Enable and Disable Phones **/
	LinearLayout outerLayout;
	
	/** The preferences. */
	SharedPreferences preferences;
	
	
	ProgressBar pending;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.quickprefs);
	    
	    preferences   = getSharedPreferences(Settings.PREFERENCENAME, 0);
	       
	    pending = (ProgressBar)findViewById(R.id.progressBar);
	    
	    outerLayout = (LinearLayout)findViewById(R.id.phoneenablelayout);
	}

    /**
     * Minispacer.
     *
     * @return the text view
     */
    TextView minispacer(){
    	TextView ret = new TextView(this);
    	ret.setText("");
    	ret.setWidth(5);
    	return ret;
    }
	
    /**
     * Spacer.
     *
     * @return the text view
     */
    TextView spacer(){
    	TextView ret = new TextView(this);
    	ret.setText("");
    	ret.setWidth(20);
    	return ret;
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
					QuickRingPreferences.this.finish();
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
		    		Intent intent = new Intent(QuickRingPreferences.this,LoginCredentials.class);
		    		startActivity(intent);
		    		QuickRingPreferences.this.finish();
				}else{
					setPhonesTask(false).execute();
				}

			}
		};
		return ret;
		
	}
	
	
	/**
	 * Sets the phones task.
	 *
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
				Toast.makeText(getApplicationContext(), "ERROR UPDATING PHONES. CHECK NETWORK AND TRY AGAIN ", Toast.LENGTH_LONG);
			}
		};
		return ret;
	}

	/**
	 * Sets the Phone Variable and Repopulates the GUI
	 *
	 * @return the async task
	 */
	AsyncTask<Void, Void, Boolean> setPhonesTask(final boolean update){ 
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
				if(!result){
					Toast.makeText(QuickRingPreferences.this, "SET PHONES FROM GOOGLE VOICE FAILED\nCHECK CONNECTIVITY AND SETTINGS AND TRY AGAIN...", Toast.LENGTH_LONG).show();
//					networkExit();
				}else{
					populatePhoneLayout(false);
//					setPhoneStatus();
				}
			}
		};
		return ret;
	}
	
	
	/**
	 * Updates the Actual Google Voice Ring Status.
	 *
	 * @return the async task
	 */
	AsyncTask<Void, Void, Boolean> ringEnableDisableTask(final int phoneId, final boolean enable){ 
		AsyncTask<Void,Void,Boolean> ret= new AsyncTask<Void,Void,Boolean>(){

			@Override
			protected void onPreExecute() {
			
				if(isNetworkConnected())
					return;
				else{
					this.cancel(true);
					networkExit();

				}
				
			}
			
			@Override
			protected Boolean doInBackground(Void... arg0) {	
				if(!this.isCancelled()){
					if(voice!=null){
						try {
							if(enable)
								voice.phoneEnable(phoneId);
							else {
								voice.phoneDisable(phoneId);
							}
							refreshVariables();
							return true;
						} catch (IOException e) {
							Toast.makeText(getApplicationContext(), "CONNECTION ERROR - SETTING LIKELY NOT CHANGED.  CHECK SETTINGS AND NETWORK CONNECTION", Toast.LENGTH_LONG).show();
							e.printStackTrace();
							return false;
						}
					}else{
						return false;
					}
				}else
					return false;
				
			}
			@Override
			protected void onPostExecute(Boolean result){
				if(!result){
					Toast.makeText(getApplicationContext(), "ERROR - CANNOT CONNECT TO GOOGLE VOICE. CHECK SETTINGS AND TRY AGAIN", Toast.LENGTH_LONG).show();
					
					//Toast.makeText(QuickRingPreferences.this, "UPDATE PHONE STATUS IN GOOGLE VOICE FAILED GOOGLE VOICE FAILED\nCHECK CONNECTIVITY AND SETTINGS AND TRY AGAIN...", Toast.LENGTH_LONG).show();
					// Consider Timed Activity finish.

				}else{
//					populatePhoneLayout(false);
//					setPhoneStatus();

				}
			}
		};
		return ret;
	}
	
	private void networkExit(){
		if(!isNetworkConnected()){
			Toast.makeText(getApplicationContext(), "Network Not Connected\nReestablish and Try Again\nExiting Activity...", Toast.LENGTH_LONG).show();
			QuickRingPreferences.this.finish();
			Log.e(TAG, "Exiting owing to Bad Network Connection!");
		}
	}
	
	/**
	 * Populate phone layout.
	 *
	 * @return true, if successful
	 */
	private boolean populatePhoneLayout(boolean update){
		AllSettings settings = null;
		try {
			settings = voice.getSettings(update);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		if(phones!=null && phones.length>0){
		
			pending.setVisibility(View.GONE);
			synchronized(this){
			for(Phone phone:phones){
				
				View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.quickprefsitem,
						null);

				TextView phoneName = (TextView)view.findViewById(R.id.phoneName);
				phoneName.setText(phone.getName());

				final ToggleButton toggle = (ToggleButton)view.findViewById(R.id.enableToggle);

				final Phone thisphone = phone;

				toggle.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						serviceDisable();
						int phoneId =thisphone.getId();
						ringEnableDisableTask(phoneId,toggle.isChecked()).execute();
					}
				});
				boolean disabled = settings.isPhoneDisabled(phone.getId());
				if(!disabled){
					toggle.setChecked(true);
				}else{
					toggle.setChecked(false);
				}
				phoneViewList.add(view);
				outerLayout.addView(view);
			}
			}//synchronized
		}

		return true;
	}

	

	
	/**
	 * Service disable.
	 */
	void serviceDisable(){
		Editor edit = preferences.edit();
		edit.putBoolean(Settings.SERVICE_ENABLED, false);
		edit.commit();
	}
	
	
	/**
	 * Instantiates the phones Array object and the phoneStrings Array.
	 *
	 * @return true, if successful
	 */
	private boolean setPhones(boolean update){
		boolean ret = false;
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
				//LAUNCH DEFAULT DIALER
				ret = false;
				e.printStackTrace();
//				launchDefaultDialer();
			}
		}else{
			ret = false;
		}
		return ret;
	}
	

	void refreshVariables(){
		//New Settings Object
		
		//New Phones Object
		setPhonesVariablesTask(true).execute();
		
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
		    			Intent i = new Intent(QuickRingPreferences.this, LoginCredentials.class);
		    			preferences.edit().remove("username").remove("password").apply();
		    			
		    			startActivity(i);
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
		    		return true;
		    	}else{
		    		//Launch Other Activity
		    		return false;
		    	}
			}
		}
		return false;
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
//			Log.d(TAG, "connected");
			return true;
		}else{
//			Log.d(TAG, "not connected");
			return false;
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override 
	public void onResume(){
		super.onResume();
		synchronized(this){
			for(View view:phoneViewList){
				outerLayout.removeView(view);
			}
			phoneViewList.clear();
		}
		pending.setVisibility(View.VISIBLE);
		if(isNetworkConnected()){
			setVoiceTask().execute();
		}else{
			networkExit();
		}

	}

	
}
