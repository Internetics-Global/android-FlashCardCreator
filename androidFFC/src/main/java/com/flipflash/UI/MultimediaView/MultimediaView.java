package com.flipflash.UI.MultimediaView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.flipflash.UI.scalablevideoview.ScalableType;
import com.flipflash.UI.scalablevideoview.ScalableVideoView;
import com.flipflash.android_ffc.MultimediaFullscreenActivity;
import com.flipflash.android_ffc.R;
import com.flipflash.event.DownloadCancelEvent;
import com.flipflash.event.MultiMediaFullscreenEvent;
import com.flipflash.helper.FileOperationHelper;
import com.flipflash.util.Global;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by internetics on 24/10/2016.
 */



public class MultimediaView extends FrameLayout {

    private static final String TAG = MultimediaView.class.getSimpleName();

    /*
     * Support both static and gif image view
     */
    private SimpleDraweeView mGifImageView;

    private FrameLayout      mGifHolderViewFrameLayout;
    private ImageButton      mGifButton;
    private ImageButton      mGifFullscreenButton;

    private ScalableVideoView mVideoView;
    private FrameLayout      mVideoHolderViewFrameLayout;
    private ImageView        mVideoThumbNail;  // this is quite different with iOS counterpart since Android does not support thumbnail
    private ImageButton      mVideoButton;
    private ImageButton      mVideoFullscreenButton;

    private FrameLayout      mVideoControlBarFrameLayout;
    private FrameLayout      mGifControlBarFrameLayout;

    /*
     * the only usage is for mVideoFullscreenButtonClicked
     */
    private String           mVideoUrlPath;

    /*
     * the only usage is for mGifFullscreenButtonClicked
     */
    private String           mGifUriPath;

    private Context          mActivity;

    public MultimediaView(Context context) {
        super(context);
        mActivity = context;
        setup();
    }

    public MultimediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = context;
        setup();
    }

    public MultimediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivity = context;
        setup();
    }


    /*
     * This is for non-gif image and small size loading.
     */
    public void setStaticImageURI(Uri uri) {

        setMultimediaType(FFCMultimediaType.ImageView);

        boolean exist = FileOperationHelper.checkFileExist(uri);
        Uri newUri;
        if (exist == false) {
            String placeholderStr = FileOperationHelper.getQuestionImagePlaceholderImagePath();
            newUri = Uri.parse(placeholderStr);
        } else {
            newUri = Uri.parse(uri.toString());
        }

        mGifImageView.setImageURI(newUri);
    }



    /*
     * Different with setStaticImageURI
     *
     * This is for gif image loading or large  size static image loading. The background is we need to take screenshot after image is full loaded
     *
     * In order to reduce memory usage as much as possible, we introduce resizing http://frescolib.org/docs/resizing-rotating.html
     * Resize is only for jpeg, while our format is png and gif
     * Down sampling supports jpeg, png, webp, but not gif. (when using down sampling, resizing is required)
     *
     * Highlighted: the view must be layout before calling this method.Otherwise, width/height will be zero
     */
    public void setAnimitableImage( Uri uri,boolean isGif,final OnFrescoImageViewLoadCompletionListener completionListener) {

        setMultimediaType(FFCMultimediaType.ImageView);

        boolean exist = FileOperationHelper.checkFileExist(uri);
        Uri newUri;
        if (exist == false) {
            String placeholderStr = FileOperationHelper.getQuestionImagePlaceholderImagePath();
            newUri = Uri.parse(placeholderStr);
        } else {
            newUri = Uri.parse(uri.toString());
        }



        if (isGif) {
            mGifUriPath = newUri.toString();
        } else {
            mGifUriPath = "";
        }

        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {
                if (anim != null) {
                    //mean it's gif
                    showGifControl();

                    if (completionListener != null) {
                        completionListener.gifLoadSucceeded(MultimediaView.this);
                    }

                } else {
                    hideGifControl();

                    if (completionListener != null) {
                        completionListener.nonGifLoadSucceeded(MultimediaView.this);
                    }
                }


            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                super.onFailure(id, throwable);

                if (completionListener != null) {
                    completionListener.failed(MultimediaView.this);
                }
            }
        };

        ResizeOptions resizeOptions = new ResizeOptions(getWidth(),getHeight());
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(newUri)
                .setResizeOptions(resizeOptions)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(mGifImageView.getController())
                .setImageRequest(request)
                .setControllerListener(controllerListener)
//                .setAutoPlayAnimations(isGif)
                .build();
        mGifImageView.getHierarchy().setProgressBarImage(new ProgressBarDrawable());

        if (isGif) {
            mGifImageView.getHierarchy().setProgressBarImage(new ProgressBarDrawable());
        }

        mGifImageView.setController(controller);

    }


    private void setup() {

        if ((mActivity instanceof Activity) == false) {
            throw new RuntimeException("mContext should be an instance of Activity");
        }

        LayoutInflater.from(getContext()).inflate(R.layout.multimedia_view,this,true);

        {
            mGifButton = (ImageButton) findViewById(R.id.gif_button);
            mGifHolderViewFrameLayout = (FrameLayout) findViewById(R.id.gif_holder_view_fl);
            mGifImageView = (SimpleDraweeView) findViewById(R.id.gif_imageview);
            mGifFullscreenButton = (ImageButton) findViewById(R.id.gif_fullscreen_button);

            mGifButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    gifButtonClicked();
                }
            });
            mGifFullscreenButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mGifFullscreenButtonClicked();
                }
            });

            mGifControlBarFrameLayout = (FrameLayout) findViewById(R.id.gif_control_bar_fl);

        }


        {
            mVideoButton = (ImageButton) findViewById(R.id.video_button);
            mVideoHolderViewFrameLayout = (FrameLayout) findViewById(R.id.video_holder_view_fl);
            mVideoView = (ScalableVideoView) findViewById(R.id.videoView);
            mVideoView.setBackgroundColor(Color.GREEN);
            mVideoFullscreenButton = (ImageButton) findViewById(R.id.video_fullscreen_button);

            mVideoButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    videoButtonClicked();
                }
            });

            mVideoFullscreenButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    videoFullscreenButtonClicked();
                }
            });


            mVideoThumbNail = (ImageView) findViewById(R.id.video_thumbnail_imageview);

            mVideoControlBarFrameLayout = (FrameLayout) findViewById(R.id.video_control_bar_fl);
        }

    }

    private void mGifFullscreenButtonClicked() {

        Intent intent = new Intent(mActivity, MultimediaFullscreenActivity.class);
        intent.putExtra("gifPath", mGifUriPath);
        mActivity.startActivity(intent);

        EventBus.getDefault().post(new MultiMediaFullscreenEvent()); //not allow to show pack list after back
    }

    private void videoFullscreenButtonClicked() {

        String videoPath = FileOperationHelper.deleteUriSchemeHeader(mVideoUrlPath);
        Intent intent = new Intent(mActivity, MultimediaFullscreenActivity.class);
        intent.putExtra("videoPath", videoPath);
        mActivity.startActivity(intent);

        EventBus.getDefault().post(new MultiMediaFullscreenEvent()); // not allow to show pack list after back

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
                mVideoUrlPath = videoUriPath;
                mVideoView.setDataSource(mActivity,Uri.parse(videoUriPath));
                mVideoView.setScalableType(ScalableType.FIT_CENTER);
                mVideoView.setVolume(0, 0);
                mVideoView.setLooping(true);
                mVideoView.prepare(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            mVideoThumbNail.setImageURI(Uri.parse(videoThumbnailUriPath));

            mVideoControlBarFrameLayout.setVisibility(VISIBLE);
            mVideoView.setVisibility(INVISIBLE);
            mVideoThumbNail.setVisibility(VISIBLE);

        } else {
            mVideoControlBarFrameLayout.setVisibility(INVISIBLE);
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


    /*
     * This is quite different with iOS counterpart because of performance limit on Android
     */
    public void showGifControl() {

        mGifControlBarFrameLayout.setVisibility(View.VISIBLE);
    }

    /*
     * This is quite different with iOS counterpart because of performance limit on Android
     */
    public void hideGifControl() {

        mGifControlBarFrameLayout.setVisibility(View.INVISIBLE);
    }



}
