package com.amgregoire.mangafeed.UI.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Widgets.GestureImageView;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ImagePagerAdapter extends PagerAdapter
{
    final public static String TAG = ImagePagerAdapter.class.getSimpleName();

    private Context mContext;
    private List<String> mImageUrlList;
    private LayoutInflater mInflater;

    private SparseArray<View> mImageViews = new SparseArray<>();

    public ImagePagerAdapter(Context aContext, List<String> aImageUrls)
    {
        this.mContext = aContext;
        this.mImageUrlList = aImageUrls;
    }

    @Override
    public int getCount()
    {
        return this.mImageUrlList.size();
    }

    @Override
    public boolean isViewFromObject(View aView, Object aObject)
    {
        return aView == (aObject);
    }

    @Override
    public Object instantiateItem(ViewGroup aContainer, int aPosition)
    {
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lView = mInflater.inflate(R.layout.item_reader_image_adapter, aContainer, false);

        GestureImageView mImage = (GestureImageView) lView.findViewById(R.id.gestureImageViewReaderChapter);

        RequestOptions lOptions = new RequestOptions();
        lOptions.fitCenter()
                .override(1024, 8192)//OpenGLRenderer max image size, if larger in X or Y it will scale the image
                .placeholder(mContext.getResources().getDrawable(R.drawable.manga_loading_image))
                .error(mContext.getResources().getDrawable(R.drawable.manga_error))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(mContext)
             .asBitmap()
             .load(mImageUrlList.get(aPosition))
             .apply(lOptions)
             .transition(new GenericTransitionOptions<>().transition(android.R.anim.fade_in))
             .into(new BitmapImageViewTarget(mImage)
             {
                 @Override public void onResourceReady(Bitmap resource, Transition<? super Bitmap> glideAnimation)
                 {
                     super.onResourceReady(resource, glideAnimation);
                     mImage.initializeView();
                     try
                     {
                         mImage.setTag(TAG + ":" + aPosition);
                     }catch (Exception aException){
                         MangaLogger.logError(TAG,"Position: " + aPosition, aException.toString());
                     }
                     mImage.startFling(0, 100000f); //large fling to initialize the image to the top for long pages
                 }

                 @Override public void onLoadFailed(@Nullable Drawable errorDrawable)
                 {
                     super.onLoadFailed(errorDrawable);
                     mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_refresh_white_24dp));
                 }
             });
        (aContainer).addView(lView);
        mImageViews.put(aPosition, lView);
        return lView;
    }

    @Override
    public void destroyItem(ViewGroup aContainer, int aPosition, Object aObject)
    {
        (aContainer).removeView((RelativeLayout) aObject);
        mImageViews.remove(aPosition);
    }
}
