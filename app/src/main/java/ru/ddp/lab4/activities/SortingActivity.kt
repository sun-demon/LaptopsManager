package ru.ddp.lab4.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_BATTERY_LIFE
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_HAS_3G
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_ID
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_MASS
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_OS
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_RAM
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_SCREEN_DIAGONAL
import ru.ddp.lab4.LaptopDbHelper.Companion.TABLE_NAME
import ru.ddp.lab4.R
import ru.ddp.lab4.activities.QueryActivity.Companion.CASTLED_ALL
import ru.ddp.lab4.getSpinnerAdapter

class SortingActivity: AppCompatActivity() {

    private lateinit var columnSpinner: Spinner
    private lateinit var orderSpinner: Spinner
    private lateinit var applyButton: Button
    private lateinit var closeButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sorting)

        columnSpinner = findViewById(R.id.columnSpinner)
        orderSpinner = findViewById(R.id.orderSpinner)
        applyButton = findViewById(R.id.applyButton)
        closeButton = findViewById(R.id.closeButton)

        columnSpinner.adapter = getSpinnerAdapter(this, R.array.columns)
        orderSpinner.adapter = getSpinnerAdapter(this, R.array.orders)
        applyButton.setOnClickListener { onApplyButtonClicked() }
        closeButton.setOnClickListener { finish() }
    }

    private fun onApplyButtonClicked() {
        val columnName = indexToColumnName(columnSpinner.selectedItemPosition)
        val orderName = indexToOrderName(orderSpinner.selectedItemPosition)
        val intent = Intent()
        val query = """
SELECT 
${CASTLED_ALL.replaceIndent("    ")}
FROM 
    $TABLE_NAME 
ORDER BY 
    $columnName 
    $orderName
        """.trimIndent()
        intent.putExtra("query", query)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun indexToColumnName(index: Int): String {
        val columnName = when (index) {
            0 -> COLUMN_ID
            1 -> COLUMN_OS
            2 -> COLUMN_MASS
            3 -> COLUMN_HAS_3G
            4 -> COLUMN_SCREEN_DIAGONAL
            5 -> COLUMN_RAM
            6 -> COLUMN_BATTERY_LIFE
            else -> throw IllegalArgumentException("unknown column index $index")
        }
        return columnName
    }

    private fun indexToOrderName(index: Int): String {
        val order = when (index) {
            0 -> "ASC"
            1 -> "DESC"
            else -> throw IllegalArgumentException("unknown order index $index")
        }
        return order
    }
}