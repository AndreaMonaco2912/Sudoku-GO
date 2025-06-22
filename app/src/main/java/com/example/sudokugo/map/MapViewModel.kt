package com.example.sudokugo.map

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

    private val _mapCenter = MutableStateFlow(GeoPoint(0.0, 0.0))
    val mapCenter: StateFlow<GeoPoint> = _mapCenter

    private val _zoomLevel = MutableStateFlow(18.0)
    val zoomLevel: StateFlow<Double> = _zoomLevel

    private val _isMapDataLoaded = MutableStateFlow(false)
    val isMapDataLoaded: StateFlow<Boolean> = _isMapDataLoaded

    private val _hasRealLocation = MutableStateFlow(false)
    val hasRealLocation: StateFlow<Boolean> = _hasRealLocation

    private val _mapDataHasBeenRead = MutableStateFlow(false)
    val mapDataHasBeenRead: StateFlow<Boolean> = _mapDataHasBeenRead

    fun updateLocationStatus(location: GeoPoint?) {
        if (location != null && !_hasRealLocation.value) {
            _hasRealLocation.value = true
        }
    }

    init {
        loadMapInfo()
    }

    private fun loadMapInfo() {
        viewModelScope.launch {
            mapRepo.mapData.collect { mapInfo ->
                if (mapInfo != null) {
                    val (lat, lon, zoom) = mapInfo
                    _mapCenter.value = GeoPoint(lat, lon)
                    _zoomLevel.value = zoom
                    _isMapDataLoaded.value = true
                }
                _mapDataHasBeenRead.value = true
            }
        }
    }


    fun saveMapInfo(lat: Double, lon: Double, zoom: Double) {
        viewModelScope.launch {
            mapRepo.save(lat, lon, zoom)
        }
    }
}
