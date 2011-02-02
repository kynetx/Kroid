package com.kynetx;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;

public class KynetxAsyncEvent extends AsyncTask<Map<String,String>,Void,JSONTokener>{

	private api app;
	private String domain;
	private String event;
	private boolean cookieStoreError = false;
	private boolean jsonError = false;
	private boolean connectionError = false;
	
	public KynetxAsyncEvent(api API, String domainSet, String eventSet){
		app = API;
		domain = domainSet;
		event = eventSet;
	}
	
	public boolean wasCookieStoreError(){
		return cookieStoreError;
	}
	
	public boolean wasConnectionError(){
		return connectionError;
	}
	
	public boolean wasJsonError(){
		return jsonError;
	}
	
	@Override
	protected JSONTokener doInBackground(Map<String,String>... arguments){
		try {
			JSONTokener json = null;
			Map<String,String> args = arguments[0];
	
	        String argumentsEncoded = app.appVersions;
	
	        if(args != null){
	        	Iterator<HashMap.Entry<String,String>> it = args.entrySet().iterator();
		        while (it.hasNext()){
		        	HashMap.Entry<String, String> pair = (HashMap.Entry<String,String>)it.next();
		        	try {
						argumentsEncoded += URLEncoder.encode( pair.getKey(), "UTF-8") + "=" + URLEncoder.encode(pair.getValue(), "UTF-8") + "&";
					} catch (UnsupportedEncodingException e) { // Will always be UTF-8, so no handling is needed.
					}
		       	}
	        }
	        
	        DefaultHttpClient httpclient = new DefaultHttpClient();
	                
	        if(app.cookieStore != null){
	        	httpclient.setCookieStore(app.cookieStore);
	        }
	        
	        String url = "http://cs.kobj.net/blue/event/" + domain + "/" + event +"/" + app.appId + "?" + argumentsEncoded;
	        
	        HttpPost httppost = new HttpPost(url);
	
	        HttpResponse response = httpclient.execute(httppost);
				
	        HttpEntity entity = response.getEntity();
	        
	        if (entity != null) {
	        	InputStream content = entity.getContent();

				
	        	String jsonString = IOUtils.toString(content);
				
	        	entity.consumeContent();
	            if(jsonString != null){
	            	app.comments = jsonString.replaceAll("^(?:((?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)))","$1");
	            	String directiveJson = jsonString.replaceAll("^(?:(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*))","").replaceAll("^//.*$", "").replaceAll("[\r\n]", "");
	            	
            		json = new JSONTokener(directiveJson);
				}
	        }
	
	        boolean cookieChanged = false;

	        if(app.cookieStore != null && httpclient.getCookieStore() != null){
		        List<Cookie> cookies = app.cookieStore.getCookies();
		        List<Cookie> cookiesReturned = httpclient.getCookieStore().getCookies();
		        
		        
		        // TODO: This is a bit ineffecient. Eh.
		        for(int i = 0; i < cookies.size(); i++){
		        	Cookie cookie = cookies.get(i);
		        	String name = cookie.getName();
		        	String value = cookie.getValue();
		        	boolean cookieFound = false;
		        	for(int j = 0; j < cookiesReturned.size(); j++){
		        		Cookie cookieReturned = cookiesReturned.get(j);
		        		if(cookieReturned.getName().equals(name)){
		        			cookieFound = true;
		        			if(!cookieReturned.getValue().equals(value)){
		        				cookieChanged = true;
		        			}
		        		}
		        	}
		        	if(!cookieFound){
		        		cookieChanged = true;
		        	}
		        }
		        
		        for(int i = 0; i < cookiesReturned.size(); i++){
		        	Cookie cookieReturned = cookiesReturned.get(i);
		        	String name = cookieReturned.getName();
		        	String value = cookieReturned.getValue();
		        	boolean cookieFound = false;
		        	for(int j = 0; j < cookies.size(); j++){
		        		Cookie cookie = cookies.get(j);
		        		if(cookie.getName().equals(name)){
		        			cookieFound = true;
		        			if(!cookie.getValue().equals(value)){
		        				cookieChanged = true;
		        			}
		        		}
		        	}
		        	if(!cookieFound){
		        		cookieChanged = true;
		        	}
		        }
	        }
	        
	        if(cookieChanged || app.cookieStore == null){
	        	tryStoreCookies(httpclient.getCookieStore());
	        }
	        
	        httpclient.getConnectionManager().shutdown();
	        return json;
		} catch(IOException e){
			connectionError = true;
		} catch(IllegalStateException e){
			connectionError = true;
		}
		return null;
	}
	
	private void tryStoreCookies(CookieStore store){
    	app.cookieStore = store;
    	try {
    		app.storeCookies(app.cookieStore);
    	} catch(IOException e){
    		cookieStoreError = true;
    	}
	}
	
	@Override
	protected void onPostExecute(JSONTokener json){
		try {
			if(json != null){
				JSONObject directives = null;

				directives = new JSONObject(json);

				
				if(directives != null){
					JSONArray actions = directives.getJSONArray("directives");

					if(actions != null){
						for(int i = 0; i < actions.length(); i++){
							JSONObject actionToRun = actions.getJSONObject(i);
							if(actionToRun != null){
								String actionName = (String)actionToRun.get("name");

								JSONObject options = actionToRun.getJSONObject("options");
								
								if(options != null){
									Iterator<?> keys = options.keys();
									HashMap<String, Object> actionArgs = new HashMap<String, Object>();
									
									if(keys != null){
										while (keys.hasNext()){
											String key = (String)keys.next();
											if(key != null){
												actionArgs.put(key, options.get(key));
											}
								        }
										
										if( app.callbacks != null && app.callbacks.containsKey(actionName)){
											Vector<onEventListener> callbacksToRun = app.callbacks.get(actionName);
											for(int j = 0; i < callbacksToRun.size(); i++){
												onEventListener eventListener = callbacksToRun.get(j);
												eventListener.onEvent(actionArgs);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch(JSONException e){
			jsonError = true;
		}
		tryErrors();
	}
	
	private void tryErrors(){
		if(connectionError){
			app.onConnectionError();
		} else {
			if(jsonError){
				app.onJsonError();
			}
		}
		if(cookieStoreError){
			app.onCookieStoreError();
		}
	}
}
