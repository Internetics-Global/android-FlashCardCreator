package com.flipflash.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

import static com.flipflash.util.LogUtils.LOGD;

/**
 * Created by BourneWang on 9/12/2015.
 */
public class FFCRatioFrameLayout extends RatioFrameLayout {

    private static final String TAG = VGViewPager.class.getName();

    private boolean isDisableTouchEvent = false;

    public FFCRatioFrameLayout(Context context) {
        super(context);
    }

    public FFCRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FFCRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FFCRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //为了避免hide/show control panel被不小心除触发（mBaseView.setOnClickListener）
        return true;
    }

    /*
     * isDisableTouchEvent不允许事件往下传递（在本项目中主要是view pager)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isDisableTouchEvent) {
            return true;
        } else {
            return false;
        }
    }


    public void disableAllTouchEvent(boolean isDisableTouchEvent) {
        LOGD(TAG, "disableAllTouchEvent");
        this.isDisableTouchEvent = isDisableTouchEvent;
    }
}
