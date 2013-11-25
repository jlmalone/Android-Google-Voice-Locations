package com.techventus.locations;

import java.util.List;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Overlay;

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

	GeoPoint centrepoint = new GeoPoint(39421791, -98084006);
	private MapController mapController;
	protected MapView mapView;

    AdView mAdView;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.currentlocation);

        mAdView= (AdView)this.findViewById(R.id.ad);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        mAdView.loadAd(adRequest);

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

			mapOverlays = mapView.getOverlays();
			drawable = this.getResources().getDrawable(
					R.drawable.mapmarkergreen);
			itemizedOverlay = new HelloItemizedOverlay(drawable, this);

			 OverlayItem overlayitem = new OverlayItem(point, "myLocation",
			 "myLocation");

			 itemizedOverlay.addOverlay(overlayitem);

			mapOverlays.add(itemizedOverlay);
			mapOverlays.add(myLocation);

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


	@Override
	public void onResume() {
		super.onResume();
		myLocation.enableMyLocation();
		myLocation.enableCompass();
	}

}
