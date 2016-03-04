package com.flipflash.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class AppConfig {
    private static final String TAG = AppConfig.class.getSimpleName();
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
            //e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
                //e.printStackTrace();
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


    /*
    存放在app_config下的config文件中
     */
    public void set(String key, String value) {
        Properties props = getProps();
        props.setProperty(key, value);
        setProps(props);
    }

    /*
    存放在app_config下的config文件中
     */
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


    public boolean isMuteSoundRecording() {
        String str = appConfig.get(Global.isMuteSoundRecording);
        if ((str != null) && (str.equals("true")))
            return true;
        else
            return false;
    }

    public void setMuteSoundRecording(boolean b) {
        if (b) {
            appConfig.set(Global.isMuteSoundRecording, "true");
        } else {
            appConfig.set(Global.isMuteSoundRecording, "false");
        }

    }

    public int getCountDown() {
        String str = appConfig.get("K_CountDown_Val");
        if ((str != null))
            return Integer.parseInt(str);
        else
            return Global.kDEFAULT_CountDown_Slider_Value;

    }

    public void setCountDown(int val) {

        String strVal = String.valueOf(val);
        appConfig.set("K_CountDown_Val",strVal);

    }


    public boolean isTextToSpeech() {
        String str = appConfig.get(Global.isTextToSpeech);
        if ((str != null) && (str.equals("true")))
            return true;
        else
            return false;
    }

    public void setTextToSpeech(boolean b) {
        if (b) {
            appConfig.set(Global.isTextToSpeech, "true");
        } else {
            appConfig.set(Global.isTextToSpeech, "false");
        }

    }



    public boolean isShowQuestionOnly() {
        String str = appConfig.get("isShowQuestionOnly");
        if ((str != null) && (str.equals("true")))
            return true;
        else
            return false;
    }

    public void setShowQuestionOnly(boolean b) {
        if (b) {
            appConfig.set("isShowQuestionOnly", "true");
        } else {
            appConfig.set("isShowQuestionOnly", "false");
        }

    }

    public boolean isMaleVoice() {
        String str = appConfig.get("isMaleVoice");
        if ((str != null) && (str.equals("true")))
            return true;
        else
            return false;
    }

    public void setMaleVoice(boolean b) {
        if (b) {
            appConfig.set("isMaleVoice", "true");
        } else {
            appConfig.set("isMaleVoice", "false");
        }

    }


    public boolean isHelpTipHasBeenShowedFirst() {

        String str = appConfig.get(Global.isHelpTipHasBeenShowedFirst);

        if (str == null) {
            return false;
        }

        if ("true".equals(str))
            return true;
        else
            return false;

    }

    public void setHelpTipHasBeenShowedFirst(boolean b) {
        if (b) {
            appConfig.set(Global.isHelpTipHasBeenShowedFirst, "true");
        } else {
            appConfig.set(Global.isHelpTipHasBeenShowedFirst, "false");
        }
    }


    public void setPackIDForLastSelected(int packID) {
        appConfig.set(Global.lastSelectedPackID, String.format("%d",packID));
    }

    public  int getPackIDForLastSelected() {

        String str = appConfig.get(Global.lastSelectedPackID);
        if (str == null) {
            return -1;
        } else {
            return Integer.parseInt(str);
        }



    }



    public int getPlayOption() {
        String str = appConfig.get(Global.PLAY_OPTION);

        if (str == null) {
            return 0;
        }

        return Integer.parseInt(str);
    }

    public void setPlayOption(int playOption) {
        String val = String.format("%d",playOption);
        appConfig.set(Global.PLAY_OPTION, val);
    }



}
