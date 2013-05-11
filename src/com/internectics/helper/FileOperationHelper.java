package com.internectics.helper;

import android.content.Context;
import android.graphics.Bitmap;
import com.internectics.android_flashcardcreator.R;
import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;

import java.io.*;
import java.util.UUID;

/*
 * Common file operations
 */
public class FileOperationHelper {

    private static int resrouceID[] = {R.drawable.logo_placeholder,
            R.drawable.question_image_placeholder,
            R.drawable.answer_image_placeholder,
            R.drawable.pack_cover_default_image,
            R.drawable.card_cover_image_placeholder};
    private static String output[] = {
            "logo_placeholder.jpg",
            "question_image_placeholder.jpg",
            "answer_image_placeholder.jpg",
            "pack_cover_default_image.jpg",
            "card_cover_image_placeholder.jpg" };


    /*
     * Cache directory
     */
    public static File cacheDirectory() {
        return AppContext.getAppContext().getCacheDir();
    }

    /**
     * All reserved images will be input here, which is copied from R.drawable during start-up
     */
    private static File reservedDirectory() {
        File tempFile = new File(cacheDirectory(), "Reserved");
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        return tempFile;
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

    /**
     * All the pack zips will be unique
     */
    public static File generateUniquePackZipFilePathForUploading() {
        String string = String.format("pack%s.zip", UUID.randomUUID().toString());
        File tempFile = new File((uploadPackDirectory()), string);
        return tempFile;
    }

    public static String covertToUriFormatFile(File file) {
        String string = String.format("file://%s", file.toString());
        return string;
    }

    public static String deleteUriSchemeHeader(String str) {
        if (str.contains("file://")) {
            String returnStr = str.substring(7,str.length());
            return returnStr;
        }
        return str;
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

    /*
     * this file(path) is fixed
     */
    public static File getUploadPackJsonFile() {
        File packJsonFile = new File(FileOperationHelper.uploadPackDirectory(), "packInformation.json");
        return packJsonFile;
    }

    /*
     * this file(path) is fixed
     */
    public static File getUploadCardJsonFile() {
        File cardJsonFile = new File(FileOperationHelper.uploadPackDirectory(), "cardTextContent.json");
        return cardJsonFile;
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

    /*
     * Copy all the images in "R.drawable" to cache/reserved folder
     */
    public static void copyResourcesImagesToCache(Context context) {

        final String isFirstStartUp = "isFirstStartUp";
        String flag = AppConfig.getInstance(context).get(isFirstStartUp);
        if ((flag != null) && flag.equals("1")) {
            return;
        }

        for (int i = 0; i < resrouceID.length; i++) {
            InputStream in = context.getResources().openRawResource(resrouceID[i]);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(new File(reservedDirectory(), output[i]));
                byte[] buff = new byte[1024];
                int read = 0;

                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
                in.close();
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

        }

        AppConfig.getInstance(context).set(isFirstStartUp, "1");
    }


    public static String getLogoPlaceholderImagePath() {
        File tempFile = new File(reservedDirectory(),output[0]);
        return covertToUriFormatFile(tempFile);
    }

    public static String getQuestionImagePlaceholderImagePath() {
        File tempFile = new File(reservedDirectory(),output[1]);
        return covertToUriFormatFile(tempFile);
    }

    public static String getAnswerImagePlaceholderImagePath() {
        File tempFile = new File(reservedDirectory(),output[2]);
        return covertToUriFormatFile(tempFile);
    }

    public static String getPackCoverDefaultImagePath() {
        File tempFile = new File(reservedDirectory(),output[3]);
        return covertToUriFormatFile(tempFile);
    }

    public static String getCardCoverDefaultImagePath() {
        File tempFile = new File(reservedDirectory(),output[4]);
        return covertToUriFormatFile(tempFile);
    }


    public static void copyImageToImagesFolder(File imageFile) {
        File targetFile = new File(imagesDirectory(),imageFile.getName());
        copyFile(imageFile,targetFile);
    }


    private static void copyFile(File orginFile, File targetFile) {
        try {
            FileInputStream inStream = new FileInputStream(orginFile);
            FileOutputStream outStream = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024 * 16];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();

            System.out.println("File is copied successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
