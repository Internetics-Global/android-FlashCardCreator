package com.internectics.util;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by bournewang on 4/16/14.
 */
public class FontHelper {

    /**
     * 与css_font(arrays.xml）的对应关系
     */
    public static Typeface fontFromArrayIndex(Context context,int index) {

        Typeface val;

        switch (index) {

            case 0:{
                val = DejaVuSansFontCache.get(Global.defaultFontType, context);
                break;
            }

            case 1:{
                val = Typeface.SANS_SERIF;
                break;
            }

            case 2:{
                val = Typeface.SERIF;
                break;
            }

            case 3:{
                val = Typeface.MONOSPACE;
                break;
            }

            case 4:{
                val = Typeface.DEFAULT_BOLD;
                break;
            }

            default:
                val = DejaVuSansFontCache.get(Global.defaultFontType, context);
                break;


        }

        return val;

    }


    public static Typeface fontFromArrayIndexString(Context context,String indexStr) {
        int index = 0;
        if ((indexStr == null) || (indexStr.length() == 0)||(StringUtils.isNumeric(indexStr) == false)) {
            index = 0;
        } else {
            index = Integer.parseInt(indexStr);
        }

        Typeface  typeface = fontFromArrayIndex(context,index);
        return typeface;
    }

    public static String convertFromiOS(String iosFont) {
        return "";
    }


}
