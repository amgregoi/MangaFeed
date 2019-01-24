package com.amgregoire.mangafeed.UI.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Widgets.GestureImageView;
import com.amgregoire.mangafeed.UI.Widgets.GestureTextView;
import com.amgregoire.mangafeed.UI.Widgets.GestureViewPager;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.util.List;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ImagePagerAdapter extends PagerAdapter
{
    final public static String TAG = ImagePagerAdapter.class.getSimpleName();

    private Fragment mParent;
    private Context mContext;
    private List<String> mImageUrlList;
    private SparseArray<View> mImageViews = new SparseArray<>();
    private boolean isManga;

    private GestureViewPager.UserGestureListener mNovelListener;

    public ImagePagerAdapter(Fragment fragment, Context context, List<String> data)
    {
        this.mContext = context;
        this.mImageUrlList = data;
        mParent = fragment;
        isManga = true;
    }

    public ImagePagerAdapter(Fragment fragment, Context context, List<String> data, GestureViewPager.UserGestureListener listener)
    {
        this.mContext = context;
        this.mImageUrlList = data;
        mParent = fragment;
        isManga = false;
        mNovelListener = listener;
    }

    public void cleanup()
    {
        mParent = null;
        mContext = null;
        mImageUrlList = null;
    }

    @Override
    public int getCount()
    {
        return this.mImageUrlList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == (object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        if (isManga) return instantiateImage(container, position);
        return instantiateNovel(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        (container).removeView((RelativeLayout) object);
        GestureImageView mImage = ((RelativeLayout) object).findViewById(R.id.gestureImageViewReaderChapter);
        mImageViews.remove(position);
        Glide.with(mParent).clear(mImage);
    }

    private View instantiateNovel(ViewGroup container, int position)
    {
        View lView = LayoutInflater.from(mContext).inflate(R.layout.item_reader_image_adapter, container, false);
        GestureTextView mNovel = lView.findViewById(R.id.gestureTextViewReaderChapter);
        NestedScrollView mContainer = lView.findViewById(R.id.scrollViewTextContainer);
        mNovel.setUserGesureListener(mNovelListener);

        mNovel.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.VISIBLE);

        String lContent = mImageUrlList.get(position).replace("</p>", "</p><br>");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            mNovel.setText(Html.fromHtml(lContent, Html.FROM_HTML_MODE_COMPACT));
        }
        else
        {
            mNovel.setText(Html.fromHtml(lContent));
        }

        (container).addView(lView);
        return lView;
    }

    private View instantiateImage(ViewGroup container, int position)
    {
        View lView = LayoutInflater.from(mContext).inflate(R.layout.item_reader_image_adapter, container, false);
        GestureImageView mImage = lView.findViewById(R.id.gestureImageViewReaderChapter);
        mImage.setVisibility(View.VISIBLE);

        RequestOptions lOptions = new RequestOptions();
        lOptions.fitCenter()
                .override(1024, 8192) //OpenGLRenderer max image size, if larger in X or Y it will scale the image
                .placeholder(mContext.getResources().getDrawable(R.drawable.manga_loading_image))
                .error(mContext.getResources().getDrawable(R.drawable.manga_error))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(mParent)
             .asBitmap()
             .load(mImageUrlList.get(position))
             .apply(lOptions)
             .transition(new GenericTransitionOptions<>().transition(android.R.anim.fade_in))
             .into(new BitmapImageViewTarget(mImage)
             {
                 @Override
                 public void onResourceReady(Bitmap resource, Transition<? super Bitmap> glideAnimation)
                 {
                     super.onResourceReady(resource, glideAnimation);
                     mImage.initializeView();
                     try
                     {
                         mImage.setTag(TAG + ":" + position);
                     }
                     catch (Exception aException)
                     {
                         MangaLogger.logError(TAG, "instantiateItem()", aException.toString());
                     }
                     mImage.startFling(0, 100000f); //large fling to initialize the image to the top for long pages
                 }

                 @Override
                 public void onLoadFailed(@Nullable Drawable errorDrawable)
                 {
                     super.onLoadFailed(errorDrawable);
                 }
             });
        (container).addView(lView);
        mImageViews.put(position, lView);
        return lView;
    }
}
