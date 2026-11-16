package com.example.myaplication.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myaplication.logic.Difficulty
import com.example.myaplication.ui.components.DifficultyFilter
import com.example.myaplication.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onEditPuzzle: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Painel do Administrador") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    val total = uiState.countByDifficulty.values.sum()
                    Text("$total puzzles cadastrados", fontWeight = FontWeight.Bold)
                    Text(
                        Difficulty.entries.joinToString(" • ") { d -> "${d.label}: ${uiState.countByDifficulty[d.label] ?: 0}" }
                    )
                    Text("Vindos da API: ${uiState.countFromApi}")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.importFromApi(3) },
                    enabled = !uiState.isImporting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isImporting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                    Text("  Importar da API")
                }

                OutlinedButton(
                    onClick = { onEditPuzzle(0L) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("  Novo puzzle")
                }
            }

            uiState.errorMessage?.let {
                MessageBanner(it, isError = true, onDismiss = viewModel::dismissMessages)
            }
            uiState.message?.let {
                MessageBanner(it, isError = false, onDismiss = viewModel::dismissMessages)
            }

            DifficultyFilter(selected = uiState.filter, onSelect = viewModel::onFilterChange)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.puzzle, key = { it.id }) { puzzle ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(puzzle.name, fontWeight = FontWeight.Bold)
                                Text(
                                    "${Difficulty.fromName(puzzle.difficulty).label} • ${puzzle.source} • id ${puzzle.id}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { onEditPuzzle(puzzle.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { viewModel.deletePuzzle(puzzle) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Excluir",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}