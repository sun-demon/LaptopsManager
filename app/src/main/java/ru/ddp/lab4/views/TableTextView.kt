package ru.ddp.lab4.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.core.content.ContextCompat
import ru.ddp.lab4.R
import ru.ddp.lab4.dpToPx

@SuppressLint("ViewConstructor", "AppCompatCustomView")
class TableTextView(
    context: Context,
    text: String,
    isTop: Boolean = false,
    isBottom: Boolean = true,
    isRight: Boolean = true,
    weight: Float = 1f
) : TextView(context) {

    init {
        val drawable =
            if (!isTop && isBottom && isRight) R.drawable.border
            else if (!isTop && isBottom) R.drawable.border_without_right
            else if (!isTop && isRight) R.drawable.border_without_bottom
            else if (isBottom && isRight) R.drawable.border_without_top
            else if (!isTop) R.drawable.border_without_bottom_right
            else if (isBottom) R.drawable.border_without_top_right
            else if (isRight) R.drawable.border_without_top_bottom
            else R.drawable.border_without_top_bottom_right
        val border = ContextCompat.getDrawable(context, drawable)
        val layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT)
        layoutParams.weight = weight
        textSize = 16f
        gravity = Gravity.CENTER
        setText(text)
        background = border
        setTextColor(ContextCompat.getColor(context, R.color.black))
        setLayoutParams(layoutParams)
        minWidth = dpToPx(context, 30)
    }
}