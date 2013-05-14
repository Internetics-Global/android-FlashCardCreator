package com.internectics.android_flashcardcreator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class PlayActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.play);
        getActionBar().hide();

        (findViewById(R.id.play_close_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
