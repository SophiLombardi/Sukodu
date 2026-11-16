package com.example.myaplication.di

import android.app.Application
import android.content.Context
import com.example.myaplication.data.local.SudokuDatabase
import com.example.myaplication.data.remote.RetrofitClient
import com.example.myaplication.data.repository.SudokuRepository

class AppContainer(context: Context) {
    private val database = SudokuDatabase.getInstance(context)
    val repository = SudokuRepository(
        puzzleDao = database.puzzleDao(),
        scoreDao = database.scoreDao(),
        settingsDao = database.settingsDao(),
        api = RetrofitClient.api
    )
    val viewModelFactory = ViewModelFactory(repository)
}

class SudokuApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}