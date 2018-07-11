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
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
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

import timber.log.Timber;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;
    public static final String ARG_TRANSITION_NAME = "transition_name";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView ivPhotoView;
    private boolean mIsCard = false;
    public Palette mPalette;
    private TextView tvBodyView;
    private Toolbar toolbar;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);


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

        ivPhotoView = (ImageView) mRootView.findViewById(R.id.photo);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
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

        TextView tvTitleView = (TextView) mRootView.findViewById(R.id.tv_article_title);
        TextView tvBylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        tvBodyView = (TextView) mRootView.findViewById(R.id.article_body);

        tvBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            tvTitleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                tvBylineView.setText(Html.fromHtml(
                        "By " + mCursor.getString(ArticleLoader.Query.AUTHOR) + "\n" +
                                DateUtils.getRelativeTimeSpanString(
                                        publishedDate.getTime(),
                                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                        DateUtils.FORMAT_ABBREV_ALL).toString()));

            } else {
                // If date is before 1902, just show the string
                tvBylineView.setText(Html.fromHtml("By " + mCursor.getString(ArticleLoader.Query.AUTHOR) + "\n" +
                        outputFormat.format(publishedDate)));

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
                                    mRootView.findViewById(R.id.meta_bar)
                                            .setBackgroundColor(mMutedColor);


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
            tvTitleView.setText("N/A");
            tvBylineView.setText("N/A");
            tvBodyView.setText("N/A");
        }
    }

    private void setColors() {
        mRootView.findViewById(R.id.meta_bar).setBackgroundColor(mPalette.getDarkMutedColor(getResources().getColor(R.color.theme_primary)));
        mRootView.findViewById(R.id.appbar).setBackgroundColor(mPalette.getDarkMutedColor(getResources().getColor(R.color.theme_primary)));

        mRootView.findViewById(R.id.fragment_background).setBackgroundColor(mPalette.getMutedColor(getResources().getColor(R.color.theme_background)));


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
