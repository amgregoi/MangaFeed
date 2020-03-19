package com.amgregoire.mangafeed.v2.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs
import kotlinx.android.synthetic.main.cv_progress_bar.view.*


class ProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr)
{
    init
    {
        val view = LayoutInflater.from(context).inflate(R.layout.cv_progress_bar, this, false)
        addView(view)

        if (attrs != null) init(attrs)
        else
        {
            hide()
            if (SharedPrefs.isLightTheme()) setProgressBarStyle(ProgressBarStyle.DarkBlue)
            else setProgressBarStyle(ProgressBarStyle.White)
        }
    }

    fun init(attrs: AttributeSet)
    {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar)
        val style = attributes.getInt(R.styleable.ProgressBar_mProgressBarStyle, 0)

        when
        {
            style != 0 -> setProgressBarStyle(style)
            SharedPrefs.isLightTheme() -> setProgressBarStyle(ProgressBarStyle.DarkBlue)
            else -> setProgressBarStyle(ProgressBarStyle.White)
        }

        if(attributes.getBoolean(R.styleable.ProgressBar_mLoading, false)) startSpin()

        attributes.recycle()
    }

    fun hide()
    {
        visibility = View.GONE
    }

    fun show()
    {
        visibility = View.VISIBLE
        startSpin()
    }

    fun isHidden(): Boolean
    {
        return visibility != View.VISIBLE
    }

    private fun startSpin()
    {
        loadingSpinner.startSpin()
    }

    private fun setProgressBarStyle(styleValue: Int)
    {
        val style = when (styleValue)
        {
            0 -> ProgressBarStyle.DarkBlue
            else -> ProgressBarStyle.White
        }

        setProgressBarStyle(style)
    }

    fun setProgressBarStyle(style: ProgressBarStyle)
    {
        val spinner = when (style)
        {
            ProgressBarStyle.DarkBlue -> R.drawable.load_spinner_dark
            else -> R.drawable.load_spinner_light
        }

        loadingSpinner.setImageDrawable(spinner)
    }

    enum class ProgressBarStyle
    {
        DarkBlue, White
    }

}