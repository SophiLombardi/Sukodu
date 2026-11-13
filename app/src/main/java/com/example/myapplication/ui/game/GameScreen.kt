package com.example.myaplication.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myaplication.logic.Difficulty
import com.example.myaplication.ui.game.SudokuBoard
import com.example.myaplication.ui.game.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    difficulty: Difficulty,
    onBack: () -> Unit
) {
    LaunchedEffect(difficulty) { viewModel.start(difficulty) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sudoku - ${uiState.difficulty.label}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.status == GameStatus.LOADING) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment =
                Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement =
                Arrangement.SpaceBetween) {
                Text("Nível: ${uiState.difficulty.label}")
                Text(
                    "Erros: ${uiState.errors}",
                    color = if (uiState.errors > 0)
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            SudokuBoard(
                board = uiState.board,
                fixedCells = uiState.fixedCells,
                wrongCells = uiState.wrongCells,
                selectedIndex = uiState.selectedIndex,
                showErrors = true,
            onCellClick = viewModel::selectCell
            )
            NumberPad(
                remaining = uiState.remainingNumbers,
                enabled = uiState.status == GameStatus.PLAYING,
                onNumberClick = viewModel::enterNumber
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement =
                Arrangement.SpaceEvenly) {
                TextButton(onClick = viewModel::erase) {
                    Icon(Icons.Filled.Clear, contentDescription = null)
                    Text(" Apagar")
                }
                TextButton(onClick = { viewModel.newGame(uiState.difficulty)
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Text(" Novo jogo")
                }
            }
        }
    }
    if (uiState.status == GameStatus.WON) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Parabéns!") },
            text = { Text("Você resolveu o Sudoku com ${uiState.errors} erro(s).") },
                    confirmButton = { Button(onClick = {
                viewModel.newGame(uiState.difficulty) }) { Text("Novo jogo") } },
                dismissButton = { OutlinedButton(onClick = onBack) {
                    Text("Voltar") } }
            )
            }
    }
