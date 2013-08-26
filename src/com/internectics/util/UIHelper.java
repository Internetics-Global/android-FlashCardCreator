package com.internectics.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.net.Uri;
import android.text.Layout;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import com.internectics.helper.FileOperationHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class UIHelper {

    public static int getPixels(int dipValue) {

        Resources r = AppContext.getAppContext().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                r.getDisplayMetrics());
        return px;
    }

    public static int pixelsToSp(Float px) {
        Resources r = AppContext.getAppContext().getResources();
        float scaledDensity = r.getDisplayMetrics().scaledDensity;
        return (int)(px/scaledDensity);
    }


    public static Bitmap resizeImageTo400(Context context, Uri imageUri) {

        Bitmap resizeBitmap = null;
        //imageUri: content://media/external/images/media/5076
        //pathName: /storage/emulated/0/Download/2013_06_04_21.51.40.png
        String pathName = FileOperationHelper.getRealImagePathFromURI(context, imageUri);
        File f = new File(pathName);
        if (f.exists()) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, opts);
            int max;
            if ((opts.outWidth > 400) || (opts.outHeight > 400) ) {
                max = (opts.outWidth > opts.outHeight)?opts.outWidth:opts.outHeight;
                opts.inSampleSize = (max/400);
            }
            opts.inJustDecodeBounds = false;
            resizeBitmap = BitmapFactory.decodeFile(pathName, opts);
        }

        return resizeBitmap;
    }

    public static File saveImageToCaches(Bitmap savedBitmap) {
        File toSaveFile = FileOperationHelper.generateUniqueImageFilePath();

        try {
            FileOutputStream fOutputStream = new FileOutputStream(toSaveFile);
            try {
                savedBitmap.compress(Bitmap.CompressFormat.PNG, 30, fOutputStream);
                fOutputStream.flush();
                fOutputStream.close();
            } catch (Exception oException) {
                oException.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }

        return toSaveFile;
    }


    public static Bitmap loadBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);

        int factor = v.getWidth()/400 + 1;

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, v.getWidth()/factor, v.getHeight()/factor, false);

        bitmap.recycle();

        return resizedBitmap;
    }

    public static int getScreenWidth(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();


        return width;
    }

    public static float getScreenWidthDPUnit (Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        float density  = metric.density;
        float dpWidth  = metric.widthPixels / density;

        return dpWidth;
    }

    public static String getCurrentPlatform() {

        DisplayMetrics metric = AppContext.getAppContext().getResources().getDisplayMetrics();

        int width = metric.widthPixels;
        int height = metric.heightPixels;
        int densityDpi = metric.densityDpi;
        String returnStr = String.format("android-%d-%d-%d", width, height, densityDpi);
        Log.d(Global.debugTag, "current platform is: " + returnStr);
        return returnStr;
    }

    public static int getCurrentCursorLine(EditText editText)
    {
        int selectionStart = Selection.getSelectionStart(editText.getText());
        Layout layout = editText.getLayout();

        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }



}
