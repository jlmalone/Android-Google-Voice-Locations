package com.techventus.locations;

import java.util.HashSet;
import java.util.Set;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
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


	}

	@Override
	protected void onResume() {
		super.onResume();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
	
		
		
		
		SQLiteDatabase db =  openOrCreateDatabase("db",0,null);
		SQLHelper.exec(db, SQLHelper.createLocationPhoneEnable);
		
		
		
		LOCATIONS = SQLHelper.getLocations(db);
		db.close();
		
		
		
		
		fixLOCATIONS();
		setListAdapter(new ArrayAdapter<String>(LocationsMenu.this,
				android.R.layout.simple_list_item_1, LOCATIONS));

		getListView().setTextFilterEnabled(true);
		
		
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

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
		this.finish();
	}
	

}







