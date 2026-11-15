package com.example.myaplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.PuzzleDao
import com.example.myapplication.data.local.dao.ScoreDao
import com.example.myapplication.data.local.dao.SettingsDao
import com.example.myapplication.data.local.entity.PuzzleEntity
import com.example.myapplication.data.local.entity.ScoreEntity
import com.example.myapplication.data.local.entity.SettingsEntity

@Database(
    entities = [PuzzleEntity::class, ScoreEntity::class,
        SettingsEntity::class],
    version = 2,
exportSchema = false
)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
    abstract fun scoreDao(): ScoreDao
    abstract fun settingsDao(): SettingsDao
    companion object {
        @Volatile
        private var INSTANCE: SudokuDatabase? = null
        fun getInstance(context: Context): SudokuDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    "sudoku.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
