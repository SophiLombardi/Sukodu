package com.example.myaplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myaplication.data.local.entity.PuzzleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzles ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PuzzleEntity>>
    @Query("SELECT * FROM puzzles WHERE id = :id")
    suspend fun getById(id: Long): PuzzleEntity?
    @Query("SELECT COUNT(*) FROM puzzles")
    suspend fun count(): Int
    @Insert
    suspend fun insert(puzzle: PuzzleEntity): Long
    @Update
    suspend fun update(puzzle: PuzzleEntity)
    @Delete
    suspend fun delete(puzzle: PuzzleEntity)
}
