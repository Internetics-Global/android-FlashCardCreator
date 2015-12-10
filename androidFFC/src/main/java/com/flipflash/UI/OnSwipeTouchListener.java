package com.flipflash.UI;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

/**
 * Created by BourneWang on 10/12/2015.
 */
public class OnSwipeTouchListener implements OnTouchListener {

    private static final String TAG = OnSwipeTouchListener.class.getName();

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeLeft() {
        LOGD(TAG, "onSwipeLeft");
    }

    public void onSwipeRight() {

        LOGD(TAG, "onSwipeRight: ");
    }

    public void onSwipeTop() {

        LOGD(TAG, "onSwipeTop");
    }

    public void onSwipeBottom() {

        LOGD(TAG, "onSwipeBottom");
    }

    public void onSimpleTapConfirmed() {

        LOGD(TAG, "onSimpleTapConfirmed: ");

    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);  //这是重点
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 30;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onSimpleTapConfirmed();

            return super.onSingleTapConfirmed(e);
        }

        // //这个方法只会执行一次，不用担心会执行多次
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                LOGD(TAG, "onFling: diffX = " + diffX + " diffY = " + diffY + " velocityX = " + velocityX + " velocityY = " + velocityY);

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0)
                            onSwipeRight();
                        else
                            onSwipeLeft();
                        result = false; //这个必须返回false,否则view page滑动过程就会中断有问题
                    } else {
                        return false;
                    }
                }
                else if (Math.abs(diffY) > Math.abs(diffX)) {

                    if (Math.abs(diffY) > Math.abs(diffX) && Math.abs(diffY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0)
                            onSwipeBottom();
                        else
                            onSwipeTop();
                        result = false; //这个必须返回false,否则view page滑动过程就会中断有问题
                    } else {
                        return false;
                    }
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}
