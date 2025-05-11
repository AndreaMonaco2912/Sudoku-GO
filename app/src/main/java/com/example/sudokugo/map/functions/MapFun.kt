package com.example.sudokugo.map.functions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun getCircularBitmap(bitmap: Bitmap): Bitmap {
    val size = minOf(bitmap.width, bitmap.height)
    val output = createBitmap(size, size)

    val canvas = Canvas(output)
    val paint = Paint().apply { isAntiAlias = true }

    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    val srcRect = Rect(
        (bitmap.width - size) / 2,
        (bitmap.height - size) / 2,
        (bitmap.width + size) / 2,
        (bitmap.height + size) / 2
    )
    val destRect = Rect(0, 0, size, size)
    canvas.drawBitmap(bitmap, srcRect, destRect, paint)

    return output
}

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
