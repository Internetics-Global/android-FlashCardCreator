package com.flipflash.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.stetho.Stetho;
import com.flipflash.UI.FCCEditText;
import com.flipflash.android_ffc.MainActivity;
import com.flipflash.android_ffc.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;

import com.squareup.leakcanary.RefWatcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import static com.flipflash.util.LogUtils.LOGD;

public class AppContext extends Application {
    private static final String TAG = AppContext.class.getSimpleName();

    private static Context                           mContext;
    private static CognitoCachingCredentialsProvider mCredentialsProvider;
    private static AmazonS3Client                    mS3Client;

    private RefWatcher refWatcher;

    private MainActivity mMainActivity;


    @Override
    public void onCreate() {
        super.onCreate();

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        refWatcher = LeakCanary.install(this);

        initImageLoader(getApplicationContext());

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
                getString(R.string.aws_identity_pool_id), // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        mS3Client = new AmazonS3Client(mCredentialsProvider);

        //Key-value storage
        Hawk.init(this)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
                .setStorage(HawkBuilder.newSqliteStorage(this))
                .setLogLevel(LogLevel.NONE)
                .build();

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

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);

//        if (Global.IS_DOGFOOD_BUILD) {
//            config.writeDebugLogs(); // Remove for release app
//        } else {
//            L.writeDebugLogs(false);
//            L.writeLogs(false);
//        }

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }


    public MainActivity getMainActivity() {
        return mMainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }
}
