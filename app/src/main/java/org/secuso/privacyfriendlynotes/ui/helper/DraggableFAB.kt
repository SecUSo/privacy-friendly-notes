package org.secuso.privacyfriendlynotes.ui.helper

import android.view.MotionEvent
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs

fun FloatingActionButton.makeDraggable(target: View = this) {
    var downX = 0f
    var downY = 0f
    var dX = 0f
    var dY = 0f

    val CLICK_DRAG_TOLERANCE = 10f

    this.setOnTouchListener { _, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                dX = target.x - downX
                dY = target.y - downY
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val viewWidth = target.width
                val viewHeight = target.height

                val viewParent = target.parent as View
                val parentWidth = viewParent.width
                val parentHeight = viewParent.height

                target.animate()
                    .x((parentWidth - viewWidth).toFloat().coerceAtMost(event.rawX + dX))
                    .y((parentHeight - viewHeight).toFloat().coerceAtMost(event.rawY + dY))
                    .setDuration(0)
                    .start()
                true
            }

            MotionEvent.ACTION_UP -> {
                val upRawX = event.rawX
                val upRawY = event.rawY

                val distanceX = upRawX - downX
                val distanceY = upRawY - downY

                // If the finger didn't move much, trigger a click
                if (abs(distanceX) < CLICK_DRAG_TOLERANCE && abs(distanceY) < CLICK_DRAG_TOLERANCE) {
                    performClick()
                }
                true
            }

            else -> false
        }
    }
}