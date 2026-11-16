package com.example.myaplication.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myaplication.di.ViewModelFactory
import com.example.myaplication.ui.admin.AdminScreen
import com.example.myaplication.ui.admin.PuzzleEditScreen
import com.example.myaplication.ui.game.GameScreen
import com.example.myaplication.ui.login.LoginScreen
import com.example.myaplication.ui.login.LoginViewModel
import com.example.myaplication.ui.puzzle.PuzzleListScreen
import com.example.myaplication.ui.ranking.RankingScreen
import com.example.myaplication.ui.settings.SettingsScreen

object Routes {
    const val LOGIN = "login"
    const val PUZZLES = "puzzles"
    const val GAME = "game/{puzzleId}"
    const val RANKING = "ranking"
    const val SETTINGS = "settings"
    const val ADMIN = "admin"
    const val PUZZLE_EDIT = "admin/edit/{puzzleId}"

    fun game(puzzleId: Long) = "game/$puzzleId"
    fun puzzleEdit(puzzleId: Long) = "admin/edit/$puzzleId"
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

private val bottomItems = listOf(
    BottomItem(Routes.PUZZLES, "Jogar", Icons.Filled.PlayArrow),
    BottomItem(Routes.RANKING, "Ranking", Icons.Filled.Star),
    BottomItem(Routes.SETTINGS, "Ajustes", Icons.Filled.Settings),
    BottomItem(Routes.ADMIN, "Admin", Icons.Filled.Build, adminOnly = true)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SudokuApp(factory: ViewModelFactory) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val session = loginState.session
    val isAdmin = session?.isAdmin == true

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val visibleItems = bottomItems.filter { !it.adminOnly || isAdmin }
    val showBottomBar = currentRoute in visibleItems.map { it.route }

    LaunchedEffect(session) {
        val route = navController.currentDestination?.route
        if (session == null && route != null && route != Routes.LOGIN) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        } else if (session != null && route == Routes.LOGIN) {
            navController.navigate(Routes.PUZZLES) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    visibleItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.PUZZLES) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(viewModel = loginViewModel)
            }
            composable(Routes.PUZZLES) {
                PuzzleListScreen(
                    viewModel = viewModel(factory = factory),
                    onPuzzleClick = { id -> navController.navigate(Routes.game(id)) }
                )
            }
            composable(
                route = Routes.GAME,
                arguments = listOf(navArgument("puzzleId") { type = NavType.LongType })
            ) { entry ->
                val puzzleId = entry.arguments?.getLong("puzzleId") ?: 0L
                GameScreen(
                    viewModel = viewModel(factory = factory),
                    puzzleId = puzzleId,
                    onBack = { navController.popBackStack() },
                    onOpenRanking = {
                        navController.navigate(Routes.RANKING) { popUpTo(Routes.PUZZLES) }
                    }
                )
            }
            composable(Routes.RANKING) {
                RankingScreen(viewModel = viewModel(factory = factory))
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(viewModel = viewModel(factory = factory))
            }
            composable(Routes.ADMIN) {
                if (isAdmin) {
                    AdminScreen(
                        viewModel = viewModel(factory = factory),
                        onEditPuzzle = { id -> navController.navigate(Routes.puzzleEdit(id)) }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Acesso restrito ao administrador.")
                    }
                }
            }
            composable(
                route = Routes.PUZZLE_EDIT,
                arguments = listOf(navArgument("puzzleId") { type = NavType.LongType })
            ) { entry ->
                val puzzleId = entry.arguments?.getLong("puzzleId") ?: 0L
                PuzzleEditScreen(
                    viewModel = viewModel(factory = factory),
                    puzzleId = puzzleId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}