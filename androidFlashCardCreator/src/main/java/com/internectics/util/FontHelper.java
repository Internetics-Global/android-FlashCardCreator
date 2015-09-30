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

        switch (index) {
            case 0:
                return (FontCache.get(Global.fontName_Default, context));

            case 1:
                return (FontCache.get(Global.fontName_Papyrus, context));

            case 2:
                return (FontCache.get(Global.fontName_Courier, context));

            case 3:
                return (FontCache.get(Global.fontName_Chalkduster, context));

            case 4:
                return (FontCache.get(Global.fontName_ArialBoldMT, context));

            case 5:
                return (FontCache.get(Global.fontName_Zapfino, context));

            default:
                return (FontCache.get(Global.fontName_Default, context));
        }

    }


    public static Typeface fontFromName(Context context,String fontStr) {

        if ((fontStr == null) || (fontStr.length() == 0)) {
            return (FontCache.get(Global.fontName_Default, context));
        }
        else if (fontStr.equalsIgnoreCase("Helvetica-Bold")) {
            return (FontCache.get(Global.fontName_Papyrus, context));
        }
        else if (fontStr.equalsIgnoreCase("Courier-Bold")) {
            return (FontCache.get(Global.fontName_Courier, context));
        }
        else if (fontStr.equalsIgnoreCase("Chalkduster")) {
            return (FontCache.get(Global.fontName_Chalkduster, context));
        }
        else if (fontStr.equalsIgnoreCase("Arial-BoldMT")) {
            return (FontCache.get(Global.fontName_ArialBoldMT, context));
        } else if (fontStr.equalsIgnoreCase("Zapfino")) {
            return (FontCache.get(Global.fontName_Zapfino, context));
        }
        else {
            return (FontCache.get(Global.fontName_Default, context));
        }

    }





}
