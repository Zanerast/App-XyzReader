package com.example.xyzreader.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.rv_article_body)
    RecyclerView rvBodyView;
    @BindView(R.id.iv_photo)
    ImageView ivPhotoView;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.share_fab)
    FloatingActionButton fab;
    @BindView(R.id.appbar)
    AppBarLayout appBar;
    @BindView(R.id.fragment_background)
    CoordinatorLayout background;


    public static final String ARG_ITEM_ID = "item_id";
    private static final String ARG_ITEM_POSITION = "item_position";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private int mMaxScrollSize;
    public Palette mPalette;
    private boolean mIsImageHidden;
    private String mTransitionName;
    private int mPosition;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    private int mScrollY;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, int position) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putInt(ARG_ITEM_POSITION, position);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID) && getArguments().containsKey(ARG_ITEM_POSITION)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
            mPosition = getArguments().getInt(ARG_ITEM_POSITION);
        }

        setHasOptionsMenu(true);
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        return mRootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        Activity activity = getActivity();

        if (!isAdded() || activity == null) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        this.mCursor = cursor;

        if (this.mCursor != null && !this.mCursor.moveToFirst()) {
            Timber.e("ID: itemId" + mItemId + " " + "Error reading item detail cursor");
            Timber.i("ID: itemId" + mItemId + " " + "onLoadFinished() cursor count: " + ((cursor != null) ? cursor.getCount() : 0));
            this.mCursor.close();
            this.mCursor = null;
        } else {
            bindViews();
        }

        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Timber.e("ID: itemId" + mItemId + " " + "onLoaderReset()");

        mCursor = null;
        bindViews();
    }




    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Timber.e("ID: itemId" + mItemId + " " + ex.getMessage());
            Timber.i("passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mCursor != null && isAdded()) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            rvBodyView.setAlpha(1);
            toolbar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();

            String author = String.valueOf(Html.fromHtml("By " + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            String date;
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                date = String.valueOf(Html.fromHtml(DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()));
            } else {
                // If date is before 1902, just show the string
                date = String.valueOf(Html.fromHtml(outputFormat.format(publishedDate)));
            }

            String longText = String.valueOf(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)
                    .replaceAll("\r\n\r\n", "<br /><br />")
                    .replaceAll("\r\n", " ")
                    .replaceAll("  ", "")));

            String[] tempParagraphs = longText.split("\\r?\\n");
            ArrayList<String> paragraphs = new ArrayList<>();
            Collections.addAll(paragraphs, tempParagraphs);
            paragraphs.add(0, author);
            paragraphs.add(1, date);

            AdapterLongBodyText bodyAdapter = new AdapterLongBodyText(paragraphs);

            rvBodyView.setAdapter(bodyAdapter);
            LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rvBodyView.setLayoutManager(layout);

            if (mPalette == null) {
                ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                        .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                Bitmap bitmap = imageContainer.getBitmap();
                                if (bitmap != null) {
                                    Palette.from(bitmap)
                                            .generate(new Palette.PaletteAsyncListener() {
                                                @Override
                                                public void onGenerated(Palette palette) {
                                                    mPalette = palette;

                                                    setColors();
                                                }
                                            });
                                    ivPhotoView.setImageBitmap(imageContainer.getBitmap());
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                            }
                        });
            }

            mTransitionName = getResources().getString(R.string.transition_name) + mPosition;
            ivPhotoView.setTransitionName(mTransitionName);
            scheduleStartPostponedTransition(ivPhotoView);
        } else {
            Timber.e("ID: itemId" + mItemId + " " + "bindViews() mCursor is null");
            mRootView.setVisibility(View.GONE);
            toolbar.setTitle("N/A");
        }
    }

    private void setColors() {
        if (mPalette.getVibrantSwatch() != null) {
            int toolbarColor = mPalette.getDarkVibrantColor(getResources().getColor(R.color.theme_primary));
            collapsingToolbar.setBackgroundColor(toolbarColor);
            collapsingToolbar.setContentScrimColor(toolbarColor);

            fab.setBackgroundTintList(ColorStateList.valueOf(
                    mPalette.getLightVibrantColor(getResources().getColor(R.color.theme_accent))));

            background.setBackgroundColor(
                    mPalette.getVibrantColor(getResources().getColor(R.color.ltgray)));
        } else {
            int toolbarColor = mPalette.getDarkMutedColor(getResources().getColor(R.color.theme_primary));
            collapsingToolbar.setBackgroundColor(toolbarColor);
            collapsingToolbar.setContentScrimColor(toolbarColor);
            collapsingToolbar.setStatusBarScrimColor(toolbarColor);


            fab.setBackgroundTintList(ColorStateList.valueOf(
                    mPalette.getLightMutedColor(getResources().getColor(R.color.theme_accent))));

            background.setBackgroundColor(
                    mPalette.getMutedColor(getResources().getColor(R.color.ltgray)));
        }
    }

    public int getUpButtonFloor() {
        if (ivPhotoView == null || ivPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        boolean mIsCard = false;
        return mIsCard
                ? (int) ivPhotoView.getTranslationY() + ivPhotoView.getHeight() - mScrollY
                : ivPhotoView.getHeight() - mScrollY;
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

//    https://www.androiddesignpatterns.com/2015/03/activity-postponed-shared-element-transitions-part3b.html
    public void scheduleStartPostponedTransition(final View sharedElement) {
        Timber.i("Start postponed Transition Name: " + mTransitionName);
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getActivity().startPostponedEnterTransition();
                        }
                        return true;
                    }
                });
    }


}
