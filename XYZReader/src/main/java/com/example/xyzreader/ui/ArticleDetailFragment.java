package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

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
import android.support.v7.graphics.Palette;
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
        LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.tv_article_author)
    TextView tvAuthor;
    @BindView(R.id.tv_article_date)
    TextView tvDate;
    @BindView(R.id.tv_article_body)
    TextView tvBodyView;
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
    public static final String ARG_TRANSITION_NAME = "transition_name";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;
    private int mMaxScrollSize;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;
    public Palette mPalette;
    private boolean mIsImageHidden;

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

    public static ArticleDetailFragment newInstance(long itemId) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        Timber.i("ID: itemId" + mItemId + " " + "onCreate()");

        setHasOptionsMenu(true);


    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(i)) * 100
                / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsImageHidden) {
                mIsImageHidden = true;

                ViewCompat.animate(fab).scaleY(0).scaleX(0).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsImageHidden) {
                mIsImageHidden = false;
                ViewCompat.animate(fab).scaleY(1).scaleX(1).start();
            }
        }
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int loaderId = (int) mItemId;

        getLoaderManager().initLoader(loaderId, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        appBar.addOnOffsetChangedListener(this);
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
        Timber.v("ID: itemId" + mItemId + " " + "bindViews()");

        if (mRootView == null) {
            Timber.e("ID: itemId" + mItemId + " " + "bindViews() mRootView = null");
            return;
        }

        tvBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            toolbar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            tvAuthor.setText(Html.fromHtml("By " + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                tvDate.setText(
                        Html.fromHtml(DateUtils.getRelativeTimeSpanString(
                                        publishedDate.getTime(),
                                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                        DateUtils.FORMAT_ABBREV_ALL).toString()));
            } else {
                // If date is before 1902, just show the string
                tvDate.setText(
                        Html.fromHtml(outputFormat.format(publishedDate)));

            }

            tvBodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)
                    .replaceAll("\r\n\r\n", "<br /><br />")
                    .replaceAll("\r\n", " ")
                    .replaceAll("  ", "")));

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
            scheduleStartPostponedTransition(ivPhotoView);
        } else {
            Timber.e("ID: itemId" + mItemId + " " + "bindViews() mCursor is null");
            mRootView.setVisibility(View.GONE);
            toolbar.setTitle("N/A");
            tvAuthor.setText("N/A");
            tvBodyView.setText("N/A");
        }
    }

    private void setColors() {
        collapsingToolbar.setBackgroundColor(mPalette.getMutedColor(getResources().getColor(R.color.theme_primary)));

        collapsingToolbar.setStatusBarScrimColor(mPalette.getMutedColor(getResources().getColor(R.color.theme_primary)));
        collapsingToolbar.setStatusBarScrimColor(mPalette.getMutedColor(getResources().getColor(R.color.theme_primary)));
        fab.setBackgroundColor(mPalette.getVibrantColor(getResources().getColor(R.color.theme_accent)));
        fab.setColorFilter(mPalette.getVibrantColor(getResources().getColor(R.color.theme_accent)));

        mRootView.findViewById(R.id.fragment_background)
                .setBackgroundColor(mPalette.getDarkMutedColor(getResources().getColor(R.color.ltgray)));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Timber.i("ID: itemId" + mItemId + " " + "onCreateLoader() LoaderId(int i): " + i);

        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (!isAdded()) {
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

        int loadId = (int) mItemId;
        getLoaderManager().destroyLoader(loadId);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Timber.e("ID: itemId" + mItemId + " " + "onLoaderReset()");

        mCursor = null;
        bindViews();
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


    public void scheduleStartPostponedTransition(final View sharedElement) {
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

        getActivity().getWindow().setSharedElementReturnTransition(null);
        getActivity().getWindow().setSharedElementReenterTransition(null);
        sharedElement.setTransitionName(null);
    }




}
