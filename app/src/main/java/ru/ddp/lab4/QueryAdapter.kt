package ru.ddp.lab4

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ru.ddp.lab4.views.TableItemView

class QueryAdapter(
    context: Context,
    private val weights: List<Float>,
    items: List<List<String>>
) : ArrayAdapter<List<String>> (context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val isBottom = (position == count - 1)
        val isTop = (position == 0)
        val itemView = TableItemView(context, item!!, isBottom, isTop, weights)
        return itemView
    }
}
