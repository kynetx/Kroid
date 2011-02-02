package com.jessieamorris.KynetxLocation;

import java.util.HashMap;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.kynetx.api;

public class KynetxService extends Service {

	//private NotificationManager mNM;

	private boolean passiveEnabled = false;
    private boolean gpsEnabled = false;
    private boolean networkEnabled = false;
    private LocationManager location;
    private long timeStamp;

    private api app  = null;
    
    private LocationListener locationUpdate = new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
			if(app == null){
				newApp();
			}
			if(app != null && timeStamp != location.getTime()){
				// If at the same time as the last, don't send the data.
				doToast(R.string.sending_data);
				Log.i("KynetxService", "Sending data to Kynetx...");
				HashMap<String, String> args = new HashMap<String, String>();
				
				String latitude = Double.toString(location.getLatitude());
				String longitude = Double.toString(location.getLongitude());
				
				args.put("accuracy", Float.toString(location.getAccuracy()));
				args.put("altitude", Double.toString(location.getAltitude()));
				args.put("latitude", latitude);
				args.put("longitude", longitude);
				args.put("newLocation", latitude + ", " + longitude );
				args.put("time", Long.toString(location.getTime()));
				
				timeStamp = location.getTime();
				
//				app.sendEvent("mobile", "location", args);
				app.sendEvent("mobile", "location_updated", args);
			} else if(app == null){
				Log.i("KynetxService", "Not sending because app is undef.");
			} else if(timeStamp == location.getTime()){
				Log.i("KynetxService", "Not sending because timestamps were the same.");
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO: Auto-generated method stub
		}

		@Override
		public void onProviderDisabled(String provider) {
			setProviderStatus(provider, false);			
		}

		@Override
		public void onProviderEnabled(String provider) {
			setProviderStatus(provider, false);
		}
    	
    };
    
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public KynetxService getService() {
            return KynetxService.this;
        }
    }
  
    
    public void setApp(api appToSet){
    	app = appToSet;
    }
    
    public api getApp(){
    	return app;
    }
    
	private void newApp(){
		if(app == null){
			app = new KynetxApp(this, new KynetxSQLHelper(this).open().fetchAllApps());
		}
	}
    
    @Override
    public void onCreate() {
        //mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        location = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        if(location != null){
        	List<String> enabledProviders = location.getProviders(true);
		    
		    for(int i = 0; i < enabledProviders.size(); i++){
		    	String provider = enabledProviders.get(i);
		    	setProviderStatus(provider, true);
		    }
        }
                
        if(passiveEnabled){
        	location.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 60000, 1000, locationUpdate);
        }
        if(gpsEnabled){
        	//location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, locationUpdate);
        }
        if(networkEnabled){
        	location.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 1000, locationUpdate);
        }
        
        doToast(R.string.local_service_started);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        Log.v("StartServiceAtBoot", "StartAtBootService (Kynetx) Created");
    }

    @SuppressWarnings("unused")
	private void doToast(String toToast){
    	Toast.makeText(getApplicationContext(), toToast, Toast.LENGTH_SHORT).show();
    }

    private void doToast(int resId){
    	Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
    }
    
    private void setProviderStatus(String provider, boolean setTo){
    	if(provider.equals(LocationManager.PASSIVE_PROVIDER)){
    		passiveEnabled = setTo;
    	} else if(provider.equals(LocationManager.GPS_PROVIDER)){
    		gpsEnabled = setTo;
    	} else if (provider.equals(LocationManager.NETWORK_PROVIDER)){
    		networkEnabled = true;
    	}
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.v("KynetxService", "StartAtBootService (Kynetx) -- onStartCommand()");	        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	Log.v("KynetxService", "StartAtBootService (Kynetx) Destroyed");

        // Cancel the persistent notification.
        //mNM.cancel(R.string.local_service_started);

        // Tell the user we stopped.
        doToast(R.string.local_service_stopped);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        /*// In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, KynetxLocation.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.local_service_started, notification);
        */
    }
}