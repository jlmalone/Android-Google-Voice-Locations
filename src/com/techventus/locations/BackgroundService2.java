package com.techventus.locations;

import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Joseph
 * Date: 14.11.13
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public class BackgroundService2  extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener {
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /** The Binder */
    private final GVLServiceInterface.Stub mBinder = new GVLServiceInterface.Stub() {

        @Override
        public int[] getCurrentCoordinatesE6() throws RemoteException {

            int[] ret = {Status.locationGeoPoint.getLatitudeE6(),Status.locationGeoPoint.getLongitudeE6()};

            return ret;	//return null;
        }

        @Override
        public void reset() throws RemoteException {

            synchronized(BackgroundService.this){
                if(geoHandle!=null)
                    try{locationManager.removeUpdates(geoHandle);locationManager = null;}catch(Exception e){e.printStackTrace();
                        Log.e(TAG + " Reset", "LocationManager Exception");}
                try{geoHandle = new GeoUpdateHandler();}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","geohandle Exception");}
                try{status.reset();}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","status Exception");}


                try{VoiceSingleton.reset()/*voiceFact=null*/;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","voice Exception");}
                try{startupStarted = false;startupComplete = false;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","Startup Flag Exception");}
                SQLiteDatabase db = null;
                try{
                    db= openOrCreateDatabase("db",0,null);
                    SQLHelper.exec(db,SQLHelper.dropLocationPhoneEnable );
                    if(SQLHelper.isTableExists("COMMAND",db) ||SQLHelper.isTableExists("PHONE",db) ){
                        SQLHelper.exec(db,SQLHelper.dropPhone );
                        SQLHelper.exec(db,SQLHelper.dropGoogle );
                        SQLHelper.exec(db,SQLHelper.dropLocations );
                        SQLHelper.exec(db,SQLHelper.dropLocation );
                        SQLHelper.exec(db, SQLHelper.dropCommand);
                        SQLHelper.exec(db, SQLHelper.dropStatus);
                        SQLHelper.exec(db, SQLHelper.dropServiceStatus);
                        SQLHelper.exec(db, SQLHelper.dropSettings);
                    }



                }catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","LocationManager Exception");}
                finally{
                    if(db!=null)
                        db.close();
                }

            }//synch
            getStartupTask().execute();
        }

        @Override
        public String getCurrentLocationString() throws RemoteException {
            return Status.currentLocationString;
        }

        @Override
        public void restart() throws RemoteException {
            BackgroundService2.this.startupStarted = false;
            //Settings.PHONE_UPDATE_FLAG=true;
            BackgroundService.this.getStartupTask().execute();
        }

        @Override
        public void update() throws RemoteException {
            BackgroundService2.this.startupStarted = false;
            //Settings.PHONE_UPDATE_FLAG=true;
            getStartupTask().execute();
        }


    };



    // Holds the location client
    private LocationClient mLocationClient;
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;
    // Defines the allowable request types.
//    public enum REQUEST_TYPE = {REQUEST_TYPE.ADD}
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    @Override
    public void onCreate() {

        // Start with the request flag set to false
        mInProgress = false;

    }





    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void addGeofences() {
        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }







    // Enum type for controlling the type of removal requested
    public enum REQUEST_TYPE  {ADD, REMOVE_INTENT, REMOVE_LIST} ;
    // Store the list of geofence Ids to remove
    List<String> mGeofencesToRemove;


    private void onConnected(Bundle dataBundle) {
        switch (mRequestType) {

            case REQUEST_TYPE.ADD :
                // Get the PendingIntent for the request
                mTransitionPendingIntent =
                        getTransitionPendingIntent();
                // Send a request to add the current geofences
                mLocationClient.addGeofences(
                        mCurrentGeofences, pendingIntent, this);
            // If removeGeofencesById was called
            case REQUEST_TYPE.REMOVE_LIST :
                mLocationClient.removeGeofences(
                        mGeofencesToRemove, this);
                break;


            case REQUEST_TYPE.REMOVE_INTENT :
                mLocationClient.removeGeofences(
                        mGeofenceRequestIntent, this);
                break;
        }
    }

    /**
     * When the request to remove geofences by IDs returns, handle the
     * result.
     *
     * @param statusCode The code returned by Location Services
     * @param geofenceRequestIds The IDs removed
     */
    @Override
    public void onRemoveGeofencesByRequestIdsResult(
            int statusCode, String[] geofenceRequestIds) {
        // If removing the geocodes was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful removal of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            // If removing the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Indicate that a request is no longer in progress
        mInProgress = false;
        // Disconnect the location client
        mLocationClient.disconnect();
    }





    /**
     * Start a request to remove monitoring by
     * calling LocationClient.connect()
     *
     */
    public void removeGeofences(List<String> geofenceIds) {
        // If Google Play services is unavailable, exit
        // Record the type of removal request
        mRequestType = REMOVE_LIST;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        // Store the list of geofences to remove
        mGeofencesToRemove = geofenceIds;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }





    /*
 * Provide the implementation of
 * OnAddGeofencesResultListener.onAddGeofencesResult.
 * Handle the result of adding the geofences
 *
 */
    @Override
    public void onAddGeofencesResult( int statusCode, String[] geofenceRequestIds) {
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        mLocationClient.disconnect();
    }



    // Implementation of OnConnectionFailedListener.onConnectionFailed
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Turn off the request flag
        mInProgress = false;
        /*
         * If the error has a resolution, start a Google Play services
         * activity to resolve it.
         */
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
            // If no resolution is available, display an error dialog
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(
                        getSupportFragmentManager(),
                        "Geofence Detection");
            }
        }
    }


    /*
 * Implement ConnectionCallbacks.onDisconnected()
 * Called by Location Services once the location client is
 * disconnected.
 */
    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mLocationClient = null;
    }
}
