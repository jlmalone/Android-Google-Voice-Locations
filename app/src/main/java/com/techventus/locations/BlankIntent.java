package com.techventus.locations;

import android.app.Activity;
import android.os.Bundle;

// TODO: Auto-generated Javadoc
/**
 * BlankIntent is an Activity which closes itself.  It was made to be launched by
 * Notifications when the User has preferences set to do nothing
 * on Notification Click.
 *
 */
public class BlankIntent extends Activity{
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		this.finish();
	}
}
