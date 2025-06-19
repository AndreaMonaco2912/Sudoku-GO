package com.example.sudokugo.map.classes

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.repositories.MapDSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(
    private val mapRepo: MapDSRepository
) : ViewModel() {

    private val _mapCenter = mutableStateOf(GeoPoint(0.0, 0.0))
    val mapCenter: State<GeoPoint> = _mapCenter

    private val _zoomLevel = mutableDoubleStateOf(18.0)
    val zoomLevel: State<Double> = _zoomLevel

    private val _isMapDataLoaded = MutableStateFlow<Boolean>(false)
    val isMapDataLoaded: StateFlow<Boolean> = _isMapDataLoaded

    private val _hasRealLocation = mutableStateOf(false)
    val hasRealLocation: State<Boolean> = _hasRealLocation

    fun updateLocationStatus(location: GeoPoint?) {
        if (location != null && !_hasRealLocation.value) {
            _hasRealLocation.value = true
        }
    }

    init {
        loadMapInfo()
    }

    fun loadMapInfo() {
        viewModelScope.launch {
            mapRepo.mapData.collect { mapInfo ->
                if (mapInfo != null) {
                    val (lat, lon, zoom) = mapInfo
                    _mapCenter.value = GeoPoint(lat, lon)
                    _zoomLevel.doubleValue = zoom
                    _isMapDataLoaded.value = true
                }
            }
        }
    }

    fun saveMapInfo(lat: Double, lon: Double, zoom: Double) {
        viewModelScope.launch {
            mapRepo.save(lat, lon, zoom)
        }
    }
}
