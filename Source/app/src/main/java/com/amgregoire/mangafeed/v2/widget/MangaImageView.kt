package com.amgregoire.mangafeed.v2.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.amgregoire.mangafeed.v2.service.Logger
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class MangaImageView(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private var gestureDetector: GestureDetector = GestureDetector(context, this)
    private var screenListener: ScreenInteraction? = null

    fun setScreenInteractionListener(listener: ScreenInteraction) {
        screenListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean = true

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean = true

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        val positionX: Float = e?.x ?: return true

        when {
            positionX < width * 0.2f -> {
                Logger.error("goBackward()")
                screenListener?.goBackward()
            }
            positionX > width * 0.8f -> {
                Logger.error("goForward()")
                screenListener?.goForward()
            }
            else -> {
                Logger.error("showToolbar()")
                screenListener?.showToolbar()
            }
        }

        return true
    }

    override fun onShowPress(e: MotionEvent?) = Unit

    override fun onSingleTapUp(e: MotionEvent?): Boolean = false

    override fun onDown(e: MotionEvent?): Boolean = false

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean = false

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = false

    override fun onLongPress(e: MotionEvent?) = Unit

    interface ScreenInteraction {
        fun goForward()
        fun goBackward()
        fun showToolbar()
    }
}