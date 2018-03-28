package com.flipflash.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.flipflash.UI.MultimediaView.MultimediaView;
import com.flipflash.UI.autoscrollviewpager.AutoScrollViewPager;
import com.flipflash.android_ffc.R;
import com.flipflash.fragment.CardDetailFragment;
import com.flipflash.util.Global;
import com.flipflash.util.UIHelper;

import java.lang.ref.WeakReference;

import static com.flipflash.util.LogUtils.LOGD;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-8-15
 * Time: 下午3:30
 * To change this template use File | Settings | File Templates.
 */
public class VGViewPager extends AutoScrollViewPager {

    public WeakReference<CardDetailFragment> mCardDetailFragmentWeakReference;

    private static final String TAG = VGViewPager.class.getSimpleName();

    protected OnViewPagerClickListener mOnViewPagerItemClickListener;

    public VGViewPager(Context context) {
        super(context);
    }

    public VGViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        LOGD(TAG, "onInterceptTouchEvent: " + event.toString());


        super.onInterceptTouchEvent(event);
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        float hitXInScreen =  event.getX() + location[0];
        float hitYInScreen =  event.getY() + location[1];


        ImageView logo_image = (ImageView)findViewWithTag(Global.mLogoImage_Showing);
        if ((logo_image != null) && isViewContains(logo_image,hitXInScreen,hitYInScreen)) {
            LOGD(TAG, "onInterceptTouchEvent: touch location in logo_image");
            return false;
        }

        MultimediaView image = (MultimediaView)findViewWithTag(Global.mImage_Showing);
        if ((image != null) && (image.getVisibility() == VISIBLE) && (image.isEnabled() == true)) {


            CardDetailFragment cardDetailFragment = mCardDetailFragmentWeakReference.get();
            boolean isYoutube = false;
            boolean isGif = false;
            boolean isLocalVideo = false;

            if (cardDetailFragment!=null) {
                if (cardDetailFragment.mIsQuestionShowing) {
                    if (cardDetailFragment.mCurrentCard.question.movieUriFormatStr.toLowerCase().contains("youtu")) {
                        isYoutube = true;
                    }

                    if (cardDetailFragment.mCurrentCard.question.imageUriFormatStr.toLowerCase().contains(".gif")) {
                        isGif = true;
                    }

                    if (cardDetailFragment.mCurrentCard.question.movieUriFormatStr.toLowerCase().contains(".3gp")) {
                        isLocalVideo = true;
                    }

                } else {
                    if (cardDetailFragment.mCurrentCard.answer.movieUriFormatStr.toLowerCase().contains("youtu")) {
                        isYoutube = true;
                    }

                    if (cardDetailFragment.mCurrentCard.answer.imageUriFormatStr.toLowerCase().contains(".gif")) {
                        isGif = true;
                    }

                    if (cardDetailFragment.mCurrentCard.answer.movieUriFormatStr.toLowerCase().contains(".3gp")) {
                        isLocalVideo = true;
                    }
                }
            }

            if (isYoutube) {

                if (isYoutbeIconContains(image, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: " + "touch location in youtube image，enable=  "+bool);
                    return false;
                }

            } else if (isGif) {
                if (isGifControlBarContain(image, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: " + "touch location in gif/local video image，enable=  "+bool);
                    return false;
                }

            } else if (isLocalVideo) {
                if (isLocalVideoControlBarContain(image, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: " + "touch location in gif/local video image，enable=  "+bool);
                    return false;
                }

            } else {

                if (isViewContains(image, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: " + "touch location in image，enable=  "+bool);
                    return false;
                }
            }

        }

        MultimediaView image2 = (MultimediaView)findViewWithTag(Global.mImage2_Showing);
        if ((image2 != null) && (image2.getVisibility() == VISIBLE) && (image2.isEnabled() == true)) {

            CardDetailFragment cardDetailFragment = mCardDetailFragmentWeakReference.get();
            boolean isYoutube = false;
            boolean isGif = false;
            boolean isLocalVideo = false;

            if (cardDetailFragment != null) {
                if (cardDetailFragment.mIsQuestionShowing) {
                    if (cardDetailFragment.mCurrentCard.question.movieUriFormatStr2.toLowerCase().contains("youtu")) {
                        isYoutube = true;
                    }

                    if (cardDetailFragment.mCurrentCard.question.imageUriFormatStr2.toLowerCase().contains(".gif")) {
                        isGif = true;
                    }

                    if (cardDetailFragment.mCurrentCard.question.movieUriFormatStr2.toLowerCase().contains(".3gp")) {
                        isLocalVideo = true;
                    }

                } else {
                    if (cardDetailFragment.mCurrentCard.answer.movieUriFormatStr2.toLowerCase().contains("youtu")) {
                        isYoutube = true;
                    }

                    if (cardDetailFragment.mCurrentCard.answer.imageUriFormatStr2.toLowerCase().contains(".gif")) {
                        isGif = true;
                    }

                    if (cardDetailFragment.mCurrentCard.answer.movieUriFormatStr2.toLowerCase().contains(".3gp")) {
                        isLocalVideo = true;
                    }
                }
            }

            if (isYoutube) {
                if (isYoutbeIconContains(image2, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image2.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: "+ "touch location in image2，enable=  "+bool);
                    return false;
                }
            } else if (isGif) {
                if (isGifControlBarContain(image2, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image2.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: "+ "touch location in gif/local video image，enable=  "+bool);
                    return false;
                }
            } else if (isLocalVideo) {
                if (isLocalVideoControlBarContain(image2, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image2.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: "+ "touch location in gif/local video image，enable=  "+bool);
                    return false;
                }
            } else {
                if (isViewContains(image2, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image2.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: "+ "touch location in image2，enable=  "+bool);
                    return false;
                }
            }

        }


        LinearLayout creatorLayout = (LinearLayout) findViewById(R.id.creator_layout);
        if (isViewContains(creatorLayout,hitXInScreen,hitYInScreen)) {
            LOGD(TAG, "onInterceptTouchEvent: touch location in creatorLayout");
            return false;
        }

        LOGD(TAG, "onInterceptTouchEvent: finally return true");

        return true;

    }


    private boolean isViewContains(View view, float rx, float ry) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = Math.abs(l[0]);
        int y = l[1];
        int w = view.getWidth();
        int h = view.getHeight();

        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }



    private boolean isYoutbeIconContains(View view, float rx, float ry) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = Math.abs(l[0]);
        int y = l[1];
        int w = view.getWidth();
        int h = view.getHeight();

        int value55 = UIHelper.getPixels(55);
        int value70 = UIHelper.getPixels(70);

        if ((rx < x + (w - value55)/2) || (rx > x + (w + value55)/2) || (ry < y + (h - value70)/2) || (ry > y + (h + value70)/2)) {
            return false;
        }
        return true;
    }


    private boolean isGifControlBarContain(View view, float rx, float ry) {
        View bar = view.findViewById(R.id.gif_control_bar_fl);
        boolean result = isViewContains(bar,rx,ry);
        return result;
    }


    private boolean isLocalVideoControlBarContain(View view, float rx, float ry) {
        View bar = view.findViewById(R.id.video_control_bar_fl);
        boolean result = isViewContains(bar,rx,ry);
        return result;
    }

    private boolean isSwipeAction = false;
    private static int swipeActionCount = 0;



    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        super.onTouchEvent(ev);

        return true;

    }




    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public interface OnViewPagerClickListener {
        void OnViewPagerClickListener();
    }

    public void setOnViewPagerClickListener(OnViewPagerClickListener listener)
    {
        mOnViewPagerItemClickListener = listener;
    }

}
