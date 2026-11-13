package com.example.myapplication.ui.puzzle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import com.example.myapplication.logic.Difficulty
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PuzzleListScreen(onPlay: (Difficulty) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp,
            Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SUDOKU", fontSize = 40.sp, fontWeight = FontWeight.Bold, color
        = MaterialTheme.colorScheme.primary)
        Text("Escolha a dificuldade para começar", style =
            MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Difficulty.entries.forEach { difficulty ->
            Button(onClick = { onPlay(difficulty) }, modifier =
                Modifier.fillMaxWidth()) {
                Text(difficulty.label)
            }
        }
    }
}
