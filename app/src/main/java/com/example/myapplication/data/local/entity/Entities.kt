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