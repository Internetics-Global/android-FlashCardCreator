package com.internectics.helper.AWS;

import com.internectics.data.Pack;
import com.internectics.util.StringUtils;

/**
 * Created by BourneWang on 29/05/15.
 */
public class AWSUtils {


    public static String fullPath_On_S3(Pack pack) {

        if (StringUtils.isEmpty(pack.fileNameOnAWS)) {
            throw  new IllegalArgumentException("fileNameOnAWS should be set backhand");
        }

        String str = AWS_Constant.S3_BASE_URL + "/" + AWS_Constant.S3_BUCKET_NAME + "/" + pack.fileNameOnAWS;
        return str;
    }
}
