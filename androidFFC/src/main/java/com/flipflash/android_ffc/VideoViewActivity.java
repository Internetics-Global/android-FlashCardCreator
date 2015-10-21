package com.flipflash.android_ffc;

import android.app.Activity;
import android.os.Bundle;
import android.widget.VideoView;


/**
 * Created by bournewang on 4/9/14.
 */
public class VideoViewActivity extends Activity {
    private static final String TAG = VideoViewActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_play);


        String videoPath = getIntent().getStringExtra("videoPath");

        VideoView videoView = (VideoView)findViewById(R.id.VideoView);
        //MediaController mediaController = new MediaController(this);
        // mediaController.setAnchorView(videoView);
        //videoView.setMediaController(mediaController);

        videoView.setVideoPath(videoPath);

        videoView.start();
    }
}