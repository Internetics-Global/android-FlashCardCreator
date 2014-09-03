/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.internectics.helper.AmazonSDB;

import android.util.Log;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.internectics.util.Global;

/**
* This class is used to get clients to the various AWS services.  Before accessing a client 
* the credentials should be checked to ensure validity.
*/
public class AmazonClientManager {

    private AmazonSimpleDBClient sdbClient = null;

    
    public AmazonClientManager() {
    }

    public AmazonSimpleDBClient sdb() {
        validateCredentials();    
        return sdbClient;
    }


    
    public void validateCredentials() {
        if ( sdbClient == null ) {
            Log.i(Global.debugTag, "Creating New Clients." );
            
            Region region = Region.getRegion(Regions.US_EAST_1);

            AWSCredentials credentials = new BasicAWSCredentials(Global.amazon_sdb_accessKey,Global.amazon_sdb_secretKey);
		    
		    sdbClient = new AmazonSimpleDBClient( credentials );
		    sdbClient.setRegion(region);

        }
    }

}
