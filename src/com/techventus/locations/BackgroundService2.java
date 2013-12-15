package com.techventus.locations;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
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
import java.util.*;

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

    AsyncTask<Void,Void,Void> mStartupTask;
    AsyncTask<Void,Void,Void> mReconnectToVoiceTask;
    AsyncTask<Void,Void,Void> mLocationChangeTask;


    String mCurrentLocation = "Elsewhere";

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
             //TODO REMOVE ALL GEOFENCES
            if(mStartupTask!=null)
            {
                mStartupTask.cancel(true);
            }
            status.reset();
            try{VoiceSingleton.reset()/*voiceFact=null*/;}catch(Exception e){e.printStackTrace();Log.e(TAG+" Reset","voice Exception");}


            //TODO DO A BUNCH OF SHIT
            //copied here. Consider it's redundancy
            status.reset();
            VoiceSingleton.reset();

            //PULL THIS INTO A SEPARATE METHOD

            SQLiteDatabase db = null;
            try
            {
                db= openOrCreateDatabase("db",0,null);
                SQLHelper.exec(db,SQLHelper.dropLocationPhoneEnable );
                if(SQLHelper.isTableExists("COMMAND",db) ||SQLHelper.isTableExists("PHONE",db) )
                {
                    SQLHelper.exec(db,SQLHelper.dropPhone );
                    SQLHelper.exec(db,SQLHelper.dropGoogle );
                    SQLHelper.exec(db,SQLHelper.dropLocations );
                    SQLHelper.exec(db,SQLHelper.dropLocation );
                    SQLHelper.exec(db, SQLHelper.dropStatus);
                    SQLHelper.exec(db, SQLHelper.dropServiceStatus);
                    SQLHelper.exec(db, SQLHelper.dropSettings);
                }

            }
            catch(Exception e)
            {
                e.printStackTrace();Log.e(TAG+" Reset","LocationManager Exception");
            }
            finally
            {
                if(db!=null)
                {
                    db.close();
                }
            }

            if(mStartupTask!=null)
            {
                mStartupTask.cancel(true);
            }

            mStartupTask = getStartupTask();
            mStartupTask.execute();
        }

        @Override
        public String getCurrentLocationString() throws RemoteException
        {
               return Status.currentLocationString;
        }

        @Override
        public void restart() throws RemoteException
        {
            if(mStartupTask!=null)
            {
                mStartupTask.cancel(true);
            }
            mStartupTask = getStartupTask();
            mStartupTask.execute();
        }

        @Override
        public void update() throws RemoteException
        {

        }


    };


    // Holds the location client
    private LocationClient mLocationClientPlayServices;
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntentPlayServices;
    // Defines the allowable request types.
    // public enum REQUEST_TYPE = {REQUEST_TYPE.ADD}
    private REQUEST_TYPE mRequestTypePlayServices;
    // Flag that indicates if a request is underway.
    private boolean mInProgressPlayServices;

    @Override
    public void onCreate() {
          super.onCreate();

        //TAKEN FROM ORIGINAL
        Toast.makeText(this,"Start BackgroundService2 ",2000).show();

        mSettings = Settings.getInstance();

        //Awareness of Network Change
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        preferences = getSharedPreferences(Settings.PREFERENCENAME, 0);
        if(!preferences.getBoolean(Settings.SERVICE_ENABLED, false)){
            this.stopSelf();
            return;
        }else{
            if(mStartupTask!=null)
            {
                mStartupTask.cancel(true);
            }
            mStartupTask = getStartupTask();
            mStartupTask.execute();
        }

        // Start with the request flag set to false
        mInProgressPlayServices = false;

        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(this);


        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();


        registerReceiver(mNetworkStateReceiver, filter);

        registerReceiver(stopServiceReceiver, new IntentFilter(
                "com.techventus.locations.stopservice"));

        registerReceiver(mGeofencesChangedReceiver, new IntentFilter("com.techventus.locations.geofenceschanged"));

    }

    @Override
    public  void onDestroy()
    {
        unregisterReceiver(stopServiceReceiver);
        unregisterReceiver(mNetworkStateReceiver);
        unregisterReceiver(mGeofencesChangedReceiver);
        mLocationClientPlayServices.disconnect();
        Toast.makeText(this,"DESTROYING SERVICE",Toast.LENGTH_LONG).show();
        super.onDestroy();
    }


    BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             //To change body of implemented methods use File | Settings | File Templates.
             Log.v(TAG, "stop service receiver triggered");
             removeGeofences(getLocationIdList());

             stopSelf();
         }
     };

    BroadcastReceiver mGeofencesChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "geofences changed receiver");
        }
    };


    //TODO Consider Launching FLAGGED TASKS FROM OTHER METHODS
    /** The network state receiver.  This allows immediate response to network change events . */
    BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.w("Network Listener", "Network Type Changed");
            Bundle extras =  intent.getExtras();
            boolean noConnectivity = extras.getBoolean(android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY );

            if(!noConnectivity)
            {
                if( mSettings.getReconnectToVoiceFlag() || mSettings.getPhoneUpdateFlag())
                {
                    if(Util.isNetworkConnected(BackgroundService2.this))
                    {
                        if(mSettings.getReconnectToVoiceFlag())
                        {
                            if(mReconnectToVoiceTask!=null)
                            {
                                mReconnectToVoiceTask.cancel(true);
                            }
                            mReconnectToVoiceTask = reconnectToVoiceTask();
                            mReconnectToVoiceTask.execute();
                        }
                        else if( mSettings.getPhoneUpdateFlag())
                        {
                            if(mLocationChangeTask!=null)
                            {
                                mLocationChangeTask.cancel(true);
                            }
                            mLocationChangeTask = LocationChangeTask();
                            mLocationChangeTask.execute();
                        }
                    }
                }
            }
        }
    };


    /**
     * Reconnect to voice task.
     *
     * @return the async task
     */
    AsyncTask<Void,Void,Void> reconnectToVoiceTask()
    {
        AsyncTask<Void,Void,Void> ret = new AsyncTask<Void,Void,Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                try{
                    reconnectToVoice();
                }catch (com.techventus.server.voice.exception.BadAuthenticationException ba){
                    preferences.edit().remove("username").remove("password").apply();

                }catch(Exception f){
                    f.printStackTrace();
                    mSettings.setReconnectToVoiceFlag(true);
                }
                return null;
            }
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                if(preferences.getBoolean(Settings.SERVICE_ENABLED, false))
                {

                    if(!Util.isNetworkConnected(BackgroundService2.this))
                    {
                        //startupStarted = false;
                        this.cancel(true);
                    }
                }
                else
                {
                    this.cancel(true);
                    BackgroundService2.this.stopSelf();
//					cancel();
                }
            }
        };
        return ret;
    }

    SimpleGeofenceStore mGeofenceStorage;

    List<Geofence>  mCurrentGeofences = new ArrayList<Geofence>();

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
        mRequestTypePlayServices = REQUEST_TYPE.ADD;
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
        mLocationClientPlayServices = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgressPlayServices) {
            // Indicate that a request is underway
            mInProgressPlayServices = true;
            // Request a connection from the client to Location Services
            mLocationClientPlayServices.connect();
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
        mRequestTypePlayServices = REQUEST_TYPE.REMOVE_INTENT;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        // Store the PendingIntent
        mGeofenceRequestIntentPlayServices = requestIntent;
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClientPlayServices = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgressPlayServices) {
            // Indicate that a request is underway
            mInProgressPlayServices = true;
            // Request a connection from the client to Location Services
            mLocationClientPlayServices.connect();
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
//    List<String> mGeofencesToRemove;

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


    public void onConnected(Bundle dataBundle)
    {
        Toast.makeText(this,"ON CONNECTED TO PLAY SERVICES", Toast.LENGTH_LONG).show();
        try {


            switch (mRequestTypePlayServices) {

                case ADD :
                    // Get the PendingIntent for the request
                    mTransitionPendingIntent =
                            getTransitionPendingIntent();
                    // Send a request to add the current geofences

                    mLocationClientPlayServices.addGeofences(mCurrentGeofences, mTransitionPendingIntent, this);
                    break;
                // If removeGeofencesById was called
                case REMOVE_LIST :
                    mLocationClientPlayServices.removeGeofences(
                            getLocationIdList(), this);
                    break;
                case REMOVE_INTENT :
                    mLocationClientPlayServices.removeGeofences(
                            mGeofenceRequestIntentPlayServices, this);
                    break;
            }

        }catch(Exception e)
        {
            Toast.makeText(this,"Exception "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            mLocationClientPlayServices.connect();
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
            int statusCode, String[] geofenceRequestIds)
    {
        // If removing the geocodes was successful
        if (LocationStatusCodes.SUCCESS == statusCode)
        {
            Log.v("TAG","SUCCESSFULLY REMOVED GEOFENCES");
            /*
             * Handle successful removal of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else
        {
            // If removing the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
            Toast.makeText(this,"PROBLEM REMOVING GEOFENCES"+statusCode,Toast.LENGTH_LONG);
        }
        // Indicate that a request is no longer in progress
        mInProgressPlayServices = false;
//        // Disconnect the location client
//        mLocationClientPlayServices.disconnect();
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
    public void removeGeofences(List<String> geofenceIds)
    {

        try {


            // If Google Play services is unavailable, exit
            // Record the type of removal request
            mRequestTypePlayServices = REQUEST_TYPE.REMOVE_LIST;
            /*
             * Test for Google Play services after setting the request type.
             * If Google Play services isn't present, the request can be
             * restarted.
             */
            if (!servicesConnected()) {
                return;
            }
            // Store the list of geofences to remove
    //        mGeofencesToRemove = geofenceIds;
            /*
             * Create a new location client object. Since the current
             * activity class implements ConnectionCallbacks and
             * OnConnectionFailedListener, pass the current activity object
             * as the listener for both parameters
             */
            mLocationClientPlayServices = new LocationClient(this, this, this);
            // If a request is not already underway
            if (!mInProgressPlayServices) {
                // Indicate that a request is underway
                mInProgressPlayServices = true;
                // Request a connection from the client to Location Services
                mLocationClientPlayServices.connect();
            } else {
                /*
                 * A request is already underway. You can handle
                 * this situation by disconnecting the client,
                 * re-setting the flag, and then re-trying the
                 * request.
                 */
            }

        }catch(Exception e)
        {
            mLocationClientPlayServices.connect();
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
            mInProgressPlayServices = false;
            mLocationClientPlayServices.disconnect();
        }



    // Implementation of OnConnectionFailedListener.onConnectionFailed
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Turn off the request flag
        mInProgressPlayServices = false;
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
        mInProgressPlayServices = false;
        // Destroy the current location client
        mLocationClientPlayServices = null;
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
//                mSettings.setRestartServiceFlag(false);



                if(timer!=null)
                {
                    timer.cancel();
                }

                super.onPreExecute();
                if(preferences.getBoolean(Settings.SERVICE_ENABLED, false))
                {

                    if(!Util.isNetworkConnected(BackgroundService2.this))
                    {
//                        startupStarted = false;
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
            protected Void doInBackground(Void... arg0)
            {
                SQLiteDatabase sql = null;


                    sql = openOrCreateDatabase("db",0,null);

                    if(SQLHelper.isTableExists("COMMAND",sql) ||SQLHelper.isTableExists("PHONE",sql) )
                    {
                        SQLHelper.exec(sql, SQLHelper.dropLocationPhoneEnable);
                        SQLHelper.exec(sql,SQLHelper.dropPhone );
                        SQLHelper.exec(sql,SQLHelper.dropGoogle );
                        SQLHelper.exec(sql,SQLHelper.dropLocations );
                        SQLHelper.exec(sql,SQLHelper.dropLocation );
                        SQLHelper.exec(sql, SQLHelper.dropStatus);
                        SQLHelper.exec(sql, SQLHelper.dropServiceStatus);
                        SQLHelper.exec(sql, SQLHelper.dropSettings);
                    }

                    //TODO REPLACE THIS WITH RESTORING LPE Preferences from an object
                    SQLHelper.createDatabases(sql);

                    setLocationList();




                if(sql!=null)
                        sql.close();


                try {
                    reconnectToVoice();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                return null;
            }
            @Override
            protected void onPostExecute(Void result)
            {
               //TODO TEMP REPLACE THIS SOMEHOW
               // setLocationUpdates();

                setUpdateTimers();

                Set<String> locationNameSet = new HashSet<String>();

                //create the geofences with unique locations
                for(LPEPref pref: mSettings.getPrefsList())
                {
                    if(!locationNameSet.contains(pref.location))
                    {
                        locationNameSet.add(pref.location);
                        //add geopointListener
                        double lat = (double)pref.latitude/1E6;
                        double lon = (double)pref.longitude/1E6;
                        if(pref.location.equalsIgnoreCase("Elsewhere") || pref.radius==0)
                        {
                            continue;
                        }

                        //TODO MESSY CUT AND PASTE
                        //PULL THIS INTO A METHOD
                        {
                            Geofence.Builder builder = new Geofence.Builder();
                            builder.setCircularRegion(lat,lon,pref.radius);
                            builder.setRequestId(pref.location+"_EXIT");
                            builder.setExpirationDuration(Geofence.NEVER_EXPIRE) ;
                            builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT);

                            Geofence a =  builder.build();

                            mCurrentGeofences.add(a);
                        }
                        {
                            Geofence.Builder builder = new Geofence.Builder();
                            builder.setCircularRegion(lat,lon,pref.radius);
                            builder.setRequestId(pref.location+"_ENTER");
                            builder.setExpirationDuration(Geofence.NEVER_EXPIRE) ;
                            builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
                            Geofence a =  builder.build();

                            mCurrentGeofences.add(a);

                        }
                        addGeofences();

                    }

                }

                //TODO register receivers here

            }
        };
        return startupTask;
    }



   private List<String> getLocationIdList(){

       List<String> ret = new ArrayList<String>();


       Set<String> locationNameSet = new HashSet<String>();

       //create the geofences with unique locations
       for(LPEPref pref: mSettings.getPrefsList())
       {
           if(!locationNameSet.contains(pref.location))
           {
               locationNameSet.add(pref.location);

               ret.add(pref.location+"_EXIT" );
               ret.add(pref.location+"_ENTER" );
           }
       }

       return ret;

   }

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
        mSettings.setPrefsList(ret);
//        prefList = ret;
    }

    Settings mSettings;

    /**
     * Reconnect to voice.
     */
    protected void reconnectToVoice() throws IOException
    {
        if(!Util.isNetworkConnected(BackgroundService2.this))
        {
            mSettings.setReconnectToVoiceFlag(true);
            return;
        }

        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        if(!username.equals("")&&!password.equals(""))
        {
            VoiceSingleton.getOrCreateVoiceSingleton(username,Settings.decrypt(password, 10));
            mSettings.setReconnectToVoiceFlag(false);
        }
        else
        {

            VoiceSingleton.reset();
        }
    }


    /** The preferences. */
    SharedPreferences preferences ;

    //TODO EXPERIMENTAL
    //TODO This should be refactored into an OS Level Wake Up and should maybe be converted to
    //a handler
    void setUpdateTimers(){

        try{
            int phoneUpdateFreq = preferences.getInt(Settings.GOOGLE_SYNC_FREQUENCY, 30)*60000;
            if(timer!=null)
                timer.cancel();
            timer = new Timer();
            if(phoneUpdateFreq>30000)
                timer.schedule(phoneUpdateTimerTask(),20000, phoneUpdateFreq);
            else
                Log.e(TAG,"Error In Set Update Timers");
        }catch(Exception e){
            e.printStackTrace();
        }

    }




    //TODO REPLACE WITH RUNNABLE for a Handler
    TimerTask phoneUpdateTimerTask(){
        TimerTask phoneUpdateTimerTask = new TimerTask(){
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
        return phoneUpdateTimerTask;
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
            for(LPEPref lpe:mSettings.getPrefsList()){
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

                if(preferences.getBoolean(Settings.SERVICE_ENABLED, false))
                {


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
//                Intent notificationIntent = new Intent(BackgroundService2.this, MainMenu.class);
//                PendingIntent pendingIntent = PendingIntent.getActivity(BackgroundService2.this, 0,notificationIntent, 0);
//                Notification notification = new Notification(R.drawable.ic_menu_compass, "New Message", System.currentTimeMillis());
//                notification.setLatestEventInfo(BackgroundService2.this,!mSettings.getPhoneUpdateFlag()+"Google Voice Locations", "Now: "+com.techventus.locations.Status.currentLocationString, pendingIntent);
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
                for(LPEPref lpe: mSettings.getPrefsList() ){
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
    private void notifyUserLocationChange(String location)
    {
        if (!preferences.getBoolean(Settings.SERVICE_ENABLED, true))
        {
            BackgroundService2.this.stopSelf();
            return;
        }
        if (preferences.getBoolean(Settings.NOTIFICATION_ACTIVE, false))
        {

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

            if(preferences.getBoolean(Settings.SOUND_ACTIVE,false ))
            {
                notification.defaults |= Notification.DEFAULT_SOUND;
            }

            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
            mNotificationManager.notify(434, notification);
        }
    }
}
