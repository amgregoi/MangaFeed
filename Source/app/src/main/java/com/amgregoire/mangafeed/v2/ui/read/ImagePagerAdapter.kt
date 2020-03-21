package com.amgregoire.mangafeed.v2.ui.read

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProviders
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.widget.GestureImageView
import com.amgregoire.mangafeed.v2.widget.GestureTextView
import com.amgregoire.mangafeed.v2.widget.GestureViewPager
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.custom.EmptyState
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

class ImagePagerAdapter(
        val parent: androidx.fragment.app.Fragment,
        val context: androidx.fragment.app.FragmentActivity,
        var data: List<String>,
        private val listener: GestureViewPager.UserGestureListener? = null
) : androidx.viewpager.widget.PagerAdapter()
{

    private val readerViewModel: ReaderViewModel by lazy {
        ViewModelProviders.of(context).get(ReaderViewModel::class.java)
    }

    private val mImageViews = SparseArray<View>()
    private var isManga: Boolean = false

    init
    {
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

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any)
    {
        container.removeView(obj as RelativeLayout)
        val mImage = obj.findViewById<GestureImageView>(R.id.gestureImageViewReaderChapter)
        mImageViews.remove(position)
        Glide.with(parent).clear(mImage)
    }

    private fun instantiateNovel(container: ViewGroup, position: Int): View
    {
        val lView = LayoutInflater.from(context).inflate(R.layout.item_reader_image_adapter, container, false)
        val mNovel: GestureTextView = lView.findViewById(R.id.gestureTextViewReaderChapter)
        val mContainer: NestedScrollView = lView.findViewById(R.id.scrollViewTextContainer)
        mNovel.setUserGesureListener(listener)

        mNovel.visibility = View.VISIBLE
        mContainer.visibility = View.VISIBLE

        val lContent = data[position].replace("</p>", "</p><br>")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            mNovel.text = Html.fromHtml(lContent, Html.FROM_HTML_MODE_COMPACT) as CharSequence?
        }
        else
        {
            mNovel.text = Html.fromHtml(lContent)
        }

        container.addView(lView)
        return lView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun instantiateImage(container: ViewGroup, position: Int): View
    {
        val lView = LayoutInflater.from(context).inflate(R.layout.item_reader_image_adapter, container, false)
        val image = lView.findViewById<GestureImageView>(R.id.gestureImageViewReaderChapter)
        val emptyState = lView.findViewById<EmptyState>(R.id.emptyStateReaderItem)

        val url = data.getOrNull(position) ?: return lView
        if (url.isEmpty()) return lView

        emptyState.tag = "$EMPTY_TAG:$position"

        emptyState.setButtonClickListener(View.OnClickListener {
            readerViewModel.setUiStateBlock()
            refresh(image, emptyState, url, position)
        })

        refresh(image, emptyState, url, position)

        container.addView(lView)
        mImageViews.put(position, lView)
        return lView
    }

    private fun refresh(image: GestureImageView, emptyState: EmptyState, url: String, position: Int)
    {
        try
        {
            emptyState.showLoader()
            setupImage(image, emptyState, url, position)
        }
        catch (ex: IllegalStateException)
        {
            Logger.error(ex)
        }
    }

    private fun setupImage(image: GestureImageView, emptyState: EmptyState, url: String, position: Int)
    {
        val lOptions = RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .fitCenter()
                .timeout(8000)


        val builder = LazyHeaders.Builder().addHeader("User-Agent", NetworkService.defaultUserAgent)

        CloudFlareService().getCFCookies(url, NetworkService.defaultUserAgent) { cookies ->
            for (cookie in cookies) builder.addHeader("Cookie", cookie)

            val glideUrl = GlideUrl(url, builder.build())

            uiScope.launch {
                try
                {
                    Glide.with(parent)
                            .asBitmap()
                            .load(glideUrl)
                            .apply(lOptions)
                            .transition(GenericTransitionOptions<Any>().transition(android.R.anim.fade_in))
                            .into(object : BitmapImageViewTarget(image)
                            {
                                override fun onResourceReady(resource: Bitmap, glideAnimation: Transition<in Bitmap>?)
                                {
                                    super.onResourceReady(resource, glideAnimation)
                                    try
                                    {
                                        if (resource.byteCount > 107647000)
                                            Logger.error("Pre compress byte count: ${resource.byteCount}")

                                        // Compress incoming image, relieves memory usage + phone slowing down with large images
                                        val stream = ByteArrayOutputStream()

                                        // TODO :: turn compression rate into shared pref to be toggled in app
                                        resource.compress(Bitmap.CompressFormat.JPEG, 40, stream)
                                        val byteArray = stream.toByteArray()
                                        val compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                                        image.setImageBitmap(compressedBitmap)

                                        if (resource.byteCount > 107647000)
                                            Logger.error("Post compress byte count: ${compressedBitmap.byteCount}")


                                        image.initializeView()
                                        image.setTag("$IMAGE_TAG:$position")

                                        image.visibility = View.VISIBLE
                                        image.startFling(0f, 100000f) //large fling to initialize the image to the top for long pages

                                        emptyState.hide()
                                    }
                                    catch (ex: Exception)
                                    {
                                        emptyState.hideLoader(true)
                                        Logger.error("Well shit..")
                                        Logger.error(ex)
                                    }
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?)
                                {
                                    super.onLoadFailed(errorDrawable)
                                    emptyState.hideLoader(true)
                                }
                            })
                }
                catch (ex: Exception)
                {
                    // View wasn't attached to fragment fast enough
                    // Occurs during fast scrolling through adapter
                }
            }
        }


    }

    companion object
    {
        val TAG = ImagePagerAdapter::class.java.simpleName
        val IMAGE_TAG = ImagePagerAdapter::class.java.simpleName + ":IMAGE"
        val EMPTY_TAG = ImagePagerAdapter::class.java.simpleName + ":EMPTY"
    }
}
