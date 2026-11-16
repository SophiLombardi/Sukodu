package com.example.myaplication.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myaplication.ui.components.DifficultyFilter
import com.example.myaplication.ui.game.SudokuBoard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleEditScreen(
    viewModel: PuzzleEditViewModel,
    puzzleId: Long,
    onBack: () -> Unit
) {
    LaunchedEffect(puzzleId) { viewModel.load(puzzleId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.finished) { if (uiState.finished) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isNew) "Novo puzzle" else "Editar puzzle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nome do puzzle") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Dificuldade", style = MaterialTheme.typography.titleMedium)
            DifficultyFilter(
                selected = uiState.difficulty,
                onSelect = { it?.let(viewModel::onDifficultyChange) },
                showAllOption = false
            )

            if (uiState.isNew) {
                Text(
                    "Ao salvar, o tabuleiro é gerado automaticamente pelo algoritmo de backtracking.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("Origem: ${uiState.source}", style = MaterialTheme.typography.bodySmall)
                SudokuBoard(
                    board = uiState.preview,
                    fixedCells = uiState.preview.indices.toSet(),
                    wrongCells = emptySet(),
                    selectedIndex = null,
                    showErrors = false,
                    onCellClick = {}
                )
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isNew) "Criar puzzle" else "Salvar alterações")
            }

            if (!uiState.isNew) {
                OutlinedButton(
                    onClick = viewModel::delete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Excluir puzzle", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}