package com.example.myaplication.ui.ranking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myaplication.ui.components.DifficultyFilter
import com.example.myaplication.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(viewModel: RankingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking") },
                actions = {
                    if (uiState.isAdmin && uiState.items.isNotEmpty()) {
                        TextButton(onClick = viewModel::clearAll) {
                            Text("Zerar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyFilter(
                selected = uiState.filter,
                onSelect = viewModel::onFilterChange
            )

            if (!uiState.isAdmin) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Só minhas partidas", modifier = Modifier.weight(1f))
                    Switch(
                        checked = uiState.onlyMine,
                        onCheckedChange = viewModel::onOnlyMineChange
                    )
                }
            }

            uiState.errorMessage?.let {
                MessageBanner(
                    message = it,
                    isError = true,
                    onDismiss = viewModel::dismissMessages
                )
            }

            uiState.message?.let {
                MessageBanner(
                    message = it,
                    isError = false,
                    onDismiss = viewModel::dismissMessages
                )
            }

            if (uiState.items.isEmpty() && !uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma pontuação ainda. Vença um puzzle!")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.items, key = { it.score.id }) { item ->
                        RankingCard(
                            item = item,
                            isAdmin = uiState.isAdmin,
                            onEdit = { viewModel.startEdit(item.score) },
                            onDelete = { viewModel.delete(item.score) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.editingScore != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelEdit,
            title = { Text("Editar jogador") },
            text = {
                OutlinedTextField(
                    value = uiState.editName,
                    onValueChange = viewModel::onEditNameChange,
                    label = { Text("Nome do jogador") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = viewModel::saveEdit) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelEdit) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun RankingCard(
    item: RankingItemUi,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val medal = when (item.position) {
        1 -> " 🥇 "
        2 -> " 🥈 "
        3 -> " 🥉 "
        else -> "${item.position}º"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = medal,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.width(44.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.score.playerName, fontWeight = FontWeight.Bold)
                Text(
                    text = "${item.score.puzzleName} • ${item.difficultyLabel}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = " ⏱ ${item.timeText} • erros: ${item.score.errors} • ${item.dateText}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "${item.score.score}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (isAdmin) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
        }
    }
}