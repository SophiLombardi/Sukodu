package com.example.myaplication.di

import android.app.Application
import android.content.Context
import com.example.myaplication.data.local.SudokuDatabase
import com.example.myaplication.data.repository.SudokuRepository

class AppContainer(context: Context) {
    private val database = SudokuDatabase.getInstance(context)
    val repository = SudokuRepository(
        puzzleDao = database.puzzleDao()
    )
    val viewModelFactory = ViewModelFactory(repository)
}

class SudokuApplication : Application() {
    // Adicione esta propriedade aqui dentro:
    val container by lazy {
        AppContainer(this)
    }
}