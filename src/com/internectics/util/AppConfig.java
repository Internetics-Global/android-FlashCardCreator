package com.internectics.util;

import android.content.Context;
import com.internectics.data.Pack;

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
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
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

    public String getCurrentPackShareLink(Pack currentPack) {
        //TODO
        return "http://dl.dropbox.com/s/t5wxndkc8s4glmv/card1360210703.422296599274701.zip";
    }

    public boolean isExamplePackDownloadedBefore() {
        String str = appConfig.get(Global.isExamplePackDownloadedSBefore_Property);
        if ((str != null) && (str.equals("1")))
            return true;
        else
            return false;
    }

    public void setExamplePackDownloadedFlag() {
        appConfig.set(Global.isExamplePackDownloadedSBefore_Property, "1");
    }

}
