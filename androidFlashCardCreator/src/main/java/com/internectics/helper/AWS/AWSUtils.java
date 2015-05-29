package com.internectics.helper.AWS;

/**
 * Created by BourneWang on 29/05/15.
 */
public class AWSUtils {

    /**
     * @param zipFileNameUploaded
     * @return
     */
    public static String fullPath_S3(String zipFileNameUploaded) {

        if (zipFileNameUploaded.contains("/")) {
            throw  new IllegalArgumentException("fileName should be just a file name, not a full path");
        }

        String str = AWS_Constant.S3_BASE_URL + "/" + AWS_Constant.S3_BUCKET_NAME + "/" + zipFileNameUploaded;
        return str;
    }
}
