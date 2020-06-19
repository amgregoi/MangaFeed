package com.amgregoire.mangafeed.v2.service
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.drawable.Drawable
//import android.view.View
//import com.amgregoire.mangafeed.Utils.NetworkService
//import com.amgregoire.mangafeed.v2.ui.read.adapter.ImageAdapter
//import com.amgregoire.mangafeed.v2.ui.read.adapter.ImageListAdapter
//import com.amgregoire.mangafeed.v2.ui.read.adapter.ImagePagerAdapter
//import com.bumptech.glide.GenericTransitionOptions
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.load.model.GlideUrl
//import com.bumptech.glide.load.model.LazyHeaders
//import com.bumptech.glide.request.RequestOptions
//import com.bumptech.glide.request.target.BitmapImageViewTarget
//import com.bumptech.glide.request.target.CustomTarget
//import com.bumptech.glide.request.transition.Transition
//import kotlinx.android.synthetic.main.fragment_image.view.*
//
//object ReadService {
//    fun urlToBitmaps(url: String, context: Context?, output: (List<ImageAdapter.Item>) -> Unit) {
//        context ?: kotlin.run {
//            output(listOf(ImageAdapter.Item.Url(url)))
//            return
//        }
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
//            try {
//                Glide.with(context)
//                        .asBitmap()
//                        .load(glideUrl)
//                        .apply(lOptions)
//                        .into(object : CustomTarget<Bitmap>() {
//                            override fun onLoadCleared(placeholder: Drawable?) = kotlin.run {
//                                output(listOf(ImageAdapter.Item.Url(url)))
//                            }
//
//                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                                try {
//
//                                    val screenHeight = (ScreenUtil.getScreenHeightDp(context) * 2).toInt()
//                                    if (resource.height > screenHeight) {
//                                        // split the shit..
//                                        var imageHeight = 0
//
//                                        val bitmaps = arrayListOf<Bitmap>()
//
//                                        while (imageHeight < resource.height) {
//                                            val newHeight =
//                                                    if (resource.height >= imageHeight + screenHeight) screenHeight
//                                                    else resource.height - imageHeight
//                                            bitmaps.add(Bitmap.createBitmap(resource, 0, imageHeight, resource.width, newHeight))
//                                            imageHeight += screenHeight
//                                        }
//
//                                        Logger.error("url[$url] => %d bitmaps".format(bitmaps.size))
//
//                                        output(bitmaps.map { ImageAdapter.Item.Bitmap(it) })
//                                    }
//
//                                } catch (ex: Exception) {
//                                    output(listOf(ImageAdapter.Item.Url(url)))
//                                    Logger.error("Well shit..")
//                                    ex.printStackTrace()
//                                }
//                            }
//                        })
//
//            } catch (ex: Exception) {
//                output(listOf())
//                ex.printStackTrace()
//                // View wasn't attached to fragment fast enough
//                // Occurs during fast scrolling through adapter
//            }
//        }
//    }
//
//
//}