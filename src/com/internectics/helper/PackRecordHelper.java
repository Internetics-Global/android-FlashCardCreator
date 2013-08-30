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

    public static String getCurrentPackShareLink(Pack currentPack) {

        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(String.format("%d", currentPack.packID), 0);
        String shareLinkage = prefs.getString(Global.shareLink_Property,StringUtils.getCurrentTimeDate());

        return shareLinkage;
    }

    /**
     * You can ignore those as long as set as null
     * @param context
     * @param currentPack
     * @param shareLink
     * @param dropboxFileName
     */
    public static void savePackUploadRecord(Context context, Pack currentPack, String shareLink, String dropboxFileName) {

        SharedPreferences prefs = context.getSharedPreferences(String.format("%d", currentPack.packID), 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(Global.shareDate_Property,StringUtils.getCurrentTimeDate());
        if ((shareLink != null) && (shareLink.length() != 0)) {
            edit.putString(Global.shareLink_Property,shareLink);
        }
        if ((dropboxFileName != null) && (dropboxFileName.length() !=0)) {
            edit.putString(Global.shareFileName_Property,dropboxFileName);
        }
        edit.commit();
    }

    public static void savePackUpdateRecord(Context context, Pack currentPack) {

        SharedPreferences prefs = context.getSharedPreferences(String.format("%d", currentPack.packID), 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(Global.updateDate_Property,StringUtils.getCurrentTimeDate());
        edit.commit();
    }

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
