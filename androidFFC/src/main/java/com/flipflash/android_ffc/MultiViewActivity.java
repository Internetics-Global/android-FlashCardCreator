package com.flipflash.android_ffc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import com.facebook.drawee.view.SimpleDraweeView;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;


/**
 * Created by bournewang on 4/9/14.
 */
public class MultiViewActivity extends Activity {
    private static final String TAG = MultiViewActivity.class.getSimpleName();

    private VideoView         mVideoView;
    private SimpleDraweeView  mImageView;

    private String            mVideoPath;
    private String            mImagePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_play);

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mImageView = (SimpleDraweeView) findViewById(R.id.image_view);

        mVideoPath = getIntent().getStringExtra("videoPath");
        if (mVideoPath != null) {
            mVideoView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
        }

        mImagePath = getIntent().getStringExtra("imagePath");
        if (mImagePath != null) {
            mVideoView.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.VISIBLE);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();



        if (mVideoView != null)  {
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.start();
        }

        if (mImagePath != null) {

        }


    }
}