package com.internectics.util;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Scroller;
import com.internectics.android_flashcardcreator.R;

import java.lang.reflect.Field;

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
        //Log.d(Global.debugTag, "onInterceptTouchEvent for VGViewPager");

        ImageView logo_image = (ImageView)findViewById(R.id.logo_image);
        Rect outRect = new Rect();
        logo_image.getHitRect(outRect);
        if (outRect.contains((int) event.getX(), (int) event.getY())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        //Log.d(Global.debugTag, "onTouchEvent for VGViewPager");
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
