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

    private Pack mCurrentPack;
    private Context mContext;
    private String mShareLinkage;

    public PackRecordHelper(Context context, Pack currentPack) {
        mContext = context;
        mCurrentPack = currentPack;
    }

    public String getmShareLink() {
        return mShareLinkage;
    }

    private void savePackUploadRecord() {

        JSONObject object = new JSONObject();
        object.put(Global.updateDate_Property, StringUtils.getCurrentTimeDate());
        object.put(Global.shareLink_Property,mShareLinkage);
        AppConfig.getInstance(mContext).set(String.format("%d",mCurrentPack.packID),object.toJSONString());
    }

    private void savePackUpdateRecord() {

        JSONObject object = new JSONObject();
        object.put(Global.updateDate_Property, StringUtils.getCurrentTimeDate());
        AppConfig.getInstance(mContext).set(String.format("%d",mCurrentPack.packID),object.toJSONString());
    }

    private boolean checkUploadPackNecessary() {
        boolean result = true;

        if (mCurrentPack == null) {
            return true;
        }

        String str = AppConfig.getInstance(mContext).get(String.format("%d", mCurrentPack.packID));
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(str);
            String updateDateStr = (String) object.get(Global.updateDate_Property);
            String shareDateStr = (String) object.get(Global.shareDate_Property);
            mShareLinkage = (String) object.get(Global.shareLink_Property);

            if ((updateDateStr != null) && (shareDateStr != null) && (mShareLinkage != null)) {
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
