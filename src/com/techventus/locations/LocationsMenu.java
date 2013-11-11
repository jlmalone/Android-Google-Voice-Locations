package com.techventus.locations;

//import java.util.ArrayList;
import java.util.HashSet;
//import java.util.List;
import java.util.Set;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
//import android.view.Menu;
//import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LocationsMenu extends ListActivity {

	String TAG = "TECHVENTUS - LocationsMenu";

	String[] LOCATIONS = new String[] { "\u2295 Add Location", "Elsewhere" };

	
	void fixLOCATIONS(){
		Set<String> lset = new HashSet<String>();
	
		//ADD EVERYTHING TO SET
		lset.add("\u2295 Add Location");
		lset.add("Elsewhere");
		for(String loc: LOCATIONS){
			lset.add(loc);
		}
		LOCATIONS = new String[ lset.size()];
		LOCATIONS[0]= "\u2295 Add Location";
		LOCATIONS[1]= "Elsewhere";
		int i=2;
		for(String loc:lset){
			if(!loc.equals("\u2295 Add Location")&& !loc.equals("Elsewhere")){
				LOCATIONS[i]=loc;
				i++;
			}
		}

	}
	


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// setContentView(R.layout.locationsmenu);

//		 Bundle bundle = getIntent().getExtras();
//		 try{
//			 if(bundle!=null && bundle.containsKey(Settings.LOCATION_ARRAY_EXTRA))
//				 LOCATIONS =  bundle.getStringArray(Settings.LOCATION_ARRAY_EXTRA);
//		 }catch(Exception e){
//			 
//		 }

	}

	@Override
	protected void onResume() {
		super.onResume();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
	
		
		
		
		SQLiteDatabase db =  openOrCreateDatabase("db",0,null);
		SQLHelper.exec(db, SQLHelper.createLocationPhoneEnable);
		
		
		
		LOCATIONS = SQLHelper.getLocations(db);
//		SQLHelper.exec(db, "DELETE FROM LOCATIONPHONEENABLE WHERE locationName = '"+locationName+"'");
//		SQLHelper.exec(db, prepareInsert());		
		db.close();
		
		
		
		
		fixLOCATIONS();
		setListAdapter(new ArrayAdapter<String>(LocationsMenu.this,
				android.R.layout.simple_list_item_1, LOCATIONS));

		getListView().setTextFilterEnabled(true);
		
		
		
		// Intent hello_service = new Intent(this, LocationService.class);
//		Intent hello_service = new Intent(this, BackgroundService.class);
//
//		bindService(hello_service, mConnection, Context.BIND_AUTO_CREATE);

		Log.e("TECHVENTUS", "RESUMING LOCATIONS MENU");
		// resetLocations();
		// setListAdapter(new ArrayAdapter<String>(LocationsMenu.this,
		// android.R.layout.simple_list_item_1, LOCATIONS));

		// getListView().setTextFilterEnabled(true);
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// System.out.println((String)
		// getListView().getItemAtPosition(position));

		if (position == 0) {
			Intent i = new Intent(this, NewLocation.class);

			startActivity(i);
		} else {
			Intent i = new Intent(this, LocationDecisionMenu.class);

			Bundle bundle = new Bundle();

			bundle.putString(Settings.LOCATION_NAME_EXTRA/*"locationName"*/, LOCATIONS[position]);

			Log.e("TECHVENTUS", bundle.getString(Settings.LOCATION_NAME_EXTRA/*"locationName"*/));
			// i.putExtra("locationName",LOCATIONS[position]);
			i.putExtras(bundle);

			startActivity(i);

			finish();
		}


	}

	@Override
	public void onPause() {
		super.onPause();
//		try {
//			unbindService(mConnection);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		this.finish();
	}
	
	
//	GVLServiceInterface mIRemoteService;
//	private ServiceConnection mConnection = new ServiceConnection() {
//		// Called when the connection with the service is established
//		public void onServiceConnected(ComponentName className, IBinder service) {
//			// Following the example above for an AIDL interface,
//			// this gets an instance of the IRemoteInterface, which we can use
//			// to call on the service
//			mIRemoteService = GVLServiceInterface.Stub.asInterface(service);
//	
//			
//	
//	
//		}
//	
//		// Called when the connection with the service disconnects unexpectedly
//		public void onServiceDisconnected(ComponentName className) {
//			Log.e(TAG, "Service has unexpectedly disconnected");
//			mIRemoteService = null;
//		}
//	};
	
	
}




//AsyncTask<Void, Void, Void> DisplayTask() {
//	AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//
//		@Override
//		protected Void doInBackground(Void... params) {
//
//
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//
//		}
//
//	};
//	return task;
//}

//void resetLocations() {
//	try {
//		LOCATIONS = mIRemoteService.getLocations();
//	} catch (RemoteException e) {

//		e.printStackTrace();
//	}
//	// SQLiteDatabase sql = openOrCreateDatabase("db",0,null);
//	// LOCATIONS =
//	// getStringArSQLVertical("SELECT locationName from LOCATIONS;",sql);
//	// SQLHelper.listPhoneAndLocationPhoneEnable(sql);
//	// sql.close();
//}





//void setLocationsAr() {
//	String[] locationsBundleAr = new String[0];
//	Set<String> l = new HashSet<String>();
//	try {
//		locationsBundleAr = mIRemoteService.getLocations();
//		l.add("\u2295 Add Location");
//		for (int i = 0; i < locationsBundleAr.length; i++) {
//			// if(!locationsBundleAr.equals("Elsewhere")){
//			l.add(locationsBundleAr[i]);
//		}
//		l.add("Elsewhere");
//		LOCATIONS = l.toArray(new String[l.size()]);
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//}

// if(position == LOCATIONS.length-1){

// }

// System.out.println("Post");




// @Override
// public void onDestroy(){
// try{
// unbindService(mConnection);
// }catch(Exception e){
// e.printStackTrace();
// }
// super.onDestroy();
// }
//
// }
/*
 * try{ Set<String> g = savedInstanceState.keySet(); List<String> locList = new
 * ArrayList<String>(); locList.add("Add Location"); for(String loc:g){
 * if(!loc.equals("Elsewhere")){ locList.add(loc); } } locList.add("Elsewhere");
 * LOCATIONS = new String [locList.size()]; int i = 0; for(String loc:locList){
 * LOCATIONS[i] = loc; i++; }
 * 
 * }catch(Exception e){ e.printStackTrace(); }
 */



//
// private String[] getStringArSQLVertical(String query, SQLiteDatabase
// sql){
// String[] ret = new String[0];
// List<String> list = new ArrayList<String>();
// list.add("\u2295 Add Location");
//
// try{
//
// Cursor c = sql.rawQuery(query, null);
// if(c!=null){
// while(c.moveToNext()){
// if(!c.getString(0).equals("Elsewhere"))
// list.add(c.getString(0));
// Log.e("TECHVENTUS",c.getString(0));
// }
// c.close();
// }
//
// }catch(Exception o){
// o.printStackTrace();
// }
//
// list.add("Elsewhere");
//
// ret = list.toArray(new String[list.size()]);
//
// return ret;
// }
//
//






