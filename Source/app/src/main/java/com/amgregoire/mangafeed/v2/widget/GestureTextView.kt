package com.amgregoire.mangafeed.v2.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import com.amgregoire.mangafeed.v2.widget.GestureViewPager.UserGestureListener

/**
 * Created by amgregoi on 6/22/17.
 */
class GestureTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs), GestureDetector.OnGestureListener, OnDoubleTapListener {

    private var gestureDetector: GestureDetector = GestureDetector(context, this)
    private var userGestureListener: UserGestureListener? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun setUserGestureListener(singleTapListener: UserGestureListener?) {
        userGestureListener = singleTapListener
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        userGestureListener?.onSingleTap()
        return false
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) = Unit

    override fun onSingleTapUp(e: MotionEvent?): Boolean = false

    override fun onDown(e: MotionEvent?): Boolean = false

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean = false

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = false

    override fun onLongPress(e: MotionEvent?) = Unit

}