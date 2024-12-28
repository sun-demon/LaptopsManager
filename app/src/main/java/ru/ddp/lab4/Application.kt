package ru.ddp.lab4

import android.annotation.SuppressLint
import ru.ddp.lab4.activities.MenuActivity.Companion.fileIndexes
import ru.ddp.lab4.activities.MenuActivity.Companion.logIndexes
import ru.ddp.lab4.activities.MenuActivity.Companion.viewIndexes
import android.app.Application as DefaultApplication

class Application: DefaultApplication() {

    @SuppressLint("CommitPrefEdits")
    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("menu_indexes", MODE_PRIVATE)
        sharedPreferences.getStringSet("view", null)?.let { stringSet ->
            viewIndexes = stringSet.map { str -> str.toInt() }.toSortedSet()
        }
        sharedPreferences.getStringSet("log", null)?.let { stringSet ->
            logIndexes = stringSet.map { str -> str.toInt() }.toSortedSet()
        }
        sharedPreferences.getStringSet("file", null)?.let { stringSet ->
            fileIndexes = stringSet.map { str -> str.toInt() }.toSortedSet()
        }
    }
}