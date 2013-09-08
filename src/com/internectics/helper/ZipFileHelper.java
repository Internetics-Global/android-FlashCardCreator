package com.internectics.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.internectics.util.Global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

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

    public static void unzipPackFile(Context context,String zipFileName,String password) {
        File outputDirectory = FileOperationHelper.downloadedPackDirectory();
        try {

            //Step1, unzip pack
            ZipFile zipFile = new ZipFile(zipFileName);

            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(outputDirectory.toString());

            ArrayList<String> zippedCardFileArray = FileOperationHelper.listAllZipCardFilesUnderDirectory(outputDirectory.toString());

            //Step2, unzip cards in the pack
            for (int i = 0; i < zippedCardFileArray.size(); i++) {
                File unzippedDirectory = new File(outputDirectory + File.separator + String.format("card%d", i));
                if (!unzippedDirectory.exists())
                    unzippedDirectory.mkdir();

                zipFile = new ZipFile(zippedCardFileArray.get(i));
                zipFile.extractAll(unzippedDirectory.toString());
            }

        } catch (ZipException e) {
            e.printStackTrace();
            Log.d(Global.debugTag,"unzip failed:" + e.getCause());
            Toast.makeText(context, "Wrong password", Toast.LENGTH_LONG).show();
        }

    }

}
