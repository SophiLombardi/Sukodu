package com.example.myaplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myaplication.data.local.dao.PuzzleDao
import com.example.myaplication.data.local.entity.PuzzleEntity

@Database(
    entities = [PuzzleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
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