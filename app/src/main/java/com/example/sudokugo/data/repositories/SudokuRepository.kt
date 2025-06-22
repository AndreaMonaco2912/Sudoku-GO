package com.example.sudokugo.data.repositories

import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.database.SudokuDAO
import java.util.Date

class SudokuRepository(
    private val dao: SudokuDAO
) {
    suspend fun insertSudoku(sudoku: ServerSudoku): Long = dao.insert(sudoku)
    suspend fun fetchSudokuById(id: Long) = dao.getById(id)
    suspend fun updateCurrentBoard(id: Long, newBoard: String) = dao.update(id, newBoard)
    suspend fun fetchAllSudokuByUser(email: String?) = dao.getAllByUser(email)
    suspend fun solveSudoku(id: Long, solveDate: Date, finishTime: Long) = dao.solve(id, solveDate, finishTime)
    suspend fun changePic(id: Long, newPic: String) = dao.changePic(id, newPic)
    suspend fun changeFav(id: Long, fav: Boolean) = dao.changeFav(id, fav)
}