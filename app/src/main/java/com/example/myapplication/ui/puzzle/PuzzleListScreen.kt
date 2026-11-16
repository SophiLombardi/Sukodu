package com.example.myaplication.ui.puzzle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.myaplication.ui.components.DifficultyFilter
import com.example.myaplication.ui.components.MessageBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleListScreen(
    viewModel: PuzzleListViewModel,
    onPuzzleClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Olá, ${uiState.playerName}! Escolha um puzzle") }
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
            OutlinedTextField(
                value = uiState.search,
                onValueChange = viewModel::onSearchChange,
                label = { Text("Buscar por nome") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            DifficultyFilter(
                selected = uiState.filter,
                onSelect = viewModel::onFilterChange
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ordenar:", style = MaterialTheme.typography.labelLarge)
                PuzzleSort.entries.forEach { sort ->
                    FilterChip(
                        selected = uiState.sort == sort,
                        onClick = { viewModel.onSortChange(sort) },
                        label = { Text(sort.label) }
                    )
                }
            }

            uiState.errorMessage?.let {
                MessageBanner(
                    message = it,
                    isError = true,
                    onDismiss = viewModel::dismissError
                )
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum puzzle encontrado.")
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            PuzzleCard(
                                item = item,
                                onClick = { onPuzzleClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PuzzleCard(
    item: PuzzleItemUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("${item.difficulty.label} • ${item.emptyCells} casas vazias • origem: ${item.source}")
                Text(
                    text = item.bestScore?.let { "Recorde: $it pts" } ?: "Ainda sem recorde",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Jogar"
            )
        }
    }
}