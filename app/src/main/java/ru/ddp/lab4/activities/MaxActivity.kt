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

class MaxActivity: AppCompatActivity() {

    private lateinit var maxSpinner: Spinner
    private lateinit var applyButton: Button
    private lateinit var closeButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_max)

        maxSpinner = findViewById(R.id.maxSpinner)
        applyButton = findViewById(R.id.applyButton)
        closeButton = findViewById(R.id.closeButton)

        maxSpinner.adapter = getSpinnerAdapter(this, R.array.numbered_columns)
        applyButton.setOnClickListener { onApplyButtonClicked() }
        closeButton.setOnClickListener { finish() }
    }

    private fun onApplyButtonClicked() {
        val query = indexToQuery(maxSpinner.selectedItemPosition)
        val intent = Intent()
        intent.putExtra("query", query)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun indexToQuery(index: Int): String {
        val maxIdQuery = """
            SELECT 
                MAX($COLUMN_ID) AS $COLUMN_ID 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val maxMassQuery = """
            SELECT 
                CAST(MAX($COLUMN_MASS) AS TEXT) || ' kg' AS $COLUMN_MASS 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val max3gQuery = """
            SELECT 
                CASE WHEN MAX($COLUMN_HAS_3G) = 0 THEN 'no' ELSE 'contains' END AS $COLUMN_HAS_3G"
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val maxScreenDiagonalQuery = """
            SELECT 
                CAST(MAX($COLUMN_SCREEN_DIAGONAL) AS TEXT) || '″' AS $COLUMN_SCREEN_DIAGONAL 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val maxRamQuery = """
            SELECT 
                CAST(MAX($COLUMN_RAM) AS TEXT) || ' GB' AS $COLUMN_RAM 
            FROM 
                $TABLE_NAME
        """.trimIndent()
        val maxBatteryLifeQuery = """
            SELECT 
                CAST(MAX($COLUMN_BATTERY_LIFE) AS TEXT) || ' mAh' AS $COLUMN_BATTERY_LIFE 
            FROM 
                $TABLE_NAME
        """.trimIndent()

        return when (index) {
            0 -> maxIdQuery
            1 -> maxMassQuery
            2 -> max3gQuery
            3 -> maxScreenDiagonalQuery
            4 -> maxRamQuery
            5 -> maxBatteryLifeQuery
            else -> throw IllegalArgumentException("invalid  index number $index")
        }
    }
}