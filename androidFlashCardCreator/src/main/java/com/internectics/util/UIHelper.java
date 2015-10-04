package com.internectics.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.internectics.android_flashcardcreator.R;
import com.internectics.helper.FileOperationHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import timber.log.Timber;

public class UIHelper {

    public static int getPixels(int dipValue) {

        Resources r = AppContext.getAppContext().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                r.getDisplayMetrics());
        return px;
    }

    public static float pixelsToSp(float px) {
        Resources r = AppContext.getAppContext().getResources();
        float scaledDensity = r.getDisplayMetrics().scaledDensity;
        return (px/scaledDensity);
    }

    public static Bitmap resizeImageTo800(Context context, Uri localImageUri) {

        Bitmap resizeBitmap = resizeImageTo(context,localImageUri,800);

        return resizeBitmap;
    }


    /**
     * local image uri, not include picasa web image
     * @param context
     * @param localImageUri
     * @return
     */
    public static Bitmap resizeImageTo400(Context context, Uri localImageUri) {

        Bitmap resizeBitmap = resizeImageTo(context,localImageUri,400);

        return resizeBitmap;
    }

    public static Bitmap resizeImageTo(Context context, Uri localImageUri,int width) {

        Bitmap resizeBitmap = null;
        String pathName = FileOperationHelper.getRealImagePathFromURI(context, localImageUri);
        File f = new File(pathName);
        if (f.exists()) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, opts);
            int max;
            if ((opts.outWidth > width) || (opts.outHeight > width) ) {
                max = (opts.outWidth > opts.outHeight)?opts.outWidth:opts.outHeight;
                opts.inSampleSize = (max/width);
            }
            opts.inJustDecodeBounds = false;
            resizeBitmap = BitmapFactory.decodeFile(pathName, opts);
        }

        return resizeBitmap;
    }

    /*
     * 单位是pixel
     */
    public static Bitmap getRoundedBottomRightCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        final Rect topRightRect = new Rect(bitmap.getWidth()/2, 0, bitmap.getWidth(), bitmap.getHeight()/2);
        final Rect leftRect = new Rect(0, 0, bitmap.getHeight()/2, bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        // Fill in upper right corner
        canvas.drawRect(topRightRect, paint);
        // Fill in bottom corners
        canvas.drawRect(leftRect, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }



    public static Bitmap resizedBitmapWithScaleToFit(Bitmap bm, int newWidth,int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();

        Bitmap resizedBitmap;

        if (scaleWidth > scaleHeight) {
            float originalScaleHeight  =  scaleHeight;
            scaleHeight = scaleWidth;

            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width,(int)(height * originalScaleHeight /scaleWidth), matrix, false);

        } else {
            float originalScaleWidth  =  scaleWidth;
            scaleWidth = scaleHeight;

            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            resizedBitmap = Bitmap.createBitmap(bm, 0, 0, (int)(width * originalScaleWidth/scaleHeight), height, matrix, false);
        }

        return resizedBitmap;
    }

    /*
    所有的图片，视频，音频资源都保存在这个目录下面。
     */
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
            Timber.tag(Global.debugTag).e("Exception: " + e.getMessage());
        }

        return toSaveFile;
    }

    public static Bitmap getVideoThumbnail(Context context,Uri uri) {

        Bitmap bMap;
        if ((uri.toString().contains("http://")) || (uri.toString().contains("https://"))) {
            //我们暂时没有更好的方法获取来自http://的thumbnail图片，比如youtube。期待更加的解决方案
            bMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.video_placeholder);
        } else {
            String path = getRealPathFromURI(context,uri);

            bMap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        }

        Bitmap bmOverlay = Bitmap.createBitmap(bMap.getWidth(), bMap.getHeight(), bMap.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bMap, new Matrix(), null);

        Bitmap playIconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.play_big);

        int left = (int)(bMap.getWidth() *0.3);
        int right = (int)(bMap.getWidth() *0.7);
        Rect rect = new Rect(left,left,right,right);
        canvas.drawBitmap(playIconBitmap,null,rect,null);

        return bmOverlay;
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
                Timber.tag(Global.debugTag).e(  "Output Stream Opened successfully");
            }

            byte[] buffer = new byte[1000];
            int bytesRead = 0;
            while ( ( bytesRead = inputStream.read( buffer, 0, buffer.length ) ) >= 0 )
            {
                outputStream.write( buffer, 0, buffer.length );
            }
        } catch ( Exception e ){
            Timber.tag(Global.debugTag).e( "Exception occurred " + e.getMessage());

        } finally{
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toSaveFile;

    }


    public static Bitmap loadBitmapFromView(View v) {

        int inWidth = v.getWidth();
        int inHeight = v.getHeight();

        if (inHeight == 0 || inHeight == 0) {
            throw new IllegalStateException("loadBitmapFromView should have a view with size >0");
        }

        Bitmap bitmap = Bitmap.createBitmap(inWidth, inHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);


        //在我们的例子中，宽度永远是大于高度的
        int outWidth = 400;
        int outHeight = (inHeight * outWidth) / inWidth;

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);

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
        Timber.tag(Global.debugTag).d( "current platform is: " + returnStr);
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
        Bitmap resizeBitmap = getResizedSizeBitmapFromPicasa(context,url,400);

        return resizeBitmap;
    }

    public static Bitmap getResizedSizeBitmapFromPicasa(Context context, Uri url,int width)
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
            if ((opts.outWidth > width) || (opts.outHeight > width) ) {
                max = (opts.outWidth > opts.outHeight)?opts.outWidth:opts.outHeight;
                opts.inSampleSize = (max/width);
            }
            opts.inJustDecodeBounds = false;
            resizeBitmap = BitmapFactory.decodeFile(tempFile.toString(), opts);

            //Step3: delete temp file
            tempFile.delete();

            return resizeBitmap;

        } catch (Exception ex) {
            Timber.tag(Global.debugTag).e( "Exception: " + ex.getMessage());
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
            val= 54;
        } else if (screenDPSize >=1500) {
            val= 52;
        } else if (screenDPSize >=1400) {
            val= 50;
        } else if (screenDPSize >=1350) {
            val= 48;
        }else if (screenDPSize >=1300) {
            val= 46;
        }else if (screenDPSize >=1200) {
            val= 44;
        } else if (screenDPSize >=1150) {
            val= 42;
        } else if (screenDPSize >=1100) {
            val= 40;
        }else if (screenDPSize >=1050) {
            val= 38;
        } else if (screenDPSize >=1000) {
            val= 36;
        } else if (screenDPSize >=900) {
            val= 32;
        } else if (screenDPSize >=800) {
            val= 28;
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

    /*
     * 仅是CardBackgroundImage的大小，不包括sidebar和title部分
     * 我们不能直接去获取R.id.card_background_image，因为这时view有可能还没有inflater
     */
    public static int getCardBackgroundWidth(Activity activity,Boolean isPlayCard) {
        int width;
        if (isPlayCard) {
            width = (UIHelper.getScreenWidth(activity) - UIHelper.getPixels(20)) * 740 / (62 +740);
        } else {
            FrameLayout cardLayout = (FrameLayout) activity.findViewById(R.id.detail);
            width = (cardLayout.getWidth()  - UIHelper.getPixels(20)) * 740 / (62 +740);
        }
        return width;
    }

    /*
     * 仅是CardBackgroundImage的大小，不包括sidebar和title部分
     * 我们不能直接去获取R.id.card_background_image，因为这时view有可能还没有inflater
     */
    public static int getCardBackgroundHeight(Activity activity,Boolean isPlayCard) {
        int height;
        if (isPlayCard) {
            height = (UIHelper.getScreenHeight(activity)) * 440 / (112 +440);;
        } else {
            FrameLayout cardLayout = (FrameLayout) activity.findViewById(R.id.detail);
            height = (cardLayout.getHeight() * 550 / (550 + 40) - UIHelper.getPixels(30)) * 440 / (112 +440);;
        }
        return height;
    }

}
