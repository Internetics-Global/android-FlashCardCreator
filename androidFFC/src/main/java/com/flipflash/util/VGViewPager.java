package com.flipflash.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.flipflash.UI.autoscrollviewpager.AutoScrollViewPager;
import com.flipflash.android_ffc.R;

import static com.flipflash.util.LogUtils.LOGD;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-8-15
 * Time: 下午3:30
 * To change this template use File | Settings | File Templates.
 */
public class VGViewPager extends AutoScrollViewPager {

    private static final String TAG = VGViewPager.class.getName();

    private boolean isDisableTouchEvent = false;

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
        if (isDisableTouchEvent) {
            return true; //防止传递给sub view
        } else {
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

            ImageView image = (ImageView)findViewWithTag(Global.mImage_Showing);
            if ((image != null) && (image.getVisibility() == VISIBLE) && (image.isEnabled() == true)) {
                if (isViewContains(image, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: " + "touch location in image，enable=  "+bool);
                    return false;
                }
            }

            ImageView image2 = (ImageView)findViewWithTag(Global.mImage2_Showing);
            if ((image2 != null) && (image2.getVisibility() == VISIBLE) && (image2.isEnabled() == true)) {
                if (isViewContains(image2, hitXInScreen, hitYInScreen)) {
                    Boolean bool = image2.isEnabled();
                    LOGD(TAG, "onInterceptTouchEvent: "+ "touch location in image2，enable=  "+bool);
                    return false;
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

    private boolean isSwipeAction = false;
    private static int swipeActionCount = 0;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        if (isDisableTouchEvent) {
            LOGD(TAG, "onTouchEvent: returned because of isDisableTouchEvent = YES");
            return false;  //防止传递给手势处理
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LOGD(TAG, "onTouchEvent: ACTION_DOWN");
                isSwipeAction = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                LOGD(TAG, "onTouchEvent: ACTION_MOVE");
                swipeActionCount++; //check how long the button is pressed
                if(swipeActionCount> 5){
                    isSwipeAction = true;
                }
                requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
                LOGD(TAG, "onTouchEvent: ACTION_UP");
                requestDisallowInterceptTouchEvent(false);
                if (isSwipeAction == false && swipeActionCount <3) {
                    if (mOnViewPagerItemClickListener != null) {
                        mOnViewPagerItemClickListener.OnViewPagerClickListener();
                    }
                }
                swipeActionCount = 0;
                isSwipeAction = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                LOGD(TAG, "onTouchEvent: ACTION_CANCEL");
                requestDisallowInterceptTouchEvent(false);
                isSwipeAction =false;
                break;
            default:
                LOGD(TAG, "onTouchEvent: other action");
                break;
        }

        return false;

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


    public void disableAllTouchEvent(boolean isDisableTouchEvent) {
        LOGD(TAG, "disableAllTouchEvent");
        this.isDisableTouchEvent = isDisableTouchEvent;
    }

}
