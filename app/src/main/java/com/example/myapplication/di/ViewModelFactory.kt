package com.example.myaplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myaplication.data.repository.SudokuRepository
import com.example.myaplication.ui.admin.AdminViewModel
import com.example.myaplication.ui.admin.PuzzleEditViewModel
import com.example.myaplication.ui.game.GameViewModel
import com.example.myaplication.ui.login.LoginViewModel
import com.example.myaplication.ui.puzzle.PuzzleListViewModel
import com.example.myaplication.ui.ranking.RankingViewModel
import com.example.myaplication.ui.settings.SettingsViewModel

class ViewModelFactory(
    private val repository: SudokuRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository)
            modelClass.isAssignableFrom(PuzzleListViewModel::class.java) -> PuzzleListViewModel(repository)
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(repository)
            modelClass.isAssignableFrom(RankingViewModel::class.java) -> RankingViewModel(repository)
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository)
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> AdminViewModel(repository)
            modelClass.isAssignableFrom(PuzzleEditViewModel::class.java) -> PuzzleEditViewModel(repository)
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        } as T
    }
}