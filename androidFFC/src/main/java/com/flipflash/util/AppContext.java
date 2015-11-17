package com.flipflash.util;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.facebook.stetho.Stetho;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.LogLevel;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import static com.flipflash.util.LogUtils.LOGD;

public class AppContext extends Application {
    private static final String TAG = AppContext.class.getName();

    private static Context                           mContext;
    private static CognitoCachingCredentialsProvider mCredentialsProvider;
    private static AmazonS3Client                    mS3Client;

    private RefWatcher refWatcher;


    @Override
    public void onCreate() {
        super.onCreate();

        refWatcher = LeakCanary.install(this);

        SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String timestamp = s.format(new Date());
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        long version = (pInfo != null)?pInfo.versionCode: -1;
        LOGD(TAG, "onCreate on " + timestamp + " with build number = " + version);

        //LeakCanary.install(this);

        AppContext.mContext = getApplicationContext();

        // Setup Amazon Cognito
        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                mContext, // Context
                "us-east-1:55b5aa55-921e-49d0-b4d3-673793805862", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        mS3Client = new AmazonS3Client(mCredentialsProvider);

        // Setup Parse
        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        // Parse的app_key和app_id是在Manifest中进行设置

        //Twitter
        ParseTwitterUtils.initialize("spW6th3vldJVq5Zjnud3Lg",
                "CZHdQXJIVGtLlBnvh6T1eEZ2WJgWPSfNUdju6jXEs");

        //Facebook
        ParseFacebookUtils.initialize(this);
        //facebook_app_id 是在Manifest中进行设置

        //Key-value storage
        Hawk.initWithoutEncryption(mContext, LogLevel.NONE);

        //facebook debug framework
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());

    }

    public static CognitoCachingCredentialsProvider getCredentialsProvider() {
        return mCredentialsProvider;
    }

    public static AmazonS3Client getS3Client() {
        return mS3Client;
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


    public static RefWatcher getRefWatcher(Context context) {
        AppContext application = (AppContext) context.getApplicationContext();
        return application.refWatcher;
    }

}
