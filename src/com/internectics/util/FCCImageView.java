package com.internectics.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.io.FileNotFoundException;

/**
 * Support two kind of input
 * 1. local resource ID, like: "3244243424"
 * 2. Uri format string, like：”file://sdcard/332343423.jpg"
 */
public class FCCImageView extends ImageView {

    public FCCImageView(Context context) {
        super(context);
    }

    public void setImage(String str) {
        Bitmap bitmap;
        ContentResolver cResolver = AppContext.getAppContext().getContentResolver();

        if (str == null) {
            return;
        }


        if (!StringUtils.isNumeric(str)) {
            this.setImageResource(Integer.parseInt(str));
        } else {
            // Local resouce ID
            String localResourceUriStr = StringUtils.convertToUriStr(str);
            Uri dataUri = Uri.parse(localResourceUriStr);

            try {
                bitmap = BitmapFactory.decodeStream(cResolver
                        .openInputStream(dataUri));
                this.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


    }

}
