package com.flipflash.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import static com.flipflash.util.LogUtils.LOGE;

public class FCCEditText extends EditText {

    private static final String TAG = FCCEditText.class.getName();

    public OnKeyboardCloseListener mCallbacks;

    public FCCEditText(Context context) {
        super(context);
    }

    public FCCEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FCCEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setCallbacks(OnKeyboardCloseListener callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            if (mCallbacks != null)

                mCallbacks.onKeyboardClose(this);
            else
                LOGE(TAG, "onKeyPreIme: mCallbacks for FCCEditText is null");
        }
        return super.onKeyPreIme(keyCode, event);

    }

    public interface OnKeyboardCloseListener {
        public void onKeyboardClose(EditText editText);

    }
}
