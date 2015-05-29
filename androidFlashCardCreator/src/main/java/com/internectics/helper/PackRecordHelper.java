package com.internectics.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.internectics.data.Pack;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: BourneWang
 * Date: 10/05/13
 * Time: 12:37 下午
 * To change this template use File | Settings | File Templates.
 */
public class PackRecordHelper {

    public static String getFullPath_S3(Pack currentPack) {

        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(String.format("%d", currentPack.packID), 0);
        String str = prefs.getString(Global.fullPath_S3_Property,"");

        return str;
    }

    public static String getShortedLink(Pack currentPack) {

        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(String.format("%d", currentPack.packID), 0);
        String str = prefs.getString(Global.shortedLink_Property,"");

        return str;
    }

    /**
     * 当某个参数为null，则这个对应的信息忽略写入
     * fullPath_S3: it's a full path in amazon s3, like https://s3.amazonaws.com/internetics.flashcardcreator/Sample_25052015.zip
     */
    public static void save(Context context, Pack currentPack, String shortedLink, String fullPath_S3) {

        SharedPreferences prefs = context.getSharedPreferences(String.format("%d", currentPack.packID), 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(Global.shareDate_Property,StringUtils.getCurrentTimeDate());
        if (StringUtils.isEmpty(shortedLink) == false) {
            edit.putString(Global.shortedLink_Property,shortedLink);
        }
        if (StringUtils.isEmpty(fullPath_S3) == false) {
            edit.putString(Global.fullPath_S3_Property,fullPath_S3);
        }
        edit.commit();
    }

    public static void savePackUpdateRecord(Context context, Pack currentPack) {

        SharedPreferences prefs = context.getSharedPreferences(String.format("%d", currentPack.packID), 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(Global.updateDate_Property,StringUtils.getCurrentTimeDate());
        edit.commit();
    }

    /*
     * 如果最近的修改时间晚于上次上传的时间，则需要重新上传
     */
    public static boolean checkUploadPackNecessary(Context context, Pack currentPack) {

        boolean result;

        SharedPreferences prefs = context.getSharedPreferences(String.format("%d", currentPack.packID), 0);

        String updateDateStr = prefs.getString(Global.updateDate_Property,"");
        String shareDateStr = prefs.getString(Global.shareDate_Property,"");

        if (updateDateStr.length() == 0) {
            // this happens when the packed is downloaded.
            savePackUpdateRecord(context,currentPack);
            return true;
        }

        if (shareDateStr.length() ==0) {
            return true;
        }

        Date updateDate = StringUtils.toDate(updateDateStr);
        Date sharedate = StringUtils.toDate(shareDateStr);
        if (updateDate.before(sharedate)) {
            result = false; //don't need to upload pack again
        } else {
            result = true;
        }

        return result;
    }
}
