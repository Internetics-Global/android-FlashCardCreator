package com.flipflash.android_ffc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.flipflash.util.AppContext;

public class AppStart extends Activity {
    private static final String TAG = AppStart.class.getName();

    long startTime;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppContext)(AppContext.getAppContext())).addActivity(this);

        setContentView(R.layout.start);

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

}