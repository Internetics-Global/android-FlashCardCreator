package com.internectics.util;

import android.content.Context;
import com.internectics.data.Pack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Properties;

public class AppConfig {
    private final static String APP_CONFIG = "config";
    private static AppConfig appConfig;
    private Context mContext;

    public final static String CONF_LAST_CREATED_PACK_NAME = "LAST_CREATED_PACK_NAME";
    public final static String CONF_LAST_CREATED_PACK_DATE = "LAST_CREATED_PACK_DATE";
    public final static String CONF_IS_SAMPLE_PACK_DOWNLOADED = "IS_SAMPLE_PACK_DOWNLOADED";

    public final static String CONF_IS_RANDOM_PLAY = "IS_RANDOM_PLAY";

    public final static String CONF_APP_UDID = "APP_UDID";

    /**
     * get AppConfig instance
     */
    public static AppConfig getInstance(Context context) {
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.mContext = context;
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

}
