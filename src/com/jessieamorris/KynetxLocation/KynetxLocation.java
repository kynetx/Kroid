package com.jessieamorris.KynetxLocation;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.kynetx.api;

public class KynetxLocation extends ListActivity {
	
	private KynetxSQLHelper mDbHelper;
	private SimpleCursorAdapter adapter;
	private boolean isBound = false;
	private api app;
	private KynetxService service;
	private Cursor apps;
	private int ACTIVITY_DETAILS = 0;
	private ListView list;
    private static final int MANAGE_ID = Menu.FIRST;
    
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);
        setTitle(R.string.app_name);
        
        list = getListView();
        list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long row) {
				Intent i = new Intent(KynetxLocation.this, NotificationDetails.class);
				i.putExtra("rowid", row);
				startActivityForResult(i, ACTIVITY_DETAILS);
			}
        });
        
        mDbHelper = new KynetxSQLHelper(this);
        mDbHelper.open();
                
        Button clear = (Button) findViewById(R.id.clear);

    	apps = mDbHelper.fetchAllNotifications();
    	apps.moveToFirst();
    	
    	String[] columns = new String[] { KynetxSQLHelper.KEY_TITLE, KynetxSQLHelper.KEY_TEXT };
    	
    	int[] to = new int[] { R.id.Title, R.id.Text };
    	
    	
    	adapter = new SimpleCursorAdapter(this, R.layout.list_layout, apps, columns, to);
         
        setListAdapter(adapter);
        
        adapter.notifyDataSetChanged();
        
        clear.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				mDbHelper.deleteAllNotifications();
				updateList();
			}
        	
        });
        doBindService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MANAGE_ID, 0, R.string.menu_manage_apps);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case MANAGE_ID:
            	Intent i = new Intent(KynetxLocation.this, ManageApps.class);
            	startActivity(i);
            	return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        updateList();
    }
    
    private void updateList(){
    	apps.requery();
        
        adapter.notifyDataSetChanged();
        adapter.notifyDataSetInvalidated();
        list.invalidateViews();
    }
    
	private void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
		if(!isBound){
		    startService(new Intent(this, KynetxService.class));
		    bindService(new Intent(KynetxLocation.this, 
		            KynetxService.class), connection, Context.BIND_AUTO_CREATE);
		    isBound = true;
		}
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
}