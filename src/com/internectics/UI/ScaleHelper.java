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

        String rawNominalSizeArray[] =  activity.getResources().getStringArray(R.array.css_size);
        int size = rawNominalSizeArray.length;
        String returnArray[] = new String[size -1 ]; //去除第一个

        for (int i=1; i < size; i ++) {
            returnArray[i-1] = rawNominalSizeArray[i];
        }
        return returnArray;
    }


    public static String[]  realSizeArray(Activity activity) {
        float scale = getScale(activity);
        int size = nominalSizeArray(activity).length;
        String returnArray[] = new String[size];
        for (int i= 0; i<size; i++) {
            String item =  (nominalSizeArray(activity))[i];
            float val = Float.parseFloat(item);
            returnArray[i] = Float.toString(val * scale);
        }
        return returnArray;

    }


    /**
     *  从nominalSize到realSize
     */
    public static float getRealSizeFromNominalSize (Activity activity,float nominalSize) {

        int index = nearestIndexForArray(nominalSizeArray(activity),nominalSize);

        String strVal = (realSizeArray(activity))[index];

        return Float.parseFloat(strVal);
    }

   /**
    *  从realSize到nominalSize
   */
    public static float getNominalSizeFromRealSize (Activity activity,float realsize) {

        int index = nearestIndexForArray(realSizeArray(activity),realsize);

        String strVal = (nominalSizeArray(activity))[index];

        return Float.parseFloat(strVal);
    }


    private static float getScale (Activity activity) {
        float screenWidthDPUnit = UIHelper.getScreenWidthDPUnit(activity);
        if (screenWidthDPUnit <= 650) {
          return (float)1.0;  // galaxy s2, s3, s4, nexus 5为基准
        } else if ((screenWidthDPUnit > 650) && (screenWidthDPUnit <= 800)) {
            return (float)1.25;

        } else if ((screenWidthDPUnit > 801) && (screenWidthDPUnit <= 950)) {
            return (float)1.5;

        } else if ((screenWidthDPUnit > 951) && (screenWidthDPUnit <= 1100)) {
            return  (float)1.75;

        } else if ((screenWidthDPUnit > 1101)) {
            return (float)2.0;  //Nexus10 (galxy tab 10.1) =  1280
        } else {
            return (float)1.0;
        }
    }

    /*
     * array为string类型的数组，需要提前从小到大排序，可以转换成double
     */
    private static int nearestIndexForArray(String array[],float elementVal) {

        int arrayLength = array.length;

        if (elementVal <= Float.parseFloat(array[0])) {
            return 0;
        }

        if (elementVal >= Float.parseFloat(array[arrayLength - 1])) {
            return arrayLength - 1;
        }

        for (int i = 0; i< arrayLength - 1; i++) {

            if ((elementVal >= Float.parseFloat(array[i])) && (elementVal < Float.parseFloat(array[i +1]))) {

                return i;
            }
        }

        return -1;


    }


}
