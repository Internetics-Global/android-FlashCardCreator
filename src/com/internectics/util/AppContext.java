package com.internectics.util;

import android.app.Application;
import android.content.Context;

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
     * get UDID
     *
     * @return
     */
    public String getUDID() {
        String uniqueID = getProperty(AppConfig.CONF_APP_UDID);
        if (StringUtils.isEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            setProperty(AppConfig.CONF_APP_UDID, uniqueID);
        }
        return uniqueID;
    }


    public void setProperty(String key, String value) {
        AppConfig.sharedInstance().set(key, value);
    }

    public String getProperty(String key) {
        return AppConfig.sharedInstance().get(key);
    }

    public void removeProperty(String key) {
        AppConfig.sharedInstance().remove(key);
    }




}
