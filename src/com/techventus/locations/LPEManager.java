package com.techventus.locations;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Joseph
 * Date: 21.11.13
 * Time: 23:00
 * To change this template use File | Settings | File Templates.
 */
public class LPEManager {

    static LocationPhoneEnablePreferences2 prefs;

    private Set<String> phoneSet;
    private Set<String> locationNameSet;


    private  void loadandRectifyPreferences(Context context)
    {
        if(prefs==null)
        {
            prefs = getLPEPreferences(context);

        }
        if(prefs==null)
        {

        }



    }


    private LPEManager singleton;


    private static final String SHARED_PREFERENCES = "SharedPreferences";

    private LPEManager(Context context){

        prefs = getLPEPreferences(context);
    }

    public LPEManager getInstance(Context context)
    {
        if(singleton==null)
        {
            singleton = new LPEManager(context);
        }
        return singleton;
    }

    private LocationPhoneEnablePreferences2 getLPEPreferences(Context context)
    {

        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String lpe_prefs = sharedPreferences.getString(LPE_PREFERENCES_KEY, null);

        if(lpe_prefs==null)
        {
            LocationPhoneEnablePreferences2 ret = new LocationPhoneEnablePreferences2();
            return ret;
        }
        else
        {
            Gson gson = new Gson();
            LocationPhoneEnablePreferences2 ret = gson.fromJson(lpe_prefs,LocationPhoneEnablePreferences2.class);
            return ret;
        }

    }

    private void saveLPEPreferences(Context context,LocationPhoneEnablePreferences2 prefs )
    {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String serialisedPrefs = gson.toJson(prefs);
        editor.putString(LPE_PREFERENCES_KEY,serialisedPrefs);
        editor.commit();
    }



    public static final String KEY_PREFIX =  "com.techventus.locations";
     public static final String LPE_PREFERENCES_KEY = "com.techventus.locations.preferences_key";


    /**
     * Given a Geofence object's ID and the name of a field
     * (for example, KEY_LATITUDE), return the key name of the
     * object's values in SharedPreferences.
     *
     * @return The full key name of a value in SharedPreferences
     */
    private String getLPEMapping(LocationPhoneEnablePreference2 lpe2)
    {
        return KEY_PREFIX + "_" + lpe2.getGvLocation().getName() + "_" + lpe2.getPhoneName();
    }



}
