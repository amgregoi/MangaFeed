package com.amgregoire.mangafeed.v2.custom

import com.amgregoire.mangafeed.R
import kotlinx.android.synthetic.main.cv_progress_bar.view.*

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.amgregoire.mangafeed.Utils.SharedPrefs


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
            if(SharedPrefs.isLightTheme()) setProgressBarStyle(ProgressBarStyle.DarkBlue)
            else setProgressBarStyle(ProgressBarStyle.White)
        }
    }

    fun init(attrs: AttributeSet)
    {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar)
        setProgressBarStyle(attributes.getInt(R.styleable.ProgressBar_mangaProgressBarStyle, 0))
        startSpin()
        attributes.recycle()
    }

    fun hide()
    {
        visibility = View.GONE
    }

    fun show()
    {
        visibility = View.VISIBLE
    }

    fun isHidden():Boolean
    {
        return visibility != View.VISIBLE
    }

    fun startSpin()
    {
        show()
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