package com.example.sudokugo.data.repositories

import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.database.SudokuDAO

class SudokuRepository(
    private val dao: SudokuDAO
) {
    suspend fun insertSudoku(sudoku: ServerSudoku): Long = dao.insert(sudoku)
    suspend fun deleteSudoku(sudoku: ServerSudoku) = dao.delete(sudoku)
    suspend fun fetchSudokuById(id: Long) = dao.getById(id)
    suspend fun updateCurrentBoard(id: Long, newBoard: String) = dao.update(id, newBoard)
    suspend fun fetchAllSudokuByUser(email: String?) = dao.getAllByUser(email)
    suspend fun solveSudoku(id: Long, solveDate: String, time: Long) = dao.solve(id, solveDate, time)
}