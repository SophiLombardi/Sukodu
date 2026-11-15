package com.example.myaplication.data.repository

import com.example.myaplication.data.remote.SudokuApi
import com.example.myaplication.logic.BoardFormat
import com.example.myaplication.logic.Difficulty
import com.example.myaplication.logic.SudokuGenerator
import com.example.myaplication.logic.SudokuValidator
import com.example.myapplication.data.local.dao.PuzzleDao
import com.example.myapplication.data.local.dao.ScoreDao
import com.example.myapplication.data.local.dao.SettingsDao
import com.example.myapplication.data.local.entity.PuzzleEntity
import com.example.myapplication.data.local.entity.ScoreEntity
import com.example.myapplication.data.local.entity.SettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val message: String) : RepositoryResult<Nothing>()
}
data class UserSession(val name: String, val isAdmin: Boolean)
class SudokuRepository(
    private val puzzleDao: PuzzleDao,
    private val scoreDao: ScoreDao,
    private val settingsDao: SettingsDao,
    private val api: SudokuApi
) {
    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()
    fun login(session: UserSession) { _session.value = session }
    fun logout() { _session.value = null }
    val puzzles: Flow<List<PuzzleEntity>> = puzzleDao.getAll()
    suspend fun getPuzzle(id: Long): RepositoryResult<PuzzleEntity> = try {
        val puzzle = puzzleDao.getById(id)
        if (puzzle != null) RepositoryResult.Success(puzzle)
        else RepositoryResult.Error("Puzzle não encontrado.")
    } catch (e: Exception) {
        RepositoryResult.Error("Erro no banco de dados: ${e.message}")
    }
    suspend fun createLocalPuzzle(name: String, difficulty: Difficulty):
            RepositoryResult<Long> = try {
        val generated = withContext(Dispatchers.Default) {
            SudokuGenerator.generate(difficulty) }
        val entity = PuzzleEntity(
            name = name,
            difficulty = difficulty.name,
            givens = BoardFormat.toText(generated.puzzle),
            solution = BoardFormat.toText(generated.solution),
            source = "LOCAL"
        )
        RepositoryResult.Success(puzzleDao.insert(entity))
    } catch (e: Exception) {
        RepositoryResult.Error("Erro ao criar puzzle: ${e.message}")
    }
    suspend fun updatePuzzle(puzzle: PuzzleEntity): RepositoryResult<Unit> =
        try {
            puzzleDao.update(puzzle)
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Erro ao atualizar puzzle: ${e.message}")
        }
    suspend fun deletePuzzle(puzzle: PuzzleEntity): RepositoryResult<Unit> =
        try {
            puzzleDao.delete(puzzle)
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Erro ao excluir puzzle: ${e.message}")
        }
    suspend fun importPuzzlesFromApi(quantity: Int): RepositoryResult<Int> {
        return try {
            val newPuzzles = mutableListOf<PuzzleEntity>()
            repeat(quantity) {
                val response = api.getNewBoard()
                response.newboard?.grids.orEmpty().forEach { grid ->
                    val givens = grid.value.orEmpty().flatten()
                    val solution = grid.solution.orEmpty().flatten()
                    if (SudokuValidator.isValidSolution(solution) &&
                        SudokuValidator.puzzleMatchesSolution(givens,
                            solution)
                    ) {
                        val difficulty = Difficulty.fromApi(grid.difficulty)
                        newPuzzles.add(
                            PuzzleEntity(
                                name = "API ${difficulty.label} ${System.currentTimeMillis() % 10000}",
                                    difficulty = difficulty.name,
                            givens = BoardFormat.toText(givens),
                            solution = BoardFormat.toText(solution),
                            source = "API"
                        )
                        )
                    }
                }
            }
            if (newPuzzles.isEmpty()) {
                RepositoryResult.Error("A API respondeu, mas não enviou puzzles válidos.")
            } else {
                puzzleDao.insertAll(newPuzzles)
                RepositoryResult.Success(newPuzzles.size)
            }
        } catch (e: IOException) {
            RepositoryResult.Error("Sem conexão com a internet. Tente novamente.")
        } catch (e: HttpException) {
            RepositoryResult.Error("O servidor respondeu com erro${e.code()}.")
        } catch (e: Exception) {
            RepositoryResult.Error("Erro inesperado ao importar:${e.message}")
        }
    }
    suspend fun ensureInitialPuzzles(): RepositoryResult<Unit> = try {
        if (puzzleDao.count() == 0) {
            Difficulty.entries.forEach { difficulty ->
                createLocalPuzzle("Inicial ${difficulty.label}", difficulty)
            }
        }
        RepositoryResult.Success(Unit)
    } catch (e: Exception) {
        RepositoryResult.Error("Erro ao preparar puzzles iniciais: ${e.message}")
    }
    val scores: Flow<List<ScoreEntity>> = scoreDao.getAll()
    suspend fun saveScore(score: ScoreEntity): RepositoryResult<Long> = try {
        RepositoryResult.Success(scoreDao.insert(score))
    } catch (e: Exception) {
        RepositoryResult.Error("Erro ao salvar pontuação: ${e.message}")
    }
    suspend fun updateScore(score: ScoreEntity): RepositoryResult<Unit> = try
    {
        scoreDao.update(score)
        RepositoryResult.Success(Unit)
    } catch (e: Exception) {
        RepositoryResult.Error("Erro ao atualizar pontuação: ${e.message}")
    }
    suspend fun deleteScore(score: ScoreEntity): RepositoryResult<Unit> = try
    {
        scoreDao.delete(score)
        RepositoryResult.Success(Unit)
    } catch (e: Exception) {
        RepositoryResult.Error("Erro ao excluir pontuação: ${e.message}")
    }
    suspend fun clearScores(): RepositoryResult<Unit> = try {
        scoreDao.deleteAll()
        RepositoryResult.Success(Unit)
    } catch (e: Exception) {
        RepositoryResult.Error("Erro ao limpar ranking: ${e.message}")
    }
    val settings: Flow<SettingsEntity> = settingsDao.get().map { it ?:
    SettingsEntity() }
    suspend fun saveSettings(settings: SettingsEntity):
            RepositoryResult<Unit> = try {
        settingsDao.save(settings)
        RepositoryResult.Success(Unit)
    } catch (e: Exception) {
        RepositoryResult.Error("Erro ao salvar configurações: ${e.message}")
    }
}