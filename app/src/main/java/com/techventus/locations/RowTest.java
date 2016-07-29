package com.techventus.locations;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
//import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Button;


/**
 * Demonstrates wrapping a layout in a ScrollView.
 *
 */
public class RowTest extends Activity {
	
	String TAG = "TECHVENTUS - RowTest";
	

	GVLServiceInterface mIRemoteService;
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // Following the example above for an AIDL interface,
	        // this gets an instance of the IRemoteInterface, which we can use to call on the service
	        mIRemoteService = GVLServiceInterface.Stub.asInterface(service);
	    }

	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "Service has unexpectedly disconnected");
	        mIRemoteService = null;
	    }
	};
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 
        
	    Intent hello_service = new Intent(this, BackgroundService.class);
	    
		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
		
		
        
        
        setContentView(R.layout.phoneenabletable);

        LinearLayout layout = (LinearLayout) findViewById(R.id.phoneenablelayout);
        
        
        
        //LinearLayout locationTile = new LinearLayout(this);
    	//upper.setOrientation(0);
        layout.addView(spacer());
        TextView place = new TextView(this);
        place.setText("HOME");
        place.setTextSize(30);
        place.setGravity(Gravity.CENTER);
        layout.addView(place);
        
        TextView minispace = new TextView(this);
        minispace.setText("  ");
        minispace.setTextSize(6);
        layout.addView(minispace);
        
    	LinearLayout upper = new LinearLayout(this);
    	upper.setOrientation(0);
    	TextView PlaceHeader = new TextView(this);
    	PlaceHeader.setText("Phone");
    	PlaceHeader.setTextSize(14);
    	upper.addView(PlaceHeader);
    	PlaceHeader.setWidth(120);
    	//upper.addView(spacer());
    	
    	TextView Enabled = new TextView(this);
    	Enabled.setText("Enable");
    	Enabled.setTextSize(14);
    	Enabled.setWidth(50);
    	upper.addView(Enabled);
    	upper.addView(spacer());
        
    	TextView Disabled = new TextView(this);
    	Disabled.setText("Disable");
    	Disabled.setTextSize(14);
    	Disabled.setWidth(50);
    	upper.addView(Disabled);
    	upper.addView(spacer());
    	
    	TextView Neutral = new TextView(this);
    	Neutral.setText("Neutral");
    	Neutral.setTextSize(14);
    	Neutral.setWidth(55);
    	upper.addView(Neutral);
    	
    	
    	
    	
        layout.addView(upper);
        layout.addView(spacer());
       
        for (int i = 2; i < 8; i++) {
        	
        	LinearLayout inner = new LinearLayout(this);
        	inner.setOrientation(0);
        	
        	
            TextView phone = new TextView(this);
            phone.setText("Phone " + i);
            phone.setPadding(0, 12, 0, 0);

            phone.setWidth(120);
            inner.addView(phone);

            //inner.addView(spacer());
            
            
            
            RadioGroup rg = new RadioGroup(this);
            rg.setOrientation(0);
            RadioButton rb1 = new RadioButton(this);
            rb1.setWidth(55);
            rg.addView(rb1);
            rg.addView(spacer());
            RadioButton rb2 = new RadioButton(this);
            rg.addView(rb2);
            rg.addView(spacer());
            rb2.setWidth(55);
            RadioButton rb3 = new RadioButton(this);
            rg.addView(rb3);
            rb3.setWidth(55);
            
          //  rg.setGravity(Gravity.);
            
            inner.addView(rg);
            /*
            CheckBox checkEnabled = new CheckBox(this);
            checkEnabled.setWidth(40);
            inner.addView(checkEnabled);
           // inner.addView(spacer());
            CheckBox checkDisabled = new CheckBox(this);
            checkDisabled.setWidth(40);
            inner.addView(checkDisabled);
            inner.addView(spacer());
            CheckBox checkNeutral = new CheckBox(this);
            checkNeutral.setWidth(40);
            inner.addView(checkNeutral);
            */
           // RadioButton rb = new RadioButton(null, null, i);
            
           // inner.addView(checkNeutral);
          //  Button buttonView = new Button(this);
           // buttonView.setText("Button " + i);
           // buttonView.setWidth(200);
            //inner.addView(buttonView);
            layout.addView(inner);
        }
        
        Button saveButton = new Button(this);
        saveButton.setText("SAVE");
        saveButton.setWidth(100);
        Button cancelButton = new Button(this);
        cancelButton.setText("CANCEL");
        cancelButton.setWidth(100);
    	LinearLayout lower = new LinearLayout(this);
    	lower.setOrientation(0);
    	lower.addView(spacer());
    	lower.addView(spacer());
    	lower.addView(saveButton);
    	lower.addView(spacer());
    	lower.addView(spacer());
    	lower.addView(cancelButton);
    	layout.addView(spacer());
        layout.addView(lower);
        
    }
    
    
    
    
    
    
    void Save(){
    	
    	//fill in code here
    	
    	
    }
    
    
    TextView spacer(){
    	TextView ret = new TextView(this);
    	ret.setText("");
    	ret.setWidth(20);
    	return ret;
    }
    
    

	@Override 
	public void onResume(){
		super.onResume();
		
	    Intent hello_service = new Intent(this, BackgroundService.class);
	    
		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onPause(){
		try{
			unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		try{
			unbindService(mConnection);
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}		
	
    
}