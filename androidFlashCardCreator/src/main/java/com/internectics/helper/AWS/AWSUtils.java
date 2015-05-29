package com.internectics.helper.AWS;

/**
 * Created by BourneWang on 29/05/15.
 */
public class AWSUtils {

    public static String fullPath_S3(String fileName) {

        if (fileName.contains("/")) {
            throw  new IllegalArgumentException("fileName should be just a file name, not a full path");
        }

        String str = AWS_Constant.S3BaseURL + "/" + AWS_Constant.S3_BUCKET_NAME + "/" + fileName;
        return str;
    }
}
