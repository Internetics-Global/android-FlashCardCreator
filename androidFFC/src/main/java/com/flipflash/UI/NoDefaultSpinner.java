package com.flipflash.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

import java.lang.reflect.Field;
/**
 * Default spinner is not allowed to select an selected item again
 * http://stackoverflow.com/questions/8937150/android-spinner-fire-event-when-same-item-selection-is-made
 */
public class NoDefaultSpinner extends Spinner {

    public NoDefaultSpinner(Context context) {
        super(context);
    }

    public NoDefaultSpinner(Context context, int mode) {
        super(context, mode);
    }

    public NoDefaultSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoDefaultSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NoDefaultSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }



    @Override
    public void setSelection(int position, boolean animate) {
        ignoreOldSelectionByReflection();
        super.setSelection(position, animate);
    }

    private void ignoreOldSelectionByReflection() {
        try {
            Class<?> c = this.getClass().getSuperclass().getSuperclass().getSuperclass();
            Field reqField = c.getDeclaredField("mOldSelectedPosition");
            reqField.setAccessible(true);
            reqField.setInt(this, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSelection(int position) {
        ignoreOldSelectionByReflection();
        super.setSelection(position);
    }
}
