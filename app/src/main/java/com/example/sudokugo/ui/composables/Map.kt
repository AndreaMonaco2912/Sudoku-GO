package com.example.sudokugo.ui.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sudokugo.map.classes.MapViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import com.example.sudokugo.map.view.MyMap
import kotlinx.coroutines.delay

@Composable
fun Map(viewModelMap: MapViewModel = viewModel(), playSudoku: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val map = remember { MyMap(context, viewModelMap.mapCenter, viewModelMap.zoomLevel, playSudoku) }

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

    AndroidView(
        factory = {
            viewModelMap.loadMapInfo()
            map.getMapView()

        },
        modifier = Modifier.fillMaxSize()
    )

    LaunchedEffect(Unit) {
        while (true) {
            val location = map.getLocation()
            if (location != null) {
                viewModelMap.saveMapInfo(location.latitude, location.longitude, map.getZoom())
                map.updateWorldMap(location)
            }
            delay(3000)
        }
    }

}

fun configureMapView(mapView: MapView) {
    mapView.setTileSource(TileSourceFactory.MAPNIK)
    mapView.setMultiTouchControls(false)
    mapView.minZoomLevel = 17.0
    mapView.maxZoomLevel = 19.0
}