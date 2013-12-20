package com.techventus.locations;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.techventus.server.voice.Voice;
import com.techventus.server.voice.datatypes.AllSettings;
import com.techventus.server.voice.datatypes.Phone;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Joseph
 * Date: 14.11.13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class ReceiveTransitionsIntentService extends IntentService
{

	private static final String TAG = ReceiveTransitionsIntentService.class.getSimpleName();

	Settings mSettings;

	SharedPreferences mPreferences;

	/**
	 * Sets an identifier for the service
	 */
	public ReceiveTransitionsIntentService()
	{
		super("ReceiveTransitionsIntentService");
		mSettings = Settings.getInstance();

	}

	/**
	 * Handles incoming intents
	 *
	 * @param intent The Intent sent by Location Services. This
	 *               Intent is provided
	 *               to Location Services (inside a PendingIntent) when you call
	 *               addGeofences()
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		if(mPreferences==null)
		{
			mPreferences = getSharedPreferences(Settings.SharedPrefKey.PREFERENCES, 0);
		}

		// First check for errors
		if (LocationClient.hasError(intent))
		{
			// Get the error code with a static method
			int errorCode = LocationClient.getErrorCode(intent);
			// Log the error
			Log.e("ReceiveTransitionsIntentService", "Location Services error: " + Integer.toString(errorCode));
	        /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
		}
		else
		{

			// Get the type of transition (entry or exit)
			int transitionType = LocationClient.getGeofenceTransition(intent);
			List<Geofence> list = LocationClient.getTriggeringGeofences(intent);
			Log.e("ReceiveTransitionsIntentService", "Geofence transition success: " + Integer.toString(transitionType));
			String lst = "";
			for (Geofence g : list)
			{
				lst += g.getRequestId() + ",";
			}


			if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
			{
				Status.currentLocationString = "Elsewhere";
				triggerLocationChange();
				return;
			}

			// Test that a valid transition was reported
			if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER))
			{


				List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);

				String[] triggerIds = new String[triggerList.size()];

				for (int i = 0; i < triggerIds.length; i++)
				{
					// Store the Id of each geofence
					triggerIds[i] = triggerList.get(i).getRequestId();

					Status.currentLocationString = triggerIds[i].replace("_ENTER", "");
				}






                /*
                 * At this point, you can store the IDs for further use
                 * display them, or display the details associated with
                 * them.
                 */
			}


			triggerLocationChange();

			//            {
			//                notificationIndex++;
			//                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			//                Intent notificationIntent = new Intent(ReceiveTransitionsIntentService.this, MainMenu.class);
			//                PendingIntent pendingIntent = PendingIntent.getActivity(ReceiveTransitionsIntentService.this, 0,notificationIntent, 0);
			//                Notification notification = new Notification(R.drawable.ic_menu_compass, "New Message", System.currentTimeMillis());
			//                notification.flags = Notification.FLAG_AUTO_CANCEL ;
			//                notification.setLatestEventInfo(ReceiveTransitionsIntentService.this,lst+transitionType+"RT GVL",
			// "Now: "+com.techventus.locations.Status.currentLocationString, pendingIntent);
			//                notificationManager.notify(notificationIndex, notification);
			//            }


		}
	}

	int notificationIndex = 543;
	//    int PHONE_ADD = 345;
	//    int PHONE_REMOVE = 7889;

	boolean locationChangedForNotificaton = false;

	/**
	 * Trigger location change.
	 */
	synchronized void triggerLocationChange()
	{
		Log.v("TECHVENTUS", "RECEIVE TRANSITIONS INTENT SERVICE TRIGGER LOCATION CHANGED");
		if (Util.isNetworkConnected(this))
		{
			Log.v("TECHVENTUS", "NETWORK CONNECTED");
			try
			{
				Voice voice = VoiceSingleton.getVoiceSingleton().getVoice();
				AllSettings voiceSettings = voice.getSettings(true);
				Phone[] phoneAr = voiceSettings.getPhones();
				for (LPEPref lpe : mSettings.getPrefsList())
				{
					Log.v("TECHVENTUS",
							"lpe phone:" + lpe.phoneString + " location pref: " + lpe.location + ", current location" + Status.currentLocationString);
					if (lpe.location.equals(Status.currentLocationString))
					{
						Log.v("TECHVENTUS", "location:" + lpe.phoneString);
						for (Phone phone : phoneAr)
						{
							if (phone.getName().equals(lpe.phoneString))
							{
								System.out.println("" + phone.getName() + " " + lpe.enablePref);
								if (lpe.enablePref == 1)
								{
									if (voiceSettings.isPhoneDisabled(phone.getId()))
									{
										Log.v(TAG, "set phone " + phone.getName() + " Enable");
										//                                        notificationIndex++;

										locationChangedForNotificaton = true;

										//                                        NotificationManager notificationManager = (NotificationManager)
										// getSystemService(NOTIFICATION_SERVICE);
										//                                        Intent notificationIntent = new Intent(ReceiveTransitionsIntentService.this,
										// MainMenu.class);
										//                                        PendingIntent pendingIntent = PendingIntent.getActivity
										// (ReceiveTransitionsIntentService.this, 0,notificationIntent, 0);
										//                                        Notification notification = new Notification(R.drawable.ic_menu_compass,
										// "PHONE ENABLE", System.currentTimeMillis());
										//                                        notification.flags = Notification.FLAG_AUTO_CANCEL ;
										//                                        notification.setLatestEventInfo(ReceiveTransitionsIntentService.this,
										// "Phone Enable "+phone.getName()+" ", "Now: "+com.techventus.locations.Status.currentLocationString, pendingIntent);
										//                                        notificationManager.notify(PHONE_ADD, notification);

										voice.phoneEnable(phone.getId());
										mSettings.setLocationChanged(true);
									}

								}
								else if (lpe.enablePref == -1)
								{
									if (!voiceSettings.isPhoneDisabled(phone.getId()))
									{
										Log.v(TAG, "set phone " + phone.getName() + " Disable");
										Log.e(TAG, "DISABLE " + phone.getId());
										mSettings.setLocationChanged(true);
										voice.phoneDisable(phone.getId());

										locationChangedForNotificaton = true;

										//                                        notificationIndex++;
										//
										//                                        NotificationManager notificationManager = (NotificationManager)
										// getSystemService(NOTIFICATION_SERVICE);
										//                                        Intent notificationIntent = new Intent(ReceiveTransitionsIntentService.this,
										// MainMenu.class);
										//                                        PendingIntent pendingIntent = PendingIntent.getActivity
										// (ReceiveTransitionsIntentService.this, 0,notificationIntent, 0);
										//                                        Notification notification = new Notification(R.drawable.ic_menu_compass,
										// "PHONE DISABLE", System.currentTimeMillis());
										//                                        notification.flags = Notification.FLAG_AUTO_CANCEL ;
										//                                        notification.setLatestEventInfo(ReceiveTransitionsIntentService.this,
										// "Phone Disable "+phone.getName()+" ", "Now: "+com.techventus.locations.Status.currentLocationString, pendingIntent);
										//                                        notificationManager.notify(PHONE_REMOVE, notification);

									}
								}
							}
						}
					}
				}
				voice.getSettings(true);
				mSettings.setReconnectToVoiceFlag(false);
				mSettings.setPhoneUpdateFlag(false);
			}
			catch (Exception e)
			{
				mSettings.setReconnectToVoiceFlag(true);
				e.printStackTrace();
			}

			if(locationChangedForNotificaton)
			{
				notifyUserLocationChange(Status.currentLocationString);
				locationChangedForNotificaton = false;
			}


		}
		else
		{
			mSettings.setPhoneUpdateFlag(true);
		}





	}


	//SET NOTIFICATION WHEN LOCATION IS CHANGED
	/**
	 * Notify user of location change.
	 *
	 * @param location the location
	 */
	private void notifyUserLocationChange(String location)
	{
		Log.v(TAG,"NOTIFY USER OF LOCATION CHANGE");
		if (mPreferences.getBoolean(Settings.SharedPrefKey.NOTIFICATION_ACTIVE, true))
		{
			Log.v(TAG,"NOTIFY USER OF LOCATION CHANGE - NOTIFICATIONS ACTIVE");
			int icon = R.drawable.globesextanticon;        // icon from resources
			CharSequence tickerText = "GV Location "+location;              // ticker-text
			long when = System.currentTimeMillis();         // notification time
			Context context = getApplicationContext();      // application Context
			CharSequence contentTitle = "GV Ring Settings Changed";  // message title
			CharSequence contentText = "Current Location: "+location;      // message text

			Intent notificationIntent;

			if(mPreferences.getBoolean(Settings.SharedPrefKey.NOTIFICATION_APP_LAUNCH, true))
				notificationIntent = new Intent(this, MainMenu.class);
			else
				notificationIntent= new Intent(this, BlankIntent.class);

			PendingIntent contentIntent = PendingIntent.getActivity(ReceiveTransitionsIntentService.this, 0, notificationIntent,0);



			//            {
			//                notificationIndex++;
			//                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			//                Intent notificationIntent = new Intent(ReceiveTransitionsIntentService.this, MainMenu.class);
			//                PendingIntent pendingIntent = PendingIntent.getActivity(ReceiveTransitionsIntentService.this, 0,notificationIntent, 0);
			//                Notification notification = new Notification(R.drawable.ic_menu_compass, "New Message", System.currentTimeMillis());
			//                notification.flags = Notification.FLAG_AUTO_CANCEL ;
			//                notification.setLatestEventInfo(ReceiveTransitionsIntentService.this,lst+transitionType+"RT GVL",
			// "Now: "+com.techventus.locations.Status.currentLocationString, pendingIntent);
			//                notificationManager.notify(notificationIndex, notification);
			//            }






			// the next two lines initialize the Notification, using the configurations above
			Notification notification = new Notification(icon, "Phone Ring Preferences Changed", when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			if(mPreferences.getBoolean(Settings.SharedPrefKey.SOUND_ACTIVE,true ))
			{
				notification.defaults |= Notification.DEFAULT_SOUND;
			}

			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			mNotificationManager.notify(434, notification);
			Log.v(TAG,"NOTIFY CALLED");
		}
	}




	//    /**
	//     * Sets an identifier for the service
	//     */
	//    public ReceiveTransitionsIntentService() {
	//        super("ReceiveTransitionsIntentService");
	//    }


	//    /**
	//     * Handles incoming intents
	//     *@param intent The Intent sent by Location Services. This
	//     * Intent is provided
	//     * to Location Services (inside a PendingIntent) when you call
	//     * addGeofences()
	//     */
	//    @Override
	//    protected void onHandleIntent(Intent intent) {
	//        // First check for errors
	//        if (LocationClient.hasError(intent)) {
	//            // Get the error code with a static method
	//            int errorCode = LocationClient.getErrorCode(intent);
	//            // Log the error
	//            Log.e("ReceiveTransitionsIntentService",
	//                    "Location Services error: " +
	//                            Integer.toString(errorCode));
	//            /*
	//             * You can also send the error code to an Activity or
	//             * Fragment with a broadcast Intent
	//             */
	//        /*
	//         * If there's no error, get the transition type and the IDs
	//         * of the geofence or geofences that triggered the transition
	//         */
	//        } else {
	//            // Get the type of transition (entry or exit)
	//            int transitionType =
	//                    LocationClient.getGeofenceTransition(intent);
	//            // Test that a valid transition was reported
	//            if (
	//                    (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
	//                            ||
	//                            (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
	//                    ) {
	//                List<Geofence> triggerList =
	//                        getTriggeringGeofences(intent);
	//
	//                String[] triggerIds = new String[geofenceList.size()];
	//
	//                for (int i = 0; i < triggerIds.length; i++) {
	//                    // Store the Id of each geofence
	//                    triggerIds[i] = triggerList.get(i).getRequestId();
	//                }
	//                /*
	//                 * At this point, you can store the IDs for further use
	//                 * display them, or display the details associated with
	//                 * them.
	//                 */
	//            }
	//            // An invalid transition was reported
	//         else
	//        {
	//            Log.e("ReceiveTransitionsIntentService",
	//                    "Geofence transition error: " +
	//                            Integer.toString()transitionType));
	//        }
	//        }
	//    }
}
