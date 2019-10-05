package com.amgregoire.mangafeed.UI.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.widget.NestedScrollView
import android.text.Html
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.UI.Widgets.GestureImageView
import com.amgregoire.mangafeed.UI.Widgets.GestureTextView
import com.amgregoire.mangafeed.UI.Widgets.GestureViewPager
import com.amgregoire.mangafeed.Utils.MangaLogger
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.v2.service.CloudflareService
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

class ImagePagerAdapter(
        val parent: Fragment,
        val context: Context,
        var data: List<String>,
        private val listener: GestureViewPager.UserGestureListener? = null
) : PagerAdapter()
{
    private val mImageViews = SparseArray<View>()
    private var isManga: Boolean = false

    init
    {
        MangaLogger.logError("ImagePagerAdapter", "${data}")
        isManga = listener == null // GestureViewPager.UserGestureListener only used with Novels
    }

    override fun getCount(): Int
    {
        return this.data.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean
    {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any
    {
        return if (isManga) instantiateImage(container, position) else instantiateNovel(container, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any)
    {
        container.removeView(`object` as RelativeLayout)
        val mImage = `object`.findViewById<GestureImageView>(R.id.gestureImageViewReaderChapter)
        mImageViews.remove(position)
        Glide.with(parent).clear(mImage)
    }

    private fun instantiateNovel(container: ViewGroup, position: Int): View
    {
        val lView = LayoutInflater.from(context)
                .inflate(R.layout.item_reader_image_adapter, container, false)
        val mNovel = lView.findViewById<GestureTextView>(R.id.gestureTextViewReaderChapter)
        val mContainer = lView.findViewById<NestedScrollView>(R.id.scrollViewTextContainer)
        mNovel.setUserGesureListener(listener)

        mNovel.visibility = View.VISIBLE
        mContainer.visibility = View.VISIBLE

        val lContent = data[position].replace("</p>", "</p><br>")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            mNovel.text = Html.fromHtml(lContent, Html.FROM_HTML_MODE_COMPACT)
        }
        else
        {
            mNovel.text = Html.fromHtml(lContent)
        }

        container.addView(lView)
        return lView
    }

    private fun instantiateImage(container: ViewGroup, position: Int): View
    {
        val lView = LayoutInflater.from(context).inflate(R.layout.item_reader_image_adapter, container, false)
        val mImage = lView.findViewById<GestureImageView>(R.id.gestureImageViewReaderChapter)
        val url = data.getOrNull(position) ?: return lView
        if(url.isEmpty()) return lView

        mImage.visibility = View.VISIBLE

        val lOptions = RequestOptions()
        lOptions.fitCenter()
                .override(1024, 8192) //OpenGLRenderer max image size, if larger in X or Y it will scale the image
                .placeholder(context.resources.getDrawable(R.drawable.manga_loading_image))
                .error(context.resources.getDrawable(R.drawable.manga_error))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)


        val builder = LazyHeaders.Builder().addHeader("User-Agent", NetworkService.defaultUserAgent)

        CloudflareService().getCFCookies(url, NetworkService.defaultUserAgent) { cookies ->
            for (cookie in cookies) builder.addHeader("Cookie", cookie)
        }

        val glideUrl = GlideUrl(url, builder.build())

        Glide.with(parent)
                .asBitmap()
                .load(glideUrl)
                .apply(lOptions)
                .transition(GenericTransitionOptions<Any>().transition(android.R.anim.fade_in))
                .into(object : BitmapImageViewTarget(mImage)
                {
                    override fun onResourceReady(resource: Bitmap, glideAnimation: Transition<in Bitmap>?)
                    {
                        super.onResourceReady(resource, glideAnimation)
                        mImage.initializeView()
                        try
                        {
                            mImage.tag = "$TAG:$position"
                        }
                        catch (aException: Exception)
                        {
                            MangaLogger.logError(TAG, "instantiateItem()", aException.toString())
                        }

                        mImage.startFling(0f, 100000f) //large fling to initialize the image to the top for long pages
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?)
                    {
                        super.onLoadFailed(errorDrawable)
                    }
                })
        container.addView(lView)
        mImageViews.put(position, lView)
        return lView
    }

    companion object
    {
        val TAG = ImagePagerAdapter::class.java.simpleName
    }
}
