package com.flipflash.android_ffc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.flipflash.util.AppContext;

import java.util.Random;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;


public class AppStart extends Activity {
    private static final String TAG = AppStart.class.getName();

    long startTime;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        collectDeviceInfoForDebugging();

        mHandler.sendEmptyMessageDelayed(0,400);
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(AppStart.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            finish();
        }
    };

    private void collectDeviceInfoForDebugging() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        LOGD(TAG, "collectDeviceInfoForDebugging: device width = " + dpWidth + " height = " + dpHeight);
    }

}