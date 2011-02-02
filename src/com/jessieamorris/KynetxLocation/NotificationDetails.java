package com.jessieamorris.KynetxLocation;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NotificationDetails extends Activity {
	private KynetxSQLHelper mDbHelper;
	private long rowId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.notification_detail);
    	setTitle(R.string.menu_notifications);
    	
    	rowId = getIntent().getExtras().getLong("rowid");
    	mDbHelper = new KynetxSQLHelper(this);
    	mDbHelper.open();
    	Cursor notification = mDbHelper.fetchNotification(rowId);
    	
    	Button clear = (Button) findViewById(R.id.clear);
    	
    	clear.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mDbHelper.deleteNotification(rowId);
				setResult(RESULT_OK);
				finish();
			}
    		
    	});
    	
    	String title = notification.getString(notification.getColumnIndexOrThrow(KynetxSQLHelper.KEY_TITLE));
    	String text = notification.getString(notification.getColumnIndexOrThrow(KynetxSQLHelper.KEY_TEXT));
    	String action = notification.getString(notification.getColumnIndexOrThrow(KynetxSQLHelper.KEY_ACTION));
    	
    	TextView titleView = (TextView)findViewById(R.id.notification_title);
    	TextView textView = (TextView)findViewById(R.id.notification_text);
    	TextView actionView = (TextView)findViewById(R.id.notification_action);
    	
    	titleView.setText(title);
    	textView.setText(text);
    	actionView.setText(action);
    }
}
