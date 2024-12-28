package ru.ddp.lab4.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import ru.ddp.lab4.LaptopDbHelper
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_BATTERY_LIFE
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_HAS_3G
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_ID
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_MASS
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_OS
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_RAM
import ru.ddp.lab4.LaptopDbHelper.Companion.COLUMN_SCREEN_DIAGONAL
import ru.ddp.lab4.LaptopDbHelper.Companion.TABLE_NAME
import ru.ddp.lab4.R
import ru.ddp.lab4.activities.MenuActivity.Companion.fileIndexes
import ru.ddp.lab4.activities.MenuActivity.Companion.logIndexes
import ru.ddp.lab4.activities.MenuActivity.Companion.viewIndexes
import ru.ddp.lab4.getItems
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class QueryActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var buttonsLayout: LinearLayout
    private lateinit var dbHelper: LaptopDbHelper

    private lateinit var buttons: List<Button>
    private var queryIndex = -1

    private val menuActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult -> enableButtons() }
    private val popupActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult -> handlePopupActivityResult(result) }

    companion object {
        val CASTLED_MASS = "CAST(ROUND($COLUMN_MASS, 2) AS TEXT) || ' kg' AS $COLUMN_MASS"
        val CASTLED_HAS_3G = "CASE WHEN $COLUMN_HAS_3G = 0 THEN 'no' ELSE 'yes' END AS $COLUMN_HAS_3G"
        val CASTLED_SCREEN_DIAGONAL = "CAST($COLUMN_SCREEN_DIAGONAL AS TEXT) || '″' AS $COLUMN_SCREEN_DIAGONAL"
        val CASTLED_RAM = "CAST($COLUMN_RAM AS TEXT) || ' GB' AS $COLUMN_RAM"
        val CASTLED_BATTERY_LIFE = "CAST($COLUMN_BATTERY_LIFE AS TEXT) || ' mAh' AS $COLUMN_BATTERY_LIFE"

        val CASTLED_ALL = """
            $COLUMN_ID, 
            $COLUMN_OS, 
            $CASTLED_MASS, 
            $CASTLED_HAS_3G, 
            $CASTLED_SCREEN_DIAGONAL, 
            $CASTLED_RAM, 
            $CASTLED_BATTERY_LIFE
        """.trimIndent()

        val GET_ALL_QUERY = """
SELECT 
${CASTLED_ALL.replaceIndent("    ")} 
FROM 
    $TABLE_NAME
        """.trimIndent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query)

        backButton = findViewById(R.id.backButton)
        menuButton = findViewById(R.id.menuButton)
        buttonsLayout = findViewById(R.id.buttonsLayout)
        dbHelper = LaptopDbHelper(this)

        backButton.setOnClickListener { finish() }
        menuButton.setOnClickListener { onMenuButtonClickListener() }
        buttons = buttonsLayout.children.map { it as Button }.toList()
        buttons.forEach { button ->
            button.setOnClickListener { onQueryButtonClickListener(button) }
        }
    }

    private fun disableButtons() {
        buttons.forEach { it.isEnabled = false }
        backButton.isEnabled = false
        menuButton.isEnabled = false
    }

    private fun enableButtons() {
        buttons.forEach { it.isEnabled = true }
        backButton.isEnabled = true
        menuButton.isEnabled = true
    }

    private fun handlePopupActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val query = result.data?.getStringExtra("query")?: GET_ALL_QUERY
            val cursor = dbHelper.readableDatabase.rawQuery(query, null)
            cursor.use {
                if (queryIndex in logIndexes) logQueryTable(query, cursor)
                if (queryIndex in fileIndexes) writeAsCsvFile(cursor)
                if (queryIndex in viewIndexes) {
                    val intent = Intent()
                    intent.putExtra("query", query)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
        enableButtons()
    }

    private fun onMenuButtonClickListener() {
        disableButtons()
        val intent = Intent(this@QueryActivity, MenuActivity::class.java)
        menuActivityResultLauncher.launch(intent)
    }

    private fun onQueryButtonClickListener(button: Button) {
        disableButtons()
        queryIndex = buttons.indexOf(button)
        Log.d("auf", queryIndex.toString())
        val action = button.contentDescription.toString().replace(" ", "_").uppercase() + "_QUERY"
        val intent = Intent("${applicationContext.packageName}.action.$action")
        popupActivityResultLauncher.launch(intent)
    }

    private fun logQueryTable(query: String, cursor: Cursor) {
        val printableTable = cursor.toPrintableTable()
        val message = "result of query:\n$query\nis:\n$printableTable"
        Log.v(this::class.simpleName, message)
        appendToLogFile(message)
    }

    private fun Cursor.toPrintableTable(): String {
        val items = getItems()
        val columnWidths = columnNames.map { it.length + 2 }.toMutableList()
        items.forEachIndexed { i, _ ->
            for (j in 0..<columnCount) {
                columnWidths[j] = maxOf(columnWidths[j], items[i][j].length + 2)
            }
        }
        val separatorLine = columnWidths.joinToString("+", "+", "+") {
            "-".repeat(it)
        } + "\n"
        val headerLine = columnNames.mapIndexed { index, name ->
            name.center(columnWidths[index])
        }.joinToString("|", "|", "|\n")
        val tableRows = items.joinToString("") { item ->
            columnNames.indices.joinToString("|", "|", "|\n") { index ->
                item[index].center(columnWidths[index])
            }
        }
        return separatorLine + headerLine + separatorLine + tableRows + separatorLine
    }

    private fun writeAsCsvFile(cursor: Cursor): File? {
        val appDir = getExternalFilesDir(null)
            ?: throw IllegalArgumentException("Bad getting of external files directory")
        if (!appDir.exists()) {
             appDir.mkdirs()
        }
        val files = appDir.listFiles { file ->
            file.isFile && file.name.matches(Regex("query\\d*\\.csv"))
        }
        val maxFileNumber = files?.maxOfOrNull { file ->
            file.name.removePrefix("query").removeSuffix(".csv").toIntOrNull() ?: 1
        }
        val filename = "query${maxFileNumber?.let { i -> i + 1 }?.toString() ?: ""}.csv"

        val csvFile = appDir.resolve(filename)
        try {
            FileWriter(csvFile).use { writer ->
                val separator = ","
                val header = cursor.columnNames.joinToString(separator)
                writer.appendLine(header)
                for (row in cursor.getItems()) {
                    val line = row.joinToString(separator)
                    writer.appendLine(line)
                }
            }
            showLongToastMessage("Success of $filename creation.")
            return csvFile
        } catch (e: IOException) {
            e.printStackTrace()
            showLongToastMessage("Bad $filename creation!")
            return null
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun appendToLogFile(message: String): File? {
        val appDir = getExternalFilesDir(null)
            ?: throw IllegalArgumentException("Bad getting of external files directory")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        val filename = "log.txt"

        val logFile = appDir.resolve(filename)
        try {
            FileWriter(logFile, true).use { writer ->
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val currentDate = sdf.format(Date())
                writer.appendLine("$currentDate : message:\n$message\n")
            }
            showLongToastMessage("Success log added.")
            return logFile
        } catch (e: IOException) {
            e.printStackTrace()
            showLongToastMessage("Bad log added!")
            return null
        }
    }

    private fun String.center(width: Int): String {
        if (this.length >= width) {
            return this  // String is already wider, or same width
        }
        val paddingSize = width - this.length
        val leftPadding = " ".repeat(paddingSize - paddingSize / 2)
        val rightPadding = " ".repeat(paddingSize / 2)

        return "$leftPadding$this$rightPadding"
    }

    private fun showLongToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}