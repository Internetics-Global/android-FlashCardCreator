package com.internectics.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class AppConfig {
    private final static String APP_CONFIG = "config";
    private static AppConfig appConfig;
    private Context mContext;

    public final static String CONF_APP_UDID = "APP_UDID";

    /**
     * get AppConfig instance
     */
    public static AppConfig sharedInstance() {
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.mContext = AppContext.getAppContext();
        }
        return appConfig;
    }

    /**
     * Private method
     */
    private Properties getProps() {
        FileInputStream fis = null;
        Properties props = new Properties();
        try {
            File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            fis = new FileInputStream(dirConf.getPath() + File.separator + APP_CONFIG);

            props.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    /**
     * Private method
     */
    private void setProps(Properties p) {
        FileOutputStream fos = null;
        try {
            File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            File conf = new File(dirConf, APP_CONFIG);
            fos = new FileOutputStream(conf);

            p.store(fos, null);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void set(String key, String value) {
        Properties props = getProps();
        props.setProperty(key, value);
        setProps(props);
    }

    public String get(String key) {
        Properties props = getProps();
        return (props != null) ? props.getProperty(key) : null;
    }

    public void remove(String... key) {
        Properties props = getProps();
        for (String k : key)
            props.remove(k);
        setProps(props);
    }

    public boolean isExamplePackDownloadedBefore() {
        String str = appConfig.get(Global.isExamplePackDownloadedSBefore_Property);
        if ((str != null) && (str.equals("true")))
            return true;
        else
            return false;
    }

    public void setExamplePackDownloadedFlag() {
        appConfig.set(Global.isExamplePackDownloadedSBefore_Property, "true");
    }


    public boolean isRandomPlay() {
        String str = appConfig.get(Global.isRandomPlay);
        if ((str != null) && (str.equals("true")))
            return true;
        else
            return false;
    }

    public void setRandomPlay(boolean b) {
        if (b) {
            appConfig.set(Global.isRandomPlay, "true");
        } else {
            appConfig.set(Global.isRandomPlay, "false");
        }

    }


}
