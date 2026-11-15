package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.PuzzleEntity
import com.example.myapplication.data.local.entity.ScoreEntity
import com.example.myapplication.data.local.entity.SettingsEntity
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
    @Insert
    suspend fun insertAll(puzzles: List<PuzzleEntity>)
    @Update
    suspend fun update(puzzle: PuzzleEntity)
    @Delete
    suspend fun delete(puzzle: PuzzleEntity)
}
@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY score DESC")
    fun getAll(): Flow<List<ScoreEntity>>
    @Insert
    suspend fun insert(score: ScoreEntity): Long
    @Update
    suspend fun update(score: ScoreEntity)
    @Delete
    suspend fun delete(score: ScoreEntity)
    @Query("DELETE FROM scores")
    suspend fun deleteAll()
}
@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun get(): Flow<SettingsEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: SettingsEntity)
}