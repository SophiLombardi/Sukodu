package com.example.myaplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myaplication.di.ViewModelFactory
import com.example.myaplication.logic.Difficulty
import com.example.myaplication.ui.game.GameScreen
import com.example.myaplication.ui.puzzle.PuzzleListScreen

object Routes {
    const val PUZZLES = "puzzles"
    const val GAME = "game/{difficulty}"
    fun game(difficulty: Difficulty) = "game/${difficulty.name}"
}

@Composable
fun SudokuApp(factory: ViewModelFactory) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.PUZZLES) {
        composable(Routes.PUZZLES) {
            PuzzleListScreen(
                onPlay = { difficulty ->
                    navController.navigate(Routes.game(difficulty))
                }
            )
        }
        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
        ) { entry ->
            val difficulty = Difficulty.fromName(entry.arguments?.getString("difficulty"))
            GameScreen(
                viewModel = viewModel(factory = factory),
                difficulty = difficulty,
                onBack = { navController.popBackStack() }
            )
        }
    }
}