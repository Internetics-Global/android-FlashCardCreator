package com.internectics.helper;

import android.content.Context;
import com.internectics.data.Pack;
import com.internectics.util.AppConfig;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: BourneWang
 * Date: 10/05/13
 * Time: 12:37 下午
 * To change this template use File | Settings | File Templates.
 */
public class PackRecordHelper {

    public static String getCurrentPackShareLink(Context context, Pack currentPack) {

        String str = AppConfig.getInstance(context).get(String.format("%d", currentPack.packID));
        JSONParser parser = new JSONParser();
        JSONObject object = null;
        try {
            object = (JSONObject) parser.parse(str);

        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        String shareLinkage = (String) object.get(Global.shareLink_Property);

        return shareLinkage;
    }

    public static void savePackUploadRecord(Context context, Pack currentPack, String shareLink) {

        JSONObject object = new JSONObject();
        object.put(Global.updateDate_Property, StringUtils.getCurrentTimeDate());
        object.put(Global.shareLink_Property,shareLink);
        AppConfig.getInstance(context).set(String.format("%d",currentPack.packID),object.toJSONString());
    }

    public static void savePackUpdateRecord(Context context, Pack currentPack) {

        JSONObject object = new JSONObject();
        object.put(Global.updateDate_Property, StringUtils.getCurrentTimeDate());
        AppConfig.getInstance(context).set(String.format("%d",currentPack.packID),object.toJSONString());
    }

    public static boolean checkUploadPackNecessary(Context context, Pack currentPack) {

        boolean result = true;

        String str = AppConfig.getInstance(context).get(String.format("%d", currentPack.packID));
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(str);
            String updateDateStr = (String) object.get(Global.updateDate_Property);
            String shareDateStr = (String) object.get(Global.shareDate_Property);
            String shareLinkStr = (String) object.get(Global.shareLink_Property);

            if ((updateDateStr != null) && (shareDateStr != null) && (shareLinkStr != null)) {
                Date updateDate = StringUtils.toDate(updateDateStr);
                Date sharedate = StringUtils.toDate(shareDateStr);
                if (updateDate.before(sharedate)) {
                    return false; //don't need to upload pack again
                } else {
                    return true;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }
}
