package com.amgregoire.mangafeed.v2.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.v2.service.AttrService
import com.amgregoire.mangafeed.v2.service.KeyboardUtil
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.cv_input_line.view.*
import org.w3c.dom.Attr

class InputLine @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr)
{
    private var compositeDisposable = CompositeDisposable()

    init
    {
        val view = LayoutInflater.from(context).inflate(R.layout.cv_input_line, this, false)
        addView(view)

        if (attrs != null) init(attrs)
        else
        {
            //defaults
        }
    }

    fun init(attrs: AttributeSet)
    {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.InputLine)

        if (attributes.hasValue(R.styleable.InputLine_mLabel)) tvLineLabel.text = attributes.getText(R.styleable.InputLine_mLabel)
        if (attributes.hasValue(R.styleable.InputLine_mHint)) etLineInput.setHint(attributes.getText(R.styleable.InputLine_mHint))

        if (attributes.getBoolean(R.styleable.InputLine_mHideBottomDivider, false)) separatorBottom.visibility = View.GONE
        if (attributes.getBoolean(R.styleable.InputLine_mHideTopDivider, false)) separatorTop.visibility = View.GONE
        if (attributes.getBoolean(R.styleable.InputLine_mHideLabel, false)) tvLineLabel.visibility = View.GONE
        if (attributes.getBoolean(R.styleable.InputLine_mFocused, false))
        {
            etLineInput.isFocusableInTouchMode = true
            etLineInput.requestFocus()
        }

        val labelColor = attributes.getColor(R.styleable.InputLine_mLabelTextColor, resources.getColor(R.color.manga_black))
        tvLineLabel.setTextColor(labelColor)

        val inputColor = attributes.getColor(R.styleable.InputLine_mInputTextColor, resources.getColor(R.color.manga_black))
        etLineInput.setTextColor(inputColor)
        etLineInput.setHintTextColor(inputColor)

        etLineInput.inputType = attributes.getInt(R.styleable.InputLine_mInputType, 0x01)
        etLineInput.imeOptions = attributes.getInt(R.styleable.InputLine_mImeOption, 0x00)

        if (etLineInput.inputType == InputType.Password.value) setPasswordToggleShow()
        else if (etLineInput.inputType == InputType.PasswordVisible.value) setPasswordToggleHide()

        constraintParent.setOnClickListener {
            etLineInput.requestFocus()
            KeyboardUtil.show((context as AppCompatActivity))
        }

        tvPasswordToggle.setOnClickListener {
            if (etLineInput.inputType == InputType.Password.value) setPasswordToggleHide()
            else setPasswordToggleShow()
        }

        attributes.recycle()
    }

    fun setLabel(labelText: String)
    {
        tvLineLabel.text = labelText
    }

    fun setInputHint(hintText: String)
    {
        etLineInput.hint = hintText
    }

    fun setInputText(inputText: String)
    {
        etLineInput.setText(inputText)
    }

    fun getInputText(): String
    {
        return etLineInput.text.toString()
    }

    fun setInputType(type: InputType)
    {
        etLineInput.inputType = type.value
    }

    enum class InputType(val value: Int)
    {
        Text(0x01),
        Name(0x61),
        Email(0xD1),
        Password(0x81),
        PasswordVisible(0x91),
        Phone(0x03)
    }

    fun hideLabel()
    {
        tvLineLabel.visibility = View.GONE
    }

    fun showLabel()
    {
        tvLineLabel.visibility = View.VISIBLE
    }

    fun showTopDivider()
    {
        separatorTop.visibility = View.VISIBLE
    }

    fun hideTopDivider()
    {
        separatorTop.visibility = View.GONE
    }

    fun showBottomDivider()
    {
        separatorBottom.visibility = View.VISIBLE
    }

    fun hideBottomDivider()
    {
        separatorBottom.visibility = View.GONE
    }

    private fun setPasswordToggleHide()
    {
        tvPasswordToggle.visibility = View.VISIBLE
        tvPasswordToggle.text = resources.getString(R.string.hide)
        tvPasswordToggle.background = resources.getDrawable(R.drawable.button_pill_dark)
        tvPasswordToggle.setTextColor(resources.getColor(R.color.manga_white))
        etLineInput.inputType = InputType.PasswordVisible.value
        etLineInput.setSelection(etLineInput.text.toString().length)
        etLineInput.typeface = ResourcesCompat.getFont(context, R.font.nunito_regular)
    }

    private fun setPasswordToggleShow()
    {
        tvPasswordToggle.visibility = View.VISIBLE
        tvPasswordToggle.text = resources.getString(R.string.show)
        tvPasswordToggle.background = resources.getDrawable(R.drawable.button_pill_light)
        tvPasswordToggle.setTextColor(resources.getColor(R.color.colorPrimaryDark))
        etLineInput.inputType = InputType.Password.value
        etLineInput.setSelection(etLineInput.text.toString().length)
        etLineInput.typeface = ResourcesCompat.getFont(context, R.font.nunito_regular)
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
}