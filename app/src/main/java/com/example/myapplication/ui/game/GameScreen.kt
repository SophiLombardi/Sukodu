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
import androidx.compose.material.icons.filled.Info
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
import com.example.myaplication.logic.BoardFormat
import com.example.myaplication.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    puzzleId: Long,
    onBack: () -> Unit,
    onOpenRanking: () -> Unit
) {
    LaunchedEffect(puzzleId) { viewModel.loadPuzzle(puzzleId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.puzzleName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.status == GameStatus.LOADING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
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
            uiState.errorMessage?.let {
                MessageBanner(
                    message = it,
                    isError = true,
                    onDismiss = viewModel::dismissError
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Nível: ${uiState.difficulty.label}")
                Text(" ⏱ ${BoardFormat.formatTime(uiState.seconds)}")
                val limit = if (uiState.maxErrors == 0) "∞" else uiState.maxErrors.toString()
                Text(
                    text = "Erros: ${uiState.errors}/$limit",
                    color = if (uiState.errors > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            SudokuBoard(
                board = uiState.board,
                fixedCells = uiState.fixedCells,
                wrongCells = uiState.wrongCells,
                selectedIndex = uiState.selectedIndex,
                showErrors = uiState.showErrors,
                onCellClick = viewModel::selectCell
            )

            NumberPad(
                remaining = uiState.remainingNumbers,
                enabled = uiState.status == GameStatus.PLAYING,
                onNumberClick = viewModel::enterNumber
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = viewModel::erase) {
                    Icon(Icons.Filled.Clear, contentDescription = null)
                    Text(" Apagar")
                }
                TextButton(onClick = viewModel::useHint) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Text(" Dica (${uiState.hintsUsed})")
                }
                TextButton(onClick = viewModel::restart) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Text(" Reiniciar")
                }
            }
        }
    }

    when (uiState.status) {
        GameStatus.WON -> AlertDialog(
            onDismissRequest = {},
            title = { Text(" 🎉 Parabéns!") },
            text = {
                Column {
                    Text("Você resolveu o puzzle!")
                    Text("Tempo: ${BoardFormat.formatTime(uiState.seconds)}")
                    Text("Erros: ${uiState.errors} • Dicas: ${uiState.hintsUsed}")
                    Text(
                        text = "Pontuação: ${uiState.finalScore}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            confirmButton = {
                Button(onClick = onOpenRanking) { Text("Ver ranking") }
            },
            dismissButton = {
                OutlinedButton(onClick = onBack) { Text("Voltar") }
            }
        )

        GameStatus.LOST -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Fim de jogo") },
            text = { Text("Você atingiu o limite de ${uiState.maxErrors} erros.") },
            confirmButton = {
                Button(onClick = viewModel::restart) { Text("Tentar de novo") }
            },
            dismissButton = {
                OutlinedButton(onClick = onBack) { Text("Voltar") }
            }
        )

        GameStatus.ERROR -> AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Erro") },
            text = { Text(uiState.errorMessage ?: "Não foi possível abrir o puzzle.") },
            confirmButton = {
                Button(onClick = onBack) { Text("Voltar") }
            }
        )

        else -> Unit
    }
}