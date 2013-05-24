package com.internectics.util;

import android.net.Uri;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class StringUtils {

    private final static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * transfer String type to Date type
     *
     * @param sdate
     * @return
     */
    public static Date toDate(String sdate) {
        try {
            return dateFormater.parse(sdate);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * get current time/date string
     *
     * @param sdate
     * @return
     */
    public static String getCurrentTimeDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS");
        String timeDateStr = format.format(new Timestamp(System.currentTimeMillis()));
        Log.d(Global.debugTag, "current time/date is:" + timeDateStr);
        return timeDateStr;
    }


    /**
     * return true if null or empty
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * return true if numeric
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }


    public static String lastComponentOfPath(Uri uri) {
        String path = uri.getPath();
        String last = path.substring(path.lastIndexOf("/") + 1);
        return last;
    }


    public static String stringDecodeForSQlite(String str) {
        String returnStr = str;
        String tempStr;

        try {
            tempStr = (URLDecoder.decode(str.replaceAll("%", "<percentage>"), "UTF-8"));
            returnStr = tempStr.replaceAll("<percentage>","%");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return returnStr;
    }

    /**
     * In order to be compatibile with iOS version
     */
    public static int convertTemplateBackgroundToIndex(String templateBackground) {
        int index = 0;

        if (templateBackground.equals("card_background_blue.png"))
            index = 0;
        else if  (templateBackground.equals("card_background_coffee.png")) {
            index = 1;
        } else if (templateBackground.equals("card_background_gray.png")) {
            index = 2;
        } else if (templateBackground.equals("card_background_purple.png")) {
            index = 3;
        } else if (templateBackground.equals("card_background_red.png")) {
            index = 4;
        } else {
            index = 0;
        }

        return index;
    }


}
