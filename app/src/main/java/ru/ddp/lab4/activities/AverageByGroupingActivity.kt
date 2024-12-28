package ru.ddp.lab4.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_BATTERY_LIFE
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_HAS_3G
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_MASS
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_OS
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_RAM
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_SCREEN_DIAGONAL
import ru.ddp.lab4.LaptopDbHelper.Companion.TABLE_NAME
import ru.ddp.lab4.R
import ru.ddp.lab4.activities.GroupingActivity.Companion.GROUPING_BATTERY_LIFE
import ru.ddp.lab4.activities.GroupingActivity.Companion.GROUPING_MASS
import ru.ddp.lab4.activities.GroupingActivity.Companion.GROUPING_OS
import ru.ddp.lab4.activities.GroupingActivity.Companion.GROUPING_SCREEN_DIAGONAL
import ru.ddp.lab4.activities.GroupingActivity.Companion.createQueryByGroups
import ru.ddp.lab4.activities.QueryActivity.Companion.CASTLED_HAS_3G
import ru.ddp.lab4.activities.QueryActivity.Companion.CASTLED_RAM
import ru.ddp.lab4.activities.QueryActivity.Companion.GET_ALL_QUERY
import ru.ddp.lab4.getSpinnerAdapter

class AverageByGroupingActivity: AppCompatActivity() {

    private lateinit var spinners: List<Spinner>
    private lateinit var applyButton: Button
    private lateinit var closeButton: ImageButton

    private val averageMass = "CAST(ROUND(AVG(l.$COLUMN_MASS), 2) AS TEXT) || ' kg' AS $COLUMN_MASS"
    private val averageHas3g = "CAST(ROUND(AVG(l.$COLUMN_HAS_3G), 3) * 100 AS TEXT) || '%' AS $COLUMN_HAS_3G"
    private val averageScreenDiagonal = "CAST(ROUND(AVG(l.$COLUMN_SCREEN_DIAGONAL), 1) AS TEXT) || '″' AS $COLUMN_SCREEN_DIAGONAL"
    private val averageRam = "CAST(ROUND(AVG(l.$COLUMN_RAM), 0) AS TEXT) || ' GB' AS $COLUMN_RAM"
    private val averageBatteryLife = "CAST(ROUND(AVG(l.$COLUMN_BATTERY_LIFE) / 100, 0) * 100 AS TEXT) || ' mAh' AS $COLUMN_BATTERY_LIFE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_average_by_grouping)

        val rowsLayout = findViewById<LinearLayout>(R.id.rowsLayout)
        spinners = rowsLayout.children
            .filterIndexed { i, _ -> i != 0 && i != rowsLayout.childCount - 1 }
            .map { view -> (view as ConstraintLayout).getChildAt(1) as Spinner }
            .toList()
        applyButton = findViewById(R.id.applyButton)
        closeButton = findViewById(R.id.closeButton)

        spinners.forEachIndexed { i, spinner ->
            spinner.adapter = getSpinnerAdapter(
                this,
                if (i == 0) R.array.no_group else R.array.group_average
            )
        }
        applyButton.setOnClickListener { onApplyButtonClicked() }
        closeButton.setOnClickListener { finish() }

        listOf(0, 1, 2, 3, 5).forEach { spinners[it].setSelection(1) }
    }

    private fun createSubquery(groupedIndexes: List<Int>, averagedIndexes: List<Int>): String {
        val onlyGroupedSubquery = """
(
    SELECT
${groupedIndexes.joinToString(",\n") { i ->
    when (i) {
        0 -> GROUPING_OS
        1 -> GROUPING_MASS
        2 -> CASTLED_HAS_3G
        3 -> GROUPING_SCREEN_DIAGONAL
        4 -> CASTLED_RAM
        5 -> GROUPING_BATTERY_LIFE
        else -> throw IllegalArgumentException("invalid group column number: $i")
    }
}.replaceIndent("        ")} 
    FROM 
        $TABLE_NAME
) AS l
        """.trimIndent()
        val subquery = """
(
    SELECT
${groupedIndexes.joinToString(",\n") { i -> 
    when (i) {
        0 -> GROUPING_OS
        1 -> GROUPING_MASS
        2 -> CASTLED_HAS_3G
        3 -> GROUPING_SCREEN_DIAGONAL
        4 -> CASTLED_RAM
        5 -> GROUPING_BATTERY_LIFE
        else -> throw IllegalArgumentException("invalid grouped column number: $i")
    }
}.replaceIndent("        ")},
${averagedIndexes.joinToString(",\n") { i ->
    when (i) {
        1 -> COLUMN_MASS
        2 -> COLUMN_HAS_3G
        3 -> COLUMN_SCREEN_DIAGONAL
        4 -> COLUMN_RAM
        5 -> COLUMN_BATTERY_LIFE
        else -> throw IllegalArgumentException("invalid averaged column number: $i")
    }
}.replaceIndent("        ")}
    FROM 
        $TABLE_NAME
) AS l
        """.trimIndent()

        return if (averagedIndexes.isEmpty()) onlyGroupedSubquery else subquery
    }

    private fun onApplyButtonClicked() {
        val groupedIndexes = spinners
            .mapIndexed { i, spinner ->
                if (i == 0 && spinner.selectedItemPosition == 1 || i != 0 && spinner.selectedItemPosition == 0)
                    i
                else null
            }
            .filterNotNull()
            .toList()
        val averagedIndexes = spinners
            .mapIndexed { i, spinner ->
                if (i > 0 && spinner.selectedItemPosition == 1)
                    i
                else null
            }
            .filterNotNull()
            .toList()
        val query = createQueryByIndexes(groupedIndexes, averagedIndexes)
        val intent = Intent()
        intent.putExtra("query", query)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun createQueryByIndexes(groupedIndexes: List<Int>, averagedIndexes: List<Int>): String {
        if (groupedIndexes.isEmpty()) {
            return GET_ALL_QUERY
        }
        if (averagedIndexes.isEmpty()) {
            return createQueryByGroups((0..5).toList())
        }
        val groupedColumns = groupedIndexes.joinToString(",\n") { i ->
            when (i) {
                0 -> "l.$COLUMN_OS"
                1 -> "l.$COLUMN_MASS"
                2 -> "l.$COLUMN_HAS_3G"
                3 -> "l.$COLUMN_SCREEN_DIAGONAL"
                4 -> "l.$COLUMN_RAM"
                5 -> "l.$COLUMN_BATTERY_LIFE"
                else -> throw IllegalArgumentException("invalid group column number: $i")
            }
        }
        val averagedColumns = averagedIndexes.joinToString(",\n") { i ->
            when (i) {
                1 -> averageMass
                2 -> averageHas3g
                3 -> averageScreenDiagonal
                4 -> averageRam
                5 -> averageBatteryLife
                else -> throw IllegalArgumentException("invalid averaged column number: $i")
            }
        }
        val query = """
SELECT
${groupedColumns.replaceIndent("    ")},
${averagedColumns.replaceIndent("    ")}
FROM
${createSubquery(groupedIndexes, averagedIndexes).replaceIndent("    ")}
GROUP BY
${groupedColumns.replaceIndent("    ")}
ORDER BY 
${groupedColumns.replaceIndent("    ")}
        """.trimIndent()
        return query
    }
}