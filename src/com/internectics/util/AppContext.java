package com.internectics.util;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.UUID;


public class AppContext extends Application {
	
	private static Context mContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		AppContext.mContext = getApplicationContext();
	}
	
	public static Context getAppContext() {
		return AppContext.mContext;
	}
	
	/**
	 * detect network
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}
	
	/**
	 * get UDID
	 * @return
	 */
	public String getUDID() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UDID);
		if(StringUtils.isEmpty(uniqueID)){
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UDID, uniqueID);
		}
		return uniqueID;
	}
	
	
	public void setProperty(String key,String value){
		AppConfig.getInstance(this).set(key, value);
	}
	
	public String getProperty(String key){
		return AppConfig.getInstance(this).get(key);
	}
	public void removeProperty(String key){
		AppConfig.getInstance(this).remove(key);
	}

}
