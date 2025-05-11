package com.example.sudokugo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import androidx.navigation.compose.rememberNavController
import com.example.sudokugo.ui.SudokuGONavGraph
import com.example.sudokugo.ui.theme.SudokuGOTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Configuration.getInstance().userAgentValue = packageName

        requestPermissionsIfNecessary(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        ))

        setContent {
            SudokuGOTheme {
                val navController = rememberNavController()
                SudokuGONavGraph(navController)
            }
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, toRequest.toTypedArray(), 1
            )
        }
    }
}
