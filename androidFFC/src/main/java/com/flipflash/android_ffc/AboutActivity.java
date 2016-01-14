package com.flipflash.android_ffc;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.flipflash.util.AppContext;
import com.flipflash.util.Global;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AboutActivity extends Activity {
    private static final String TAG = AboutActivity.class.getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.NavigationBarItem_More_About);
        setContentView(R.layout.about);

        TextView versionTextView = (TextView) findViewById(R.id.version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionTextView.setText("Version " + versionName + "\n\n Build:" + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didClick();
            }
        });

    }

    private int tapCount = 0;
    private void didClick() {
        Log.d("ccaa","Tap down count:" + tapCount);
        if (tapCount == 4) {
            tapCount = 0;

            if (Global.IS_DOGFOOD_BUILD) {
                Global.IS_DOGFOOD_BUILD = false;

                new SweetAlertDialog(AboutActivity.this,SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Alert")
                        .setContentText("Log is disabled")
                        .show();

            } else {

                Global.IS_DOGFOOD_BUILD = true;

                new SweetAlertDialog(AboutActivity.this,SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Alert")
                        .setContentText("Log is enabled")
                        .show();
            }

        } else {
            tapCount++;
        }

    }
}