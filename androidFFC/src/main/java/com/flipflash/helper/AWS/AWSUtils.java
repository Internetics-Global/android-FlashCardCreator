package com.flipflash.helper.AWS;

import com.flipflash.data.Pack;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by BourneWang on 29/05/15.
 */
public class AWSUtils {

    private static final String TAG = AWSUtils.class.getSimpleName();

    public static String fullPath_On_S3(Pack pack) {

        if (StringUtils.isEmpty(pack.fileNameOnAWS)) {
            throw  new IllegalArgumentException("fileNameOnAWS should be set backhand");
        }

        String fullBucketName = getFullBucketName();

        String str = AWS_Constant.S3_BASE_URL + "/" + fullBucketName + "/" + pack.fileNameOnAWS;
        return str;
    }


    public static String getFullBucketName() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            throw  new IllegalStateException("Should not be here, Parse account should be registered beforehand");
        }
        String expectedBucketName = FirebaseAuth.getInstance().getCurrentUser().getEmail().toLowerCase();

        expectedBucketName = StringUtils.removeAllCharactersExceptAlphanumericFromString(expectedBucketName);
        expectedBucketName = String.format("%s-%s",expectedBucketName, Global.BucketPostfixAfterUserName);

        return expectedBucketName;

    }
}
