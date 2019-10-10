package com.amgregoire.mangafeed.v2

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView.ScaleType
import android.graphics.drawable.Drawable
import android.support.annotation.NonNull
import android.widget.ImageView
import com.amgregoire.mangafeed.R
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition

class ScaleImageViewTarget(val imageView: ImageView) : BaseTarget<Bitmap>()
{
    override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?)
    {
        if(resource == null){
            imageView.setImageResource(R.drawable.manga_error)
            return
        }

        imageView.scaleType = ScaleType.FIT_XY
        imageView.setImageBitmap(resource)
    }

    override fun getSize(cb: SizeReadyCallback?)
    {
        cb?.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL)
    }

    override fun removeCallback(cb: SizeReadyCallback?) = Unit

    override fun onLoadStarted(placeholder: Drawable?)
    {
        imageView.scaleType = ScaleType.CENTER
        imageView.setImageDrawable(placeholder)
        super.onLoadStarted(placeholder)
    }

    override fun onLoadFailed(errorDrawable: Drawable?)
    {
        imageView.scaleType = ScaleType.CENTER
        imageView.setImageDrawable(errorDrawable)
        super.onLoadFailed(errorDrawable)
    }


}