package com.flipflash.cryptor;


import android.util.Log;

import com.amazonaws.util.IOUtils;
import com.flipflash.cryptor.JNCryptor.AES256JNCryptor;
import com.flipflash.cryptor.JNCryptor.AES256JNCryptorInputStream;
import com.flipflash.cryptor.JNCryptor.AES256JNCryptorOutputStream;
import com.flipflash.cryptor.JNCryptor.CryptorException;
import com.flipflash.util.AppContext;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by BourneWang on 4/06/15.
 */
public class CryptoHelper {

    private static final String TAG = CryptoHelper.class.getName();

    private final static String password = "@$4245dfsfer42r4243sfds";

    public static boolean encryptFileWithSameOutput(File filePath) {

        boolean result = false;

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        AES256JNCryptorOutputStream aes256JNCryptorOutputStream = null;

        File encryptFile = new File(AppContext.getAppContext().getCacheDir(),"ffc_downloaded_pack_encrypt_temp.zip");


        try {
            inputStream = new FileInputStream(filePath);

            outputStream = new FileOutputStream(encryptFile);

            aes256JNCryptorOutputStream = new AES256JNCryptorOutputStream(outputStream,password.toCharArray());

            int read = 0;
            byte[] bytes = new byte[1024*128];
            while ((read = inputStream.read(bytes)) != -1) {
                aes256JNCryptorOutputStream.write(bytes, 0, read);
                //Log.d("ccaa","encrypt write with bytes no: " + read);
            }

            aes256JNCryptorOutputStream.close();
            aes256JNCryptorOutputStream = null;
            outputStream.close();
            outputStream = null;
            result = true;

            boolean success = FileUtils.deleteQuietly(filePath);
            if (success) {
                FileUtils.moveFile(
                        encryptFile,
                        filePath);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CryptorException e) {
            e.printStackTrace();
        }  finally {

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (aes256JNCryptorOutputStream != null) {
            try {
                aes256JNCryptorOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (outputStream != null) {
            try {
                // outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

        return result;

    }

    public static boolean encryptFileWithSameOutput(String filePath) {

        File newFile = new File(filePath);

        boolean result = encryptFileWithSameOutput(newFile);

        return result;


    }

    public static boolean decryptFileWithSameOutput(File filePath) {

        boolean result = false;

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        AES256JNCryptorInputStream aes256JNCryptorInputStream = null;

        File decryptFile = new File(AppContext.getAppContext().getCacheDir(),"ffc_downloaded_pack_decrypt_temp.zip");

        try {

            inputStream = new FileInputStream(filePath);

            outputStream = new FileOutputStream(decryptFile);

            aes256JNCryptorInputStream = new AES256JNCryptorInputStream(inputStream,password.toCharArray());

            int read = 0;
            byte[] bytes = new byte[1024*128];
            while ((read = aes256JNCryptorInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
                //Log.d("ccaa","decrypt write with bytes no: " + read);
            }
            outputStream.close();
            outputStream = null;
            aes256JNCryptorInputStream.close();
            aes256JNCryptorInputStream = null;
            result = true;

            boolean success = FileUtils.deleteQuietly(filePath);
            if (success) {
                FileUtils.moveFile(
                        decryptFile,
                        filePath);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (aes256JNCryptorInputStream != null) {
                try {
                    aes256JNCryptorInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }

        return result;

    }


    public static boolean decryptFileWithSameOutput(String filePath) {

        File newFile = new File(filePath);

        boolean result = decryptFileWithSameOutput(newFile);

        return result;


    }

}
