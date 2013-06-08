package com.internectics.android_flashcardcreator;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class AboutActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.more_about);
        setContentView(R.layout.about);
    }


}