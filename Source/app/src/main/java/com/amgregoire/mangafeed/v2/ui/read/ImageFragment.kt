package com.amgregoire.mangafeed.v2.ui.read

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.extension.visible
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment
import com.amgregoire.mangafeed.v2.ui.read.adapter.ImageListAdapter
import com.amgregoire.mangafeed.v2.ui.read.adapter.ImagePagerAdapter
import com.amgregoire.mangafeed.v2.widget.MangaImageView
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.fragment_image2.view.*
import kotlinx.android.synthetic.main.fragment_image_new.view.*

class ImageFragment : BaseFragment() {

    private val readerViewModel: ReaderViewModel? by lazy {
        val parent = activity ?: return@lazy null
        ViewModelProvider(parent).get(ReaderViewModel::class.java)
    }

    private val url by lazy { arguments!![URL_KEY] as? String }
    private val bitmap by lazy { arguments!![BITMAP_KEY] as? Int }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image2, null).also {
            self = it
        }
    }

    override fun onStart() {
        super.onStart()

        url?.let {
            //            setupImage(it)

            val screenListener = object : MangaImageView.ScreenInteraction{
                override fun goForward() {
                    readerViewModel?.incrementPage()
                }

                override fun goBackward() {
                    readerViewModel?.decrementPage()
                }

                override fun showToolbar() {
                    readerViewModel?.toggleUiState()
                }
            }


            self.rv.layoutManager = LinearLayoutManager(context)
            self.rv.adapter = ImageListAdapter(it, screenListener)
            return
        }

//        bitmap?.let { index ->
//            ChapterCache.chapterBitmap?.let {
//                val bitmap = it.getOrNull(index) ?: return
//                //                self.iv2.visible()
//                //                self.iv2.setImageBitmap(bitmap)
//
//                self.iv.visible()
//
//
//                self.iv.setImage(ImageSource.bitmap(bitmap))
//
//                //                self.iv.setImageBitmap(bitmap)
//                //
//                //
//                //                self.iv.initializeView()
//                self.iv.tag = "${ImagePagerAdapter.IMAGE_TAG}:$index"
//
//            }
//        }
    }

    //    private fun setupImage(url: String) {
    //        val context = context ?: return
    //
    //        val lOptions = RequestOptions()
    //                .skipMemoryCache(true)
    //                .diskCacheStrategy(DiskCacheStrategy.DATA)
    //                .fitCenter()
    //                .timeout(3000)
    //
    //
    //        val builder = LazyHeaders.Builder().addHeader("User-Agent", NetworkService.defaultUserAgent)
    //
    //        CloudFlareService().getCFCookies(url, NetworkService.defaultUserAgent) { cookies ->
    //            for (cookie in cookies) builder.addHeader("Cookie", cookie)
    //
    //            val glideUrl = GlideUrl(ImageUrlService.format(url), builder.build())
    //
    //            uiScope.launch {
    //                try {
    //                    Glide.with(context)
    //                            .asBitmap()
    //                            .load(glideUrl)
    //                            .apply(lOptions)
    //                            .transition(GenericTransitionOptions<Any>().transition(android.R.anim.fade_in))
    //                            .into(object : BitmapImageViewTarget(self.iv) {
    //                                override fun onResourceReady(resource: Bitmap, glideAnimation: Transition<in Bitmap>?) {
    //                                    super.onResourceReady(resource, glideAnimation)
    //                                    try {
    //                                        if (resource.byteCount > 107647000)
    //                                            Logger.error("Pre compress byte count: ${resource.byteCount}")
    //
    //                                        // Compress incoming image, relieves memory usage + phone slowing down with large images
    //                                        val stream = ByteArrayOutputStream()
    //
    //                                        // TODO :: turn compression rate into shared pref to be toggled in app
    //                                        resource.compress(Bitmap.CompressFormat.JPEG, 40, stream)
    //                                        val byteArray = stream.toByteArray()
    //                                        val compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    //                                        self.iv.setImageBitmap(compressedBitmap)
    //
    //                                        if (resource.byteCount > 107647000)
    //                                            Logger.error("Post compress byte count: ${compressedBitmap.byteCount}")
    //
    //
    //                                        self.iv.initializeView()
    //                                        self.iv.setTag("${ImagePagerAdapter.IMAGE_TAG}:$0")
    //
    //                                        self.iv.visibility = View.VISIBLE
    //                                        self.iv.startFling(0f, 100000f) //large fling to initialize the image to the top for long pages
    //
    ////                                        emptyState.hide()
    //                                    } catch (ex: Exception) {
    ////                                        emptyState.hideLoader(true)
    //                                        Logger.error("Well shit..")
    //                                        Logger.error(ex)
    //                                    }
    //                                }
    //
    //                                override fun onLoadFailed(errorDrawable: Drawable?) {
    //                                    super.onLoadFailed(errorDrawable)
    ////                                    emptyState.hideLoader(true)
    //                                }
    //                            })
    //                } catch (ex: Exception) {
    //                    // View wasn't attached to fragment fast enough
    //                    // Occurs during fast scrolling through adapter
    //                }
    //            }
    //        }
    //
    //
    //    }
    //

    companion object {
        val TAG: String = ImageFragment::class.java.simpleName
        val URL_KEY = "$TAG:URL"
        val BITMAP_KEY = "$TAG:BITMAP"
        fun newInstance(url: String) = ImageFragment().apply {
            arguments = Bundle().apply {
                putString(URL_KEY, url)
            }
        }

//        fun newInstance(bitmapPosition: Int) = ImageFragment().apply {
//            arguments = Bundle().apply {
//                putInt(BITMAP_KEY, bitmapPosition)
//            }
//        }
    }
}