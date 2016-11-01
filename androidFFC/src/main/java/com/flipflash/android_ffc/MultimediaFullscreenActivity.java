package com.flipflash.android_ffc;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;


/**
 * Created by bournewang on 4/9/14.
 */
public class MultimediaFullscreenActivity extends Activity {
    private static final String TAG = MultimediaFullscreenActivity.class.getSimpleName();

    private VideoView         mVideoView;
    private SimpleDraweeView  mGifImageView;

    private String            mVideoPath;
    private String            mGifUriPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_play);

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mGifImageView = (SimpleDraweeView) findViewById(R.id.image_view);

        mVideoPath = getIntent().getStringExtra("videoPath");
        if (mVideoPath != null) {
            mVideoView.setVisibility(View.VISIBLE);
            mGifImageView.setVisibility(View.INVISIBLE);
        }

        mGifUriPath = getIntent().getStringExtra("gifPath");
        if (mGifUriPath != null) {
            mVideoView.setVisibility(View.INVISIBLE);
            mGifImageView.setVisibility(View.VISIBLE);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mVideoPath != null)  {
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.start();
        }

        if (mGifUriPath != null) {
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(mGifUriPath))
                    .setAutoPlayAnimations(true)
            .build();
            mGifImageView.setController(controller);
        }


    }


}