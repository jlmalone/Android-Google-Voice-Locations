package com.techventus.locations;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import com.google.android.maps.GeoPoint;
import com.techventus.server.voice.Voice;
import com.techventus.server.voice.datatypes.AllSettings;
import com.techventus.server.voice.datatypes.Phone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

        Toast.makeText(this,"Start BackgroundService2 ",2000).show();
        // Start with the request flag set to false
        mInProgress = false;

        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(this);


        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();
        mGeofencesToRemove = new ArrayList<String>();


        Geofence.Builder builder = new Geofence.Builder();
        builder.setCircularRegion(49.28459,-123.136855,20);
        builder.setRequestId("a");
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE) ;
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT);

        Geofence a =  builder.build();

        builder = new Geofence.Builder();
        builder.setCircularRegion(49.28459,-123.136855,20);
        builder.setRequestId("b");
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE) ;
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);


        Geofence b =  builder.build();

        builder = new Geofence.Builder();
        builder.setCircularRegion(49.28459,-123.136855,50);
        builder.setRequestId("c");
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE) ;
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);



        Geofence d =  builder.build();

        builder = new Geofence.Builder();
        builder.setCircularRegion(49.28459,-123.136855,50);
        builder.setRequestId("d");
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE) ;
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT);



        Geofence c =  builder.build();

        mCurrentGeofences.add(a);
        mCurrentGeofences.add(b);
        mCurrentGeofences.add(c);
        mCurrentGeofences.add(d);

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

            Toast.makeText(this,"BackgroundService2 servicesConnected ",Toast.LENGTH_LONG);
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
                break;
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

                Toast.makeText(this,"BackgroundService2 CONNECTION_FAILURE_RESOLUTION_REQUEST",Toast.LENGTH_LONG).show();


            // If no resolution is available, display an error dialog
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services

            Toast.makeText(this,"BackgroundService2 CONNECTION_FAILURE_RESOLUTION_REQUEST error code:"+errorCode,Toast.LENGTH_LONG).show();




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








    /**
     * Gets the startup task.
     *
     * @return the startup task
     */
    AsyncTask<Void,Void,Void> getStartupTask(){
        AsyncTask<Void, Void, Void> startupTask = new AsyncTask<Void,Void,Void>(){

            @Override
            protected void onPreExecute(){
                mSettings.setRestartServiceFlag(false);

                if(timer!=null)
                {
                    timer.cancel();
                }

                super.onPreExecute();
                if(preferences.getBoolean(Settings.SERVICE_ENABLED, false))
                {

                    if(!Util.isNetworkConnected(BackgroundService2.this))
                    {
                        startupStarted = false;
                        this.cancel(true);
                    }
                }
                else
                {
                    this.cancel(true);
                    BackgroundService2.this.stopSelf();
                }
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                startupStarted = true;//trt
                SQLiteDatabase sql = null;

                try{

                    sql = openOrCreateDatabase("db",0,null);


                    if(SQLHelper.isTableExists("COMMAND",sql) ||SQLHelper.isTableExists("PHONE",sql) ){
                        SQLHelper.exec(sql, SQLHelper.dropLocationPhoneEnable);
                        SQLHelper.exec(sql,SQLHelper.dropPhone );
                        SQLHelper.exec(sql,SQLHelper.dropGoogle );
                        SQLHelper.exec(sql,SQLHelper.dropLocations );
                        SQLHelper.exec(sql,SQLHelper.dropLocation );
                        SQLHelper.exec(sql, SQLHelper.dropStatus);
                        SQLHelper.exec(sql, SQLHelper.dropServiceStatus);
                        SQLHelper.exec(sql, SQLHelper.dropSettings);
                    }




                    SQLHelper.createDatabases(sql);

                    setLocationList();

                    reconnectToVoice();



                }catch(Exception e){

                    Log.e("TECHVENTUS","Startup First EstablishTestDB exception");
                    e.printStackTrace();
                }finally{
                    if(sql!=null)
                        sql.close();
                }

                return null;
            }
            @Override
            protected void onPostExecute(Void result){


               //TODO TEMP REPLACE THIS SOMEHOW
               // setLocationUpdates();

                setUpdateTimers();
                startupComplete = true;
            }
        };
        return startupTask;
    }

    /** The startup started. */
    boolean startupStarted = false;

    /** The startup complete. */
    boolean startupComplete = false;

//
//    //TODO CHANGE CRItERIA TO SETTINGS VALUES
//    synchronized void setLocationUpdates(){
//        try
//        {
//
//            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            int locationpowercriteria = preferences.getInt(Settings.POWER_SETTING, Criteria.POWER_LOW);
//            int locationaccuracycriteria = preferences.getInt(Settings.ACCURACY_SETTING, Criteria.NO_REQUIREMENT);
//            Criteria criteria= new Criteria();
//            criteria.setCostAllowed(false);
//            criteria.setAltitudeRequired(false);
//            criteria.setBearingRequired(false);
//            criteria.setPowerRequirement(locationpowercriteria);
//            criteria.setAccuracy(locationaccuracycriteria);
//            String provider = "NETWORK";
//            if(preferences.getString(Settings.LOCATION_PROVIDER_SETTING, "BEST_PROVIDER").equals("BEST_PROVIDER")){
//                provider = locationManager.getBestProvider(criteria, true);
//            }else{
//                provider = preferences.getString(Settings.LOCATION_PROVIDER_SETTING, "BEST_PROVIDER");
//            }
//            if(geoHandle!=null){
//                locationManager.removeUpdates(geoHandle);
//                Log.e(TAG, "**********GEOHANDLE REMOVED HOPEFULLY**********");
//            }
//
//            if(runLocationCheckCondition()){
//                geoHandle = new GeoUpdateHandler();
//                locationManager.requestLocationUpdates( provider, preferences.getInt(Settings.LOCATION_FREQUENCY, LocationFrequencyToggle.fivemin)*60000 , 0, geoHandle);
//
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//            Log.e("Techventus","No Location Service Available");
//
//        }
//    }


    /**
     * Sets the location list.
     */
    void setLocationList(){
        List<LPEPref> ret = new ArrayList<LPEPref>();
        try{
            SQLiteDatabase db =  openOrCreateDatabase("db",0,null);
            SQLHelper.exec(db, SQLHelper.createLocationPhoneEnable);
            Cursor c = db.query("LOCATIONPHONEENABLE", null
                    , null ,null, null, null, null);
            if(c!=null){
                while(c.moveToNext())
                {
                    try{
                        String locationName = c.getString(c.getColumnIndex("locationName"));
                        String phoneName = c.getString(c.getColumnIndex("phoneName"));
                        Integer pref = c.getInt(c.getColumnIndex("phoneEnable"));
                        Integer latitude = c.getInt(c.getColumnIndex("locationLatitudeE6"));
                        Integer longitude = c.getInt(c.getColumnIndex("locationLongitudeE6"));
                        Integer radius = c.getInt(c.getColumnIndex("radius"));
                        LPEPref lpe = new LPEPref(locationName, phoneName, radius, pref, latitude, longitude);
                        ret.add(lpe);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                c.close();
            }

            db.close();
        }catch(Exception adsf){
            adsf.printStackTrace();
        }
        prefList = ret;
    }

    Settings mSettings;

    /**
     * Reconnect to voice.
     */
    protected void reconnectToVoice() throws IOException {
        if(!Util.isNetworkConnected(BackgroundService2.this)){
            mSettings.setReconnectToVoiceFlag(true);
            return;
        }

        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        if(!username.equals("")&&!password.equals("")){
            VoiceSingleton.getOrCreateVoiceSingleton(username,Settings.decrypt(password, 10));
            mSettings.setReconnectToVoiceFlag(false);
        }else{

            VoiceSingleton.reset();
        }
    }

//    /**
//     * Checks if is network connected.
//     *
//     * @return true, if is network connected
//     */
//    private boolean isNetworkConnected(){
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//        NetworkInfo ni = cm.getActiveNetworkInfo();
//
//        if (ni!=null && ni.isConnected()){
//            return true;
//        }else{
//            return false;
//        }
//    }


    /** The preferences. */
    SharedPreferences preferences ;


    /** The pref list. */
    List<LPEPref> prefList = new ArrayList<LPEPref>();

    //TODO EXPERIMENTAL
    void setUpdateTimers(){

        try{
            int phoneUpdateFreq = preferences.getInt(Settings.GOOGLE_SYNC_FREQUENCY, 30)*60000;
            if(timer!=null)
                timer.cancel();
            timer = new Timer();
            if(phoneUpdateFreq>30000)
                timer.schedule(phoneUpdateTask(),20000, phoneUpdateFreq);
            else
                Log.e(TAG,"Error In Set Update Timers");
        }catch(Exception e){
            e.printStackTrace();
        }

    }


    //TODO EXPERIMENTAL
    TimerTask phoneUpdateTask(){
        TimerTask phoneUpdateTask = new TimerTask(){
            @Override
            public void run() {
                Log.e(TAG, "PHONE UPDATE TASK");
                if(Util.isNetworkConnected(BackgroundService2.this))
                    LocationChangeTask().execute();
                else{
                    mSettings.setPhoneUpdateFlag(true);
                }
            }
        };
        return phoneUpdateTask;
    }




    /**
     * The Class GeoUpdateHandler.
     */
    public class GeoUpdateHandler implements LocationListener {

        /* (non-Javadoc)
         * @see android.location.LocationListener#onLocationChanged(android.location.Location)
         */
        @Override
        public void onLocationChanged(Location location) {
            //Do Not Execute if Service Disabled
            if(!preferences.getBoolean(Settings.SERVICE_ENABLED, true)){
                BackgroundService2.this.stopSelf();
                return;
            }


            Log.e(TAG,"Geopoint Update Handler - updating location GeoPoint from "+location.getProvider());
            Log.e(TAG,"Accuracy"+location.getAccuracy());


            int lat = (int) (location.getLatitude() * 1E6);
            int lng = (int) (location.getLongitude() * 1E6);

            setLocation(lat,lng,Status.currentLocationString);
            boolean action = false;
            for(LPEPref lpe:prefList){
                if(lpe.location.equals("Elsewhere")){
                    continue;
                }
                if(Settings.distInMetres(lpe.getGeoPoint(), Status.locationGeoPoint)<lpe.radius+location.getAccuracy()){

                    Log.e("TECHVENTUS","CHANGING LOCATION from "+Status.currentLocationString+" to "+lpe.location);
                    action = true;
                    if(!lpe.location.equals(Status.currentLocationString)){
                        Status.currentLocationString = lpe.location;
                        Log.e("TECHVENTUS","CHANGING LOCATION to "+lpe.location);
                        LocationChangeTask().execute();
                        break;
                    }
                }

            }
            if(!action && !Status.currentLocationString.equals("Elsewhere")){
                Log.e("TECHVENTUS","CHANGING LOCATION to ELSEWHERE from "+Status.currentLocationString);
                Status.currentLocationString = "Elsewhere";
                setLocation(Status.locationGeoPoint.getLatitudeE6(),Status.locationGeoPoint.getLongitudeE6(),"Elsewhere");
                LocationChangeTask().execute();
                Log.e("TECHVENTUS","LOCATION GEOPOINT SET "+Status.locationGeoPoint.getLatitudeE6());
            }

        }

        /* (non-Javadoc)
         * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
         */
        @Override
        public void onProviderDisabled(String provider) {

        }

        /* (non-Javadoc)
         * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
         */
        @Override
        public void onProviderEnabled(String provider) {
        }

        /* (non-Javadoc)
         * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //TODO Raise Notification
        }
    }


    /**
     * Sets the location class variables for locationName and coordinates.
     *
     * @param lat the lat
     * @param lon the lon
     * @param loc the loc
     */
    private void setLocation(int lat , int lon, String loc){
        if(!preferences.getBoolean(Settings.SERVICE_ENABLED, true)){
            BackgroundService2.this.stopSelf();
            return;
        }
        synchronized(this){
            try{
                Log.e("TECHVENTUS","SET LOCATION");

                Status.currentLocationString = loc;
                Status.locationGeoPoint = new GeoPoint(lat,lon);


            }catch(Exception u){
                u.printStackTrace();
            }
        }
    }



    Timer timer = new Timer();

    /** The status. */
    Status status;




    /**
     * Location change task. Called By GeoHandler when a change in User-Set Locations is
     * Detected.
     *
     * @return the async task
     */
    AsyncTask<Void,Void,Void> LocationChangeTask()
    {
        AsyncTask<Void,Void,Void> ret = new AsyncTask<Void,Void,Void>()
        {
            @Override
            protected void onPreExecute(){

                super.onPreExecute();

                if(preferences.getBoolean(Settings.SERVICE_ENABLED, false)){

                    if(mSettings.getRestartServiceFlag())
                    {
                        try
                        {
                            this.cancel(true);
                            BackgroundService2.this.mBinder.restart();
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    if(!Util.isNetworkConnected(BackgroundService2.this))
                    {
                        mSettings.setPhoneUpdateFlag(true);
                        this.cancel(true);
                    }
                }else
                {
                    this.cancel(true);
                    BackgroundService2.this.stopSelf();
                }
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                triggerLocationChange();

                return null;
            }

            /* (non-Javadoc)
             * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
             */
            @Override
            protected void onPostExecute(Void params)
            {
//                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                Intent notificationIntent = new Intent(BackgroundService.this, MainMenu.class);
//                PendingIntent pendingIntent = PendingIntent.getActivity(BackgroundService.this, 0,notificationIntent, 0);
//                Notification notification = new Notification(R.drawable.ic_menu_compass, "New Message", System.currentTimeMillis());
//                notification.setLatestEventInfo(BackgroundService.this,!mSettings.getPhoneUpdateFlag()+"Google Voice Locations", "Now: "+com.techventus.locations.Status.currentLocationString, pendingIntent);
//                notificationManager.notify(9999, notification);

                //TEMP
                //removed !mSettings.getPhoneUpdateFlag() &&
                if( ! mSettings.getReconnectToVoiceFlag() && mSettings.getLocationChanged() )
                {
                    mSettings.setLocationChanged(false);

                    notifyUserLocationChange(com.techventus.locations.Status.currentLocationString);
                }
            }
        };
        return ret;
    }


    /**
     * Trigger location change.
     */
    synchronized void triggerLocationChange(){

        if(Util.isNetworkConnected(BackgroundService2.this)){
            try {
                Voice voice = VoiceSingleton.getVoiceSingleton().getVoice();
                AllSettings voiceSettings =voice.getSettings(false);
                Phone[] phoneAr = voiceSettings.getPhones();
                for(LPEPref lpe:prefList){
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


    //SET NOTIFICATION WHEN LOCATION IS CHANGED
    /**
     * Notify user of location change.
     *
     * @param location the location
     */
    private void notifyUserLocationChange(String location){
        if(!preferences.getBoolean(Settings.SERVICE_ENABLED, true)){
            BackgroundService2.this.stopSelf();
            return;
        }
        if(preferences.getBoolean(Settings.NOTIFICATION_ACTIVE, false)){


            int icon = R.drawable.globesextanticon;        // icon from resources
            CharSequence tickerText = "GV Location "+location;              // ticker-text
            long when = System.currentTimeMillis();         // notification time
            Context context = getApplicationContext();      // application Context
            CharSequence contentTitle = "GV Location Changed";  // message title
            CharSequence contentText = "Current Location: "+location;      // message text

            Intent notificationIntent;

            if(preferences.getBoolean(Settings.NOTIFICATION_APP_LAUNCH, false))
                notificationIntent = new Intent(this, MainMenu.class);
            else
                notificationIntent= new Intent(this, BlankIntent.class);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,0);



            // the next two lines initialize the Notification, using the configurations above
            Notification notification = new Notification(icon, tickerText, when);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            if(preferences.getBoolean(Settings.SOUND_ACTIVE,false )){
                notification.defaults |= Notification.DEFAULT_SOUND;
            }

            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
            mNotificationManager.notify(434, notification);
        }
    }




}
