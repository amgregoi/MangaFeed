package com.amgregoire.mangafeed.v2.custom

import android.animation.Animator
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.cv_empty_state.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmptyState @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr)
{
    private var compositeDisposable = CompositeDisposable()
    private var hasButton: Boolean = false

    init
    {
        val view = LayoutInflater.from(context).inflate(R.layout.cv_empty_state, this, false)
        addView(view)

        if (attrs != null) init(attrs)
        else
        {
            //defaults
        }
    }

    fun init(attrs: AttributeSet)
    {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.EmptyState)

        imageView.setImageResource(attributes.getResourceId(R.styleable.EmptyState_mDrawableImage, R.drawable.img_noactivity))

        if (attributes.hasValue(R.styleable.EmptyState_mPrimaryText)) textViewEmptyStatePrimary.text = attributes.getText(R.styleable.EmptyState_mPrimaryText)
        if (attributes.hasValue(R.styleable.EmptyState_mSecondaryText)) textViewEmptyStateSecondary.text = attributes.getText(R.styleable.EmptyState_mSecondaryText)
        else textViewEmptyStateSecondary.visibility = View.GONE

        if (attributes.getBoolean(R.styleable.EmptyState_mButtonVisible, false))
        {
            hasButton = true
            buttonAction.visibility = View.VISIBLE
        }

        if (attributes.getBoolean(R.styleable.EmptyState_mLoading, false)) showLoader()
        else hideLoader(true)

        if (attributes.hasValue(R.styleable.EmptyState_mButtonText)) buttonAction.text = (attributes.getText(R.styleable.EmptyState_mButtonText).toString())

        if (SharedPrefs.isLightTheme()) progressBarLoading.setProgressBarStyle(ProgressBar.ProgressBarStyle.DarkBlue)
        else progressBarLoading.setProgressBarStyle(ProgressBar.ProgressBarStyle.White)


        buttonAction.setOnClickListener(OnClickListener {
        })
        attributes.recycle()
    }

    fun hide()
    {
        val listener = object : Animator.AnimatorListener
        {
            override fun onAnimationRepeat(animation: Animator?) = Unit
            override fun onAnimationStart(animation: Animator?) = Unit
            override fun onAnimationEnd(animation: Animator?) = reset()
            override fun onAnimationCancel(animation: Animator?) = reset()

            fun reset()
            {
                constraintParent.visibility = View.GONE
                constraintParent.alpha = 1f
            }
        }

        constraintParent.animate().setListener(listener).alpha(0f).duration = 500
    }

    fun setLoadTimeout(delayDurationMs: Long)
    {
        ioScope.launch {
            delay(delayDurationMs)

            if (!progressBarLoading.isHidden() && constraintParent.visibility == View.VISIBLE)
            {
                uiScope.launch { hideLoader(true) }
            }
        }
    }

    fun hideImmediate()
    {
        constraintParent.visibility = View.GONE
    }

    fun show()
    {
        constraintParent.visibility = View.VISIBLE
    }

    fun hideLoader(isEmpty: Boolean)
    {
        show()
        progressBarLoading.hide()
        if (isEmpty) clEmptyState.visibility = View.VISIBLE
        if (hasButton)
        {
            buttonAction.visibility = View.VISIBLE
        }
    }

    fun showLoader()
    {
        buttonAction.visibility = View.GONE
        clEmptyState.visibility = View.GONE
        show()
        progressBarLoading.show()
    }

    fun setImageIcon(drawableId: Int)
    {
        imageView.setImageResource(drawableId)
    }

    fun setButtonText(buttonText: String)
    {
        buttonAction.text = buttonText
    }

    fun showButton()
    {
        buttonAction.visibility = View.VISIBLE
    }

    fun hideButton()
    {
        buttonAction.visibility = View.GONE
    }

    fun setButtonClickListener(listener: View.OnClickListener)
    {
        buttonAction.setOnClickListener(listener)
    }

    fun setPrimaryText(primaryText: String)
    {
        textViewEmptyStatePrimary.text = primaryText
    }

    fun setSecondaryText(secondaryText: String)
    {
        textViewEmptyStateSecondary.text = secondaryText
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