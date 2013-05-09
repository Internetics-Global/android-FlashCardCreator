package com.internectics.helper;

import android.graphics.Bitmap;
import com.internectics.util.AppContext;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileOperationHelper {
    public static File cacheDirectory() {
        return AppContext.getAppContext().getCacheDir();
    }

    /**
     * All card related images will be input here
     */
    public static File imagesDirectory() {
        File tempFile = new File(cacheDirectory(), "Images");
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        return tempFile;
    }

    /**
     * All the downloaded zip will be input here
     */
    public static File downloadedPackDirectory() {
        File tempFile = new File(cacheDirectory(), "Downloaded Pack");
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        return tempFile;
    }

    /**
     * All the uploaded zip will be input here
     */
    public static File uploadPackDirectory() {
        File tempFile = new File(cacheDirectory(), "Upload Pack");
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        return tempFile;
    }

    /**
     * All the image resouces in pack/card will be JPG format Everytime you call
     * this method, the file path will be unique
     */
    public static File generateUniqueImageFilePath() {
        String string = String.format("%s.jpg", UUID.randomUUID().toString());
        File tempFile = new File(imagesDirectory(), string);
        return tempFile;
    }

    public static String covertToUriFormatString(File file) {
        String string = String.format("file://%s", file.toString());
        return string;
    }

    /**
     * scale while maintaining the image's aspect ratio
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int originWidth = bitmap.getWidth();
        int originHeight = bitmap.getHeight();

        // no need to resize
        if (originWidth < maxWidth && originHeight < maxHeight) {
            return bitmap;
        }

        int width = originWidth;
        int height = originHeight;

        if (originWidth > maxWidth) {
            width = maxWidth;

            double i = originWidth * 1.0 / maxWidth;
            height = (int) Math.floor(originHeight / i);

            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }

        if (height > maxHeight) {
            height = maxHeight;
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }

        return bitmap;
    }


    /**
     * Test purpose
     */
    public static File getTestFile() {
        File tempFile = new File(cacheDirectory(), "test.json");
        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return tempFile;
    }

    /**
     * Test purpose
     */
    public static File getTestFile2() {
        File tempFile = new File(cacheDirectory(), "test2.json");
        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return tempFile;
    }


}
