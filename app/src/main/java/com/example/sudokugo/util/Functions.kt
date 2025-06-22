package com.example.sudokugo.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault())
    return formatter.format(date)
}