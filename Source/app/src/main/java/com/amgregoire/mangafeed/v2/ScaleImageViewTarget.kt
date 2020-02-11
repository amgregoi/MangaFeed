package com.amgregoire.mangafeed.v2

import android.graphics.Bitmap
import android.widget.ImageView.ScaleType
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.amgregoire.mangafeed.R
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.transition.Transition

class ScaleImageViewTarget(val imageView: ImageView) : BaseTarget<Bitmap>()
{
    override fun onResourceReady(@NonNull resource: Bitmap, @Nullable transition: Transition<in Bitmap>?)
    {
        if(resource == null){
            imageView.scaleType = ScaleType.CENTER
            imageView.setImageResource(R.drawable.manga_error)
            return
        }

        imageView.scaleType = ScaleType.FIT_XY
        imageView.setImageBitmap(resource)
    }

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

    override fun getSize(@NonNull cb: SizeReadyCallback)
    {
        cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL)
    }

    override fun onStop()
    {
        super.onStop()
        imageView.scaleType = ScaleType.CENTER
    }

    override fun removeCallback(@NonNull cb: SizeReadyCallback) = Unit
}