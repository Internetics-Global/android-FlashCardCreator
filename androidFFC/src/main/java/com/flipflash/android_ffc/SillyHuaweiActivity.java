package com.flipflash.android_ffc;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.flipflash.util.Global;

import cn.pedant.SweetAlert.SweetAlertDialog;

// important background:
// https://medium.com/@amitshekhar/android-memory-leaks-inputmethodmanager-solved-a6f2fe1d1348
// inputmethodmanager.mlastsrvview memory leak issue on Huawei phone seems to be an identified bug and never fixed.
// https://www.google.com.au/search?q=inputmethodmanager.mlastsrvview&oq=inputmethodmanager.mlastsrvview&aqs=chrome..69i57.9533j0j4&sourceid=chrome&ie=UTF-8

public class SillyHuaweiActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }
}