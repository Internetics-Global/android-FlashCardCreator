package com.flipflash.UI.MultimediaView;

import android.view.View;


/**
 * Created by internetics on 1/11/2016.
 */

public interface OnFrescoImageViewLoadCompletionListener {

    public void gifLoadSucceeded(View view);
    public void nonGifLoadSucceeded(View view);
    public void failed(View view);
}
