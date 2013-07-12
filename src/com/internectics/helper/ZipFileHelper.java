package com.internectics.helper;

import android.util.Log;
import com.internectics.util.Global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: BourneWang
 * Date: 6/05/13
 * Time: 2:47 下午
 * To change this template use File | Settings | File Templates.
 */
public class ZipFileHelper {

    /*
    * @param zipFileName new created zip file name (full path)
    * @param fs file array to create zip file
     */
    public static void zipPackFiles(String zipFileName, ArrayList<String> fs) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zipFileName));
        byte[] buffer = new byte[16 * 1024];
        for (int i = 0; i < fs.size(); i++) {
            ZipEntry entry = new ZipEntry(new File(fs.get(i)).getName());
            out.putNextEntry(entry);
            FileInputStream in = new FileInputStream(fs.get(i));
            int b;
            while ((b = in.read(buffer)) != -1)
                out.write(buffer, 0, b);
            in.close();
        }
        out.close();
    }

    public static void unzipPackFile(String zipFileName, String outputDirectory) {
        try {
            //Step1, unzip pack
            ArrayList<String> zippedCardFileArray = unzip(zipFileName, outputDirectory);

            //Step2, unzip cards in the pack
            for (int i = 0; i < zippedCardFileArray.size(); i++) {
                File unzippedDirectory = new File(outputDirectory + File.separator + String.format("card%d", i));
                if (!unzippedDirectory.exists())
                    unzippedDirectory.mkdir();
                unzip(zippedCardFileArray.get(i), unzippedDirectory.toString());
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Global.debugTag,"unzip failed:" + e.getCause());
        }
    }

    /*
     * if unzip a pack, an array will be returned which inculude all .zip card
     */
    private static ArrayList<String> unzip(String zipFile, String outputFolder) {

        ArrayList<String> zippedCardFileArray = new ArrayList<String>();
        byte[] buffer = new byte[1024];

        try {
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();

                //if the unzipped is still a zip file, we continue it
                if (ze.getName().contains(".zip")) {
                    zippedCardFileArray.add(newFile.toString());
                }

                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d(Global.debugTag,"unzip failed:" + zipFile + ";description:" + ex.getCause());
        }

        return zippedCardFileArray;
    }

}
