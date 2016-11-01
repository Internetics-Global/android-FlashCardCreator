package com.flipflash.UI.MultimediaView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import com.flipflash.UI.scalablevideoview.ScalableType;
import com.flipflash.UI.scalablevideoview.ScalableVideoView;
import com.flipflash.android_ffc.R;

import java.io.IOException;

/**
 * Created by internetics on 24/10/2016.
 */



public class MultimediaView extends FrameLayout {

    private static final String TAG = MultimediaView.class.getSimpleName();

    private SimpleDraweeView mGifImageView;
    private FrameLayout      mGifHolderViewFrameLayout;
    private ImageButton      mGifButton;

    private ScalableVideoView mVideoView;
    private FrameLayout      mVideoHolderViewFrameLayout;
    private ImageButton      mVideoButton;
    private ImageView        mVideoThumbNail;

    private Context          mContext;

    public MultimediaView(Context context) {
        super(context);
        mContext = context;
        setup();
    }

    public MultimediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setup();
    }

    public MultimediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setup();
    }


    public SimpleDraweeView getGifImageView() {
        return mGifImageView;
    }


    private void setup() {

        LayoutInflater.from(getContext()).inflate(R.layout.multimedia_view,this,true);

        {
            mGifButton = (ImageButton) findViewById(R.id.gif_button);
            mGifHolderViewFrameLayout = (FrameLayout) findViewById(R.id.gif_holder_view_fl);
            mGifImageView = (SimpleDraweeView) findViewById(R.id.gif_imageview);

            mGifButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    gifButtonClicked();
                }
            });

        }


        {
            mVideoButton = (ImageButton) findViewById(R.id.video_button);
            mVideoHolderViewFrameLayout = (FrameLayout) findViewById(R.id.video_holder_view_fl);
            mVideoView = (ScalableVideoView) findViewById(R.id.videoView);
            mVideoView.setBackgroundColor(Color.GREEN);

            mVideoButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    videoButtonClicked();
                }
            });


            mVideoThumbNail = (ImageView) findViewById(R.id.video_thumbnail_imageview);
        }

    }

    private void videoButtonClicked() {

        if (isPlayingVideo()) {
            pauseVideo();
        } else {
            playVideo();
        }


    }

    private void gifButtonClicked() {

        if (isPlayingGif()) {
            pauseGif();
        } else {
            playGif();
        }


    }

    public void setMultimediaType(FFCMultimediaType multimediaType) {

        switch (multimediaType) {
            case ImageView: {
                if (mGifHolderViewFrameLayout.getVisibility() != VISIBLE) {
                    mVideoHolderViewFrameLayout.setVisibility(GONE);
                    mGifHolderViewFrameLayout.setVisibility(VISIBLE);
                }
                break;
            }
            case Video: {
                if (mVideoHolderViewFrameLayout.getVisibility() != VISIBLE) {
                    mVideoHolderViewFrameLayout.setVisibility(VISIBLE);
                    mGifHolderViewFrameLayout.setVisibility(GONE);
                }
                break;
            }

            case YoutubeVideo: {
                break;
            }

            default: {
                break;
            }
        }



    }

    public void clean() {

        mGifHolderViewFrameLayout.setVisibility(View.GONE);

        mVideoHolderViewFrameLayout.setVisibility(View.GONE);

    }


    public void setVideoUriPath (@NonNull  String videoUriPath,@NonNull  String videoThumbnailUriPath) {

        if (mVideoView != null) {
            try {

                mVideoView.release();

                mVideoView.setDataSource(mContext,Uri.parse(videoUriPath));
                mVideoView.setScalableType(ScalableType.FIT_CENTER);
                mVideoView.setVolume(0, 0);
                mVideoView.prepare(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            mVideoThumbNail.setImageURI(Uri.parse(videoThumbnailUriPath));

            mVideoButton.setVisibility(VISIBLE);
            mVideoView.setVisibility(INVISIBLE);
            mVideoThumbNail.setVisibility(VISIBLE);

        } else {
            mVideoButton.setVisibility(INVISIBLE);
        }


    }

    private boolean isPlayingVideo() {

        if (mVideoHolderViewFrameLayout.getVisibility() == VISIBLE) {

            if (mVideoView != null && mVideoView.isPlaying()) {
                return true;
            }

        }

        return false;
    }

    public void pauseVideo() {

        if (mVideoHolderViewFrameLayout.getVisibility() == VISIBLE) {

            if (mVideoView != null && mVideoView.isPlaying()) {
                mVideoView.pause();
                mVideoButton.setImageResource(R.drawable.play_button);
            }

        }

    }

    public void stopVideo() {

        if (mVideoHolderViewFrameLayout.getVisibility() == VISIBLE) {

            if (mVideoView != null && mVideoView.isPlaying()) {
                mVideoView.stop();
                mVideoButton.setImageResource(R.drawable.play_button);

                mVideoView.setVisibility(INVISIBLE);
                mVideoThumbNail.setVisibility(VISIBLE);
            }

        }

    }


    public void playVideo() {

        if (mVideoHolderViewFrameLayout.getVisibility() == VISIBLE) {

            if (mVideoView != null && mVideoView.isPlaying() == false) {
                mVideoView.start();
                mVideoButton.setImageResource(R.drawable.pause_button);

                mVideoView.setVisibility(VISIBLE);
                mVideoThumbNail.setVisibility(INVISIBLE);
            }

        }


    }

    public void pauseGif() {

        if (mGifHolderViewFrameLayout.getVisibility() == VISIBLE && mGifImageView != null && mGifImageView.getController() != null) {

            Animatable animatable = mGifImageView.getController().getAnimatable();
            if (animatable != null && animatable.isRunning()) {
                animatable.stop();
                mGifButton.setImageResource(R.drawable.play_button);
            }

        }

    }

    public void stopGif() {

        pauseGif();

    }



    private boolean isPlayingGif() {

        if (mGifHolderViewFrameLayout.getVisibility() == VISIBLE && mGifImageView != null && mGifImageView.getController() != null) {

            Animatable animatable = mGifImageView.getController().getAnimatable();
            if (animatable != null && animatable.isRunning()) {
                return true;
            }

        }

        return false;

    }

    public void playGif() {

        if (mGifHolderViewFrameLayout.getVisibility() == VISIBLE && mGifImageView != null) {

            if (isPlayingGif() == false) {
                Animatable animatable = mGifImageView.getController().getAnimatable();
                if (animatable != null) {
                    animatable.start();
                    mGifButton.setImageResource(R.drawable.pause_button);
                }
            }

        }



    }


    public void showGifControl() {

        mGifButton.setVisibility(View.VISIBLE);
    }

    public void hideGifControl() {

        mGifButton.setVisibility(View.INVISIBLE);
    }



}
