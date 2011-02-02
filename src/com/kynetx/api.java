/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kynetx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONObject;

import com.jessieamorris.KynetxLocation.KynetxSQLHelper;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended). 585-7864
 */
public class api {
	
	protected Context context;
	protected String appId;
	protected Map<String,Vector<onEventListener>> callbacks;
	protected String comments;
	protected JSONObject directives;
	protected CookieStore cookieStore;
	protected String appVersions;
	
	protected void onConnectionError(){
	}
	
	protected void onJsonError(){
	}
	
	protected void onCookieStoreError(){
	}
	
	public api(Context where, String newAppId){
		context = where;
		callbacks = new HashMap<String,Vector<onEventListener>>();
		cookieStore = fetchCookies();
		setApp(newAppId);
	}
	
	public api(Context where, Cursor apps){
		context = where;
		callbacks = new HashMap<String,Vector<onEventListener>>();
		cookieStore = fetchCookies();
		setApp(apps);
	}
	
	public String getComments(){
		return comments;
	}
	
	public void setApp(Cursor apps){
		appId = "";
		appVersions = "";
		
		apps.moveToFirst();
		
		while(apps.isAfterLast() == false){
			if(!apps.getString(apps.getColumnIndex(KynetxSQLHelper.KEY_VERSION)).equals("none")){
				appId += apps.getString(apps.getColumnIndex(KynetxSQLHelper.KEY_APPID));
				appVersions += appId + ":kynetx_app_version=" + apps.getString(apps.getColumnIndex(KynetxSQLHelper.KEY_VERSION)) + "&";
				if(!apps.isLast()){
					appId += ";";
				}
			}
			apps.moveToNext();
		}
		apps.close();
	}
	
	public void setApp(String app){
		appId = app;
		appVersions = app + ":kynetx_app_version=prod&";
	}
	
	public void onDirective(String directive, onEventListener toRun){
		if(callbacks != null){
			if(callbacks.containsKey(directive)){
				Vector<onEventListener> toRunVector = callbacks.get(directive);
				toRunVector.add(toRun);
			} else {
				Vector<onEventListener> toRunVector = new Vector<onEventListener>();
				toRunVector.add(toRun);
				callbacks.put(directive, toRunVector);
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void sendEvent(String domain, String event, Map<String, String> args){
		KynetxAsyncEvent asyncEvent = new KynetxAsyncEvent(this, domain, event);
		asyncEvent.execute(args);
	}
	
	private boolean isWritable(){
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageWriteable = true;
		}
		return mExternalStorageWriteable;
	}
	
	private boolean isReadable(){
		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		}
		return mExternalStorageAvailable;
	}
	
	protected void storeCookies(CookieStore toStore) throws IOException{
		if( isWritable() ){
			File path = new File(context.getExternalFilesDir(null), "cookies");
			
			OutputStream output = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(output);
			
			List<Cookie> cookies = toStore.getCookies();
			
			oos.writeInt(cookies.size());
			
			for(int i = 0; i < cookies.size(); i++){
				Cookie cookie = cookies.get(i);
				oos.writeObject(cookie.getName());
				oos.writeObject(cookie.getValue());
				oos.writeObject(cookie.getDomain());
				oos.writeObject(cookie.getPath());
				oos.writeInt(cookie.getVersion());
			}
			
			oos.close();
		} else {
			throw new IOException();
		}
	}
	
	protected CookieStore fetchCookies(){
		CookieStore toReturn = null;
		int size = 0;
		
		if(isReadable() || isWritable()){
			try {
				File path = new File(context.getExternalFilesDir(null), "cookies");
				FileInputStream fin = new FileInputStream(path);
				ObjectInputStream ois = new ObjectInputStream(fin);
				
				size = ois.readInt();
				
				toReturn = new BasicCookieStore();
				
				
				
				for(int i = 0; i < size; i++){
					BasicClientCookie cookie = new BasicClientCookie((String)ois.readObject(),(String)ois.readObject());
					
					cookie.setDomain((String)ois.readObject());
					cookie.setPath((String)ois.readObject());
					cookie.setVersion(ois.readInt());
					toReturn.addCookie((Cookie)cookie);
				}
				
				ois.close();
			} catch (Exception e){
				return null;
			}
		}
		if(size > 0){
			return toReturn;
		} else {
			return null;
		}
	}
	
	public void clearCookies(){
		cookieStore = null;
		try {
			storeCookies(new BasicCookieStore());
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
}



