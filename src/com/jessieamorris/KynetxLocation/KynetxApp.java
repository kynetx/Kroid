package com.jessieamorris.KynetxLocation;

import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.kynetx.api;
import com.kynetx.onEventListener;

public class KynetxApp extends api {
	
	private KynetxSQLHelper mDbHelper;

	public KynetxApp(Context where, Cursor apps){
		super(where, apps);

		mDbHelper = new KynetxSQLHelper(context);
		mDbHelper.open();
		
		this.onDirective("say", say);
		this.onDirective("toast", toast);
		this.onDirective("notify", notify);
	}

	public KynetxApp(Context where, String newAppId) {
		super(where, newAppId);
		
		mDbHelper = new KynetxSQLHelper(context);
		mDbHelper.open();
		
		this.onDirective("say", say);
		this.onDirective("toast", toast);
		this.onDirective("notify", notify);
	}

	private onEventListener say = new onEventListener() {
		public void onEvent(HashMap<String, Object> args){
			doToast((String)args.get("text"));
		}
	};

	private onEventListener toast = new onEventListener() {
		public void onEvent(HashMap<String, Object> args){
			doToast((String)args.get("text"));
		}
	};
	
	private onEventListener notify = new onEventListener(){
		public void onEvent(HashMap<String, Object> args){
			doNotify((String)args.get("title"), (String)args.get("text"), (String)args.get("url"));
		}
	};


	@Override
	public void onConnectionError(){
		doToast(R.string.error_connecting);
	}

	@Override
	public void onCookieStoreError(){
		doToast(R.string.error_storing_cookies);
	}

	@Override
	public void onJsonError(){
		doToast(R.string.error_parsing);
	}

	private void doToast(String toToast){
		Toast.makeText(context, toToast, Toast.LENGTH_SHORT).show();
	}

	private void doToast(int resId){
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}
	
	private void doNotify(CharSequence title, CharSequence text, CharSequence url){
		if( text != null){
		     // In this sample, we'll use the same text for the ticker and the expanded notification
	        Intent contentIntent = null;
	        NotificationManager mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	
	        // Set the icon, scrolling text and timestamp
	        Notification notification = new Notification(R.drawable.icon, text,
	                System.currentTimeMillis());
	
	        // The PendingIntent to launch our activity if the user selects this notification
	        if(url != null){
	        	contentIntent = new Intent(Intent.ACTION_VIEW);
	        	contentIntent.setData(Uri.parse((String) url));
	        }
	      
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        // Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(context, title, text, PendingIntent.getActivity(context, 0, contentIntent, 0));
	
	        // Send the notification.
	        // We use a layout id because it is a unique number.  We use it later to cancel.
	        mNM.notify((int)Math.floor(Math.random() * 1020), notification);
			mDbHelper.createNotification((String)title, (String)text, (String)url);
		}
	}
}
