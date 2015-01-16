package com.internectics.android_flashcardcreator;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

public class AboutActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.more_about);
        setContentView(R.layout.about);

        TextView versionTextView = (TextView) findViewById(R.id.version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionTextView.setText("Version " + versionName + "\n\n Build:" + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


}