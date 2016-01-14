package com.flipflash.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.flipflash.data.Pack;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: BourneWang
 * Date: 10/05/13
 * Time: 12:37 下午
 * To change this template use File | Settings | File Templates.
 */
public class PackRecordHelper {

    private static final String TAG = PackRecordHelper.class.getSimpleName();

    public static void savePackUploadRecord(Pack currentPack) {

        if (currentPack == null || StringUtils.isEmpty(currentPack.shareLink) || StringUtils.isEmpty(currentPack.fileNameOnAWS)) {
            throw new IllegalArgumentException("currentPack,currentPack.shareLink or currentPack.fileNameOnAWS should not be null");
        }

        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(String.format("%d", currentPack.packID), 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(Global.shareDate_Property,StringUtils.getCurrentTimeDate());
        edit.commit();
    }


    public static void savePackUpdateRecord(Pack currentPack) {

        SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(String.format("%d", currentPack.packID), 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(Global.updateDate_Property,StringUtils.getCurrentTimeDate());
        edit.commit();
    }

    /*
     * 如果最近的修改时间晚于上次上传的时间，则需要重新上传
     */
    public static boolean checkUploadPackNecessary(Pack currentPack) {

        if (true) {
            //之所以disable这个逻辑，因为这个会引起误解，不如用户没有改变任何的数据，但是想改变max downloaded和password。
            // 所以，这里索性无论何种情况，都重新来一次upload的所有流程。
            return true;
        } else {

            boolean result;

            SharedPreferences prefs = AppContext.getAppContext().getSharedPreferences(String.format("%d", currentPack.packID), 0);

            String updateDateStr = prefs.getString(Global.updateDate_Property,"");
            String shareDateStr = prefs.getString(Global.shareDate_Property,"");

            if (updateDateStr.length() == 0) {
                // this happens when the packed is downloaded.
                savePackUpdateRecord(currentPack);
                return true;
            }

            if (shareDateStr.length() ==0) {
                return true;
            }

            Date updateDate = StringUtils.toDate(updateDateStr);
            Date shareDate = StringUtils.toDate(shareDateStr);
            if (updateDate.before(shareDate)) {
                result = false; //don't need to upload pack again
            } else {
                result = true;
            }

            return result;
        }
    }
}
