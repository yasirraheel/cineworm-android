package com.cineworm.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TouchEventInterceptorLayout extends FrameLayout {

    public TouchEventInterceptorLayout(@NonNull Context context) {
        super(context);
    }

    public TouchEventInterceptorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchEventInterceptorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        requestDisallowInterceptTouchEvent(true);
        return handled;
    }
}
