package com.flipflash.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.flipflash.UI.autoscrollviewpager.AutoScrollViewPager;
import com.flipflash.android_ffc.R;
import com.flipflash.fragment.CardDetailFragment;
import com.flipflash.util.Global;

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

    /*
     *核心是必须时刻保持对current CardDetailFragment保持引用
     */
    public WeakReference<CardDetailFragment> mCardDetailFragmentWeakReference;

    private static final String TAG = VGViewPager.class.getName();

    protected OnViewPagerClickListener mOnViewPagerItemClickListener;

    public VGViewPager(Context context) {
        super(context);
    }

    public VGViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /*
     * 作用：决定touch event是否可以向下传递：传递到sub view中
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        LOGD(TAG, "onInterceptTouchEvent: " + event.toString());


        super.onInterceptTouchEvent(event);
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        float hitXInScreen =  event.getX() + location[0];
        float hitYInScreen =  event.getY() + location[1];


        //由于ViewPager包含多个card，而通过findViewById会只获取到第一个，这样就会出现问题（比如当前显示第二个卡片，但是这里就会获取到第一个）
        ImageView logo_image = (ImageView)findViewWithTag(Global.mLogoImage_Showing);
        if ((logo_image != null) && isViewContains(logo_image,hitXInScreen,hitYInScreen)) {
            LOGD(TAG, "onInterceptTouchEvent: touch location in logo_image");
            return false;
        }

        //当image包含视频时，isEnabled() = true
        ImageView image = (ImageView)findViewWithTag(Global.mImage_Showing);
        if ((image != null) && (image.getVisibility() == VISIBLE) && (image.isEnabled() == true)) {


            CardDetailFragment cardDetailFragment = mCardDetailFragmentWeakReference.get();
            boolean isYoutube = false;

            if (cardDetailFragment!=null) {
                if (cardDetailFragment.mIsQuestionShowing) {
                    if (cardDetailFragment.mCurrentCard.question.movieUriFormatStr.toLowerCase().contains("youtube")) {
                        isYoutube = true;
                    }
                } else {
                    if (cardDetailFragment.mCurrentCard.answer.movieUriFormatStr.toLowerCase().contains("youtube")) {
                        isYoutube = true;
                    }
                }
            }

            if (isYoutube) {

                if (isYoutbeIconContains(image, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: " + "touch location in youtube image，enable=  "+bool);
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

        //当image包含视频时，isEnabled() = true
        ImageView image2 = (ImageView)findViewWithTag(Global.mImage2_Showing);
        if ((image2 != null) && (image2.getVisibility() == VISIBLE) && (image2.isEnabled() == true)) {

            CardDetailFragment cardDetailFragment = mCardDetailFragmentWeakReference.get();
            boolean isYoutube = false;

            if (cardDetailFragment != null) {
                if (cardDetailFragment.mIsQuestionShowing) {
                    if (cardDetailFragment.mCurrentCard.question.movieUriFormatStr2.toLowerCase().contains("youtube")) {
                        isYoutube = true;
                    }
                } else {
                    if (cardDetailFragment.mCurrentCard.answer.movieUriFormatStr2.toLowerCase().contains("youtube")) {
                        isYoutube = true;
                    }
                }
            }

            if (isYoutube) {
                if (isYoutbeIconContains(image2, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image2.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: "+ "touch location in image2，enable=  "+bool);
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

    /*
      rx, ry都是先对于屏幕的坐标
     */
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


    /*
      youtube的icon只是 imageview的1/5，我们希望touch area只作用在icon上。
      0.4的值来源于UIHelper
     */
    private boolean isYoutbeIconContains(View view, float rx, float ry) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = Math.abs(l[0]);
        int y = l[1];
        int w = view.getWidth();
        int h = view.getHeight();

        if (rx < x + 0.4*w || rx > x + w - 0.4*w || ry < y + h*0.4 || ry > y + h - h*0.4) {
            return false;
        }
        return true;
    }

    private boolean isSwipeAction = false;
    private static int swipeActionCount = 0;


    /*
     * 作用：决定touch event是否可以向上传递：parent view
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        super.onTouchEvent(ev);  //从源代码可以看到，这个必不可少，而实践中也发现如果少了这个，就无法滑动了。

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
