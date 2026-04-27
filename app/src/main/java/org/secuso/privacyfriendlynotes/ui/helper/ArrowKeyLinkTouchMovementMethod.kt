package org.secuso.privacyfriendlynotes.ui.helper

import android.text.Selection
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView

class ArrowKeyLinkTouchMovementMethod : ArrowKeyMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val offset = widget.layout.let {
                it.getOffsetForHorizontal(
                    it.getLineForVertical(y),
                    x.toFloat()
                )
            }

            val link = buffer.getSpans(offset, offset, ClickableSpan::class.java)

            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget)
                } else {
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                }
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

    companion object {
        private var instance: ArrowKeyLinkTouchMovementMethod? = null

        fun getInstance(): ArrowKeyLinkTouchMovementMethod {
            if (instance == null) {
                instance = ArrowKeyLinkTouchMovementMethod()
            }
            return instance!!
        }
    }
}
