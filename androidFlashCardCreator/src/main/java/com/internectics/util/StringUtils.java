package com.internectics.util;

import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;

import com.internectics.android_flashcardcreator.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
     * @param
     * @return
     */
    public static String getCurrentTimeDate() {
        String timeDateStr = dateFormater.format(new Date());
        return timeDateStr;
    }


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
     * Used to diff whether it's a resource ID or not
     * return true if numeric
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 在写入到json文件中，我们不再关心它的目录，因为在不同平台(android,ios)，这个目录是不一样的
     * @return
     */
    public static String lastComponentOfPath(Uri uri) {
        String path = uri.getPath();
        String last = path.substring(path.lastIndexOf("/") + 1);
        return last;
    }

    /**
     * 在写入到json文件中，我们不再关心它的目录，因为在不同平台(android,ios)，这个目录是不一样的
     * @param stringUri
     * @return
     */
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
            returnStr = tempStr.replaceAll("<percentage>", "%");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return returnStr;
    }

    /**
     * In order to be compatible with iOS version
     */
    public static int[] convertTemplateBackgroundStringToResourceID(String templateBackground) {
        int[] resourceID = {0, 0, 0};

        if (templateBackground.equals("card_background_blue.png")) {
            resourceID[0] = 0;
            resourceID[1] = R.drawable.shape_card_blue_left_corner;
            resourceID[2] = R.drawable.card_title_bg_blue;
        } else if (templateBackground.equals("card_background_coffee.png")) {
            resourceID[0] = 1;
            resourceID[1] = R.drawable.shape_card_coffee_left_corner;
            resourceID[2] = R.drawable.card_title_bg_coffee;
        } else if (templateBackground.equals("card_background_gray.png")) {
            resourceID[0] = 2;
            resourceID[1] = R.drawable.shape_card_gray_left_corner;
            resourceID[2] = R.drawable.card_title_bg_gray;
        } else if (templateBackground.equals("card_background_purple.png")) {
            resourceID[0] = 3;
            resourceID[1] = R.drawable.shape_card_purple_left_corner;
            resourceID[2] = R.drawable.card_title_bg_purple;
        } else if (templateBackground.equals("card_background_red.png")) {
            resourceID[0] = 4;
            resourceID[1] = R.drawable.shape_card_red_left_corner;
            resourceID[2] = R.drawable.card_title_bg_red;
        } else {
            resourceID[0] = 0;
            resourceID[1] = R.drawable.shape_card_blue_left_corner;
            resourceID[2] = R.drawable.card_title_bg_blue;
        }

        return resourceID;
    }


    public static String convertTemplateBackgroundIndexToString(int index) {
        String result;

        if (index == 0)
            result = "card_background_blue.png";
        else if (index == 1) {
            result = "card_background_coffee.png";
        } else if (index == 2) {
            result = "card_background_gray.png";
        } else if (index == 3) {
            result = "card_background_purple.png";
        } else if (index == 4) {
            result = "card_background_red.png";
        } else {
            result = "card_background_blue.png";
        }

        return result;
    }


    /**
     * Horizontal alignment conversion
     */
    public static int convertGravityStringToInt(String gravity) {
        if (gravity.equals("Left")) {
            return Gravity.LEFT;
        } else if (gravity.equals("Center")) {
            return Gravity.CENTER;
        } else if (gravity.equals("Right")) {
            return Gravity.RIGHT;
        } else  {
            return Gravity.LEFT;
        }
    }

    /**
     * Vertical alignment conversion
     */
    public static int convertVerticalGravityStringToInt(String gravity) {
        if (gravity.equals("Vertical")) {
            return Gravity.CENTER_VERTICAL;
        } else  {
            return Gravity.TOP;
        }
    }

    /**
     * opposite operation compared with convertGravityStringToInt
     */
    public static String convertGravityIntToString(int gravity) {
        if (gravity == Gravity.LEFT) {
            return "Left";
        } else if (gravity == Gravity.CENTER) {
            return "Center";
        } else if (gravity == Gravity.RIGHT) {
            return "Right";
        } else {
            return "Left";
        }
    }

    /**
     * Used to convert iOS style("Red",etc) to android style(Color.RED)
     */
    public static int convertColorStringToInt(String color) {
        if (color.toLowerCase().equals("red")) {
            return Color.RED;
        } else if (color.toLowerCase().toLowerCase().equals("blue")) {
            return Color.BLUE;
        } else if (color.toLowerCase().equals("black")) {
            return Color.BLACK;
        } else if (color.toLowerCase().equals("yellow")) {
            return Color.YELLOW;
        } else if (color.toLowerCase().equals("green")) {
            return Color.GREEN;
        } else if (color.toLowerCase().equals("white")) {
            return Color.WHITE;
        }else {
            return Color.BLACK;
        }
    }


    /**
     * opposite operation compared with convertColorStringToInt
     */
    public static String convertColorIntToString(int color) {
        if (color == Color.RED) {
            return "Red";
        } else if (color == Color.BLUE) {
            return "Blue";
        } else if (color == Color.BLACK) {
            return "Black";
        } else if (color == Color.YELLOW) {
            return "Yellow";
        } else if (color == Color.GREEN) {
            return "Green";
        } else if (color == Color.WHITE) {
            return "White";
        }else {
            return "Black";
        }
    }

    public static boolean isCorrectImageName(String str) {
        boolean result = false;
        result = ((str != null) && ((str.toLowerCase().contains(".png")) || (str.toLowerCase().contains(".jpg"))));
        return result;
    }

    public static boolean isCorrectMov3GPName(String str) {
        boolean result = false;
        result = ((str != null) && (str.toLowerCase().contains(".3gp")));
        return result;
    }

    public static boolean isCorrect3GPName(String str) {
        boolean result = false;
        result = ((str != null) && (str.toLowerCase().contains(".3gp")));
        return result;
    }

    public static boolean isCorrect3GPOrAACName(String str) {
        boolean result = false;
        result = ((str != null) && ((str.toLowerCase().contains(".3gp"))||(str.toLowerCase().contains(".aac"))));
        return result;
    }


    public static String removeAllLinesTrailingSpace(String str) {


        String splitStr =  System.getProperty ("line.separator");
        String[] lines = str.split(splitStr);

        //由于split方法会对末尾的\n进行不正确处理，导致会忽略掉末尾的换行符，所以需要执行这个逻辑
        int length = 0;
        int i = 0;
        for (String item: lines) {
            if (i == lines.length - 1) {
                length = item.length() + length;
            } else {
                length = item.length() + length + splitStr.length();
            }
            i++;
        }

        int numberOfLineSeparator = (str.length() - length) / splitStr.length();


        String result = "";

        i = 0;
        for (String strLine:lines) {
            //注意，replaceAll或replaceFirst里面是正则表达式，而replace则不是
            String trimmedStr = strLine.replaceAll("\\s+$", "");
            if (i == lines.length - 1) {
                result = result + trimmedStr;
            } else {
                result = result + trimmedStr + splitStr;
            }

            i++;
        }

        for (int j = 0; j < numberOfLineSeparator; j ++) {
            result = result + splitStr;
        }

        return result;
    }


    public static boolean isYoutubeLinkage(String str) {
        boolean b;

        if ((str.contains("http://www.youtube.com/"))
                || (str.contains("http://m.youtube.com/"))
                      || (str.contains("https://www.youtube.com/"))
                             || (str.contains("https://m.youtube.com/"))) {
            return true;
        } else {
            return false;
        }
    }


}
