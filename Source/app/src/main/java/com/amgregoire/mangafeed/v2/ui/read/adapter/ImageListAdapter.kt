package com.amgregoire.mangafeed.v2.ui.read.adapter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.ImageUrlService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.ui.read.ChapterFragment
import com.amgregoire.mangafeed.v2.widget.GestureImageView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.fragment_image.view.*
import kotlinx.coroutines.launch

class ImageListAdapter() : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {

    var items: ArrayList<Item> = arrayListOf()

    constructor(url: String) : this() {
        items.add(Item.Url(url))
    }

    constructor(bitmaps: List<Bitmap>) : this() {
        bitmaps.forEach { items.add(Item.Bitmap(it)) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(position: Int) {
            val item = items[position]

            itemView.iv.setOnClickListener {
                val thing = itemView.iv.drawable as BitmapDrawable
                val extraThing = thing.bitmap
                Logger.error("this working?")
            }

            when (item) {
                is Item.Url -> setupImage(position, item.value)
                is Item.Bitmap -> {
                    itemView.iv.setImageBitmap(item.value)
                    itemView.iv.initializeView()
                    itemView.iv.setTag("${ImagePagerAdapter.IMAGE_TAG}:$0")
                    itemView.iv.visibility = View.VISIBLE

                }
            }
        }

        private fun setupImage(position: Int, url: String) {
            val lOptions = RequestOptions()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .fitCenter()
                    .timeout(3000)


            val builder = LazyHeaders.Builder().addHeader("User-Agent", NetworkService.defaultUserAgent)

            CloudFlareService().getCFCookies(url, NetworkService.defaultUserAgent) { cookies ->
                for (cookie in cookies) builder.addHeader("Cookie", cookie)

                val glideUrl = GlideUrl(ImageUrlService.format(url), builder.build())

                    try {
                        val context = itemView.context ?: return@getCFCookies
                        Glide.with(context)
                                .asBitmap()
                                .load(glideUrl)
                                .apply(lOptions)
                                .transition(GenericTransitionOptions<Any>().transition(android.R.anim.fade_in))
                                .into(object : BitmapImageViewTarget(itemView.iv) {
                                    override fun onResourceReady(resource: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                                        super.onResourceReady(resource, glideAnimation)

                                        try {

                                            val screenHeight = (ScreenUtil.getScreenHeightDp(context) * 1.5).toInt()
                                            if (resource.height > screenHeight) {
                                                // split the shit..
                                                var imageHeight = 0

                                                val bitmaps = arrayListOf<Bitmap>()

                                                while (imageHeight < resource.height) {
                                                    val newHeight =
                                                            if (resource.height >= imageHeight + screenHeight) screenHeight
                                                            else resource.height - imageHeight
                                                    bitmaps.add(Bitmap.createBitmap(resource, 0, imageHeight, resource.width, newHeight))
                                                    imageHeight += screenHeight
                                                }

                                                val indexOf = items.indexOfFirst { it is Item.Url && it.value == url }
                                                if (indexOf >= 0) {

                                                    Logger.error("url[$url] => %d bitmaps".format(bitmaps.size))


                                                    items.clear()
                                                    bitmaps.forEach { bitmap ->
                                                        items.add(Item.Bitmap(bitmap))
                                                    }
                                                    notifyDataSetChanged()
//                                                    itemView.emptyState.hide()

                                                    return
                                                }
                                            }

                                            Logger.error("Image is small enough, using default image...")

                                            itemView.iv.setImageBitmap(resource)
                                            itemView.iv.initializeView()
                                            itemView.iv.setTag("${ImagePagerAdapter.IMAGE_TAG}:$0")

                                            itemView.iv.visibility = View.VISIBLE
                                            itemView.iv.startFling(0f, 100000f) //large fling to initialize the image to the top for long pages

//                                            itemView.emptyState.hide()
                                        } catch (ex: Exception) {
//                                            itemView.emptyState.hideLoader(true)
                                            Logger.error("Well shit..")
                                            Logger.error(ex)
                                        }
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        super.onLoadFailed(errorDrawable)
//                                        itemView.emptyState.hideLoader(true)
                                    }
                                })
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        // View wasn't attached to fragment fast enough
                        // Occurs during fast scrolling through adapter
                    }
                }
        }

    }

    sealed class Item {
        class Url(val value: String) : Item()
        class Bitmap(val value: android.graphics.Bitmap) : Item()
    }


}
