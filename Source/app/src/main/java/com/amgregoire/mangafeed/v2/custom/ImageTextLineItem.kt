package com.amgregoire.mangafeed.v2.custom

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.amgregoire.mangafeed.R
import kotlinx.android.synthetic.main.cv_iv_tv_line_item.view.*

class ImageTextLineItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr)
{

    init
    {
        val view = LayoutInflater.from(context).inflate(R.layout.cv_iv_tv_line_item, this, false)
        addView(view)

        if (attrs != null) init(attrs)
        else
        {
            //defaults
        }
    }

    fun init(attrs: AttributeSet)
    {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ImageTextLineItem)

        val icon = attributes.getResourceId(R.styleable.ImageTextLineItem_mangaIconSrc, 0)
        if(icon == 0) ivIcon.visibility = View.GONE
        else ivIcon.setImageResource(icon)

        tvPrimary.text = attributes.getText(R.styleable.ImageTextLineItem_mangaText)

        val text = attributes.getText(R.styleable.ImageTextLineItem_mangaTextSecondary)
        if (text.isNullOrEmpty()) tvSecondary.visibility = View.GONE
        else tvSecondary.text = text

        attributes.recycle()
    }

    fun setIcon(iconDrawable: Int)
    {
        ivIcon.setImageDrawable(resources.getDrawable(iconDrawable))
    }

    fun setText(text: String)
    {
        tvPrimary.text = text
    }
}