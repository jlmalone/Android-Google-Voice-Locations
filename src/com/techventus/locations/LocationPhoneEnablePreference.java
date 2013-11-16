package com.techventus.locations;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

@Deprecated
public class LocationPhoneEnablePreference {

	private final static String TAG = "TECHVENTUS - LocationPhoneEnablePreference";

	private final static Map<String,GeoPoint> locationMap = new HashMap<String,GeoPoint>();
	private final static Map<String,Integer> radiusMap = new HashMap<String,Integer>();
	private final static HashMap<PreferenceKey,Integer> preferenceMap = new HashMap<PreferenceKey,Integer>();
	private static LocationPhoneEnablePreference singleton = null;
	
	public void reset(){
		locationMap.clear();
		preferenceMap.clear();
		radiusMap.clear();

		singleton = null;
		
	}
	
	public static  LocationPhoneEnablePreference  getLocationPhoneEnablePreference(Context context){
		if(singleton==null){
			singleton = new LocationPhoneEnablePreference();
			loadFromDB(context);
		}
		return singleton;
		
		
	}

	public static void loadFromDB(Context context)
    {
		SQLiteDatabase sql = context.openOrCreateDatabase("db",0,null);
		try
        {
			
			Cursor c = sql.rawQuery("SELECT locationName,phoneName,phoneEnable, locationLatitudeE6, locationLongitudeE6,radius FROM LOCATIONPHONEENABLE;", null);
			c.moveToNext();
			if(c.isFirst()){
				do
                {
					   Log.e("TECHVENTUS","RESULT - Load LocationPhoneEnable from DB "+c.getString(0)+", "+c.getString(1)+","+c.getString(2)+","+c.getString(3)+","+c.getString(4)+","+c.getString(5));
					   PreferenceKey key = new PreferenceKey(c.getString(0),c.getString(1));
					   preferenceMap.put(key, c.getInt(2));
					   locationMap.put(c.getString(0),new GeoPoint(c.getInt(3),c.getInt(4)));
					   radiusMap.put(c.getString(0), c.getInt(5));
					   LPEPref lpe = new LPEPref(c.getString(0), c.getString(1), c.getInt(5), c.getInt(2), c.getInt(3), c.getInt(4) );
				}while(c.moveToNext());
			}else{
				throw new Exception("loadFromDB.  Cursor Not First");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			sql.close();
		}
		
		
		locationMap.put("Elsewhere", new GeoPoint(0,0));
		radiusMap.put("Elsewhere", 0);
		
	}
	
	
	
	public static List<LPEPref> loadListFromDB(Context context, String condition){
		SQLiteDatabase sql = context.openOrCreateDatabase("db",0,null);
		List<LPEPref> ret = new ArrayList<LPEPref>();
		try{
			
			Cursor c = sql.rawQuery("SELECT locationName,phoneName,phoneEnable, locationLatitudeE6, locationLongitudeE6,radius FROM LOCATIONPHONEENABLE WHERE "+condition+";", null);
			c.moveToNext();
			if(c.isFirst()){
				do{
					   Log.e("TECHVENTUS","RESULT - Load LocationPhoneEnable from DB "+c.getString(0)+", "+c.getString(1)+","+c.getString(2)+","+c.getString(3)+","+c.getString(4)+","+c.getString(5));

					   LPEPref lpe = new LPEPref(c.getString(0), c.getString(1), c.getInt(5), c.getInt(2), c.getInt(3) , c.getInt(4));
					   ret.add(lpe);
				}while(c.moveToNext());
			}else{
				throw new Exception("loadFromDB.  Cursor Not First");
			}
		}catch(Exception e)
        {
			e.printStackTrace();
		}finally
        {
			sql.close();
		}
		
		return ret;
		
	}
	
	public static List<LPEPref> loadListFromDB(Context context)
    {
		SQLiteDatabase sql = context.openOrCreateDatabase("db",0,null);
		List<LPEPref> ret = new ArrayList<LPEPref>();
		try{
			
			Cursor c = sql.rawQuery("SELECT locationName,phoneName,phoneEnable, locationLatitudeE6, locationLongitudeE6,radius FROM LOCATIONPHONEENABLE;", null);
			c.moveToNext();
			if(c.isFirst()){
				do{
					   Log.e("TECHVENTUS","RESULT - Load LocationPhoneEnable from DB "+c.getString(0)+", "+c.getString(1)+","+c.getString(2)+","+c.getString(3)+","+c.getString(4)+","+c.getString(5));
					   LPEPref lpe = new LPEPref(c.getString(0), c.getString(1), c.getInt(5), c.getInt(2), c.getInt(3), c.getInt(4) );
					   ret.add(lpe);
				}while(c.moveToNext());
			}else{
				throw new Exception("loadFromDB.  Cursor Not First");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			sql.close();
		}
		
		return ret;

	}
	
	
	
	
	public static void updatePreference(String location, String phone, int preference, int lat, int lon) throws Exception{
		if(preference>1||preference<-1){
			throw new Exception("INVALID PHONE PREFERENCE, MUST BE {+1, 0, -1}");
		}
		
		locationMap.put(location,new GeoPoint(lat,lon));
		preferenceMap.put(new PreferenceKey(location,phone),preference);
		
	}



	
	 private static class PreferenceKey{
		protected String phone;
		protected String location;
		public PreferenceKey(String location, String phone){
			this.phone = phone;
			this.location = location;
		}
		
		@Override
		public boolean equals(Object o){
			try{
				if(this.phone.equals(((PreferenceKey)o).phone)){
					if(this.location.equals(((PreferenceKey)o).location))
						return true;
				}
			
			}catch(Exception g){
			
			}
			return false;
		}
	}

	public String[] getLocations()
    {
		return locationMap.keySet().toArray(new String[locationMap.keySet().size()]);
	}
	
	public static String[] getLocations(Context context){
		SQLiteDatabase sql = context.openOrCreateDatabase("db",0,null);
		Set<String> ret = new HashSet<String>();
		try{
			
			Cursor c = sql.rawQuery("SELECT locationName,phoneName,phoneEnable, locationLatitudeE6, locationLongitudeE6,radius FROM LOCATIONPHONEENABLE;", null);
			c.moveToNext();
			if(c.isFirst()){
				do{
					   Log.e("TECHVENTUS","RESULT - Load Location from DB "+c.getString(0));

					   ret.add(c.getString(0));
				}while(c.moveToNext());
			}else{
				throw new Exception("loadFromDB.  Cursor Not First");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			sql.close();
		}
		
		if(!ret.contains("Elsewhere")){
			ret.add("Elsewhere");
		}

		return ret.toArray(new String[ret.size()]);
	}
}
