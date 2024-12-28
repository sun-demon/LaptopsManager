package ru.ddp.lab4.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_BATTERY_LIFE
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_HAS_3G
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_ID
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_MASS
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_RAM
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_SCREEN_DIAGONAL
import ru.ddp.lab4.LaptopDbHelper.Companion.TABLE_NAME
import ru.ddp.lab4.R
import ru.ddp.lab4.activities.QueryActivity.Companion.CASTLED_ALL
import ru.ddp.lab4.getSpinnerAdapter

class OneGreaterActivity: AppCompatActivity() {

    private lateinit var columnSpinner: Spinner
    private lateinit var valueEdit: EditText
    private lateinit var applyButton: Button
    private lateinit var closeButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_greater)

        columnSpinner = findViewById(R.id.columnSpinner)
        valueEdit = findViewById(R.id.valueEdit)
        applyButton = findViewById(R.id.applyButton)
        closeButton = findViewById(R.id.closeButton)

        columnSpinner.adapter = getSpinnerAdapter(this, R.array.numbered_columns)
        applyButton.setOnClickListener { onApplyButtonClicked() }
        closeButton.setOnClickListener { finish() }
    }

    private fun onApplyButtonClicked() {
        val columnName = indexToColumnName(columnSpinner.selectedItemPosition)
        val value = valueEdit.text.toString().toFloatOrNull()
        if (value == null) {
            valueEdit.error = "must be not empty!"
            return
        }
        val query = """
SELECT 
${CASTLED_ALL.replaceIndent("    ")}
FROM 
    $TABLE_NAME 
WHERE
    $columnName > $value
ORDER BY
    $columnName
LIMIT 1
        """.trimIndent()
        val intent = Intent()
        intent.putExtra("query", query)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun indexToColumnName(index: Int): String {
        val columnName = when (index) {
            0 -> COLUMN_ID
            1 -> COLUMN_MASS
            2 -> COLUMN_HAS_3G
            3 -> COLUMN_SCREEN_DIAGONAL
            4 -> COLUMN_RAM
            5 -> COLUMN_BATTERY_LIFE
            else -> throw IllegalArgumentException("unknown column index $index")
        }
        return columnName
    }
}