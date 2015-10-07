package com.internectics.helper;

import android.content.Context;
import android.widget.Toast;

import com.internectics.android_flashcardcreator.R;
import com.internectics.util.Global;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cn.pedant.SweetAlert.SweetAlertDialog;
import timber.log.Timber;

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


}
