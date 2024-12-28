package ru.ddp.lab4.views

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import androidx.core.content.ContextCompat
import ru.ddp.lab4.R

@SuppressLint("ViewConstructor", "AppCompatCustomView")
class TableImageView(
    context: Context,
    drawable: Int,
    isTop: Boolean = false,
    isBottom: Boolean = true,
    isRight: Boolean = true,
    weight: Float = 1f
) : ImageView(context) {

    init {
        val border =
            if (!isTop && isBottom && isRight) R.drawable.border
            else if (!isTop && isBottom) R.drawable.border_without_right
            else if (!isTop && isRight) R.drawable.border_without_bottom
            else if (isBottom && isRight) R.drawable.border_without_top
            else if (!isTop) R.drawable.border_without_bottom_right
            else if (isBottom) R.drawable.border_without_top_right
            else if (isRight) R.drawable.border_without_top_bottom
            else R.drawable.border_without_top_bottom_right
        background = ContextCompat.getDrawable(context, border)
        val layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT)
        layoutParams.weight = weight
        setImageResource(drawable)
        setLayoutParams(layoutParams)
    }
}