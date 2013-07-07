package com.internectics.helper;

import com.internectics.android_flashcardcreator.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-7-2
 * Time: 下午2:56
 * To change this template use File | Settings | File Templates.
 */
public class SymbolHelper {

    public static Integer[] mImageResourceIDArray = {
            R.drawable.s001,R.drawable.s002,R.drawable.s003,R.drawable.s004,R.drawable.s005,
            R.drawable.s006,R.drawable.s007,R.drawable.s008,R.drawable.s009,R.drawable.s010,
            R.drawable.s012,R.drawable.s013,R.drawable.s014,R.drawable.s015,
            R.drawable.s016,R.drawable.s017,R.drawable.s018,R.drawable.s019,R.drawable.s020,
            R.drawable.s021,R.drawable.s023,R.drawable.s024,R.drawable.s025,
            R.drawable.s026,R.drawable.s027,R.drawable.s028,R.drawable.s029,R.drawable.s030,
            R.drawable.s031,R.drawable.s032,R.drawable.s035,
            R.drawable.s036,R.drawable.s037,R.drawable.s038,R.drawable.s040,
            R.drawable.s041,R.drawable.s042,R.drawable.s045,
            R.drawable.s046,R.drawable.s049,R.drawable.s050,
            R.drawable.s051,R.drawable.s053,R.drawable.s054,R.drawable.s055

    };

    public static String[] mUnicodeArray = {
            "×", "÷", "∑", "λ", "∩", "∪", "∫", "∴", "≠", "≈",
            "≤", "≥", "⊂", "⊃", "°", "♭", "♯", "µ", "♩",
            "♪", "✓", "✗", "★", "□", "►", "∞", "Ω", "❄",
            "♻", "⚠", "✈", "✇", "☎", "✓", "♥",
            "✝", "✚", "♂", "♀", "○", "◁",
            "▷", "♢", "▽", "△"
    };


    public static String[] mDescriptionArray = {
            "Multiple", "Division", "Summation", "Lambda", "Intersection", "Union", "Integral", "Therefore", "Not equal", "Approxi",
            "Less than", "More than", "Subset", "Superset", "Degree", "Flat sign", "Sharp sign", "Micro", "Quarter note",
            "Eighth note", "Check", "Cross", "Star", "Square", "Pointer", "Infinity", "Omega", "Snowflake",
            "Recycling", "Caution", "Airplane", "Recording", "Telephone", "Check mark", "Heart",
            "Latin cross", "Cross", "Male", "Female", "Circle", "Hexagon",
            "Pentagon", "Rhombus", "triangle", "triangle"
    };


    public static int getSymbolCount() {
        int count = mImageResourceIDArray.length;
        return count;
    }

}
