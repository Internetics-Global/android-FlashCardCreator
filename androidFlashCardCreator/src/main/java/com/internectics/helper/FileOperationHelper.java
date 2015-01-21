package com.internectics.helper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import com.internectics.android_flashcardcreator.R;
import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;
import com.internectics.util.Global;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

/*
 * Common file operations
 */
public class FileOperationHelper {

    private static int resrouceID[] = {R.drawable.question_placeholder_logo,
            R.drawable.question_placeholder_content,
            R.drawable.answer_placeholder_content,
            R.drawable.default_pack_cover_image,
            R.drawable.card_cover_image_placeholder};
    private static String output[] = {
            "question_placeholder_logo.jpg",
            "question_placeholder_content.jpg",
            "answer_placeholder_content.jpg",
            "default_pack_cover_image.jpg",
            "card_cover_image_placeholder.jpg"};


    /*
     * Cache directory
     */
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
        String string = String.format("%s.png", UUID.randomUUID().toString());
        File tempFile = new File(imagesDirectory(), string);
        return tempFile;
    }

    /**
     * All the video resouces in pack/card will be  format of .3gp Everytime you call
     * this method, the file path will be unique
     */
    public static File generateUniqueVideoFilePath() {
        String string = String.format("%s.3gp", UUID.randomUUID().toString());
        File tempFile = new File(imagesDirectory(), string);
        return tempFile;
    }

    /**
     * All the audio resouces in pack/card will be  format of .3gp Everytime you call
     * this method, the file path will be unique
     */
    public static File generateUniqueAudio3GPFilePath() {
        String string = String.format("%s.3gp", UUID.randomUUID().toString());
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

    public static String convertToUriFormatFile(File file) {
        String string = String.format("file://%s", file.toString());
        return string;
    }


    public static String deleteUriSchemeHeader(String str) {

        int index = str.indexOf("://");
        if (index != -1) {
            String returnStr = str.substring(index + 3);
            return returnStr;
        }
        return str;
    }

    public static String getRealImagePathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };

        //This method was deprecated in API level 11
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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
    public static File getUploadCardQuestionJsonFile() {
        File cardJsonFile = new File(FileOperationHelper.uploadPackDirectory(), "questionTextContent.json");
        return cardJsonFile;
    }

    /*
     * this file(path) is fixed
     */
    public static File getUploadCardAnswerJsonFile() {
        File cardJsonFile = new File(FileOperationHelper.uploadPackDirectory(), "answerTextContent.json");
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
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
        return tempFile;
    }

    /*
     * Copy all the images in "R.drawable" to cache/reserved folder
     */
    public static void copyResourcesImagesToCache(Context context) {

        String flag = AppConfig.sharedInstance().get(Global.isFirstStartUp);
        if ((flag != null) && flag.equals("true")) {
            return;
        }

        for (int i = 0; i < resrouceID.length; i++) {
            InputStream in = context.getResources().openRawResource(resrouceID[i]);
            FileOutputStream out;
            try {
                out = new FileOutputStream(new File(imagesDirectory(), output[i]));
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

        AppConfig.sharedInstance().set(Global.isFirstStartUp, "false");
    }


    public static String getLogoPlaceholderImagePath() {
        File tempFile = new File(imagesDirectory(), output[0]);
        return convertToUriFormatFile(tempFile);
    }

    public static String getQuestionImagePlaceholderImagePath() {
        File tempFile = new File(imagesDirectory(), output[1]);
        return convertToUriFormatFile(tempFile);
    }

    public static String getAnswerImagePlaceholderImagePath() {
        File tempFile = new File(imagesDirectory(), output[2]);
        return convertToUriFormatFile(tempFile);
    }

    public static String getPackCoverDefaultImagePath() {
        File tempFile = new File(imagesDirectory(), output[3]);
        return convertToUriFormatFile(tempFile);
    }

    public static String getCardCoverDefaultImagePath() {
        File tempFile = new File(imagesDirectory(), output[4]);
        return convertToUriFormatFile(tempFile);
    }


    public static File copyImageVideoToImagesFolder(File file) {

        File targetFile = new File(imagesDirectory(), file.getName());
        copyFile(file, targetFile);
        return targetFile;
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
            //e.printStackTrace();
        }
    }


    public static void deleteFolder(File folder) {
        if (!folder.exists())
            return;

        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }


    public static void deleteAllFileUnderFolder(File folder) {
        if (!folder.exists())
            return;

        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    public static ArrayList<String> listAllZipCardFilesUnderDirectory(String folder) {

        ArrayList<String> zippedCardFileArray = new ArrayList<String>();

        File folderFile = new File(folder);
        File[] listOfFiles = folderFile.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName = listOfFiles[i].getName();

            if (listOfFiles[i].isFile() && (fileName.endsWith(".zip")) && (fileName.substring(fileName.lastIndexOf("/") + 1).indexOf("card") == 0)) {
                System.out.println("File " + listOfFiles[i].getName());
                zippedCardFileArray.add(listOfFiles[i].toString());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }

        return zippedCardFileArray;
    }


    public static boolean moveFile(String srcFileName, String destFileName) {

        File srcFile = new File(srcFileName);
        if(!srcFile.exists() || !srcFile.isFile())
            return false;

        File destFile = new File(destFileName);

        return srcFile.renameTo(destFile);
    }

}
