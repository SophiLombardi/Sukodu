package com.example.myaplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.ui.game.GameViewModel

class ViewModelFactory(
    private val repository: SudokuRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel()
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        } as T
    }
}