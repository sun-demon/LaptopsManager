package ru.ddp.lab4

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.random.Random

class LaptopDbHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "laptops.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "laptops"
        const val COLUMN_ID = "id"
        const val COLUMN_OS = "operating_system"
        const val COLUMN_MASS = "mass"
        const val COLUMN_HAS_3G = "has_3g"
        const val COLUMN_SCREEN_DIAGONAL = "screen_diagonal"
        const val COLUMN_RAM = "ram"
        const val COLUMN_BATTERY_LIFE = "battery_life"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_OS TEXT,
                $COLUMN_MASS REAL,
                $COLUMN_HAS_3G INTEGER,
                $COLUMN_SCREEN_DIAGONAL REAL,
                $COLUMN_RAM INTEGER,
                $COLUMN_BATTERY_LIFE INTEGER
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
        populateDatabase(db)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun populateDatabase(db: SQLiteDatabase) {
        val count = getTableRowCount(db)
        if (count == 0) {
            val laptops = generateSampleLaptops(30 + Random.nextInt(20))
            insertAllLaptops(db, laptops)
        }
    }

    private fun getTableRowCount(db: SQLiteDatabase): Int {
        val selectCountQuery = "SELECT COUNT(*) FROM $TABLE_NAME"
        var count = 0
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectCountQuery, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    count = it.getInt(0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            count = 0
        } finally {
            cursor?.close()
        }
        return count
    }

    private fun insertAllLaptops(db: SQLiteDatabase, laptops: List<Laptop>): Boolean {
        var success: Boolean
        db.beginTransaction()
        try {
            for(laptop in laptops){
                val values = ContentValues().apply {
                    put(COLUMN_OS, laptop.operatingSystem)
                    put(COLUMN_MASS, laptop.mass)
                    put(COLUMN_HAS_3G, if (laptop.has3G) 1 else 0)
                    put(COLUMN_SCREEN_DIAGONAL, laptop.screenDiagonal)
                    put(COLUMN_RAM, laptop.ram)
                    put(COLUMN_BATTERY_LIFE, laptop.batteryLife)
                }
                db.insertOrThrow(TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
            success = true
        } catch (e: Exception) {
            Log.e("insertAllLaptops", "Error inserting laptops", e)
            success = false
        } finally {
            db.endTransaction()
        }
        return success
    }

    private fun generateSampleLaptops(count: Int): List<Laptop> {
        val operatingSystems = listOf(
            "Windows 7",
            "Windows 8",
            "Windows 10",
            "Windows 11",
            "macOS Monterey",
            "macOS Ventura",
            "Linux Mint",
            "Ubuntu",
            "Fedora",
            "Chrome OS"
        )
        return List(count) {
            Laptop(
                operatingSystem = operatingSystems[Random.nextInt(operatingSystems.size)],
                mass = Random.nextLaptopMass(),
                has3G = Random.nextBoolean(),
                screenDiagonal = Random.nextLaptopScreenDiagonal(),
                ram = Random.nextLaptopRam(),
                batteryLife = Random.nextBatteryLife()
            )
        }
    }

    private fun Random.nextLaptopMass(): Double {
        val sourceMass: Double = 1.0 + nextDouble() * 2.5
        val result = (sourceMass * 100).roundToInt() / 100.0
        return result
    }

    private fun Random.nextLaptopScreenDiagonal(): Double {
        val sourceScreenDiagonal = 10.0 + nextDouble() * 8.0
        val realDiagonals =
            listOf(
                11.6,
                12.1, 12.3, 13.0, 13.3, 13.5,
                14.0,
                15.0, 15.6, 16.0, 16.2,
                17.0, 17.3, 18.0
            )
        val result = findClosestDouble(realDiagonals, sourceScreenDiagonal)
        return result!!
    }

    private fun findClosestDouble(array: List<Double>, target: Double): Double? {
        if (array.isEmpty()) return null
        var closest = array[0]
        var minDiff = abs(array[0].minus(target))
        for (i in 1 until array.size) {
            val currentDiff = abs(array[i] - target)
            if (currentDiff < minDiff) {
                minDiff = currentDiff
                closest = array[i]
            }
        }
        return closest
    }

    private fun Random.nextLaptopRam(): Int {
        val sourceRam = 4 + nextInt(60)
        val result = 1 shl (8 - log2(sourceRam.toDouble()).roundToInt())
        return result
    }

    private fun Random.nextBatteryLife(): Int {
        val sourceBatteryLife = 2000 + nextInt(6000)
        val result = (sourceBatteryLife / 100.0).roundToInt() * 100
//            if (sourceBatteryLife < 7000)
//                (sourceBatteryLife / 500.0).roundToInt() * 500
//            else
//                (sourceBatteryLife / 1000.0).roundToInt() * 1000
        return result
    }

    fun selectAll(): Cursor {
        return readableDatabase.query(TABLE_NAME, null, null, null, null, null, null)
    }
}