package com.example.sudokugo.map.classes

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.map.functions.drawUserCenteredCircle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapViewModel (
    private val context: Application
) : AndroidViewModel(context) {

    private val _mapCenter = mutableStateOf(GeoPoint(0.0, 0.0))
    val mapCenter: State<GeoPoint> = _mapCenter

    private val _zoomLevel = mutableDoubleStateOf(18.0)
    val zoomLevel: State<Double> = _zoomLevel

//    private var backgroundJob: Job? = null

    init {
        loadMapInfo()
    }

    fun loadMapInfo() {
        val prefs = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("latitude", 45.4642f)
        val lon = prefs.getFloat("longitude", 9.1900f)
        val zoom = prefs.getFloat("zoom", 18f)

        _mapCenter.value = GeoPoint(lat.toDouble(), lon.toDouble())
        _zoomLevel.doubleValue = zoom.toDouble()
    }

    fun saveMapInfo(lat: Double, lon: Double, zoom: Double) {
        val prefs = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat("latitude", lat.toFloat())
            putFloat("longitude", lon.toFloat())
            putFloat("zoom", zoom.toFloat())
            apply()
        }
    }
}
