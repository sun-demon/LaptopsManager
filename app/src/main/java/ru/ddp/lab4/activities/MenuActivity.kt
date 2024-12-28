package ru.ddp.lab4.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import ru.ddp.lab4.R
import java.util.SortedSet

class MenuActivity: AppCompatActivity() {

    private lateinit var closeButton: ImageButton
    private lateinit var applyButton: Button
    private lateinit var resetButton: Button
    private lateinit var checkboxesLayout: LinearLayout
    private lateinit var checkboxesMatrix: List<List<CheckBox>>

    private var tmpViewIndexes = viewIndexes.toSortedSet()
    private var tmpLogIndexes = logIndexes.toSortedSet()
    private var tmpFileIndexes = fileIndexes.toSortedSet()

    companion object {
        val SRC_VIEW_INDEXES = listOf(2, 4, 6, 7).map { it - 1 }.toSortedSet()
        val SRC_LOG_INDEXES = listOf(1, 2, 3, 4, 5, 6, 7, 8).map { it - 1 }.toSortedSet()
        val SRC_FILE_INDEXES = listOf(1, 3, 4).map { it - 1 }.toSortedSet()

        var viewIndexes: SortedSet<Int> = SRC_VIEW_INDEXES
        var logIndexes: SortedSet<Int> = SRC_LOG_INDEXES
        var fileIndexes: SortedSet<Int> = SRC_FILE_INDEXES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        closeButton = findViewById(R.id.closeButton)
        applyButton = findViewById(R.id.applyButton)
        resetButton = findViewById(R.id.resetButton)
        checkboxesLayout = findViewById(R.id.checkboxesLayout)
        checkboxesMatrix = checkboxesLayout.children
            .filterIndexed { i, _ -> i > 1 && i != checkboxesLayout.childCount - 1 }
            .map { view ->
                val innerLayout = view as LinearLayout
                innerLayout.children
                    .filterIndexed { i, _ -> i != 0 }
                    .map { innerView -> innerView as CheckBox }
                    .toList()
            }
            .toList()

        closeButton.setOnClickListener { finish() }
        applyButton.setOnClickListener { onApplyButtonClickListener() }
        resetButton.setOnClickListener { onResetButtonClickListener() }

        checkboxesMatrix.forEachIndexed { i, checkboxes ->
            checkboxes.forEachIndexed { j, checkbox ->
                checkbox.setOnClickListener { onCheckboxClickListener(checkbox, i, j) }
            }
        }

        listOf(tmpViewIndexes, tmpLogIndexes, tmpFileIndexes).forEachIndexed { j, indexes ->
            for (i in indexes) {
                checkboxesMatrix[i][j].isChecked = true
            }
        }
        updateResetButtonView()
    }

    private fun onResetButtonClickListener() {
        tmpViewIndexes = SRC_VIEW_INDEXES
        tmpLogIndexes = SRC_LOG_INDEXES
        tmpFileIndexes = SRC_FILE_INDEXES
        val message = "Success of reset menu indexes."
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        onApplyButtonClickListener()
    }

    private fun onCheckboxClickListener(checkBox: CheckBox, i: Int, j: Int) {
        when (j) {
            0 -> if (checkBox.isChecked) tmpViewIndexes += i else tmpViewIndexes -= i
            1 -> if (checkBox.isChecked) tmpLogIndexes += i else tmpLogIndexes -= i
            2 -> if (checkBox.isChecked) tmpFileIndexes += i else tmpFileIndexes -= i
            else -> throw IllegalArgumentException("invalid j argument: $j")
        }
    }

    private fun updateResetButtonView() {
        resetButton.visibility = when (
            viewIndexes != SRC_VIEW_INDEXES ||
            logIndexes != SRC_LOG_INDEXES ||
            fileIndexes != SRC_FILE_INDEXES
        ) {
            true -> View.VISIBLE
            false -> View.GONE
        }
    }

    private fun onApplyButtonClickListener() {
        viewIndexes = tmpViewIndexes
        logIndexes = tmpLogIndexes
        fileIndexes = tmpFileIndexes
        val sharedPreferences = getSharedPreferences("menu_indexes", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("view", tmpViewIndexes.map { i -> i.toString() }.toSet())
        editor.putStringSet("log", tmpLogIndexes.map { i -> i.toString() }.toSet())
        editor.putStringSet("file", tmpFileIndexes.map { i -> i.toString() }.toSet())
        editor.apply()
        setResult(Activity.RESULT_OK)
        finish()
    }
}