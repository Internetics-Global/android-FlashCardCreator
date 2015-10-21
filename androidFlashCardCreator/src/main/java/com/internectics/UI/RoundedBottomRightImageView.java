package com.internectics.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.internectics.android_flashcardcreator.R;

/**
 * Created by bournewang on 5/30/14.
 */
public class RoundedBottomRightImageView extends ImageView{

    private static final String TAG = RoundedBottomRightImageView.class.getName();

    public static float radius = 250;//default value


    public RoundedBottomRightImageView(Context context) {
        super(context);
        radius = getResources().getDimensionPixelSize(R.dimen.card_round_corner);
    }

    public RoundedBottomRightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        radius = getResources().getDimensionPixelSize(R.dimen.card_round_corner);
    }

    public RoundedBottomRightImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        radius = getResources().getDimensionPixelSize(R.dimen.card_round_corner);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        Path path = new Path ();
        path.moveTo (0, 0);
        path.lineTo(this.getWidth(), 0);
        path.lineTo (this.getWidth(), this.getHeight() - radius);
        path.arcTo(new RectF(this.getWidth()-2*radius,this.getHeight()-2*radius,this.getWidth(),this.getHeight()),0,90);
        path.lineTo (0, this.getHeight());
        path.lineTo (0, 0);
        clipPath.addPath(path);
        canvas.clipPath(clipPath); //依赖于硬件加速，在4.3上，硬件加速开启下是支持的；是如果4.3以下，硬件加速开启下，则失去这个效果
        super.onDraw(canvas);
    }
}
