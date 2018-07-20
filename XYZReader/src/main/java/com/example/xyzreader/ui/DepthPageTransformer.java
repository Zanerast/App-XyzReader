package com.example.xyzreader.ui;

import android.support.v4.view.ViewPager;
import android.view.View;
import com.example.xyzreader.R;


public class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {

        if (position <= -1.0F || position >= 1.0F) {
        } else if (position <= 1.0f) {
            view.findViewById(R.id.iv_photo).setTranslationX(-position * view.getWidth() / 2);

        }

    }
}
