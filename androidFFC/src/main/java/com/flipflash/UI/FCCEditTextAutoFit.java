package com.flipflash.UI;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * 注意，FCCEditTextAutoFit只适合于需要根据输入自动shrink的场景，比如creator, sidebar, job  title等，
 * 不适用于main,sub, subheading。
 */
public class FCCEditTextAutoFit extends FCCEditText {

    private Paint mTestPaint;

    public FCCEditTextAutoFit(Context context) {
        super(context);
        initialise();
    }

    public FCCEditTextAutoFit(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public FCCEditTextAutoFit(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialise();
    }

    private void initialise() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
        //max size defaults to the initially specified text size unless it is too small
    }

    private void refitText(String text, int textWidth)
    {
        float DELTA = 0.2f;

        if (textWidth <= 0) {
            return;
        }

        mTestPaint.set(this.getPaint());

        float size = getTextSize();

        int   targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float measuredTextWidth = mTestPaint.measureText(text);

        while(measuredTextWidth >= targetWidth) {
            size -= DELTA;
            mTestPaint.setTextSize(size);
            measuredTextWidth = mTestPaint.measureText(text);
            Log.d("refitText", "refitText measuredTextWidth = " + measuredTextWidth + " targetWidth = " + targetWidth + " size = " + size);

            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }

        Log.d("refitText","do nothing");

    }


    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        refitText(getText().toString(), this.getWidth());
        Log.d("refitText","onLayout:" + getText() + "width = " + this.getWidth());
    }
}
