package com.flipflash.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.Layout;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.flipflash.android_ffc.R;
import com.flipflash.helper.FileOperationHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

public class UIHelper {

    private static final String TAG = UIHelper.class.getSimpleName();


    public static float convertDpToPixel(float dp){
        Resources resources = AppContext.getAppContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

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


    public static Bitmap bitmapFromUri(Activity activity,Uri imageUri, int imageWidth) {

        Bitmap resultBitmap = null;

        if (true) {

            try {
                Bitmap tempBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                resultBitmap = resizeImageTo(activity,tempBitmap,imageWidth);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            final String[] filePathColumn = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME };
            Cursor cursor = activity.getContentResolver().query(imageUri, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex;
                // if it is a Picasa image on newer devices with OS 3.0 and up
                if ((imageUri.toString().startsWith("content://com.google.android.gallery3d"))
                        ||(imageUri.toString().startsWith("content://com.sec.android.gallery3d"))){
                    columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        final Uri picasaUri = imageUri;
                        resultBitmap = UIHelper.getResizedSizeBitmapFromPicasa(activity, picasaUri,imageWidth);
                    }
                } else { // it is a regular local image file
                    resultBitmap = UIHelper.resizeImageTo(activity, imageUri,imageWidth);
                }
                cursor.close();
            } else {
                resultBitmap = UIHelper.resizeImageTo(activity, imageUri, imageWidth);
            }
        }



        if (resultBitmap == null) {
            new SweetAlertDialog(activity)
                    .setTitleText(activity.getString(R.string.DIALOG_AlERT))
                    .setContentText(activity.getString(R.string.DIALOG_UNSUPPORTED_IMAGE_SOURCE))
                    .show();
        }

        return resultBitmap;
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
                opts.inSampleSize = nextPowerOf2(max/width);
            }
            opts.inJustDecodeBounds = false;
            resizeBitmap = BitmapFactory.decodeFile(pathName, opts);
        }

        return resizeBitmap;
    }

    public static Bitmap resizeImageTo(Context context, Bitmap bitmap,int width) {

        if (bitmap == null) {
            return null;
        }

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int outHeight = (originalHeight * width) / originalWidth;

        Bitmap resizedBitmap;
        if (width < originalHeight) {
            return bitmap;
        } else {
            resizedBitmap= Bitmap.createScaledBitmap(bitmap, width, outHeight, false);
            return resizedBitmap;
        }
    }

    private static int nextPowerOf2(final int a)
    {
        int b = 1;
        while (b < a)
        {
            b = b << 1;
        }
        return b;
    }


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


    public static File saveGIFToCaches(File file) {
        File toSaveFile = FileOperationHelper.generateUniqueGIFFilePath();
        try {
            FileUtils.copyFile(
                file,
                toSaveFile);
            return toSaveFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

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
            LOGE(TAG, "saveImageToCaches: " + "Exception: " + e.getMessage());
        }

        return toSaveFile;
    }

    public static Bitmap getVideoThumbnail(Context context,@NonNull  Uri uri) {

        boolean isInternetVideo =  (uri.toString().contains("http://")) || (uri.toString().contains("https://"));

        Bitmap bMap;
        if (isInternetVideo) {
            bMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.video_placeholder);
        } else {


            String path = getRealPathFromURI(context,uri);

            bMap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        }

        if (bMap == null) {
            LOGE(TAG, "getVideoThumbnail: use default since can not fetch from:" + uri.toString());
            bMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.video_placeholder);
        }

        if (isInternetVideo) {

            Bitmap bmOverlay = Bitmap.createBitmap(bMap.getWidth(), bMap.getHeight(), bMap.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
            canvas.drawBitmap(bMap, new Matrix(), null);

            Bitmap playIconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.video_link_3);

            int value55 = UIHelper.getPixels(55);
            int value70 = UIHelper.getPixels(70);

            int left = (int)(bMap.getWidth()/2 - value55/2);
            int top = (int)(bMap.getWidth()/2 - value70/2);
            int right = (int)(left + value55);
            int bottom = (int)(top + value70);
            Rect rect = new Rect(left,top,right,bottom);
            canvas.drawBitmap(playIconBitmap,null,rect,null);

            return bmOverlay;
        } else {
            return bMap;
        }

    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return getRealPathFromURI_After_18(context,contentUri);
        } else {
            return getRealPathFromURI_Before_18(context,contentUri);
        }
    }



    private static String getRealPathFromURI_Before_18(Context context, Uri contentUri) {
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getRealPathFromURI_After_18(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
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
                LOGD(TAG, "saveVideoToCaches: Output Stream Opened successfully");
            }

            byte[] buffer = new byte[1000];
            int bytesRead = 0;
            while ( ( bytesRead = inputStream.read( buffer, 0, buffer.length)) >= 0) {
                outputStream.write(buffer, 0, buffer.length );
            }
        } catch ( Exception e ){
            LOGE(TAG, "saveVideoToCaches: " +  "Exception occurred " + e.getMessage());

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

        Bitmap bitmap = Bitmap.createBitmap(inWidth, inHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);


        int outWidth = 400;
        int outHeight = (inHeight * outWidth) / inWidth;

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true);

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

    public static double getCardHeightDPUnit(Activity activity) {

        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        double heightPixel = getCardHeight(activity);

        float density  = metric.density;
        double dpWidth  = heightPixel / density;

        return dpWidth;

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
                new int[]{android.R.attr.actionBarSize});
        int mActionBarHeight = (int) styledAttributes.getDimension(0, 0);

        return mActionBarHeight;
    }

    /*
     * see getReferenceFontSizeArrayForCurrentDevice and find out all popular devices width
     */
    public static float getScreenWidthDPUnit (Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        float density  = metric.density;
        float dpWidth  = metric.widthPixels / density;

        return dpWidth;
    }


    /*
     * same as getScreenWidthDPUnit, but no context necessary
     */
    public static float getScreenWidthDPUnit () {

        WindowManager wm = (WindowManager) AppContext.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        float density  = metrics.density;
        float dpWidth  = metrics.widthPixels / density;

        return dpWidth;
    }

    public static String getCurrentPlatform() {

        DisplayMetrics metric = AppContext.getAppContext().getResources().getDisplayMetrics();

        int width = metric.widthPixels;
        int height = metric.heightPixels;
        int densityDpi = metric.densityDpi;
        String returnStr = String.format("android-%d-%d-%d", width, height, densityDpi);
        //LOGD(TAG, "getCurrentPlatform: " + "current platform is: " + returnStr);
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
        Bitmap resizeBitmap = getResizedSizeBitmapFromPicasa(context, url, 400);

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
            LOGE(TAG, "getResizedSizeBitmapFromPicasa: " + "Exception: " + ex.getMessage());
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

    public static int[] getReferenceFontSizeArrayForCurrentDevice() {

        float screenDPSize = getScreenWidthDPUnit();

        if (screenDPSize <=480) {
            int[] intArray = {13,11,11,13,11,11,40};
            return intArray;

        }else if (screenDPSize <=500) {
            int[] intArray = {15,13,13,15,13,13,42};
            return intArray;

        } else if (screenDPSize <=590) {
            int[] intArray = {17,15,15,17,17,15,44};
            return intArray;

        } else if (screenDPSize <=650) {
            //<!--nexus5,s5 and galaxy note3,dp= 600;   jianguo dp = 640-->
            int[] intArray = {19,17,17,19,17,17,46};
            return intArray;

        } else if (screenDPSize <=700) {
            //<!--nexus6p,dp= 689-->   (not verified )
            int[] intArray = {20,18,18,20,18,18,46};
            return intArray;

        } else if (screenDPSize <=800) {
            int[] intArray = {22,20,20,22,20,20,50};
            return intArray;

        } else if (screenDPSize <=900) {
            int[] intArray = {24,22,22,24,22,22,60};
            return intArray;

        } else if (screenDPSize <=950) {
            int[] intArray = {25,23,23,25,23,23,80};
            return intArray;

        } else if (screenDPSize <=1000) {
            //<!--nexus7, xperia tablet z,dp= 960-->
            int[] intArray = {28,26,26,28,26,26,85};
            return intArray;

        } else if (screenDPSize <=1100) {
            int[] intArray = {30,28,28,30,28,28,90};
            return intArray;

        } else if (screenDPSize <=1200) {
            int[] intArray = {32,30,30,32,30,30,95};
            return intArray;

        } else if (screenDPSize <=1300) {
            //<!--galaxy tab s2, nexus10: dp = 1280-->
            int[] intArray = {38,36,36,38,36,36,100};
            return intArray;

        } else if (screenDPSize <=1400) {
            int[] intArray = {42,40,40,42,40,40,110};
            return intArray;

        } else if (screenDPSize <=1500) {
            int[] intArray = {46,44,44,46,44,44,110};
            return intArray;

        } else{
            int[] intArray = {52,48,48,52,48,48,120};
            return intArray;

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
            val= 26;
        } else if (screenDPSize >=600) {
            //For example, JianGuo dp width = 640
            val= 24;
        } else if (screenDPSize >=500) {
            val= 22;
        } else if (screenDPSize >=450) {
            val= 17;
        } else if (screenDPSize >=350) {
            val= 15;
        } else {
            val= 14;
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
