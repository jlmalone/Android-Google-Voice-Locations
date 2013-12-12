package com.techventus.locations;

import java.io.IOException;

import android.app.Activity;


//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
//import android.os.IBinder;
//import android.os.RemoteException;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Class LoginCredentials.
 */
public class LoginCredentials extends Activity{
	
	private String TAG = "TECHVENTUS - LoginCredentials";
	SharedPreferences settings ;
	String PREFERENCENAME = "TECHVENTUS";
	EditText loginTextBox;
	EditText loginPassBox;
	

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.login); // bind the layout to the activity
		
		settings = getSharedPreferences(PREFERENCENAME, 0);
		
		
		loginTextBox = (EditText)findViewById(R.id.loginnamebox);
		loginPassBox = (EditText)findViewById(R.id.loginpasswordbox);
		Button loginButton = (Button)findViewById(R.id.loginconfirmbutton);
		Button cancelButton = (Button)findViewById(R.id.logincancelbutton);
		loginButton.setOnClickListener(loginClick);
		cancelButton.setOnClickListener(cancelClick);
		Log  .e(TAG, "LoginCredentials");
	}
	
	/** The login click. */
	OnClickListener loginClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			
			if(!isNetworkConnected()){
				Toast.makeText(LoginCredentials.this, "No Network Connection.  Please Establish Connectivity and Try Again!", Toast.LENGTH_LONG).show();
				try{
					Editor ed = settings.edit();
					ed.remove("username");
					ed.remove("password");
					ed.commit();
				}catch(Exception e){
					e.printStackTrace();
				}
				return;
			}

			String username =""; username = loginTextBox.getText().toString();
			String password =""; password = loginPassBox.getText().toString();
			
			try {
				Log.e(TAG, "*************************************************");
				Log.e(TAG, "SETTING VOICE SINGLETON");
				
				//CONSIDER MAKING A BACKGROUND TASK. 
				
				VoiceSingleton.getVoiceSingleton().setVoice(username, password);
			} catch (IOException e) {
				
				//WRAPING IN ANOTHER TRY AS A PATCH FOR EXTRANEOUS FATAL EXCEPTION
				
				try{
					
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
					Toast.makeText(LoginCredentials.this, "ERROR CONNECTING TO GOOGLE VOICE.  CHECK CREDENTIALS, GOOGLE ACCOUNT STATUS, AND CONNECTIVITY, THEN TRY AGAIN!", Toast.LENGTH_LONG).show();
					loginPassBox.setText("");
					loginPassBox.requestFocus();
					
				}catch(Exception g){}
				return;
			}catch (Exception g){
				
				//WRAPING IN ANOTHER TRY AS A PATCH FOR EXTRANEOUS FATAL EXCEPTION
				
				try{
					
					Log.e(TAG, g.getMessage());
					g.printStackTrace();
					Toast.makeText(LoginCredentials.this, "AN UNKNOWN ERROR OCCURRED.  CONTACT DEVELOPER TO ASSIST IN ITS CORRECTION.", Toast.LENGTH_LONG).show();
					
				}catch(Exception t){}
				return;
				
			}
			Log.e(TAG, "VOICE SINGLETON SET with NEW VOICE OBJECT");
			
			
			Log.e(TAG, "Committing Encrypted Login Credentials to Settings...");
			String encryptedpassword = Settings.encrypt( loginPassBox.getText().toString(), 10);
			
			if(!username.equals("")&&!password.equals("")){
				Editor edit = settings.edit();
				edit.putString("username", username);
				edit.putString("password",encryptedpassword);
				edit.commit();
//				VoiceSingleton.getOrCreateVoiceSingleton(username, password);
			}

			LoginCredentials.this.finish();//finalize();
			
		}
		
	};
	
	
	OnClickListener cancelClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			Log.e("TECHVENTUS","Exiting CREDENTIALS FROM CANCEL");
			LoginCredentials.this.finish();
		}
		
	};

	
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
	
	
	
}


