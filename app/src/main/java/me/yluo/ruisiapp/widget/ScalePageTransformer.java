package me.yluo.ruisiapp.widget;


import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class ScalePageTransformer implements ViewPager.PageTransformer {
    private static final float MAX_SCALE = 1.15f;
    private static final float MIN_SCALE = 0.85f;

    @Override
    public void transformPage(View page, float position) {
        if (position < -1) {
            position = -1;
        } else if (position > 1) {
            position = 1;
        }
        float tempScale = position < 0 ? 1 + position : 1 - position;
        float slope = (MAX_SCALE - MIN_SCALE) / 1;
        //一个公式
        float scaleValue = MIN_SCALE + tempScale * slope;
        page.setScaleX(scaleValue);
        page.setScaleY(scaleValue);
    }
}