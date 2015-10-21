package com.flipflash.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Created with IntelliJ IDEA.
 * User: BourneWang
 * Date: 6/05/13
 * Time: 2:47 下午
 * To change this template use File | Settings | File Templates.
 */
public class ZipFileHelper {

    private static final String TAG = ZipFileHelper.class.getName();

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


}
