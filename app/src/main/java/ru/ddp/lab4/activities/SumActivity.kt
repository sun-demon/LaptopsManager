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
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_RAM
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_SCREEN_DIAGONAL
import ru.ddp.lab4.LaptopDbHelper.Companion.TABLE_NAME
import ru.ddp.lab4.R
import ru.ddp.lab4.getSpinnerAdapter

class SumActivity: AppCompatActivity() {

    private lateinit var sumSpinner: Spinner
    private lateinit var applyButton: Button
    private lateinit var closeButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sum)

        sumSpinner = findViewById(R.id.sumSpinner)
        applyButton = findViewById(R.id.applyButton)
        closeButton = findViewById(R.id.closeButton)

        sumSpinner.adapter = getSpinnerAdapter(this, R.array.numbered_columns)
        applyButton.setOnClickListener { onApplyButtonClicked() }
        closeButton.setOnClickListener { finish() }
    }

    private fun onApplyButtonClicked() {
        val query = indexToQuery(sumSpinner.selectedItemPosition)
        val intent = Intent()
        intent.putExtra("query", query)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun indexToQuery(index: Int): String {
        val sumIdQuery = """
            SELECT 
                SUM($COLUMN_ID) AS $COLUMN_ID 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val sumMassQuery = """
            SELECT 
                CAST(ROUND(SUM($COLUMN_MASS), 2) AS TEXT) || ' kg' AS $COLUMN_MASS 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val sum3gQuery = """
            SELECT 
                SUM($COLUMN_HAS_3G) AS $COLUMN_HAS_3G 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val sumScreenDiagonalQuery = """
            SELECT 
                CAST(ROUND(SUM($COLUMN_SCREEN_DIAGONAL), 1) AS TEXT) || '″' AS $COLUMN_SCREEN_DIAGONAL 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val sumRamQuery = """
            SELECT 
                CAST(SUM($COLUMN_RAM) AS TEXT) || ' GB' AS $COLUMN_RAM 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val sumBatteryLifeQuery = """
            SELECT 
                CAST(SUM($COLUMN_BATTERY_LIFE) AS TEXT) || ' mAh' AS $COLUMN_BATTERY_LIFE 
            FROM 
                $TABLE_NAME
        """.trimIndent()

        return when (index) {
            0 -> sumIdQuery
            1 -> sumMassQuery
            2 -> sum3gQuery
            3 -> sumScreenDiagonalQuery
            4 -> sumRamQuery
            5 -> sumBatteryLifeQuery
            else -> throw IllegalArgumentException("invalid  index number $index")
        }
    }
}