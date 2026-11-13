package com.example.myapplication.data.repository
import com.example.myapplication.data.local.dao.PuzzleDao
import com.example.myapplication.data.local.entity.PuzzleEntity
import com.example.myapplication.logic.BoardFormat
import com.example.myapplication.logic.Difficulty
import com.example.myapplication.logic.SudokuGenerator
import kotlinx.coroutines.flow.Flow
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val message: String) : RepositoryResult<Nothing>()
}
class SudokuRepository(
    private val puzzleDao: PuzzleDao
) {
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
        val generated = SudokuGenerator.generate(difficulty)
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
    suspend fun deletePuzzle(puzzle: PuzzleEntity): RepositoryResult<Unit> =
        try {
            puzzleDao.delete(puzzle)
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Erro ao excluir puzzle: ${e.message}")
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
}
