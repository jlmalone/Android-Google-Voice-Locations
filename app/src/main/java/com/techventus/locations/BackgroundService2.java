package com.techventus.locations;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 *
 *
 *   1. Ensure Background Service is Enabled - Stop Self Otherwise.
 *   2. Load Shared Preferences Geofences, PhoneEnable.
 *   3. On a Frequency, update Google Settings
 *   4. On a Frequency, enforce Phone Preferences
 *   5. On a Location Change, enforce Phone Preferences
 *   6. Create Task FLAGS - If no Connectivity, Raise Task Flag
 *   7. Upon Reconnection Execute Flagged Tasks
 *
 *
 *
 * User: Joseph
 * Date: 14.11.13
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public class BackgroundService2  extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnRemoveGeofencesResultListener,
        LocationClient.OnAddGeofencesResultListener {



    public static final String TAG =   BackgroundService2.class.getSimpleName();

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /** The Binder */
    private final GVLServiceInterface.Stub mBinder = new GVLServiceInterface.Stub()
    {

        @Override
        public int[] getCurrentCoordinatesE6() throws RemoteException
        {

            int[] ret = {Status.locationGeoPoint.getLatitudeE6(),Status.locationGeoPoint.getLongitudeE6()};

            return ret;	//return null;
        }

        @Override
        public void reset() throws RemoteException
        {

        }

        @Override
        public String getCurrentLocationString() throws RemoteException
        {
               return Status.currentLocationString;
        }

        @Override
        public void restart() throws RemoteException
        {

        }

        @Override
        public void update() throws RemoteException
        {

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
          super.onCreate();
        // Start with the request flag set to false
        mInProgress = false;

        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(this);


        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();
        mGeofencesToRemove = new ArrayList<String>();


        Geofence.Builder builder = new Geofence.Builder();
        builder.setCircularRegion(12.34456,12.34567,200);
        builder.setRequestId("a");
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE) ;
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);

        Geofence a =  builder.build();

        builder = new Geofence.Builder();
        builder.setCircularRegion(12.34456,12.34567,200);
        builder.setRequestId("b");
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE) ;
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);

        Geofence b =  builder.build();

        mCurrentGeofences.add(a);
        mCurrentGeofences.add(b);


        Log.v(TAG,"ADD THE GEOFENCES");
        addGeofences();

    }

    SimpleGeofenceStore  mGeofenceStorage;

    List<Geofence>  mCurrentGeofences;

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Geofence Detection",
                    "Google Play services is available.");

            Toast.makeText(this," servicesConnected ",Toast.LENGTH_LONG);
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            return false;
        }

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
        if (!servicesConnected())
        {
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



    /**
     * Start a request to remove geofences by calling
     * LocationClient.connect()
     */
    public void removeGeofences(PendingIntent requestIntent) {
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_INTENT;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        // Store the PendingIntent
        mGeofenceRequestIntent = requestIntent;
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

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
        private PendingIntent getTransitionPendingIntent() {
            // Create an explicit Intent
            Intent intent = new Intent(this,
                    ReceiveTransitionsIntentService.class);
            /*
             * Return the PendingIntent
             */
            return PendingIntent.getService(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }


    PendingIntent mTransitionPendingIntent;


    public void onConnected(Bundle dataBundle) {
        switch (mRequestType) {

            case ADD :
                // Get the PendingIntent for the request
                mTransitionPendingIntent =
                        getTransitionPendingIntent();
                // Send a request to add the current geofences
                mLocationClient.addGeofences(
                        mCurrentGeofences, mTransitionPendingIntent, this);
            // If removeGeofencesById was called
            case REMOVE_LIST :
                mLocationClient.removeGeofences(
                        mGeofencesToRemove, this);
                break;


            case REMOVE_INTENT :
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

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * Start a request to remove monitoring by
     * calling LocationClient.connect()
     *
     */
    public void removeGeofences(List<String> geofenceIds) {
        // If Google Play services is unavailable, exit
        // Record the type of removal request
        mRequestType = REQUEST_TYPE.REMOVE_LIST;
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

                Toast.makeText(this,"CONNECTION_FAILURE_RESOLUTION_REQUEST",Toast.LENGTH_LONG).show();


            // If no resolution is available, display an error dialog
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services

            Toast.makeText(this,"CONNECTION_FAILURE_RESOLUTION_REQUEST error code:"+errorCode,Toast.LENGTH_LONG).show();




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
