package com.example.sudokugo.data.repositories

import com.example.sudokugo.data.database.Sudoku
import com.example.sudokugo.data.database.SudokuDAO
import com.example.sudokugo.data.models.ServerSudoku
import com.example.sudokugo.supabase
import io.github.jan.supabase.postgrest.from

class SudokuRepository(
    private val dao: SudokuDAO
) {
    val sudokus = dao.getAll()

    suspend fun upsertSudoku(sudoku: Sudoku) = dao.upsert(sudoku)
    suspend fun deleteSudoku(sudoku: Sudoku) = dao.delete(sudoku)

    suspend fun fetchSudokuById(id: Int) : ServerSudoku?{
        return supabase.from("sudoku")
            .select{
               filter {
                   eq("id", id)
               }
                limit(1)
            }
            .decodeSingleOrNull<ServerSudoku>()
    }
    suspend fun fetchAllSudoku() : List<ServerSudoku>{
        return supabase.from("sudoku")
            .select()
            .decodeList<ServerSudoku>()
    }
}