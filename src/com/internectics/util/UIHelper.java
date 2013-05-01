package com.internectics.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created with IntelliJ IDEA.
 * User: BourneWang
 * Date: 1/05/13
 * Time: 4:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class UIHelper {

    public static int getPixels(int dipValue) {

        Resources r = AppContext.getAppContext().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                r.getDisplayMetrics());
        return px;
    }
}
