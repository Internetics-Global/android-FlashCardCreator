package com.flipflash.android_ffc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;


import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

/**
 * Created by BourneWang on 5/12/2015.
 */
public class PlayOptionActivity extends Activity {

    Button checkManually;
    Button checkAuto;
    Button checkAutoLoop;

    private static final String TAG = PlayActivity.class.getName();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate:");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.play_option_setting);

        setTitle(getString(R.string.NavigationBarItem_More_Play_Option));


        checkManually = (Button) findViewById(R.id.rl_play_option_manually_check);
        checkAuto = (Button) findViewById(R.id.rl_play_option_auto_check);
        checkAutoLoop = (Button) findViewById(R.id.rl_play_option_auto_with_loop_check);

        int playOption = AppConfig.sharedInstance().getPlayOption();
        if (playOption == 0) {

            checkManually.setVisibility(View.VISIBLE);
            checkAuto.setVisibility(View.INVISIBLE);
            checkAutoLoop.setVisibility(View.INVISIBLE);

        } else if (playOption == 1) {

            checkManually.setVisibility(View.INVISIBLE);
            checkAuto.setVisibility(View.VISIBLE);
            checkAutoLoop.setVisibility(View.INVISIBLE);

        } else {

            checkManually.setVisibility(View.INVISIBLE);
            checkAuto.setVisibility(View.INVISIBLE);
            checkAutoLoop.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.rl_play_option_manually).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkManually.setVisibility(View.VISIBLE);
                checkAuto.setVisibility(View.INVISIBLE);
                checkAutoLoop.setVisibility(View.INVISIBLE);

                AppConfig.sharedInstance().setPlayOption(0);

            }
        });


        findViewById(R.id.rl_play_option_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkManually.setVisibility(View.INVISIBLE);
                checkAuto.setVisibility(View.VISIBLE);
                checkAutoLoop.setVisibility(View.INVISIBLE);

                AppConfig.sharedInstance().setPlayOption(1);

            }
        });

        findViewById(R.id.rl_play_option_auto_with_loop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkManually.setVisibility(View.INVISIBLE);
                checkAuto.setVisibility(View.INVISIBLE);
                checkAutoLoop.setVisibility(View.VISIBLE);

                AppConfig.sharedInstance().setPlayOption(2);

            }
        });

    }
}