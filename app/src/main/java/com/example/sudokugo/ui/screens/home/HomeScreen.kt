package com.example.sudokugo.ui.screens.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.map.Map
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import com.example.sudokugo.util.PermissionStatus
import com.example.sudokugo.util.rememberMultiplePermissions
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(navController: NavController, setVolume: (Float) -> Unit) {
    val ctx = LocalContext.current
    val homeViewModel = koinViewModel<HomeScreenViewModel>()
    val volume by homeViewModel.volume.collectAsStateWithLifecycle()
    val localVolume = volume
    var canShowMap by remember { mutableStateOf(false) }
    var permaDenied by remember { mutableStateOf(false) }

    val locationPermissions = rememberMultiplePermissions(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
        )
    ) { statuses ->
        when {
            statuses.any { it.value == PermissionStatus.Granted } -> {
                canShowMap = true
                permaDenied = false
            }

            statuses.all { it.value == PermissionStatus.PermanentlyDenied } ->
                permaDenied = true
        }

    }
    LaunchedEffect(Unit) {
        locationPermissions.launchPermissionRequest()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && permaDenied) {
                locationPermissions.launchPermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (localVolume != null) {
        setVolume(localVolume)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val playSudoku = {
        navController.navigate(SudokuGORoute.Solve())
    }
    if (canShowMap) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { TopSudokuGoAppBar(navController, title = "SudokuGO") },
            bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.PLAY) }
        ) { contentPadding ->
            Log.e("HomeScreen", permaDenied.toString())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
//            if (showMap.value) {
                Map(playSudoku = playSudoku)
//            }
            }
        }
    } else {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Log.e("HomeScreen", permaDenied.toString())
                if (permaDenied) {
                    LaunchedEffect(snackbarHostState) {
                        val res = snackbarHostState.showSnackbar(
                            "Location permission is required.",
                            "Go to Settings",
                            duration = SnackbarDuration.Long
                        )
                        if (res == SnackbarResult.ActionPerformed) {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", ctx.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            if (intent.resolveActivity(ctx.packageManager) != null) {
                                ctx.startActivity(intent)
                            }
                        }
                    }
                }
                Text(
                    text = "Can't access to location",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "The app need location permission to show map",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
                if (!permaDenied) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        locationPermissions.launchPermissionRequest()
                    }) {
                        Text("Give permission")
                    }
                }
            }
        }

    }
}
