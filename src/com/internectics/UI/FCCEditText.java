package com.internectics.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import com.internectics.util.Global;

public class FCCEditText extends EditText {

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

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            if (mCallbacks != null)

                mCallbacks.onKeyboardClose(this);
            else
                Log.w(Global.debugTag, "mCallbacks for FCCEditText is null");
        }
        return super.onKeyPreIme(keyCode, event);

    }

    public interface OnKeyboardCloseListener {
        public void onKeyboardClose(EditText editText);

    }
}
