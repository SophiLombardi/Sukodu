package com.example.myaplication.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun SudokuBoard(
    board: List<Int>,
    fixedCells: Set<Int>,
    wrongCells: Set<Int>,
    selectedIndex: Int?,
    showErrors: Boolean,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.onSurface
    val selectedValue = selectedIndex?.let { board[it] } ?: 0
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, lineColor)
            .drawWithContent {
                drawContent()
                val cell = size.width / 9
                for (i in 1 until 9) {
                    val stroke = if (i % 3 == 0) 2.dp.toPx() else
                        0.5.dp.toPx()
                    drawLine(lineColor, Offset(i * cell, 0f), Offset(i *
                            cell, size.height), stroke)
                    drawLine(lineColor, Offset(0f, i * cell),
                        Offset(size.width, i * cell), stroke)
                }
            }
    ) {
        for (row in 0 until 9) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until 9) {
                    val index = row * 9 + col
                    SudokuCell(
                        value = board[index],
                        isFixed = index in fixedCells,
                        isWrong = showErrors && index in wrongCells,
                        isSelected = index == selectedIndex,
                    isRelated = selectedIndex != null && isRelated(index,
                        selectedIndex),
                    isSameNumber = selectedValue != 0 && board[index] ==
                            selectedValue,
                    onClick = { onCellClick(index) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
    }
}
private fun isRelated(a: Int, b: Int): Boolean {
    val sameRow = a / 9 == b / 9
    val sameCol = a % 9 == b % 9
    val sameBox = (a / 27 == b / 27) && ((a % 9) / 3 == (b % 9) / 3)
    return sameRow || sameCol || sameBox
}
@Composable
private fun SudokuCell(
    value: Int,
    isFixed: Boolean,
    isWrong: Boolean,
    isSelected: Boolean,
    isRelated: Boolean,
    isSameNumber: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val background = when {
        isSelected -> primary.copy(alpha = 0.35f)
        isSameNumber -> primary.copy(alpha = 0.22f)
        isRelated -> primary.copy(alpha = 0.08f)
        else -> Color.Transparent
    }
    val textColor = when {
        isWrong -> MaterialTheme.colorScheme.error
        isFixed -> MaterialTheme.colorScheme.onSurface
        else -> primary
    }
    Box(
        modifier = modifier.background(background).clickable(onClick =
            onClick),
    contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                color = textColor,
                fontSize = 20.sp,
                fontWeight = if (isFixed) FontWeight.Bold else
                    FontWeight.Normal
            )
        }
    }
}
@Composable
fun NumberPad(
    remaining: List<Int>,
    enabled: Boolean,
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement =
        Arrangement.spacedBy(4.dp)) {
        for (number in 1..9) {
            val left = remaining[number - 1]
            FilledTonalButton(
                onClick = { onNumberClick(number) },
                enabled = enabled && left > 0,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(number.toString(), fontSize = 20.sp, fontWeight =
                        FontWeight.Bold)
                    Text(left.coerceAtLeast(0).toString(), fontSize = 10.sp)
                }
            }
        }
    }
}
