package com.techventus.locations;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.IBinder;
//import android.os.RemoteException;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;

/**
 * The Class LoginCredentials.
 */
public class LoginCredentials extends Activity
{

	private String TAG = "TECHVENTUS - LoginCredentials";
	SharedPreferences settings;
	String PREFERENCENAME = "TECHVENTUS";
	EditText mLoginEditText;
	EditText mPasswordEditText;


	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.login); // bind the layout to the activity

		settings = getSharedPreferences(PREFERENCENAME, 0);


		mLoginEditText = (EditText) findViewById(R.id.loginnamebox);
		mPasswordEditText = (EditText) findViewById(R.id.loginpasswordbox);
		Button loginButton = (Button) findViewById(R.id.loginconfirmbutton);
		Button cancelButton = (Button) findViewById(R.id.logincancelbutton);
		loginButton.setOnClickListener(loginClick);
		cancelButton.setOnClickListener(cancelClick);
		Log.e(TAG, "LoginCredentials");
	}



	String mUsername = "";
	String mPassword = "";

	/**
	 * The login click.
	 */
	OnClickListener loginClick = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if (!isNetworkConnected())
			{
				Toast.makeText(LoginCredentials.this, "No Network Connection.  Please Establish Connectivity and Try Again!", Toast.LENGTH_LONG).show();
				try
				{
					Editor ed = settings.edit();
					ed.remove("username");
					ed.remove("password");
					ed.commit();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}

			if(mLoginTask!=null)
			{
				mLoginTask.cancel(true);

			}
			mLoginTask = loginTask();
			mLoginTask.execute();



			mUsername = mLoginEditText.getText().toString();
			mPassword = mPasswordEditText.getText().toString();


			Log.e(TAG, "Committing Encrypted Login Credentials to Settings...");
			String encryptedpassword = Settings.encrypt(mPasswordEditText.getText().toString(), 10);

			if (!mUsername.equals("") && !mPassword.equals(""))
			{
				Editor edit = settings.edit();
				edit.putString("username", mUsername);
				edit.putString("password", encryptedpassword);
				edit.commit();
				//				VoiceSingleton.getOrCreateVoiceSingleton(username, password);
			}

			LoginCredentials.this.finish();//finalize();

		}

	};


	OnClickListener cancelClick = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			Log.e("TECHVENTUS", "Exiting CREDENTIALS FROM CANCEL");
			LoginCredentials.this.finish();
		}

	};


	/**
	 * Checks if is network connected.
	 *
	 * @return true, if is network connected
	 */
	private boolean isNetworkConnected()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		if (ni != null && ni.isConnected())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	AsyncTask<Void, Void, Boolean> mLoginTask;


	AsyncTask<Void, Void, Boolean> loginTask()
	{
		AsyncTask<Void, Void, Boolean> ret = new AsyncTask<Void, Void, Boolean>()
		{
			@Override
			protected void onPostExecute(Boolean result)
			{

				  if(result)
				  {
					  LoginCredentials.this.finish();
				  }
				else
				  {
					  Toast.makeText(LoginCredentials.this,"There was an error. Please check username, password, and connectivity and try again.",
							  Toast.LENGTH_LONG).show();
				  }
			}


			@Override
			protected Boolean doInBackground(Void... voids)
			{

				try
				{
					Log.v(TAG, "*************************************************");
					Log.v(TAG, "SETTING VOICE SINGLETON");

					//CONSIDER MAKING A BACKGROUND TASK.

					VoiceSingleton.getVoiceSingleton().setVoice(mUsername, mPassword);

					Log.v(TAG, "DONE SETTING VOICE SINGLETON");

					return true;
				}
				catch (IOException e)
				{

					//WRAPING IN ANOTHER TRY AS A PATCH FOR EXTRANEOUS FATAL EXCEPTION

					try
					{

						Log.e(TAG, "IO ExCEPTION "+e.getMessage());
						e.printStackTrace();

						mPasswordEditText.setText("");
						mPasswordEditText.requestFocus();

					}
					catch (Exception g)
					{
						g.printStackTrace();
					}
					return false;
				}
				catch (Exception g)
				{

					//WRAPING IN ANOTHER TRY AS A PATCH FOR EXTRANEOUS FATAL EXCEPTION

					try
					{

						Log.e(TAG, "SECONDARY EXCEPTION "+g.getMessage());
						g.printStackTrace();
//						Toast.makeText(LoginCredentials.this, "AN UNKNOWN ERROR OCCURRED.  CONTACT DEVELOPER TO ASSIST IN ITS CORRECTION.", Toast.LENGTH_LONG)
//								.show();

					}
					catch (Exception t)
					{
					}
					return false;

				}
//				Log.e(TAG, "VOICE SINGLETON SET with NEW VOICE OBJECT");

			}
		};

		return ret;

	}

}


