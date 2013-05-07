package com.internectics.helper;

import android.util.Log;
import com.internectics.util.Global;

import java.io.*;
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
    public static void zipPack(String zipFileName, File fs[]) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zipFileName));
        zip(out, fs, "");
        out.close();
    }


    /*
      will be unzipped to folder: /cache/Downloaded Pack/
      @param zipFileName the zip file used to be unzipped
     */
    public static void unzipPack(String zipFileName) throws Exception {
        File outputDirectory = FileOperationHelper.downloadedPackDirectory();
        unzip(zipFileName,outputDirectory.toString());
    }

    /*
    ** not take care folder in zip
     */
    private static void zip(ZipOutputStream out, File fs[], String base) throws Exception {
        for (File f:fs) {
            out.putNextEntry(new ZipEntry(base));
            FileInputStream in = new FileInputStream(f);
            int b;
            while ((b = in.read()) != -1)
                out.write(b);
            in.close();
        }
    }

    /*
    ** take care folder in zip
     */
    private static void unzip(String zipFileName, String outputDirectory) throws Exception {

        ZipInputStream in = new ZipInputStream(new FileInputStream(zipFileName));
        ZipEntry z;
        while ((z = in.getNextEntry()) != null) {
            System.out.println("unziping " + z.getName());
            if (z.isDirectory()) {
                String name = z.getName();
                name = name.substring(0, name.length() - 1);
                File f = new File(outputDirectory + File.separator + name);
                f.mkdir();
                System.out.println("mkdir " + outputDirectory + File.separator
                        + name);
            } else {
                File f = new File(outputDirectory + File.separator
                        + z.getName());
                f.createNewFile();
                FileOutputStream out = new FileOutputStream(f);
                int b;
                while ((b = in.read()) != -1)
                    out.write(b);
                out.close();
            }
        }
        in.close();

    }
}
