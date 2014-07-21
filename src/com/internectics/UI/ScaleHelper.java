package com.internectics.UI;

import android.app.Activity;

import com.internectics.android_flashcardcreator.R;
import com.internectics.util.UIHelper;

/**
 * Created by bournewang on 7/21/14.
 * toolbar显示的text size并不是一个真实的尺寸(real size)，实际上只是一个名义尺寸(nominal size)，本帮助类的作用就在于此
 */
public class ScaleHelper {

    public static String[]  nominalSizeArray(Activity activity) {
        return activity.getResources().getStringArray(R.array.css_size);
    }


    public static String[]  realSizeArray(Activity activity) {
        double scale = getScale(activity);
        int size = nominalSizeArray(activity).length;
        String returnArray[] = new String[size];
        for (int i= 0; i<size; i ++) {
            String item =  (nominalSizeArray(activity))[i];
            double val = Double.parseDouble(item);
            returnArray[i] = Double.toString(val * scale);
        }
        return returnArray;

    }


    /**
     *  从nominalSize到realSize
     */
    public static double getRealSizeFromNominalSize (Activity activity,double nominalSize) {

        int index = nearestIndexForArray(nominalSizeArray(activity),nominalSize);

        String strVal = (realSizeArray(activity))[index];

        return Double.parseDouble(strVal);
    }

   /**
    *  从realSize到nominalSize
   */
    public static double getNominalSizeFromRealSize (Activity activity,double realsize) {

        int index = nearestIndexForArray(realSizeArray(activity),realsize);

        String strVal = (nominalSizeArray(activity))[index];

        return Double.parseDouble(strVal);
    }


    private static double getScale (Activity activity) {
        double screenWidthDPUnit = UIHelper.getScreenWidthDPUnit(activity);
        if (screenWidthDPUnit <= 650) {
          return 1.0;  // galaxy s2, s3, s4, nexus 5为基准
        } else if ((screenWidthDPUnit > 650) && (screenWidthDPUnit <= 800)) {
            return 1.25;

        } else if ((screenWidthDPUnit > 801) && (screenWidthDPUnit <= 950)) {
            return 1.5;

        } else if ((screenWidthDPUnit > 951) && (screenWidthDPUnit <= 1100)) {
            return  1.75;

        } else if ((screenWidthDPUnit > 1101)) {
            return 2.0;  //Nexus10 (galxy tab 10.1) =  1280
        } else {
            return 1.0;
        }
    }

    /*
     * array为string类型的数组，需要提前从小到大排序，可以转换成double
     */
    private static int nearestIndexForArray(String array[],double elementVal) {

        int arrayLength = array.length;

        if (elementVal <= Double.parseDouble(array[0])) {
            return 0;
        }

        if (elementVal >= Double.parseDouble(array[arrayLength - 1])) {
            return arrayLength - 1;
        }

        for (int i = 0; i< arrayLength - 1; i++) {

            if ((elementVal >= Double.parseDouble(array[i])) && (elementVal < Double.parseDouble(array[i +1]))) {

                return i;
            }
        }

        return -1;


    }


}
