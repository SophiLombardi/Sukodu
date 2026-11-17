package com.example.myaplication.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configurações") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.errorMessage?.let {
                MessageBanner(
                    message = it,
                    isError = true,
                    onDismiss = viewModel::dismissError
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(uiState.userName, fontWeight = FontWeight.Bold)
                        Text(
                            if (uiState.isAdmin) "Perfil: Administrador"
                            else "Perfil: Jogador"
                        )
                    }
                }
            }

            Text(
                "Limite de erros por partida",
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3 to "3 erros", 5 to "5 erros", 0 to "Sem limite")
                    .forEach { (value, label) ->
                        FilterChip(
                            selected = uiState.maxErrors == value,
                            onClick = { viewModel.onMaxErrorsChange(value) },
                            label = { Text(label) }
                        )
                    }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Destacar erros em vermelho",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Mostra na hora quando um número está errado",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = uiState.showErrors,
                    onCheckedChange = viewModel::onShowErrorsChange
                )
            }

            HorizontalDivider()

            Text(
                "Dificuldade padrão na lista",
                style = MaterialTheme.typography.titleMedium
            )
            DifficultyFilter(
                selected = uiState.defaultDifficulty,
                onSelect = viewModel::onDefaultDifficultyChange
            )

            HorizontalDivider()

            OutlinedButton(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Text(" Sair / Trocar usuário")
            }
        }
    }
}