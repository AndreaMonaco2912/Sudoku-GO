package com.example.sudokugo.ui.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sudokugo.map.classes.MapViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import com.example.sudokugo.map.view.MyMap
import com.example.sudokugo.ui.composables.profilePic.UserPictureViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun Map(playSudoku: () -> Unit) {
    val viewModelMap = koinViewModel<MapViewModel>()
    val userPictureViewModel = koinViewModel<UserPictureViewModel>()
    val userPic by userPictureViewModel.userPic.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasLocation = viewModelMap.hasRealLocation.value
    val isLoaded = viewModelMap.isMapDataLoaded.collectAsStateWithLifecycle().value
    val map = if (isLoaded) {
        Log.d("MAP_USER_PIC", "userPic = $userPic")
        MyMap(context, viewModelMap.mapCenter.value, viewModelMap.zoomLevel.value, playSudoku)
    } else null


    when {
        map != null -> {
            AndroidView(
                factory = { map.getMapView() }, modifier = Modifier.fillMaxSize()
            )
        }

        !hasLocation -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text("No Position No Play", color = Color.White)
            }
        }
    }


    if (map != null) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> map.onResume()
                    Lifecycle.Event.ON_PAUSE -> map.onPause()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        LaunchedEffect(Unit) {
            while (true) {
                val location = map.getLocation()
                viewModelMap.updateLocationStatus(location)
                if (location != null) {
                    viewModelMap.saveMapInfo(location.latitude, location.longitude, map.getZoom())
                    map.updateWorldMap(location)
                }
                delay(3000)
            }
        }
    }
}

fun configureMapView(mapView: MapView) {
    mapView.setTileSource(TileSourceFactory.MAPNIK)
    mapView.setMultiTouchControls(false)
    mapView.minZoomLevel = 17.0
    mapView.maxZoomLevel = 19.0
}