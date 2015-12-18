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
import com.flipflash.util.Global;

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

        //cardDPHeightInEditMode = screen height -  actionbarHeight -  card top margin - card bottom margin -segment height - segment bottom margin
        float cardDPHeightInEditMode = dpHeight - 40 - 10 - 10 - 24 - 10;
        float cardDPHeightInPlayMode = dpHeight - 10 - 10;
        float ratio = cardDPHeightInPlayMode/cardDPHeightInEditMode;
        if (ratio >Global.scaleInPlayMode) {
            Global.scaleInPlayMode = ratio;  //默认值是1.2，如果比这个数值更大，才进行赋值。这种情况只有在小屏手机中才有这个问题
        }



        LOGD(TAG, "collectDeviceInfoForDebugging: device width = " + dpWidth + " height = " + dpHeight);

        float density = getResources().getDisplayMetrics().density;
        LOGD(TAG, "desnity * dp = px, here density = " + density);
    }

}