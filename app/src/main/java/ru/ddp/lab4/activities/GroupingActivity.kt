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
import ru.ddp.lab4.activities.QueryActivity.Companion.CASTLED_HAS_3G
import ru.ddp.lab4.activities.QueryActivity.Companion.CASTLED_RAM
import ru.ddp.lab4.activities.QueryActivity.Companion.GET_ALL_QUERY
import ru.ddp.lab4.getSpinnerAdapter


class GroupingActivity: AppCompatActivity() {

    private lateinit var spinners: List<Spinner>
    private lateinit var applyButton: Button
    private lateinit var closeButton: ImageButton

    companion object {
        val GROUPING_OS = """
        CASE
            WHEN $COLUMN_OS LIKE 'Windows%' THEN 'Windows'
            WHEN $COLUMN_OS LIKE 'macOS%' THEN 'macOS'
            WHEN $COLUMN_OS LIKE 'Chrome OS%' THEN 'Chrome OS'
            WHEN $COLUMN_OS IN ('Linux Mint', 'Ubuntu', 'Fedora') THEN 'Linux'
            ELSE 'other'
        END AS $COLUMN_OS
    """.trimIndent()

        val GROUPING_MASS = """
        CASE
            WHEN $COLUMN_MASS < 1 THEN ' <1 kg'
            WHEN $COLUMN_MASS BETWEEN 1 AND 1.49 THEN '1-1.5 kg'
            WHEN $COLUMN_MASS BETWEEN 1.5 AND 1.99 THEN '1.5-2 kg'
            WHEN $COLUMN_MASS BETWEEN 2 AND 2.5 THEN '2-2.5 kg'
            WHEN $COLUMN_MASS > 2.5 THEN '>2.5 kg'
            ELSE 'other'
        END AS $COLUMN_MASS
    """.trimIndent()

        val GROUPING_SCREEN_DIAGONAL = """
        CASE
            WHEN $COLUMN_SCREEN_DIAGONAL BETWEEN 10.1 AND 13.5 THEN '10-13″'
            WHEN $COLUMN_SCREEN_DIAGONAL BETWEEN 14.0 AND 15.6 THEN '14-15″'
            WHEN $COLUMN_SCREEN_DIAGONAL BETWEEN 16.0 AND 17.3 THEN '16-17″'
            WHEN $COLUMN_SCREEN_DIAGONAL BETWEEN 18.0 AND 18.4 THEN '18″'
            ELSE 'other'
        END AS $COLUMN_SCREEN_DIAGONAL
    """.trimIndent()

        val GROUPING_BATTERY_LIFE = """
        CASE
            WHEN $COLUMN_BATTERY_LIFE < 3000 THEN ' <3000 mAh'
            WHEN $COLUMN_BATTERY_LIFE BETWEEN 3000 AND 3999 THEN '3000-4000 mAh'
            WHEN $COLUMN_BATTERY_LIFE BETWEEN 4000 AND 4999 THEN '4000-4000 mAh'
            WHEN $COLUMN_BATTERY_LIFE BETWEEN 5000 AND 5999 THEN '5000-6000 mAh'
            WHEN $COLUMN_BATTERY_LIFE BETWEEN 6000 AND 7000 THEN '6000-7000 mAh'
            WHEN $COLUMN_BATTERY_LIFE > 7000 THEN '>7000 mAh'
            ELSE 'other'
        END AS $COLUMN_BATTERY_LIFE
    """.trimIndent()

        fun createQueryByGroups(indexes: List<Int>): String {
            val columns = indexes.joinToString(",\n") { i ->
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
            if (columns.isEmpty()) {
                return GET_ALL_QUERY
            }
            val query = """
SELECT
${columns.replaceIndent("    ")},
    COUNT(*) AS count 
FROM
${getGroupedSubquery(indexes).replaceIndent("    ")}
GROUP BY
${columns.replaceIndent("    ")}
ORDER BY 
${columns.replaceIndent("    ")}
        """.trimIndent()
            return query
        }

        private fun getGroupedSubquery(indexes: List<Int>): String {
            val subqueryTable = """
(
    SELECT
${indexes.joinToString(",\n") { i ->
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
            return subqueryTable
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grouping)

        val rowsLayout = findViewById<LinearLayout>(R.id.rowsLayout)
        spinners = rowsLayout.children
            .filterIndexed { i, _ -> i != 0 && i != rowsLayout.childCount - 1 }
            .map { view -> (view as ConstraintLayout).getChildAt(1) as Spinner }
            .toList()
        applyButton = findViewById(R.id.applyButton)
        closeButton = findViewById(R.id.closeButton)

        spinners.forEach { spinner ->
            spinner.adapter = getSpinnerAdapter(this, R.array.no_group)
        }
        applyButton.setOnClickListener { onApplyButtonClicked() }
        closeButton.setOnClickListener { finish() }

        listOf(0, 4).forEach { spinners[it].setSelection(1) }
    }

    private fun onApplyButtonClicked() {
        val groupIndexes = spinners
            .mapIndexed { i, spinner -> if (spinner.selectedItemPosition == 1) i else null }
            .filterNotNull()
            .toList()
        val query =createQueryByGroups(groupIndexes)
        val intent = Intent()
        intent.putExtra("query", query)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}