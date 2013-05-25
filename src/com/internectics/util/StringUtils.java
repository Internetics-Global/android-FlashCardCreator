package com.internectics.util;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;

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

    public static String lastComponentOfPath(String stringUri) {
        String path = Uri.parse(stringUri).getPath();
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


    /**
     * Used to convert iOS style("Left", "Center","Right" to android style(Gravity.Center...)
     */
    public static int convertGravityStringToInt(String gravity) {
        if (gravity.equals("Left")) {
            return Gravity.LEFT;
        } else if (gravity.equals("Center")) {
            return Gravity.CENTER;
        } else if (gravity.equals("Right")) {
            return Gravity.RIGHT;
        } else {
            return Gravity.LEFT;
        }
    }

    /**
     * Used to convert iOS style("Red",etc) to android style(Color.RED)
     */
    public static int convertColorStringToInt(String color) {
        if (color.equals("Red")) {
            return Color.RED;
        } else if (color.equals("Blue")) {
            return Color.BLUE;
        } else if (color.equals("Black")) {
            return Color.BLACK;
        } else if (color.equals("Yellow")) {
            return Color.YELLOW;
        } else if (color.equals("Green")) {
            return Color.GREEN;
        } else {
            return Color.BLACK;
        }
    }



}
