package com.techventus.locations;

import java.util.ArrayList;
import java.util.List;

import com.techventus.server.voice.Voice;
import com.techventus.server.voice.datatypes.AllSettings;
import com.techventus.server.voice.datatypes.Phone;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * The Class SQLHelper.
 */
public class SQLHelper {

	static String dropLocation = "DROP TABLE LOCATION;";
	/** The drop locations. */
	static String dropLocations = "DROP TABLE LOCATIONS;";
	static String dropGoogle = "DROP TABLE GOOGLE;";
	/** The drop phone. */
    static String dropPhone = "DROP TABLE PHONE;";
	
	/** The drop location phone enable. */
	static String dropLocationPhoneEnable = "DROP TABLE LOCATIONPHONEENABLE;";
	
	static String dropStatus = "DROP TABLE STATUS;";
	static String dropServiceStatus = "DROP TABLE SERVICESTATUS;";
	static String dropSettings = "DROP TABLE SETTINGS;";
	

	//TODO - CONSIDER DELETING THIS TABLE AS DISTINCT QUERY FROM CREATE LOCATION PHONE ENABLE OUGHT TO BE SUFFICIENT

	//TODO - When Ts+Cs are Approved, Create this table.
	/** The create location phone enable. */
	static String createLocationPhoneEnable =  "CREATE TABLE IF NOT EXISTS LOCATIONPHONEENABLE  (locationName VARCHAR NOT NULL, phoneName VARCHAR NOT NULL, phoneEnable INTEGER NOT NULL, locationLatitudeE6 INTEGER NOT NULL, locationLongitudeE6 INTEGER NOT NULL, radius INTEGER NOT NULL, PRIMARY KEY (locationName,phoneName));";
	

    //TODO CONSIDER MOVING ALL THIS TO UNIT TEST CASES
    /*
    *//** The phone insert test1. *//*
   static String phoneInsertTest1 = "INSERT INTO PHONE (phoneName) VALUES ('android');";
    
    *//** The phone insert test2. *//*
    static String phoneInsertTest2 = "INSERT INTO PHONE (phoneName) VALUES ('gizmo');";
    
    *//** The phone insert test3. *//*
    static String phoneInsertTest3 = "INSERT INTO PHONE (phoneName) VALUES ('techventus');";
    
    *//** The phone insert test4. *//*
    static String phoneInsertTest4 = "INSERT INTO PHONE (phoneName) VALUES ('home');";
    
    *//** The phone insert test5. *//*
    static String phoneInsertTest5 = "INSERT INTO PHONE (phoneName) VALUES ('work');";
    
    *//** The location phone enable insert test1. *//*
    static String locationPhoneEnableInsertTest1 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation1', 'android', 1);";
    
    *//** The location phone enable insert test2. *//*
    static String locationPhoneEnableInsertTest2 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation1', 'gizmo', 1);";
    
    *//** The location phone enable insert test3. *//*
    static String locationPhoneEnableInsertTest3 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation1', 'techventus', 0);";
    
    *//** The location phone enable insert test4. *//*
    static String locationPhoneEnableInsertTest4 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation1', 'home', -1);";
    
    *//** The location phone enable insert test5. *//*
    static String locationPhoneEnableInsertTest5 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation1', 'work', 2);";
    
    *//** The location phone enable insert test6. *//*
    static String locationPhoneEnableInsertTest6 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation2', 'android', 2);";
    
    *//** The location phone enable insert test7. *//*
    static String locationPhoneEnableInsertTest7 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation2', 'gizmo', 2);";
    
    *//** The location phone enable insert test8. *//*
    static String locationPhoneEnableInsertTest8 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation2', 'techventus', 1);";
    
    *//** The location phone enable insert test9. *//*
    static String locationPhoneEnableInsertTest9 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation2', 'home', 0);";
    
    *//** The location phone enable insert test10. *//*
    static String locationPhoneEnableInsertTest10 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation2', 'work', 2);";
    
    *//** The location phone enable insert test11. *//*
    static String locationPhoneEnableInsertTest11 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation3', 'android', 2);";
    
    *//** The location phone enable insert test12. *//*
    static String locationPhoneEnableInsertTest12 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation3', 'gizmo', 2);";
    
    *//** The location phone enable insert test13. *//*
    static String locationPhoneEnableInsertTest13 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation3', 'techventus', 1);";
    
    *//** The location phone enable insert test14. *//*
    static String locationPhoneEnableInsertTest14 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation3', 'home', 1);";
    
    *//** The location phone enable insert test15. *//*
    static String locationPhoneEnableInsertTest15 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation3', 'work', -1);";
    
    *//** The location phone enable insert test16. *//*
    static String locationPhoneEnableInsertTest16 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation4', 'android', -1);";
    
    *//** The location phone enable insert test17. *//*
    static String locationPhoneEnableInsertTest17 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation4', 'gizmo', -1);";
    
    *//** The location phone enable insert test18. *//*
    static String locationPhoneEnableInsertTest18 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation4', 'techventus', -1);";
    
    *//** The location phone enable insert test19. *//*
    static String locationPhoneEnableInsertTest19 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation4', 'home', -1);";
    
    *//** The location phone enable insert test20. *//*
    static String locationPhoneEnableInsertTest20 = "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('testlocation4', 'work', -1);";
    */
//    static String updateGoogleConnectedStatement = "UPDATE STATUS SET status = 'Connected' WHERE key = 'google';";
//    static String insertNoGoogleConnectionStatement = "INSERT INTO STATUS (key,status) VALUES ('google','No Connection');";
//    static String insertNoGoogleCredentialsStatement = "INSERT INTO STATUS (key,status) VALUES ('google','No Login Credentials');";
//    static String insertGoogleConnectedStatement = "INSERT INTO STATUS (key,status) VALUES ('google','Connected');";
//    static String insertPowerLowStatement = "INSERT INTO STATUS (key,status) VALUES ('power','POWER_LOW');";
//    static String insertAccuracyFineStatement = "INSERT INTO STATUS (key,status) VALUES ('accuracy','ACCURACY_COARSE');";
    
    //static String updateNoGoogleConnectionStatement = "UPDATE STATUS SET status = 'No Connection' WHERE key = 'google';";
	/** The select stored google credentials statement. */
	static String selectStoredGoogleCredentialsStatement = "SELECT loginName,password FROM GOOGLE;";

	
	

	/** The select locations statement. */
	static String selectLocationsStatement = "SELECT locationName,locationLatitude,locationLongitude,locationRadius, enabled FROM LOCATIONS;";
	

	
	
	public static boolean isTableExists(String tableName, SQLiteDatabase db ) {
	   

	    Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
	    if(cursor!=null) {
	        if(cursor.getCount()>0) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
	
    /**
	 * Exec.
	 *
	 * @param db the db
	 * @param statement the statement
	 */
	public static void exec(SQLiteDatabase db, String statement){
    	try{
    		Log.e("TECHVENTUS","EXECUTION: "+statement);
    		db.execSQL(statement);
    	}catch(Exception e){
    		Log.e("TECHVENTUS","EXECUTION: FAIL");
    		e.printStackTrace();
    	}
    }
    
    /**
     * Creates the databases.
     *
     * @param db the db
     */
    public static void createDatabases(SQLiteDatabase db){
    	Log.e("TECHVENTUS","CREATE TABLES");
    	exec(db,createLocationPhoneEnable );
    }
    
    /**
     * Establish test db settings.
     *
     * @param db the db
     */
    public static void establishTestDBSettings(SQLiteDatabase db){

    	Log.e("TECHVENTUS","CREATING TABLES");

    	exec(db,createLocationPhoneEnable );

    }
    
    /**
     * Report table.
     *
     * @param sql the sql
     * @param query the query
     * @param dimension the dimension
     */
    public static void reportTable(SQLiteDatabase sql, String query, int dimension ){
    	System.out.println("Printing Table "+query+" dimension:"+dimension);
    	try{
    	Cursor c = sql.rawQuery(query, null);
    	if(c!=null){
    		while(c.moveToNext()){
    			for(int i=0;i<dimension;i++){
    				System.out.println(c.getString(i)+" , ");
    			}
    			System.out.println();
    		}
    		c.close();
    	}
    	
    	}catch(Exception y){
    		y.printStackTrace();
    	}
    }
    
    /**
     * List voice phone list.
     *
     * @param voice the voice
     */
    public static void listVoicePhoneList(Voice voice){
    	try{
    		Log.e("TECHVENTUSPHONE","Attempt to listVoicePhone");
    		if(voice!=null){
    			AllSettings voiceSettings = voice.getSettings(false);
    			if(voiceSettings!=null){
        			if(voiceSettings.getPhones()!=null){
        				if(voiceSettings.getPhones().length>0){
        		    		Log.e("TECHVENTUSPHONE","LISTING Voice.phoneList");
        		    		for(Phone phone:voiceSettings.getPhones()){
        		    			Log.e("TECHVENTUSPHONE","voice.phone "+phone.getName());
        		    		}
        		    		Log.e("TECHVENTUSPHONE","Done LISTING Voice.phoneList");
        				}else{
        					Log.e("TECHVENTUSPHONE","Error.  phoneList Not NULL, but Empty");
        				}
        			}else{
        				Log.e("TECHVENTUSPHONE","Error.  phoneList is Null");
        			}
    			}else{
    				Log.e("TECHVENTUSPHONE","Error.  voice AllSettings is Null");
    			}

    		}else{
    			Log.e("TECHVENTUSPHONE","Error.  Voice si NULL");
    		}
    		

    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

    
    /**
     * Gets the locations.
     *
     * @param sql the sql
     * @return the locations
     */
    public static String[] getLocations(SQLiteDatabase sql){
    	List<String > res = new ArrayList<String>();
			Cursor c = sql.rawQuery("SELECT DISTINCT locationName FROM LOCATIONPHONEENABLE;", null);
			if(c!=null){
				Log.e("TECHVENTUSPHONE","Cursor NOT NULL SELECT * FROM PHONE block...Printing output");
				while (c.moveToNext()){
					res.add(c.getString(0));
					Log.e("TECHVENTUSPHONE","Phone : "+c.getString(0));
				}
				Log.e("TECHVENTUSPHONE","DONE PRINTING PHONE block");
				c.close();
			}else{
				Log.e("TECHVENTUSPHONE","Null cursor in SELECT * FROM PHONE block");
			}
			
    	return res.toArray(new String[res.size()]);
    }
    
    
}
    
    

    

