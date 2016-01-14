package com.flipflash.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-7-31
 * Time: 上午7:27
 * To change this template use File | Settings | File Templates.
 */
public class FCCImageView extends ImageView {

    private static final String TAG = FCCImageView.class.getSimpleName();

    public FCCImageView(Context context) {
        super(context);
    }


    public FCCImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FCCImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int finalHeight = (int) Math.round(widthSize/1.45);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }
}
