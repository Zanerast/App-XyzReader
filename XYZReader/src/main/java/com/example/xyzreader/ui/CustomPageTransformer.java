package com.example.xyzreader.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
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
        RecyclerView articleBody = (RecyclerView) view.findViewById(R.id.rv_article_body);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);

        if (position <= -1) {
            articleBody.setAlpha(0);
        } else if (position <= 1) {
            view.findViewById(R.id.iv_photo).setTranslationX(-position * view.getWidth() / 2);
            fab.setRotation(360*position*2);

            if (position < 0) { // animate the exiting page
                articleBody.setAlpha(1+position);
            } else { // animate the entering page
                articleBody.setAlpha(1-position);
            }



        } else {
            articleBody.setAlpha(0);
        }

    }
}
