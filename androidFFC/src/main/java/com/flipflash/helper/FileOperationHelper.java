package com.flipflash.helper;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import com.flipflash.android_ffc.R;
import com.flipflash.data.Pack;
import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static com.flipflash.util.LogUtils.LOGE;

/*
 * Common file operations
 */
public class FileOperationHelper {

    private static final String TAG = FileOperationHelper.class.getSimpleName();

    private static int resourceID[] = {R.drawable.question_placeholder_logo,
            R.drawable.question_placeholder_content,
            R.drawable.answer_placeholder_content,
            R.drawable.default_pack_cover_image,
            R.drawable.card_cover_image_placeholder,
            R.drawable.default_pack_cover_image_transparent};
    private static String output[] = {
            "question_placeholder_logo.png",
            "question_placeholder_content.png",
            "answer_placeholder_content.png",
            "default_pack_cover_image.png",
            "card_cover_image_placeholder.png",
            "default_pack_cover_image_transparent.png"};


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
     * All the image resouces in pack/card will be PNG format. Every time you call
     * this method, the file path will be unique
     */
    public static File generateUniqueImageFilePath() {
        String string = String.format("%s.png", UUID.randomUUID().toString());
        File tempFile = new File(imagesDirectory(), string);
        return tempFile;
    }

    public static File generateUniqueGIFFilePath() {
        String string = String.format("%s.gif", UUID.randomUUID().toString());
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
    public static File generateUniquePackZipFilePathForUploading(Pack pack) {
        String packName = pack.packName == null? "":pack.packName;

        String purifiedPackName = StringUtils.removeAllCharactersExceptAlphanumericFromString(packName);

        String string = String.format("%s%d%d.zip", purifiedPackName,(int)(System.currentTimeMillis()/1000),Math.abs((new Random()).nextInt()));
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


    /*
     * delete file except it's a placeholder file
     * 返回true: 成功delete或者没有进行delete操作
     * 返回false: 没有成功delete
     */
    public  static boolean deleteFileExceptPlaceHolder(String fileName) {

        if (StringUtils.isEmptyOrPlaceHolder(fileName)) {
            return true;
        }

        if (fileName.endsWith("/")) {
            //表明这只是一个目录，不是一个文件
            return true;
        }

        File file;
        int index = fileName.indexOf("://");
        if (index == -1) {
            //普通的file path
            file = new File(fileName);
        } else {
            //uri格式
            file = new File(deleteUriSchemeHeader(fileName));
        }

        boolean success = file.delete();


        return success;

    }

    /*
     * 有两种情况：content provider形式的(content://）和文件形式的(file:///)
     */
    public static String getRealImagePathFromURI(Context context, Uri contentUri) {

        String uriHeader = contentUri.getScheme();

        if (uriHeader.equals("file")) {

            String body = contentUri.toString().substring(uriHeader.length() + 3); // remove file://
            return body;

        } else if (uriHeader.equals("content")) {
            String[] proj = { MediaStore.Images.Media.DATA };
            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    contentUri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();

            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            String returnStr = cursor.getString(column_index);

            cursor.close();

            return returnStr;
        } else {
            throw new IllegalStateException("Unexpected URI for getRealImagePathFromURI");
        }

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

        for (int i = 0; i < resourceID.length; i++) {
            InputStream in = context.getResources().openRawResource(resourceID[i]);
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


    public static void copyFile(File orginFile, File targetFile) {

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

            //System.out.println("File is copied successful!");

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

    public static boolean  checkFileExist(Uri uri) {

        if (uri == null) {
            return false;
        }

        File file = new File(deleteUriSchemeHeader(uri.toString()));
        if (file.exists()) {
            return true;
        }

        return false;

    }

    public static File getFullPathFromUriFormatStr(String uriFormatStr) {

        if (StringUtils.isEmpty(uriFormatStr)) {
            return null;
        }

        String fileName = StringUtils.lastComponentOfPath(Uri.parse(uriFormatStr));
        File fullFilePath = new File(FileOperationHelper.imagesDirectory(), fileName);
        return fullFilePath;
    }

}
