package com.techventus.locations;

//import java.util.ArrayList;
import java.util.List;

//import android.app.Activity;
//import android.content.Context;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.content.pm.ActivityInfo;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;
import android.graphics.drawable.Drawable;
//import android.location.Location;
////import android.location.LocationListener;
//import android.location.LocationManager;

import android.os.Bundle;
//import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
//import android.widget.RelativeLayout;
//import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
//import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

//import com.google.android.maps.OverlayItem;

public class LocationMap extends MapActivity {

	String TAG = "TECHVENTUS - LocationMap";

	Button myLocationButton;
	Button setButton;
	Button cancelButton;

	String locationName;
	boolean isNewLocation = false;
	int radius = 100;

	List<Overlay> mapOverlays;
	Drawable drawable;
	HelloItemizedOverlay itemizedOverlay;
	MyLocationOverlay myLocation;
	 GeoPoint point = new GeoPoint(37421791,-122084006);
	//GeoPoint point = new GeoPoint(0, 0);

	GeoPoint centrepoint = new GeoPoint(39421791, -98084006);
	private MapController mapController;
	protected MapView mapView;

	// private LocationManager locationManager;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		// setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
		setContentView(R.layout.currentlocation); // bind the layout to the
													// activity
		mapView = (MapView) findViewById(R.id.mapview);
		myLocationButton = (Button) findViewById(R.id.thisLocation);
		setButton = (Button) findViewById(R.id.mapSetButton);
		cancelButton = (Button) findViewById(R.id.mapCancelButton);

		
		myLocation = new MyLocationOverlay(this, mapView);
		myLocation.enableMyLocation();
		myLocation.enableCompass();

		Bundle receivedBundle = getIntent().getExtras();

		locationName = receivedBundle
				.getString(Settings.LOCATION_NAME_EXTRA/* "locationName" */);

		try {
			radius = receivedBundle.getInt(Settings.RADIUS_EXTRA/* "radius" */);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (receivedBundle.containsKey("isNew"))
				isNewLocation = receivedBundle.getBoolean("isNew");
		} catch (Exception r) {
			r.printStackTrace();
		}

		mapController = mapView.getController();
		mapController.setCenter(centrepoint);
		mapController.setZoom(4);
		try {
			int lat = receivedBundle
					.getInt(Settings.LATITUDE_EXTRA/* "latitude" */);
			int lon = receivedBundle
					.getInt(Settings.LONGITUDE_EXTRA/* "longitude" */);
			if (lat != 0 && lon != 0 && lat != -1 && lon != -1) {
				point = new GeoPoint(lat, lon);
				Log.e("TECHVENTUS", "LAT LON SET " + lat + " " + lon);
				mapController.setCenter(point);
				mapController.setZoom(6);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		setButton.setOnClickListener(setClick);
		cancelButton.setOnClickListener(cancelClick);

		try {
			mapView.setBuiltInZoomControls(true);
			myLocationButton.bringToFront();
			myLocationButton.setClickable(true);

			try {
				GeoPoint gpp = myLocation.getMyLocation();

				if (gpp != null) {
					point = gpp;
					mapController.setCenter(point);
				}
			} catch (Exception t) {
				Log.e("FAIL", "Exception Occured " + t.getLocalizedMessage()
						+ t.getStackTrace()[0] + t.getStackTrace()[1]);
			}

			// myLocation.enableCompass();

			Log.i("TECHVENTUS", "Before Map Overlay");
			mapOverlays = mapView.getOverlays();
			drawable = this.getResources().getDrawable(
					R.drawable.mapmarkergreen);
			Log.i("TECHVENTUS", "Resource Definition");
			itemizedOverlay = new HelloItemizedOverlay(drawable, this);
			Log.i("TECHVENTUS", "Done with BasisOverlay Creation");

			 OverlayItem overlayitem = new OverlayItem(point, "myLocation",
			 "myLocation");

			// BasisOverlay bo = new BasisOverlay(itemizedOverlay);
			Log.i("TECHVENTUS", "Done with OverlayItem");
			 itemizedOverlay.addOverlay(overlayitem);

			// Overlay g = new Overlay();
			// mapOverlays.add(bo);
			mapOverlays.add(itemizedOverlay);
			// mapOverlays.
			mapOverlays.add(myLocation);
			Log.i("TECHVENTUS", "Done adding Overlay");

			myLocationButton.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					try {
						
						GeoPoint p = myLocation.getMyLocation();
						if (p != null) {
							itemizedOverlay.onTap(p, mapView);
							mapController.setCenter(p);
							if (mapView.getZoomLevel() < 6) {
								mapController.setZoom(6);
							}
						}
					} catch (Exception e) {
						System.out.println("Exception caught with Button");
					}
				}
			});

		} catch (Exception e) {
			Log.e("FAIL",
					"Exception Occured " + e.getLocalizedMessage()
							+ e.getStackTrace()[0] + e.getStackTrace()[1]);
		}
		
		myLocation.runOnFirstFix(new Runnable(){
			@Override
			public void run() {
				try{
				if(point.getLatitudeE6()==37421791 && point.getLongitudeE6()==-122084006){
					mapController.animateTo(myLocation.getMyLocation());
					itemizedOverlay.onTap(myLocation.getMyLocation(), mapView);
					mapController.setZoom(9);
				}
				}catch(Exception d){
					d.printStackTrace();
				}
			}});
	}

	OnClickListener setClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(point.getLatitudeE6()!=0 && point.getLongitudeE6()!=0){
				Intent setPrefIntent = new Intent(LocationMap.this,PhonePreference.class);
				setPrefIntent.putExtra(Settings.LOCATION_NAME_EXTRA,locationName);
				setPrefIntent.putExtra(Settings.LATITUDE_EXTRA, point.getLatitudeE6());
				setPrefIntent.putExtra(Settings.LONGITUDE_EXTRA, point.getLongitudeE6());
				setPrefIntent.putExtra(Settings.RADIUS_EXTRA, radius);
				setPrefIntent.putExtra("isNew", isNewLocation);
				startActivity(setPrefIntent);
				LocationMap.this.finish();
			}
		}

	};

	OnClickListener cancelClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.e("TECHVENTUS", "CANCEL CLICK");
			myLocation.disableCompass();
			myLocation.disableMyLocation();
			finish();
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		myLocation.disableMyLocation();
		myLocation.disableCompass();
		this.finish();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	void fireErrorToast() {
		Toast.makeText(
				this,
				"Error in Geopoint\n\rReselect Point.\n\rContact Developer if Error Persists",
				Toast.LENGTH_LONG);
	}

	@Override
	public void onResume() {
		super.onResume();
		myLocation.enableMyLocation();
		myLocation.enableCompass();
	}

}

/*
 * try{
 * 
 * locationManager = (LocationManager)
 * getSystemService(Context.LOCATION_SERVICE);
 * 
 * Location l
 * =locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); int lat
 * = (int) (l.getLatitude() * 1E6); int lng = (int) (l.getLongitude() * 1E6);
 * System.out.println(lat+" "+lng); GeoPoint point = new GeoPoint(lat, lng);
 * mapController.setCenter(point); }catch(Exception r){ Log.e("Joe",
 * "Location exception"+r.getLocalizedMessage()); }
 */
/*
 * locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000,
 * 0, new GeoUpdateHandler());
 */

/*
 * private void locClick(){ Intent i = new Intent(LocationMap.this,
 * LocationsMenu.class);
 * 
 * Log.w("TECHVENTUS", "Starting Activity"); LocationMap.this.startActivity(i);
 * }
 */

// @Override
// public void onPause(){
// try{
// unbindService(mConnection);
// }catch(Exception e){
// e.printStackTrace();
// }
// super.onPause();
// }
//
// @Override
// public void onDestroy(){
// try{
// unbindService(mConnection);
// }catch(Exception e){
// e.printStackTrace();
// }
// super.onDestroy();
// }

/*
 * private String[] getStringArSQLVertical(String query, SQLiteDatabase sql){
 * String[] ret = new String[0]; try{ Cursor c = sql.rawQuery(query, null);
 * List<String> list = new ArrayList<String>(); if(c!=null){
 * while(c.moveToNext()){ list.add(c.getString(0));
 * Log.e("TECHVENTUS","GET STRING AR"+c.getString(0)); //c.moveToNext(); } }
 * 
 * 
 * if(list.size()>0){ ret = list.toArray(new String[list.size()]);
 * Log.e("TECHVENTUS","GET STRING AR List size"+list.size()); //ret = new
 * String[list.size()]; //for(int i=0;i<list.size();i++){ // ret[i] =
 * list.get(i); //} } //c.getString(0); c.close(); }catch(Exception o){
 * 
 * o.printStackTrace(); } return ret; }
 */

/*
 * OnClickListener setClick = new OnClickListener(){
 * 
 * @Override public void onClick(View arg0) {
 * Log.e("TECHVENTUS","Starting SET CLICK FINISH MAP"); //SQL SQLiteDatabase sql
 * = openOrCreateDatabase("db",0,null); try{ if(isNewLocation){
 * 
 * // String sqlString =
 * "INSERT INTO LOCATIONS (locationName, locationLatitude, locationLongitude, locationRadius, enabled) VALUES "
 * + //
 * "('"+locationName+"',"+point.getLatitudeE6()+","+point.getLongitudeE6()+","
 * +radius+",'"+true+"');"; // System.out.println(sqlString); // //
 * sql.execSQL(sqlString); // // // // sqlString =
 * "SELECT phoneName FROM PHONE;"; // //
 * System.out.println("SELECT phoneName from PHONE"); //
 * 
 * 
 * // String[] phones =getStringArSQLVertical(sqlString,sql); //
 * if(phones.length>0){ //
 * Log.e("TECHVENTUSPHONE","PHONES LENGTH "+phones.length); // for(int
 * i=0;i<phones.length;i++){ // System.out.println(
 * "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('"
 * +locationName+"','"+phones[i]+"',-"+2+");"); // sql.execSQL(
 * "INSERT INTO LOCATIONPHONEENABLE (locationName,phoneName,phoneEnable) VALUES ('"
 * +locationName+"','"+phones[i]+"',-"+2+");"); // // } // } //
 * sql.execSQL("INSERT INTO COMMAND (command) VALUES ('rectify');"); // }else{
 * 
 * // // String sqlString =
 * "UPDATE LOCATIONS SET locationLatitude = "+point.getLatitudeE6
 * ()+" , locationLongitude = "
 * +point.getLongitudeE6()+" where locationName = '"+locationName+"';"; //
 * System.out.println(sqlString); // sql.execSQL(sqlString); } sql.close();
 * }catch(Exception y){ Log.e("TECHVENTUS","Exception thrown");
 * y.printStackTrace(); }
 * 
 * 
 * // locationName VARCHAR, locationLatitude DOUBLE," // + " locationLongitude
 * DOUBLE, locationRadius INTEGER
 * 
 * 
 * if(isNewLocation){
 * 
 * if(point==null){ fireErrorToast(); }else{ Intent i = new
 * Intent(LocationMap.this,LocationDecisionMenu.class);
 * 
 * i.putExtra("locationName",locationName); i.putExtra("locationLatitude",
 * point.getLatitudeE6()); i.putExtra("locationLongitude",
 * point.getLongitudeE6()); i.putExtra("locationRadius", radius);
 * i.putExtra("locationEnabled", true); startActivity(i); } }
 * 
 * Log.e("TECHVENTUS","ABOUT TO FINISH MAP"); myLocation.disableCompass();
 * myLocation.disableMyLocation(); LocationMap.this.finish(); }
 * 
 * };
 */


// GVLServiceInterface mIRemoteService;
// private ServiceConnection mConnection = new ServiceConnection() {
// // Called when the connection with the service is established
// public void onServiceConnected(ComponentName className, IBinder service)
// {
// // Following the example above for an AIDL interface,
// // this gets an instance of the IRemoteInterface, which we can use to
// call on the service
// mIRemoteService = GVLServiceInterface.Stub.asInterface(service);
// }
//
// // Called when the connection with the service disconnects unexpectedly
// public void onServiceDisconnected(ComponentName className) {
// Log.e(TAG, "Service has unexpectedly disconnected");
// mIRemoteService = null;
// }
// };