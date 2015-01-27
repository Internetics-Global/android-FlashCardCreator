package com.internectics.util;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import com.internectics.android_flashcardcreator.R;
import android.util.Log;
import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-8-15
 * Time: 下午3:30
 * To change this template use File | Settings | File Templates.
 */
public class VGViewPager extends ViewPager {

    public VGViewPager(Context context) {
        super(context);
        setMyScroller();
    }

    public VGViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMyScroller();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        super.onInterceptTouchEvent(event);

        int[] location = new int[2];

        this.getLocationOnScreen(location);
        float hitXInScreen =  event.getX() + location[0];
        float hitYInScreen =  event.getY() + location[1];


        //由于ViewPager包含多个card，而通过findViewById会只获取到第一个，这样就会出现问题（比如当前显示第二个卡片，但是这里就会获取到第一个）
        ImageView logo_image = (ImageView)findViewWithTag(Global.mLogoImage_Showing);
        if ((logo_image != null) && isViewContains(logo_image,hitXInScreen,hitYInScreen)) {
            Timber.d(Global.debugTag, "touch location in logo_image");
            return false;
        }

        ImageView image = (ImageView)findViewWithTag(Global.mImage_Showing);
        if ((image != null) && (image.getVisibility() == VISIBLE)) {
            if (isViewContains(image,hitXInScreen,hitYInScreen)) {
                Boolean bool = image.isEnabled();
                Timber.d(Global.debugTag, "touch location in image，enable=  "+bool);
                return false;
            }
        }

        ImageView image2 = (ImageView)findViewWithTag(Global.mImage2_Showing);
        if ((image2 != null) && (image2.getVisibility() == VISIBLE)) {
            if (isViewContains(image2,hitXInScreen,hitYInScreen)) {
                Boolean bool = image2.isEnabled();
                Timber.d(Global.debugTag, "touch location in image2，enable=  "+bool);
                return false;
            }
        }


        LinearLayout creatorLayout = (LinearLayout) findViewById(R.id.creator_layout);
        if (isViewContains(creatorLayout,hitXInScreen,hitYInScreen))
        {
            Timber.d(Global.debugTag,"touch location in creatorLayout");
            return false;
        }
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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        //Timber.d(Global.debugTag, "onTouchEvent for VGViewPager");
        return false;
    }


    private void setMyScroller()
    {
        try
        {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public class MyScroller extends Scroller
    {
        public MyScroller(Context context)
        {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration)
        {
            super.startScroll(startX, startY, dx, dy, 500 /*1 secs*/);
        }
    }

}
