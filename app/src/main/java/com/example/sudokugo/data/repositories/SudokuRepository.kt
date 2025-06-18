package com.example.sudokugo.data.repositories

import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.database.SudokuDAO

class SudokuRepository(
    private val dao: SudokuDAO
) {
    suspend fun insertSudoku(sudoku: ServerSudoku): Long = dao.insert(sudoku)
    suspend fun deleteSudoku(sudoku: ServerSudoku) = dao.delete(sudoku)
    suspend fun fetchSudokuById(id: Long) = dao.getById(id)
    suspend fun updateCurrentBoard(id: Long, newBoard: String) = dao.update(id.toLong(), newBoard)
    suspend fun fetchAllSudoku() = dao.getAll()
    suspend fun solveSudoku(id: Long) = dao.solve(id)
}