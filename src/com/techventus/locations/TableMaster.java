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
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

@Deprecated
public class TableMaster extends Activity{
	
	String TAG = "TECHVENTUS - TableMaster";


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
	protected void  onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      
	    Intent hello_service = new Intent(this, BackgroundService.class);
	    
		bindService( hello_service, mConnection,Context.BIND_AUTO_CREATE);
		
		
      
      
      setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); 
        this.setContentView(R.layout.tablemaster);
       TableLayout tl = (TableLayout) findViewById(R.id.tablematrix);

      //  TableLayout tl = new TableLayout(R.id.tablematrix);
        

        for(int i=0;i<10;i++){
        	System.out.println("ROW"+i);
        	TableRow tr = new TableRow(this);
            tr.setId(100+i);
        //   tr.setLayoutParams(new LayoutParams(
        //            LayoutParams.FILL_PARENT,
         //           LayoutParams.WRAP_CONTENT)); 
        	TextView tv =new TextView(this);
        	tv.setId(200+i);
        	tv.setGravity(Gravity.LEFT);
        	
        	tv.setText("Hello ");
            tv.setLayoutParams(new LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));
            tr.addView(tv);

            /* Create a Button to be the row-content. */
            Button b = new Button(this);
            b.setText("Dynamic Button");
            b.setLayoutParams(new LayoutParams(
                      LayoutParams.FILL_PARENT,
                      LayoutParams.WRAP_CONTENT));
            /* Add Button to row. */
            tr.addView(b); 
            
            
        	TextView tv2 =new TextView(this);
        	tv2.setId(500+i);
        	tv2.setGravity(Gravity.RIGHT);
        	tv2.setText("Goodbye ");
        	//tv2.setTextColor(ColorS)
            tv2.setLayoutParams(new LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));
            tr.addView(tv2);
            
            
            
            
        	
        	//TextView tv2 =new TextView(tr.getContext());
        	//tv2.setText("Goodbye 200000"+i+"");
        	
        	//tv2.setGravity(Gravity.RIGHT);
        	//tr.addView(tv);
        	//this.
        	//tr.addView(tv2);
        	//TableRow.LayoutParams lp = new TableRow.LayoutParams();
        	//lp.column
        	//TableLayout.LayoutParams lp2 = new TableLayout.LayoutParams();
        	
        	
        	//tr.addView(tv);
        	//tr.addView(tv2);
            tl.addView(tr, new TableLayout.LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));


        	
        }
       
        
        //TableLayout table = (TableLayout)findViewById(R.layout.tablemaster);
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
