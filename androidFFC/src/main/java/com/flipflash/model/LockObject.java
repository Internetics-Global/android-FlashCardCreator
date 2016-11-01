package com.flipflash.model;

import android.support.annotation.NonNull;

/**
 * Created by internetics on 27/10/2016.
 */

public class LockObject extends Object {

    private String tagStr = "";


    public String getTagStr() {
        return tagStr;
    }

    public void setTagStr(@NonNull  String tagStr) {
        this.tagStr = tagStr;
    }

    public void clearTagStr() {
        this.tagStr = "";
    }
}
