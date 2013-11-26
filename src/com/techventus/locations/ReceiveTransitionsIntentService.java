package com.techventus.locations;

import android.app.*;
import android.content.Intent;
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
    /**
     * Sets an identifier for the service
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
        mSettings = Settings.getInstance();
    }
    /**
     * Handles incoming intents
     *@param intent The Intent sent by Location Services. This
     * Intent is provided
     * to Location Services (inside a PendingIntent) when you call
     * addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent)
    {
        // First check for errors
        if (LocationClient.hasError(intent))
        {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode));
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
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            List<Geofence> list = LocationClient.getTriggeringGeofences(intent);
            Log.e("ReceiveTransitionsIntentService",
                    "Geofence transition error: " +
                            Integer.toString(transitionType));
             String lst = "";
            for(Geofence g:list)
            {
                lst+= g.getRequestId()+",";
            }


            {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Intent notificationIntent = new Intent(ReceiveTransitionsIntentService.this, MainMenu.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(ReceiveTransitionsIntentService.this, 0,notificationIntent, 0);
                Notification notification = new Notification(R.drawable.ic_menu_compass, "New Message", System.currentTimeMillis());
                notification.setLatestEventInfo(ReceiveTransitionsIntentService.this,lst+transitionType+"RT GVL", "Now: "+com.techventus.locations.Status.currentLocationString, pendingIntent);
                notificationManager.notify(9999, notification);
            }



            // Test that a valid transition was reported
            if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) || (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT))
            {
                List <Geofence> triggerList =  LocationClient.getTriggeringGeofences(intent);

                String[] triggerIds = new String[triggerList.size()];

                for (int i = 0; i < triggerIds.length; i++) {
                    // Store the Id of each geofence
                    triggerIds[i] = triggerList.get(i).getRequestId();
                }
                /*
                 * At this point, you can store the IDs for further use
                 * display them, or display the details associated with
                 * them.
                 */
            }


        }
}

    /**
     * Trigger location change.
     */
    synchronized void triggerLocationChange(){

        if(Util.isNetworkConnected(this)){
            try {
                Voice voice = VoiceSingleton.getVoiceSingleton().getVoice();
                AllSettings voiceSettings =voice.getSettings(false);
                Phone[] phoneAr = voiceSettings.getPhones();
                for(LPEPref lpe:mSettings.getPrefsList()){
                    if(lpe.location.equals(Status.currentLocationString)){
                        for(Phone phone:phoneAr){
                            if(phone.getName().equals(lpe.phoneString)){
                                System.out.println(""+phone.getName()+" "+lpe.enablePref);
                                if(lpe.enablePref==1){
                                    if(voiceSettings.isPhoneDisabled(phone.getId())){
                                        voice.phoneEnable(phone.getId());
                                        mSettings.setLocationChanged(true);
                                    }

                                }else if(lpe.enablePref==-1){
                                    if(!voiceSettings.isPhoneDisabled(phone.getId())){
                                        Log.e(TAG, "DISABLE "+phone.getId());
                                        mSettings.setLocationChanged(true);
                                        voice.phoneDisable(phone.getId());
                                    }

                                }
                            }
                        }
                    }
                }
                voice.getSettings(true);
                mSettings.setReconnectToVoiceFlag(false);
                mSettings.setPhoneUpdateFlag(false);
            } catch (Exception e) {
                mSettings.setReconnectToVoiceFlag(true);
                e.printStackTrace();
            }
        }
        else
        {
            mSettings.setPhoneUpdateFlag(true);
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
