package com.internectics.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.*;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import com.internectics.helper.FileOperationHelper;

import java.io.*;
import java.net.URI;
import java.net.URL;

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


    /**
     * local image uri, not include picasa web image
     * @param context
     * @param localImageUri
     * @return
     */
    public static Bitmap resizeImageTo400(Context context, Uri localImageUri) {

        Bitmap resizeBitmap = null;
        String pathName = FileOperationHelper.getRealImagePathFromURI(context, localImageUri);
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

    public static Bitmap getVideoThumbnail(Context context,Uri uri) {

        String path = getRealPathFromURI(context,uri);

        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);

        return bMap;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public static File saveVideoToCaches(Context context, Uri uri) {
        File toSaveFile = FileOperationHelper.generateUniqueVideoFilePath();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try
        {
            ContentResolver content = context.getContentResolver();
            inputStream = content.openInputStream(uri);

            outputStream = new FileOutputStream( toSaveFile);
            if(outputStream != null){
                Log.e( Global.debugTag, "Output Stream Opened successfully");
            }

            byte[] buffer = new byte[1000];
            int bytesRead = 0;
            while ( ( bytesRead = inputStream.read( buffer, 0, buffer.length ) ) >= 0 )
            {
                outputStream.write( buffer, 0, buffer.length );
            }
        } catch ( Exception e ){
            Log.e(Global.debugTag, "Exception occurred " + e.getMessage());

        } finally{

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

    public static int getScreenHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int height = display.getHeight();

        return height;
    }

    public static double getCardHeight(Activity activity) {
        double cardHeight = (getScreenHeight(activity)- getActionbarHeight(activity) - getPixels(10 + 10 + 10)) * 550 /595;
        return cardHeight;
    }

    public static double getCardWidth(Activity activity) {
        double cardWidth = getScreenWidth(activity) *3/4;
        return (cardWidth);
    }

    public static double getCardRatio(Activity activity) {
        double cardHeight = getCardHeight(activity);
        double cardWidth = getCardWidth(activity);
        return cardHeight/cardWidth;
    }


    /*
    Unit is pixel
     */
    public static int getActionbarHeight(Activity activity) {
        TypedArray styledAttributes = activity.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int mActionBarHeight = (int) styledAttributes.getDimension(0, 0);

        return mActionBarHeight;
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


    public static Bitmap getResized400SizeBitmapFromPicasa(Context context, Uri url)
    {
        File cacheDir;
        Bitmap resizeBitmap = null;

        // if the device has an SD card
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),".OCFL311");
        } else {
            // it does not have an SD card
            cacheDir=context.getCacheDir();
        }
        if(!cacheDir.exists())
            cacheDir.mkdirs();

        File tempFile=new File(cacheDir, "tempfile.jpg");

        try {

            //Step1: copy picasa image to local
            InputStream is = null;
            if ((url.toString().startsWith("content://com.google.android.gallery3d"))
            ||(url.toString().startsWith("content://com.sec.android.gallery3d"))){
                is=context.getContentResolver().openInputStream(url);
            } else {
                is=new URL(url.toString()).openStream();
            }
            OutputStream os = new FileOutputStream(tempFile);
            copyStream(is, os);
            os.close();

            //Step2:resize it
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(tempFile.toString(), opts);
            int max;
            if ((opts.outWidth > 400) || (opts.outHeight > 400) ) {
                max = (opts.outWidth > opts.outHeight)?opts.outWidth:opts.outHeight;
                opts.inSampleSize = (max/400);
            }
            opts.inJustDecodeBounds = false;
            resizeBitmap = BitmapFactory.decodeFile(tempFile.toString(), opts);

            //Step3: delete temp file
            tempFile.delete();

            return resizeBitmap;

        } catch (Exception ex) {
            Log.e(Global.debugTag, "Exception: " + ex.getMessage());
            ex.printStackTrace();
            return resizeBitmap;
        }
    }

    private static void copyStream(InputStream is, OutputStream os) {
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static float getBestFontSize(float screenDPSize) {
        float val = 0;
        if (screenDPSize >=1600) {
            val= 52;
        } else if (screenDPSize >=1500) {
            val= 50;
        } else if (screenDPSize >=1400) {
            val= 48;
        } else if (screenDPSize >=1350) {
            val= 46;
        }else if (screenDPSize >=1300) {
            val= 44;
        }else if (screenDPSize >=1200) {
            val= 42;
        } else if (screenDPSize >=1150) {
            val= 40;
        } else if (screenDPSize >=1100) {
            val= 38;
        }else if (screenDPSize >=1050) {
            val= 36;
        } else if (screenDPSize >=1000) {
            val= 34;
        } else if (screenDPSize >=900) {
            val= 30;
        } else if (screenDPSize >=800) {
            val= 26;
        } else if (screenDPSize >=700) {
            val= 24;
        } else if (screenDPSize >=600) {
            val= 20;
        } else if (screenDPSize >=500) {
            val= 16;
        } else if (screenDPSize >=450) {
            val= 14;
        } else if (screenDPSize >=350) {
            val= 12;
        } else {
            val= 11;
        }
        return (val);
    }


    public static Bitmap toRoundCorner(Bitmap bitmap, float pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
