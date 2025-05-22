package com.example.sudokugo.data.repositories

import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.database.SudokuDAO
import com.example.sudokugo.supabase
import io.github.jan.supabase.postgrest.from

class SudokuRepository(
    private val dao: SudokuDAO
) {
    val sudokus = dao.getAll()

    suspend fun insertSudoku(sudoku: ServerSudoku): Long = dao.insert(sudoku)
    suspend fun deleteSudoku(sudoku: ServerSudoku) = dao.delete(sudoku)
    suspend fun fetchSudokuById(id: Long) = dao.getById(id)
    suspend fun updateCurrentBoard(id: Long, newBoard: String) = dao.update(id.toLong(), newBoard)
    fun fetchAllSudoku() = dao.getAll()


//    suspend fun fetchSudokuById(id: Int) : ServerSudoku?{
//        return supabase.from("sudoku")
//            .select{
//               filter {
//                   eq("id", id)
//               }
//                limit(1)
//            }
//            .decodeSingleOrNull<ServerSudoku>()
//    }
//    suspend fun fetchAllSudoku() : List<ServerSudoku>{
//        return supabase.from("sudoku")
//            .select()
//            .decodeList<ServerSudoku>()
//    }
}