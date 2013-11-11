package com.techventus.locations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


/**
 * LocationsReceiver. This Receiver is Triggered on Bootup.
 * It checks the SharedPreferences to ensure Service and Startup
 * are enabled.  If so, the Background Service is launched 
 * at Bootup.
 */
public class LocationsReceiver extends BroadcastReceiver{
	

	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		

		SharedPreferences preferences   = context.getSharedPreferences(Settings.PREFERENCENAME, 0);
        
		if(preferences.getBoolean(Settings.STARTUP_ENABLED, false))
			if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
				Intent serviceIntent = new Intent(context,BackgroundService.class);
				context.startService(serviceIntent);
			}
	}
}


