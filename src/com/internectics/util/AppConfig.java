package com.internectics.util;

import android.R.bool;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class AppConfig {
	private final static String APP_CONFIG = "config";
	private static AppConfig appConfig;
	private Context mContext;
	
	public final static String CONF_LAST_CREATED_PACK_NAME = "LAST_CREATED_PACK_NAME";
	public final static String CONF_LAST_CREATED_PACK_DATE = "LAST_CREATED_PACK_DATE";
	public final static String CONF_IS_SAMPLE_PACK_DOWNLOADED = "IS_SAMPLE_PACK_DOWNLOADED";
	
	public final static String CONF_APP_UDID = "APP_UDID";
	
	/**
	 * get AppConfig instance
	 */
	public static AppConfig getAppConfigInstance(Context context)
	{
		if(appConfig == null){
			appConfig = new AppConfig();
			appConfig.mContext = context;
		}
		return appConfig;
	}
	
	/**
	 * get Preference setting
	 */
	public static SharedPreferences getSharedPreferences(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/**
	 * check whether sample pack has been downloaded
	 */
	public static boolean isSamplePackDownloaded(Context context)
	{
		return getSharedPreferences(context)
				.getBoolean(CONF_IS_SAMPLE_PACK_DOWNLOADED, true);
	}
	
	public void setSamplePackDownloadedFlag(bool val){
		set(CONF_IS_SAMPLE_PACK_DOWNLOADED, String.valueOf(val));
	}
	
	/**
	 * return null if not exist
	 */
	public static String getLastCreatedPackName(Context context)
	{
		return getSharedPreferences(context)
				.getString(CONF_LAST_CREATED_PACK_NAME, null);
	}
	
	public void setLastCreatedPackName(String str){
		set(CONF_LAST_CREATED_PACK_NAME, str);
	}
	
	
	
	/**
	 * return null if not exist
	 */
	public static String getLastCreatedPackDate(Context context)
	{
		return getSharedPreferences(context)
				.getString(CONF_LAST_CREATED_PACK_DATE, null);
	}
	
	public void setLastCreatedPackDate(String dateStr){
		set(CONF_LAST_CREATED_PACK_DATE, dateStr);
	}
	
	
	/**
	 * Private method
	 */
	private Properties getProps() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try{
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			fis = new FileInputStream(dirConf.getPath() + File.separator + APP_CONFIG);
			
			props.load(fis);
		}catch(Exception e){
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return props;
	}
	
	/**
	 * Private method
	 */
	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try{
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File conf = new File(dirConf, APP_CONFIG);
			fos = new FileOutputStream(conf);
			
			p.store(fos, null);
			fos.flush();
		}catch(Exception e){	
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	public void set(String key,String value)
	{
		Properties props = getProps();
		props.setProperty(key, value);
		setProps(props);
	}
	
	public String get(String key)
	{
		Properties props = getProps();
		return (props!=null)?props.getProperty(key):null;
	}
	
	public void remove(String...key)
	{
		Properties props = getProps();
		for(String k : key)
			props.remove(k);
		setProps(props);
	}

}
