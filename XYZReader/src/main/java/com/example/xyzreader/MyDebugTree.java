package com.example.xyzreader;

import com.example.xyzreader.ui.ArticleDetailFragment;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * Created by Zane on 09/07/2018.
 */

public class MyDebugTree extends Timber.DebugTree {

    @Override
    protected void log(int priority, String tag, @NotNull String message, Throwable t) {
        message = "TIMBER! " + message;
        super.log(priority, tag, message, t);
    }
}
