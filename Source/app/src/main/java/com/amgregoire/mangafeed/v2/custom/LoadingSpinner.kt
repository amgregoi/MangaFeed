package com.amgregoire.mangafeed.v2.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs

class LoadingSpinner : ImageView
{
    constructor(context: Context) : super(context)
    {
        if (SharedPrefs.isLightTheme()) setImageDrawable(context.resources.getDrawable(R.drawable.load_spinner_dark, context.theme))
        else setImageDrawable(context.resources.getDrawable(R.drawable.load_spinner_light, context.theme))

        startSpin()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        if (SharedPrefs.isLightTheme()) setImageDrawable(context.resources.getDrawable(R.drawable.load_spinner_dark, context.theme))
        else setImageDrawable(context.resources.getDrawable(R.drawable.load_spinner_light, context.theme))
        startSpin()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        if (SharedPrefs.isLightTheme()) setImageDrawable(context.resources.getDrawable(R.drawable.load_spinner_dark, context.theme))
        else setImageDrawable(context.resources.getDrawable(R.drawable.load_spinner_light, context.theme))
        startSpin()
    }

    fun setImageDrawable(drawableId: Int)
    {
        setImageDrawable(context.resources.getDrawable(drawableId, context.theme))
        startSpin()
    }

    fun setBitmap(bitmap: Bitmap)
    {
        background = BitmapDrawable(resources, bitmap)
    }

    fun startSpin()
    {

        val anim = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.duration = 1440

        animation = anim
        animate()
    }

    fun stopSpin()
    {
        if (animation != null) animation.cancel()
    }
}
