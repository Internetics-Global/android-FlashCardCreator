package com.internectics.cryptor;


import com.amazonaws.util.IOUtils;
import com.internectics.cryptor.JNCryptor.AES256JNCryptor;
import com.internectics.cryptor.JNCryptor.CryptorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by BourneWang on 4/06/15.
 */
public class CryptoHelper {

    private static final String TAG = CryptoHelper.class.getName();

    private final static String password = "@$4245dfsfer42r4243sfds";

    public static boolean encryptFileWithSameOutput(File filePath) {

        boolean result = false;

        InputStream targetStream = null;
        try {
            targetStream = new FileInputStream(filePath);
            byte[] byteArray = IOUtils.toByteArray(targetStream);

            AES256JNCryptor cryptor = new AES256JNCryptor();
            byte[] cryptByteArray = cryptor.encryptData(byteArray,password.toCharArray());

            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(cryptByteArray);
            fos.close();

            result = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CryptorException e) {
            e.printStackTrace();
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

        InputStream targetStream = null;
        try {
            targetStream = new FileInputStream(filePath);
            byte[] byteArray = IOUtils.toByteArray(targetStream);

            AES256JNCryptor cryptor = new AES256JNCryptor();
            byte[] cryptByteArray = cryptor.decryptData(byteArray, password.toCharArray());

            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(cryptByteArray);
            fos.close();

            result = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CryptorException e) {
            e.printStackTrace();
        }

        return result;

    }


    public static boolean decryptFileWithSameOutput(String filePath) {

        File newFile = new File(filePath);

        boolean result = decryptFileWithSameOutput(newFile);

        return result;


    }

}
