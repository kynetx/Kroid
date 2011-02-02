package com.jessieamorris.KynetxLocation;

import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.kynetx.api;

public class ManageApps extends Activity {
	
	private EditText title;
	private api app;
    private KynetxSQLHelper mDbHelper;
	private KynetxService service;
	private boolean isBound;
	private static final int ACTIVITY_CREATE=0;
    private static final int CREATE_ID = Menu.FIRST;

	
	private ServiceConnection connection = new ServiceConnection(){
	    public void onServiceConnected(ComponentName className, IBinder serviceToBind) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        service = ((KynetxService.LocalBinder)serviceToBind).getService();
	        if(service.getApp() == null){
	        	setApp();
	        }
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        service = null;
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTitle("Test");


		title = (EditText) findViewById(R.id.title);
		Button confirmButton = (Button) findViewById(R.id.send);
		Button start = (Button)findViewById(R.id.start);
		Button stop = (Button)findViewById(R.id.stop);
		Button clear = (Button)findViewById(R.id.clear);
		Button setLocation = (Button)findViewById(R.id.send_location);
		
		start.setOnClickListener(startListener);
		stop.setOnClickListener(stopListener);
		
		mDbHelper = new KynetxSQLHelper(this);
		mDbHelper.open();
		
		clear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(app != null){
					app.clearCookies();
				}
			}
		});

		confirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				sendTest();
			}
		});
		
		setLocation.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				LocationManager loc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Location location = loc.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				
		    	Toast.makeText(getApplicationContext(), R.string.sending_data, Toast.LENGTH_SHORT).show();
				
				Log.i("KynetxService", "Sending data to Kynetx...");
				HashMap<String, String> args = new HashMap<String, String>();
				
				if(location == null){
					location = loc.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
				}
				
				if(location != null){
				
					String latitude = Double.toString(location.getLatitude());
					String longitude = Double.toString(location.getLongitude());
					
					args.put("accuracy", Float.toString(location.getAccuracy()));
					args.put("altitude", Double.toString(location.getAltitude()));
					args.put("latitude", latitude);
					args.put("longitude", longitude);
					args.put("newLocation", latitude + ", " + longitude );
					args.put("time", Long.toString(location.getTime()));
					
					app.sendEvent("mobile", "location", args);
					app.sendEvent("mobile", "location_updated", args);
				}
			}
		});
		
		fillSpinner();
		setApp();
		doBindService();
	}
	
    private void fillSpinner(){
    	Spinner appSpinner = (Spinner)findViewById(R.id.AppsSpinner);
    	Cursor apps = mDbHelper.fetchAllApps();
    	apps.moveToFirst();
    	String[] columns = new String[] { KynetxSQLHelper.KEY_APPID, KynetxSQLHelper.KEY_VERSION };
    	
    	int[] to = new int[] { R.id.appid, R.id.version };
    	
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.spinner_layout, apps, columns, to);
    			
    	appSpinner.setAdapter(adapter);
    	appSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long row) {
				if(row != 0){
					updateApp(row);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
    	});
    	
    }
    
    private void updateApp(long rowid){
    	Intent i = new Intent(ManageApps.this, EditApps.class);
    	i.putExtra(KynetxSQLHelper.KEY_ROWID,rowid);
    	startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    private void createApp() {
        Intent i = new Intent(ManageApps.this, EditApps.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
	private void sendTest(){
		HashMap<String, String> args = new HashMap<String, String>();
		String textToSend = title.getText().toString();
		
		Toast.makeText(this, R.string.sending_data, Toast.LENGTH_LONG);

		if(textToSend.equals("")){
			textToSend = "~test";
		}

		args.put("message", textToSend);

		app.sendEvent("mobile", "text", args);
	}
	
	private void setApp(){
		if(app == null){
			if(service != null && service.getApp() != null){
				app = service.getApp();
			} else {
				Cursor apps = mDbHelper.fetchAllApps();
				app = new KynetxApp(this, apps);
				if(service != null){	
					service.setApp(app);
				}
			} 
		} else {
			app.setApp(mDbHelper.fetchAllApps());
			if(service != null && service.getApp() == null){
				service.setApp(app);
			}
		}
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillSpinner();
        setApp();
    }
	
	private void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
		if(!isBound){
		    startService(new Intent(this, KynetxService.class));
		    bindService(new Intent(ManageApps.this, 
		            KynetxService.class), connection, Context.BIND_AUTO_CREATE);
		    isBound = true;
		}
	}

	private void doUnbindService() {
	    if (isBound) {
	        // Detach our existing connection.
	    	stopService(new Intent(this, KynetxService.class));
	    	unbindService(connection);
	        isBound = false;
	    }
	}
    
	private OnClickListener startListener = new OnClickListener() {
		public void onClick(View v){
			doBindService();
		}	        	
	};

	private OnClickListener stopListener = new OnClickListener() {
		public void onClick(View v){
			doUnbindService();
		}	        	
	};
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, CREATE_ID, 0, R.string.menu_add_app);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case CREATE_ID:
            	createApp();
        }

        return super.onMenuItemSelected(featureId, item);
    }
    
	@Override
	protected void onDestroy(){
		super.onDestroy();
		mDbHelper.close();
	}
}
