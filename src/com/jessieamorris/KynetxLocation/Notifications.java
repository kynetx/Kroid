package com.jessieamorris.KynetxLocation;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class Notifications extends ListActivity {
	
	private KynetxSQLHelper mDbHelper;
	private SimpleCursorAdapter adapter;
	private Cursor apps;
	private int ACTIVITY_DETAILS = 0;
	private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);
        setTitle(R.string.app_name);
        
        list = getListView();
        list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long row) {
				Intent i = new Intent(Notifications.this, NotificationDetails.class);
				i.putExtra("rowid", row);
				startActivityForResult(i, ACTIVITY_DETAILS);
			}
        });
        
        mDbHelper = new KynetxSQLHelper(this);
        mDbHelper.open();
                
        Button exit = (Button) findViewById(R.id.exit);
        Button clear = (Button) findViewById(R.id.clear);

    	apps = mDbHelper.fetchAllNotifications();
    	apps.moveToFirst();
    	
    	String[] columns = new String[] { KynetxSQLHelper.KEY_TITLE, KynetxSQLHelper.KEY_TEXT };
    	
    	int[] to = new int[] { R.id.Title, R.id.Text };
    	
    	
    	adapter = new SimpleCursorAdapter(this, R.layout.list_layout, apps, columns, to);
         
        setListAdapter(adapter);
        
        adapter.notifyDataSetChanged();
                
        exit.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
        	
        });
        
        clear.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				mDbHelper.deleteAllNotifications();
				updateList();
			}
        	
        });
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
}