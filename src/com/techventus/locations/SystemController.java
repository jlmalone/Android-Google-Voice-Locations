package com.techventus.locations;

import android.os.Handler;

/**
 * Created with IntelliJ IDEA.
 * User: Joseph
 * Date: 23.11.13
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */
public class SystemController
{
    private static final String SHARED_PREFERENCES = "SharedPreferences";

    //Migrate Databases


    //1. Network Receiver Registration
//        if network available
    long TIMER_FREQUENCY_MILLIS = 5000L*60;
    boolean SERVICE_ENABLED ;





//    static mSharedPreferences ;



    private SystemController instance;

    private SystemController(){};



    public void load()
    {

    }

    public void save()
    {

    }

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            //DO SOME STUFF
            timerHandler.postDelayed(this, TIMER_FREQUENCY_MILLIS);
        }
    };











}
