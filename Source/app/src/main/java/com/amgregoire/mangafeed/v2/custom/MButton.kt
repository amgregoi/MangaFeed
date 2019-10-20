package com.amgregoire.mangafeed.v2.custom

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.service.AttrService
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.cv_button.view.*

enum class ButtonStyle
{
    BORDER, SOLID
}

class MButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr)
{
    private var compositeDisposable = CompositeDisposable()
    private var style: ButtonStyle = ButtonStyle.BORDER
    private var isButtonClickDisabled: Boolean = false

    init
    {
        val view = LayoutInflater.from(context).inflate(R.layout.cv_button, this, false)
        addView(view)

        if (attrs != null) init(attrs)
        else
        {
            setButtonStyle(style)
        }
    }

    fun init(attrs: AttributeSet)
    {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MButton)

        setButtonStyle(attributes.getInt(R.styleable.MButton_mButtonStyle, 1))
        setButtonText(attributes.getString(R.styleable.MButton_mButtonText) ?: "")


        if (attributes.getBoolean(R.styleable.MButton_mLoading, false)) startLoading()

        attributes.recycle()
    }

    fun isVisible(): Boolean
    {
        return visibility == View.VISIBLE
    }

    fun disableButton()
    {
        isButtonClickDisabled = true
    }

    fun enableButton()
    {
        isButtonClickDisabled = false
    }

    fun setButtonText(buttonText: String)
    {
        textViewButtonText.text = buttonText
    }

    fun setClickListener(listener: View.OnClickListener)
    {
        if (!isButtonClickDisabled) constraintParent.setOnClickListener(listener)
    }

    fun setLongClickListener(listener: OnLongClickListener)
    {
        if (!isButtonClickDisabled) constraintParent.setOnLongClickListener(listener)
    }

    fun startLoading()
    {


        textViewButtonText.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        progressBar.startSpin()
    }

    fun stopLoading()
    {
        textViewButtonText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        progressBar.stopSpin()

    }

    fun setButtonStyle(style: ButtonStyle)
    {
        if (SharedPrefs.isLightTheme()) progressBar.setImageDrawable(context.getDrawable(R.drawable.load_spinner_light))
        else progressBar.setImageDrawable(context.getDrawable(R.drawable.load_spinner_dark))


        // Custom Styles
        when (style)
        {
            ButtonStyle.BORDER ->
            {
                viewButton.background = context.getDrawable(R.drawable.button_border)
            }
            ButtonStyle.SOLID ->
            {
                viewButton.background = context.getDrawable(R.drawable.button_solid)
            }
        }
    }

    override fun onAttachedToWindow()
    {
        super.onAttachedToWindow()
        compositeDisposable = CompositeDisposable()
    }

    override fun onDetachedFromWindow()
    {
        super.onDetachedFromWindow()
        compositeDisposable.dispose()
    }

    fun setButtonStyle(style: Int)
    {
        when (style)
        {
            1 -> setButtonStyle(ButtonStyle.BORDER)
            2 -> setButtonStyle(ButtonStyle.SOLID)
        }
    }
}