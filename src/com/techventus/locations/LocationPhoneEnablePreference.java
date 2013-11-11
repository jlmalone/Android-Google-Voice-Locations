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
	//private static  Context mContext;
	
	private final static Map<String,GeoPoint> locationMap = new HashMap<String,GeoPoint>();
	private final static Map<String,Integer> radiusMap = new HashMap<String,Integer>();
	
//	private final static Set<String> phoneSet = new HashSet<String>();
	//Set<PreferenceKey> preferenceKeys = new HashSet<PreferenceKey>();
	private final static HashMap<PreferenceKey,Integer> preferenceMap = new HashMap<PreferenceKey,Integer>();
	
	private static LocationPhoneEnablePreference singleton = null;
	
	
	
//	
//	public String[] getPhoneArray(){
//		List<String> tmpPhoneList = new ArrayList<String>();
//		for(String phone:phoneSet){
//			if(phone.equalsIgnoreCase("NOPHONE")){
//				
//			}else{
//				tmpPhoneList.add(phone);
//			}
//		}
//		String[] ret = tmpPhoneList.toArray(new String[tmpPhoneList.size()]);
//		return ret;
//	}
//	
	
	public void reset(){
		//context = null;
		locationMap.clear();
//		phoneSet.clear();
		preferenceMap.clear();
		radiusMap.clear();
		//locationMap = new HashMap<String,GeoPoint>();
		 //phoneSet = new HashSet<String>();
		//Set<PreferenceKey> preferenceKeys = new HashSet<PreferenceKey>();
		//preferenceMap = new HashMap<PreferenceKey,Integer>();
		
		singleton = null;
		
	}
	
	public static  LocationPhoneEnablePreference  getLocationPhoneEnablePreference(Context context){
		if(singleton==null){
			singleton = new LocationPhoneEnablePreference();
//			mContext = context;
			loadFromDB(context);
		}
//		mContext = context;
		return singleton;
		
		
	}
	
//	public static void updateContext(Context context){
//		LocationPhoneEnablePreference.mContext = context;
//	}
	
//	public static  void updatePhoneSet(List<String> phoneList, Context context){
//		phoneSet.clear();
//		phoneSet.addAll(phoneList);
//		phoneSet.add("NOPHONE");
//		rectifyPreferencesTask(context).execute();
//		//rectifyPreferences();
//		
//	}
	
//	public static void addLocation(String location, GeoPoint geo, Context context){
//		locationMap.put(location,geo);
//		rectifyPreferencesTask(context).execute();
//		//rectifyPreferences();
//	}
	
//	public static void removeLocation(String location, Context context){
//		locationMap.remove(location);
//		rectifyPreferencesTask(context).execute();
//		//rectifyPreferences();
//	}
	
//	
//	public  synchronized void updateLocationSet(Map<String,GeoPoint> newlocationMap, Context context) {
//		locationMap.clear();
//		locationMap.putAll(newlocationMap);
//		rectifyPreferencesTask(context).execute();
//		//rectifyPreferences();
//		
//	}
	

//	
//	public static AsyncTask<Void,Void,Void> rectifyPreferencesTask(final Context context){
//
//		 AsyncTask<Void,Void,Void> ret = new AsyncTask<Void,Void,Void>(){
//
//			@Override
//			protected Void doInBackground(Void... arg0) {
//				//return null;
////				cleanTable();
//				storeValues(context);
//				return null;
//			}
//			
//		};
//		return ret;
//	}
//	
	
//	
//	private static  void storeValues(Context context){
//		try{
//		SQLiteDatabase sql = context.openOrCreateDatabase("db",0,null);
//		//sql.execSQL(SQLHelper.createLocationPhoneEnable);
//		SQLHelper.exec(sql, SQLHelper.clearLocationPhoneEnable);
//		for(PreferenceKey key:preferenceMap.keySet()){
//			SQLiteStatement stmt = sql.compileStatement("INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable, locationLatitudeE6, locationLongitudeE6,radius) VALUES (?,?,?,?,?,?);");
//			//'"+key.phone+"',"+preferenceMap.get(key).intValue()+";)");
//			stmt.bindString(1, key.location);
//			stmt.bindString(2, key.phone);
//			stmt.bindLong(3,preferenceMap.get(key).intValue() );
//			stmt.bindDouble(4,(double)( locationMap.get(key.location).getLatitudeE6()));
//			stmt.bindDouble(5, (double)( locationMap.get(key.location).getLongitudeE6()));
//			stmt.bindLong(6,(long)300);
//			
//			stmt.execute();
//		}
//		}catch(Exception g){
//			g.printStackTrace();
//			Log.e(TAG,"store Value InsertError");
//		}
//	}
//	
	
//	public static void rectifyPreferences(){
//		//DELETE ANY ENTRIES WITH BAD LOCATIONS
//		//DELETE ANY ENTRIES WITH BAD PHONES
//		//CREATE LOCAL LIST OF ALL GOOD LOCATION PHONE PAIRS
//		//INSERT NONEXISTENT LOCATION,KEY PAIRS WITH
//		cleanTable();
//		
//		//MAKE PERSISTENT BY STORING TO DATABASE
//		storeValues();
//	}
	
	public static void loadFromDB(Context context){
		SQLiteDatabase sql = context.openOrCreateDatabase("db",0,null);
		try{
			
			Cursor c = sql.rawQuery("SELECT locationName,phoneName,phoneEnable, locationLatitudeE6, locationLongitudeE6,radius FROM LOCATIONPHONEENABLE;", null);
			c.moveToNext();
			if(c.isFirst()){
				do{
					   Log.e("TECHVENTUS","RESULT - Load LocationPhoneEnable from DB "+c.getString(0)+", "+c.getString(1)+","+c.getString(2)+","+c.getString(3)+","+c.getString(4)+","+c.getString(5));
					   //SQLHelper.exec(sql, "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES '"+c.getString(0)+"','"+c.getString(1)+"',"+c.getInt(2)+");");
					   PreferenceKey key = new PreferenceKey(c.getString(0),c.getString(1));
					   preferenceMap.put(key, c.getInt(2));
					   locationMap.put(c.getString(0),new GeoPoint(c.getInt(3),c.getInt(4)));
					   radiusMap.put(c.getString(0), c.getInt(5));
					   //locationMap.put(c.getString(0), c.getInt(4));
//					   phoneSet.add(c.getString(1));
					   LPEPref lpe = new LPEPref(c.getString(0), c.getString(1), c.getInt(5), c.getInt(2), c.getInt(3), c.getInt(4) );
				}while(c.moveToNext());
			}else{
				throw new Exception("loadFromDB.  Cursor Not First");
			}
			//sql.execSQL(SQLHelper.createLocationPhoneEnable);
			//SQLHelper.exec(sql, SQLHelper.clearLocationPhoneEnable);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			sql.close();
		}
		
		
//		phoneSet.add("NOPHONE");
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
			//sql.execSQL(SQLHelper.createLocationPhoneEnable);
			//SQLHelper.exec(sql, SQLHelper.clearLocationPhoneEnable);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			sql.close();
		}
		
		return ret;
		
	}
	
	public static List<LPEPref> loadListFromDB(Context context){
		SQLiteDatabase sql = context.openOrCreateDatabase("db",0,null);
		List<LPEPref> ret = new ArrayList<LPEPref>();
		try{
			
			Cursor c = sql.rawQuery("SELECT locationName,phoneName,phoneEnable, locationLatitudeE6, locationLongitudeE6,radius FROM LOCATIONPHONEENABLE;", null);
			c.moveToNext();
			if(c.isFirst()){
				do{
					   Log.e("TECHVENTUS","RESULT - Load LocationPhoneEnable from DB "+c.getString(0)+", "+c.getString(1)+","+c.getString(2)+","+c.getString(3)+","+c.getString(4)+","+c.getString(5));
					   //SQLHelper.exec(sql, "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES '"+c.getString(0)+"','"+c.getString(1)+"',"+c.getInt(2)+");");
//					   PreferenceKey key = new PreferenceKey(c.getString(0),c.getString(1));
//					   preferenceMap.put(key, c.getInt(2));
//					   locationMap.put(c.getString(0),new GeoPoint(c.getInt(3),c.getInt(4)));
//					   radiusMap.put(c.getString(0), c.getInt(5));
//					   //locationMap.put(c.getString(0), c.getInt(4));
//					   phoneSet.add(c.getString(1));
					   LPEPref lpe = new LPEPref(c.getString(0), c.getString(1), c.getInt(5), c.getInt(2), c.getInt(3), c.getInt(4) );
					   ret.add(lpe);
				}while(c.moveToNext());
			}else{
				throw new Exception("loadFromDB.  Cursor Not First");
			}
			//sql.execSQL(SQLHelper.createLocationPhoneEnable);
			//SQLHelper.exec(sql, SQLHelper.clearLocationPhoneEnable);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			sql.close();
		}
		
		return ret;
//		phoneSet.add("NOPHONE");
//		locationMap.put("Elsewhere", new GeoPoint(0,0));
//		radiusMap.put("Elsewhere", 0);
		
	}
	
	
	
	
	public static void updatePreference(String location, String phone, int preference, int lat, int lon) throws Exception{
		if(preference>1||preference<-1){
			throw new Exception("INVALID PHONE PREFERENCE, MUST BE {+1, 0, -1}");
		}
		
//		phoneSet.add(phone);
		locationMap.put(location,new GeoPoint(lat,lon));
		preferenceMap.put(new PreferenceKey(location,phone),preference);
		
	}



//
//	private static void cleanTable(){
//		for(PreferenceKey key:preferenceMap.keySet()){
//			if(!locationMap.containsKey(key.location)){
//				preferenceMap.remove(key);
//			}
////			if(!phoneSet.contains(key.phone)){
////				preferenceMap.remove(key);
////			}
//		}
//		
//		for(String location:locationMap.keySet()){
//			for(String phone:phoneSet){
//				PreferenceKey key = new PreferenceKey(location,phone);
//			
//				if(!preferenceMap.containsKey(key)){
//					preferenceMap.put(key, 0);
//				}
//			}
//		}
//	}
	
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

	public String[] getLocations() {
		
		
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

					   //LPEPref lpe = new LPEPref(c.getString(0), c.getString(1), c.getInt(5), c.getInt(2) );
					   ret.add(c.getString(0));
				}while(c.moveToNext());
			}else{
				throw new Exception("loadFromDB.  Cursor Not First");
			}
			//sql.execSQL(SQLHelper.createLocationPhoneEnable);
			//SQLHelper.exec(sql, SQLHelper.clearLocationPhoneEnable);
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


//	public void deleteLocation(String locationName, Context context) {
//		locationMap.remove(locationName);
//		
//		for(PreferenceKey key:preferenceMap.keySet()){
//			if(key.location.equals(locationName)){
//				preferenceMap.remove(key);
//			}
//		}
//		radiusMap.remove(locationName);
////		cleanTable();
//		storeValues(context);
//		
//		
//		
//	}
	
	
}



//
//
//public void clearPhoneSet(){
//	phoneSet.clear();
//}
//
//public void clearLocationSet(){
//	locationSet.clear();
//}
//

