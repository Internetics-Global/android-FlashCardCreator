package com.internectics.util;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

import java.util.UUID;

import timber.log.Timber;


public class AppContext extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        AppContext.mContext = getApplicationContext();

        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "n6hQQEqaa52887A46KF3ThYgxG4dSmQBTHJMArkW", "VUDwIHOFGEkAe9ngdFgdBqVFDgOLEINdnd0DkF2i");

        if (Global.isDebug) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
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


    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.HollowTree {
        @Override public void i(String message, Object... args) {
            // TODO e.g., Crashlytics.log(String.format(message, args));
        }

        @Override public void i(Throwable t, String message, Object... args) {
            i(message, args); // Just add to the log.
        }

        @Override public void e(String message, Object... args) {
            i("ERROR: " + message, args); // Just add to the log.
        }

        @Override public void e(Throwable t, String message, Object... args) {
            e(message, args);

            // TODO e.g., Crashlytics.logException(t);
        }
    }


}
