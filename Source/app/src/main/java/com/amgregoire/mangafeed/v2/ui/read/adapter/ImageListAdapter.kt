package com.amgregoire.mangafeed.v2.ui.read.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.ImageUrlService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.service.ScreenUtil
import com.amgregoire.mangafeed.v2.widget.MangaImageView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.fragment_image_new.view.*

class ImageListAdapter(
        val url: String,
        val screenListener: MangaImageView.ScreenInteraction,
        val onCompleteListener: (Boolean) -> Unit
) : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {

    var items: ArrayList<Item> = arrayListOf()

    init {
        items.add(Item.Url(url))
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_image_new, parent, false)
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

            when (val item = items[position]) {
                is Item.Url -> setupImage(position, item.value)
                is Item.Bitmap -> {
                    itemView.iv.setImage(ImageSource.cachedBitmap(item.value))
                    itemView.iv.setScreenInteractionListener(screenListener)
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
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                                    try {

                                        val resHeight = resource.getScaledHeight(itemView.resources.displayMetrics)
                                        val resWidth = resource.getScaledWidth(itemView.resources.displayMetrics)
                                        val screenHeight = (ScreenUtil.getScreenHeight(context) * 1.5).toInt()

                                        if (resHeight > screenHeight) {
                                            itemView.rlParent.layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT
                                            itemView.rlParent.requestLayout()

                                            // split the bitmap
                                            var imageHeight = 0

                                            val bitmaps = arrayListOf<Bitmap>()

                                            while (imageHeight < resource.height) {
                                                val newHeight =
                                                        if (resHeight >= imageHeight + screenHeight) screenHeight
                                                        else resHeight - imageHeight
                                                bitmaps.add(Bitmap.createBitmap(resource, 0, imageHeight, resWidth, newHeight))
                                                imageHeight += screenHeight
                                            }

                                            val indexOf = items.indexOfFirst { it is Item.Url && it.value == url }
                                            if (indexOf >= 0) {

                                                items.clear()
                                                bitmaps.forEach { bitmap ->
                                                    items.add(Item.Bitmap(bitmap))
                                                }
                                                notifyDataSetChanged()
                                                onCompleteListener(true)
                                                return
                                            }
                                        }


                                        // Using default url
                                        itemView.rlParent.layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT
                                        itemView.rlParent.requestLayout()

                                        itemView.iv.setImage(ImageSource.cachedBitmap(resource))
                                        itemView.iv.setScreenInteractionListener(screenListener)
                                        onCompleteListener(true)
                                    }
                                    catch (ex: Exception) {
                                        onCompleteListener(false)
                                        Logger.error("Well shit..")
                                        Logger.error(ex)
                                    }
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    onCompleteListener(false)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                }
                            })
                }
                catch (ex: Exception) {
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
