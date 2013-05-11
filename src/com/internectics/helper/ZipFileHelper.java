package com.internectics.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    public static void zipFiles(String zipFileName, ArrayList<String> fs) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zipFileName));
        for (int i = 0; i < fs.size(); i++) {
            ZipEntry entry = new ZipEntry(new File(fs.get(i)).getName());
            out.putNextEntry(entry);
            FileInputStream in = new FileInputStream(fs.get(i));
            int b;
            while ((b = in.read()) != -1)
                out.write(b);
            in.close();
        }
        out.close();
    }


    /*
      will be fixedly unzipped to folder: /cache/Downloaded Pack/
      @param zipFileName the zip file used to be unzipped
     */
    public static void unzipFile(String zipFileName,String outputDirectory) throws Exception {

        ZipInputStream in = new ZipInputStream(new FileInputStream(zipFileName));
        ZipEntry z;
        int i = 0;
        while ((z = in.getNextEntry()) != null) {
            if (z.isDirectory()) {
                String name = z.getName();
                name = name.substring(0, name.length() - 1);
                File f = new File(outputDirectory + File.separator + name);
                f.mkdir();
            }
            else {
                File f = new File(outputDirectory + File.separator
                        + z.getName());
                f.createNewFile();
                FileOutputStream out = new FileOutputStream(f);
                byte[] buf = new byte[1024*16];
                int b;
                while ((b = in.read(buf)) != -1)
                    out.write(b);
                out.close();

                //if the unzipped is still a zip file, we continue it
                if (z.getName().contains(".zip")){
                    File unzippedDirectory = new File(outputDirectory + File.separator + String.format("card%d",i));
                    unzippedDirectory.mkdir();
                    unzipFile(f.toString(),unzippedDirectory.toString());
                    i++;
                }
            }
        }
        in.close();


    }

}
