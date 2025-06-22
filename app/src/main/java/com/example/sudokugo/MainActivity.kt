package com.example.sudokugo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.osmdroid.config.Configuration
import androidx.navigation.compose.rememberNavController
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.ui.SudokuGONavGraph
import com.example.sudokugo.ui.screens.login.LoginViewModel
import com.example.sudokugo.ui.screens.register.RegisterViewModel
import com.example.sudokugo.ui.screens.settings.SettingsViewModel
import com.example.sudokugo.ui.screens.user.UserScreenViewModel
import com.example.sudokugo.ui.theme.SudokuGOTheme
import android.media.MediaPlayer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.androidx.compose.koinViewModel

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://mkncxrzzbmjlrmmwmwkj.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1rbmN4cnp6Ym1qbHJtbXdtd2tqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDcwMTI2NDksImV4cCI6MjA2MjU4ODY0OX0.ZtetcerEvRH9yiyA523c31J57Vi5ySNxEjRMcWNQIwQ"
) {
    install(Postgrest)
    install(Auth)
}

class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.sax).apply {
            isLooping = true
            setVolume(0.0f,0.0f)
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        Configuration.getInstance().userAgentValue = packageName



        setContent {
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            val themeState by settingsViewModel.state.collectAsStateWithLifecycle()
            val loginViewModel = koinViewModel<LoginViewModel>()
            val registerViewModel = koinViewModel<RegisterViewModel>()
            val userScreenViewModel = koinViewModel<UserScreenViewModel>()

            SudokuGOTheme(
                darkTheme = when (themeState.theme) {
                    Theme.Dark -> true
                    Theme.Light -> false
                    Theme.System -> isSystemInDarkTheme()
                }
            ) {
                val navController = rememberNavController()
                SudokuGONavGraph(
                    navController,
                    settingsViewModel,
                    themeState,
                    loginViewModel,
                    registerViewModel,
                    userScreenViewModel
                ) {
                    volume -> mediaPlayer.setVolume(volume, volume)
                    if (!mediaPlayer.isPlaying && volume > 0f) {
                        mediaPlayer.start()
                    } else if (volume == 0f && mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mediaPlayer.isInitialized && !mediaPlayer.isPlaying) {
            try {
                mediaPlayer.start()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
