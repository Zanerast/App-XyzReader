package com.example.xyzreader.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;

import com.example.xyzreader.R;

import timber.log.Timber;


public class CustomPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.share_fab);

        if (position <= -1.0F || position >= 1.0F) {
//            view.findViewById(R.id.rv_article_body).setAlpha(0.0F);
        } else if (position <= 1.0f) {
            view.findViewById(R.id.iv_photo).setTranslationX(-position * view.getWidth() / 2);
//                view.findViewById(R.id.cardview).setTranslationX(pageWidth * -position);


//            ViewCompat.animate(fab).
//                    rotation(135f).
//                    withLayer().
//                    setDuration(300L).
//                    setInterpolator(new OvershootInterpolator()).
//                    start();
        } else {
//            AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
//            animation1.setDuration(1000);
//            animation1.setStartOffset(2000);
//            animation1.setFillAfter(true);
//            view.findViewById(R.id.rv_article_body).startAnimation(animation1);
        }

//            // Fade the page out.
//
//
//            // Counteract the default slide transition
//
//
//            // Scale the page down (between MIN_SCALE and 1)
//            float scaleFactor = MIN_SCALE
//                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
//            view.setScaleX(scaleFactor);
//            view.setScaleY(scaleFactor);

    }
}
