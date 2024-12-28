package ru.ddp.lab4

import android.content.Context
import android.database.Cursor
import android.util.TypedValue
import android.widget.ArrayAdapter

fun dpToPx(context: Context, dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

fun Cursor.getItems(): List<List<String>> {
    val result = mutableListOf<List<String>>()
    if (moveToFirst()) {
        val columnCount = columnCount
        do {
            val row = mutableListOf<String>()
            for (i in 0 until columnCount) {
                val text: String = when (getType(i)) {
                    Cursor.FIELD_TYPE_INTEGER -> getInt(i).toString()
                    Cursor.FIELD_TYPE_FLOAT -> getDouble(i).toString()
                    Cursor.FIELD_TYPE_STRING -> getString(i) ?: ""
                    Cursor.FIELD_TYPE_BLOB -> "BLOB"
                    Cursor.FIELD_TYPE_NULL -> ""
                    else -> throw IllegalArgumentException("unknown table type number ${getType(i)}")
                }
                row.add(text)
            }
            result.add(row)
        } while (moveToNext())
    }
    return result
}

fun getSpinnerAdapter(context: Context, textArrayResId: Int): ArrayAdapter<CharSequence> {
    val adapter = ArrayAdapter.createFromResource(
        context,
        textArrayResId,
        R.layout.spinner_item
    )
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
    return adapter
}