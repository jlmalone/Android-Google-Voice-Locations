package com.techventus.locations;


import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class DeleteLocation extends ListActivity{
	
	String TAG = "TECHVENTUS - DeleteLocation";


	
	String[] LOCATIONS ;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		this.setContentView(R.layout.deletelocation);

	        
	    LinearLayout outerlayout= (LinearLayout)  this.findViewById(R.id.deletelocation);
	        

	    if(LOCATIONS!=null){
	    	
	    
	    	
	    	for(int i=0;i<LOCATIONS.length;i++){
	    		LinearLayout ll = new LinearLayout(this);
	    		ll.setOrientation(LinearLayout.VERTICAL);
	    		TextView loc = new TextView(this);
	    		loc.setText(LOCATIONS[i]);
	    		loc.setTextSize(23);
	    		
	    		TextView spacer = new TextView(this);
	    		spacer.setHeight(1);
	    		spacer.setWidth(LayoutParams.MATCH_PARENT);
	    		spacer.setBackgroundColor(Color.parseColor("#444444"));
	    		//loc.setLayoutParams(params)
	    		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
	    		
	    		LayoutParams loclp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
	    		//loclp.setMargins(8, 16, 0, 16);	    		
	    		loc.setLayoutParams(loclp);
	    		loc.setPadding(16, 16, 0, 16);
	    		ll.setLayoutParams(lp);
	    		ll.addView(loc);
	    		ll.addView(spacer);
	    		outerlayout.addView(ll);
	    		
	    		final String finalLocationName = LOCATIONS[i];
	    		
	    		ll.setOnClickListener(destroyClick(finalLocationName));
	    	}
	    }else{
    		LinearLayout ll = new LinearLayout(this);
    		ll.setOrientation(LinearLayout.VERTICAL);
    		TextView loc = new TextView(this);
    		loc.setText("No Locations Set");
    		loc.setTextSize(23);
    		
    		
    		TextView spacer = new TextView(this);
    		spacer.setHeight(1);
    		spacer.setWidth(LayoutParams.MATCH_PARENT);
    		spacer.setBackgroundColor(Color.parseColor("#444444"));
    		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    		
    		LayoutParams loclp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    		loc.setPadding(8, 8, 0, 8);
    		loc.setLayoutParams(loclp);
    		ll.setLayoutParams(lp);
    		ll.addView(loc);
    		ll.addView(spacer);
    		outerlayout.addView(ll);
    		ll.setOnClickListener(closeClick);
	    }
	    

	}
	
	OnClickListener closeClick = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			DeleteLocation.this.finish();
			
		}
		
	};
	
	 OnClickListener destroyClick(final String locationName){
		
		OnClickListener ret = new OnClickListener(){
		

			@Override
			public void onClick(View v) {
				try{
					
					Bundle b = new Bundle();
					b.putString(Settings.LOCATION_NAME_EXTRA/*"locationName"*/, locationName);
					Intent i = new Intent(DeleteLocation.this,ConfirmDelete.class);
					i.putExtras( b);
					DeleteLocation.this.startActivity(i);
				
					DeleteLocation.this.finish();

				}catch(Exception e){
					e.printStackTrace();
				}
				//ADD MORE SHIT LATER
				DeleteLocation.this.finish();
			}
			
		};
		
		return ret;
	}
	

	
	@Override 
	public void onResume(){
		super.onResume();
	}

	@Override
	public void onPause(){
		this.finish();
	}
	

	
	
}
