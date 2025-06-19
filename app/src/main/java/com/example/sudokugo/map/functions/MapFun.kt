package com.example.sudokugo.map.functions

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun haversineDistance(p1: IGeoPoint, p2: IGeoPoint): Double {
    val R = 6371000.0 // raggio Terra in metri
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)
    val lat1 = Math.toRadians(p1.latitude)
    val lat2 = Math.toRadians(p2.latitude)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c
}

fun drawUserCenteredCircle(mapView: MapView, center: GeoPoint, radiusInMeters: Double) {
    val circle = Polygon().apply {
        points = Polygon.pointsAsCircle(center, radiusInMeters)
        outlinePaint.color = Color.DKGRAY
        outlinePaint.strokeWidth = 4f
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.isAntiAlias = true

        setInfoWindow(null)
    }

    mapView.overlays.removeIf { it is Polygon && it.infoWindow == null }

    mapView.overlays.add(circle)
    mapView.invalidate()
}

fun createLocationOverlay(context: Context, mapView: MapView): MyLocationNewOverlay {
    val gpsProvider = GpsMyLocationProvider(context).apply {
        locationUpdateMinDistance = 5.0f
        locationUpdateMinTime = 2000
    }

    return MyLocationNewOverlay(gpsProvider, mapView).apply {
        isDrawAccuracyEnabled = false
        enableMyLocation()
        enableFollowLocation()
    }
}