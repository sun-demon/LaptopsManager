package ru.ddp.lab4.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout

@SuppressLint("ViewConstructor")
class TableItemView(
    context: Context,
    item: List<String>,
    isBottom: Boolean = true,
    isTop: Boolean = false,
    weights: List<Float> = List(item.size) { 1f }
) : LinearLayout(context) {

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_HORIZONTAL

        item.forEachIndexed { i, text ->
            val isRight = (i == item.size - 1)
            val cellView = TableTextView(context, text, isTop, isBottom, isRight, weights[i])
            addView(cellView)
        }
    }
}