package com.example.sudokugo.ui.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun Map(playSudoku: () -> Unit) {
    val viewModelMap = koinViewModel<MapViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasLocation by viewModelMap.hasRealLocation
    val isLoaded by viewModelMap.isMapDataLoaded.collectAsStateWithLifecycle()
    var wasEverLoaded by rememberSaveable { mutableStateOf(false) }

    val mapDataHasBeenRead by viewModelMap.mapDataHasBeenRead.collectAsStateWithLifecycle()

    if (!mapDataHasBeenRead) {
        Loading("Loading Map...")
        return
    }

    if (isLoaded) {
        wasEverLoaded = true
    }

    val map = remember(context, isLoaded) {
        if (isLoaded || !wasEverLoaded) {
            MyMap(context, viewModelMap.mapCenter.value, viewModelMap.zoomLevel.value, playSudoku)
        } else null
    }

    val alpha = when {
        !wasEverLoaded && !hasLocation -> 0f
        else -> 1f
    }

    if (!hasLocation && !isLoaded || map == null) {
        Loading("Trying to access fine position...")
    }

    if (map != null) {
        AndroidView(
            factory = { map.getMapView() },
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
        )

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