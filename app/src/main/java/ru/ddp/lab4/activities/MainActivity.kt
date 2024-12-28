package ru.ddp.lab4.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ru.ddp.lab4.LaptopDbHelper
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_BATTERY_LIFE
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_HAS_3G
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_OS
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_RAM
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_SCREEN_DIAGONAL
import ru.ddp.lab4.QueryAdapter
import ru.ddp.lab4.R
import ru.ddp.lab4.activities.QueryActivity.Companion.GET_ALL_QUERY
import ru.ddp.lab4.dpToPx
import ru.ddp.lab4.getItems
import ru.ddp.lab4.views.TableImageView
import ru.ddp.lab4.views.TableTextView
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var tableHeader: LinearLayout
    private lateinit var tableBody: ListView
    private lateinit var queryButton: ImageButton
    private lateinit var resetButton: Button
    private lateinit var dbHelper: LaptopDbHelper

    private val queryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult -> handleQueryActivityResult(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tableHeader = findViewById(R.id.tableHeader)
        tableBody = findViewById(R.id.tableBody)
        queryButton = findViewById(R.id.queryButton)
        resetButton = findViewById(R.id.resetButton)
        dbHelper = LaptopDbHelper(this)

        queryButton.setOnClickListener { onQueryButtonClickListener() }
        resetButton.setOnClickListener { onResetButtonClickListener() }
        processQuery(GET_ALL_QUERY.replaceIndent(""))
    }

    private fun handleQueryActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val query =
                result.data?.getStringExtra("query")
                    ?: throw IllegalArgumentException("query extra doesn't found")
            processQuery(query)
            resetButton.visibility = if (query != GET_ALL_QUERY) View.VISIBLE else View.GONE
        }
    }

    private fun onResetButtonClickListener() {
        resetButton.visibility = View.GONE
        processQuery(GET_ALL_QUERY)
    }

    private fun onQueryButtonClickListener() {
        val intent = Intent(this, QueryActivity::class.java)
        queryActivityResultLauncher.launch(intent)
    }

    private fun processQuery(query: String) {
        val cursor = dbHelper.readableDatabase.rawQuery(query, null)
        cursor.use { processTable(it.columnNames, it.getItems()) }
    }

    private fun processTable(headers: Array<String>, items: List<List<String>>) {
        val weights = createWeights(headers, items)
        processTableHeader(weights, headers)
        processTableBody(weights, items)
    }

    private fun columnNameToShortest(columnName: String): String? {
        return when (columnName) {
            COLUMN_OS -> "OS"
            COLUMN_HAS_3G -> "3G"
            COLUMN_SCREEN_DIAGONAL -> null
            COLUMN_RAM -> "RAM"
            COLUMN_BATTERY_LIFE -> null
            else -> columnName
        }
    }

    private fun processTableHeader(weights: List<Float>, columnNames: Array<String>) {
        tableHeader.removeAllViews()
        columnNames.forEachIndexed { i, columnName ->
            val isRight = (i == columnNames.size - 1)
            val view: View = when (val newColumnName = columnNameToShortest(columnName)) {
                null -> {
                    val drawableId = when (columnName) {
                        COLUMN_SCREEN_DIAGONAL -> R.drawable.baseline_open_in_full_24
                        COLUMN_BATTERY_LIFE -> R.drawable.baseline_battery_charging_full_24
                        else -> R.drawable.baseline_close_32
                    }
                    TableImageView(this, drawableId, isRight = isRight, weight =  weights[i])
                }
                else -> {
                    val textView =
                        TableTextView(this, newColumnName, isRight = isRight, weight = weights[i])
                    textView.setTypeface(null, Typeface.BOLD);
                    textView
                }
            }
            tableHeader.addView(view)
        }
    }

    private fun processTableBody(weights: List<Float>, items: List<List<String>>) {
        val adapter = QueryAdapter(this, weights, items)
        tableBody.adapter = adapter
    }

    private fun getMeasuredWidthByText(text: String): Float {
        val textView = TextView(this)
        textView.text = text
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textView.measure(0, 0)
        return textView.measuredWidth.toFloat()
    }

    private fun createWeights(headers: Array<String>, items: List<List<String>>): List<Float> {
        val minWidth = dpToPx(this, 20).toFloat()
        val imageWidth = dpToPx(this, 24).toFloat()
        val maxWidth = dpToPx(this, 80).toFloat()
        val weights = List(headers.size) { minWidth }.toMutableList()
        val textView = TextView(this)
        headers.map { columnNameToShortest(it) }.forEachIndexed { i, text ->
            val weight = when (text) {
                null -> imageWidth
                else -> getMeasuredWidthByText(text)
            }
            weights[i] = min(maxWidth, max(weights[i], weight))
        }
        items.forEach { row ->
            row.forEachIndexed { i, text ->
                weights[i] = min(maxWidth, max(weights[i], getMeasuredWidthByText(text)))
            }
        }
        return weights.toList()
    }
}