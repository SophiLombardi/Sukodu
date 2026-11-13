package com.example.myaplication.logic

object BoardFormat {
    fun toText(board: List<Int>): String = board.joinToString(separator = "")
    fun fromText(text: String): List<Int> = text.map { it.digitToIntOrNull()
        ?: 0 }
    fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}