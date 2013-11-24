package com.techventus.locations;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * The Class GoogleFrequencyToggle. This Activity allows the User to 
 * select the frequency of background Google Voice Settings Updates.
 * 
 * Hourly or even daily is recommended, and these updates consume battery 
 * and data.
 */
public class GoogleFrequencyToggle extends Activity{

	private final String TAG="TECHVENTUS - GoogleFrequencyToggle";
	String PREFERENCENAME = "TECHVENTUS";
	String TOGGLE_KEY = Settings.GOOGLE_SYNC_FREQUENCY;
	SharedPreferences settings;
	
	private RadioGroup m_RadioGroup;
	private int m_iCheckedTime;
	
	private Button m_btnOK;
	private Button m_btnCancel;
	
	final public static int fivemin = 5;
	final public static int thirtymin = 30;
	final public static int hourly = 60;
	final public static int daily = 1440;
	final public static int fortnightly = 20160;

	Settings mSettings;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"search dialog is created!!!");

		mSettings = Settings.getInstance();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.google_check_frequency);
		
		settings = this.getSharedPreferences(PREFERENCENAME, 0);

		m_RadioGroup = (RadioGroup)findViewById(R.id.rg_SetFrequencyTime);
		
		m_iCheckedTime =  settings.getInt(TOGGLE_KEY, hourly);
				
		switch(m_iCheckedTime){
			case fivemin:
				m_RadioGroup.check(R.id.rb_FiveMin);
				break;
			case thirtymin:
				m_RadioGroup.check(R.id.rb_ThirtyMin);
				break;
			case hourly:
				m_RadioGroup.check(R.id.rb_Hour);
				break;
			case daily:
				m_RadioGroup.check(R.id.rb_Daily);
				break;
			case fortnightly:
				m_RadioGroup.check(R.id.rb_Fortnightly);
				break;
			case -1:
				m_RadioGroup.check(R.id.rb_Off);
				break;
			default:
				Log.e(TAG,"the frequency time is not transport into this dialog!!!");
				m_RadioGroup.check(R.id.rb_Hour);
				break;
		}
		
		m_RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId){

					case R.id.rb_FiveMin:
						m_iCheckedTime = fivemin;
						break;
					case R.id.rb_ThirtyMin:
						m_iCheckedTime = thirtymin;
						break;	
					case R.id.rb_Hour:
						m_iCheckedTime = hourly;
						break;	
					case R.id.rb_Daily:
						m_iCheckedTime = daily;
						break;	
					case R.id.rb_Fortnightly:
						m_iCheckedTime = fortnightly;
						break;	
					case R.id.rb_Off:
						m_iCheckedTime = -1;
						break;	
					default:
						Toast.makeText(GoogleFrequencyToggle.this, "ERROR: the frequency time is not chosen in this dialog!!!", Toast.LENGTH_SHORT);
						Log.e(TAG,"the frequency time is not chosen in this dialog!!!");
						break;
				}
			}
		});
		
		m_btnOK = (Button)findViewById(R.id.btn_SetFrequencyOK);
		m_btnCancel = (Button)findViewById(R.id.btn_SetFrequencyCancel);
		
		m_btnOK.setOnClickListener(new OnClickListener(){

			/* (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("RefreshFrequencyTime", m_iCheckedTime);
				setResult(RESULT_OK, intent);
				
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putInt(TOGGLE_KEY, m_iCheckedTime);
			    editor.commit();
			    mSettings.setRestartServiceFlag(true);

				finish();
			}});
		
		m_btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}});
	}

}