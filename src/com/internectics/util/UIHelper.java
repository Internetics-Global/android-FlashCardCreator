package com.internectics.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
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


    public static Bitmap resizeImageTo400(Context context, Uri imageUri) {
        ContentResolver cResolver = context.getContentResolver();
        Bitmap resizeBitmap = null;
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(cResolver
                    .openInputStream(imageUri));
            resizeBitmap = FileOperationHelper.resizeBitmap(
                    bitmap, 400, 400);

        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }
        return resizeBitmap;
    }

    public static File saveImageToCaches(Bitmap savedBitmap) {
        File toSaveFile = FileOperationHelper.generateUniqueImageFilePath();

        try {
            FileOutputStream fOutputStream = new FileOutputStream(toSaveFile);
            try {
                savedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOutputStream);
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
        //we freeze the size
        Bitmap returnedBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =v.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        v.draw(canvas);
        return returnedBitmap;
    }
}
