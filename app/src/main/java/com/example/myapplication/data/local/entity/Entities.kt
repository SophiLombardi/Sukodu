package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val difficulty: String,
    val givens: String,
    val solution: String,
    val source: String,
    val createdAt: Long = System.currentTimeMillis()
)
@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerName: String,
    val puzzleName: String,
    val difficulty: String,
    val score: Int,
    val timeSeconds: Int,
    val errors: Int,
    val date: Long = System.currentTimeMillis()
)
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val maxErrors: Int = 3,
    val showErrors: Boolean = true,
    val defaultDifficulty: String = "TODAS"
)
