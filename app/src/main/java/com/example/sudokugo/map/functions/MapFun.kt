package com.example.sudokugo.map.functions

import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.example.sudokugo.MainActivity.TimedPOI
import com.example.sudokugo.R
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random


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

fun createPoiIcon(context: Context, drawableId: Int, size: Int): Bitmap {
    val output = createBitmap(size, size)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE // Sfondo bianco pieno
        style = Paint.Style.FILL
    }

    // Disegna lo sfondo bianco interamente
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

    // Carica l'immagine dell'icona
    val drawable = ContextCompat.getDrawable(context, drawableId) as BitmapDrawable
    val originalBitmap = drawable.bitmap

    // Calcola proporzioni corrette per scalare l'immagine
    val scale = minOf(
        size.toFloat() / originalBitmap.width,
        size.toFloat() / originalBitmap.height
    )

    val newWidth = (originalBitmap.width * scale).toInt()
    val newHeight = (originalBitmap.height * scale).toInt()

    val left = (size - newWidth) / 2
    val top = (size - newHeight) / 2

    val destRect = Rect(left, top, left + newWidth, top + newHeight)

    // Disegna l'immagine sopra
    canvas.drawBitmap(originalBitmap, null, destRect, null)

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

//private fun animateDespawn(poi: TimedPOI) {
//    val item = poi.item
//    val originalDrawable = item.drawable as? BitmapDrawable ?: return
//    val originalBitmap = originalDrawable.bitmap
//
//    val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
//    val canvas = Canvas(mutableBitmap)
//    val paint = Paint().apply { isAntiAlias = true }
//
//    val animator = ValueAnimator.ofInt(255, 0).apply {
//        ValueAnimator.setDuration = 500
//        addUpdateListener {
//            val alpha = it.animatedValue as Int
//            paint.alpha = alpha
//            mutableBitmap.eraseColor(Color.TRANSPARENT)
//            canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
//            item.setMarker(mutableBitmap.toDrawable(resources))
//            mapView.invalidate()
//        }
//        doOnEnd {
//            // Dopo fade out completo, rimuovi
//            poiItems.remove(poi)
//            recreatePoiOverlay()
//        }
//        start()
//    }
//}
//
//private fun recreatePoiOverlay() {
//    poiOverlay?.let { mapView.overlays.remove(it) }
//    poiOverlay = ItemizedIconOverlay(
//        poiItems.map { it.item },
//        object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
//            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
//                val userLocation = locationOverlay.myLocation
//                if (item != null && userLocation != null) {
//                    val distance = haversineDistance(userLocation, item.point)
//                    if (distance <= 120.0) {
//                        Toast.makeText(this@MainActivity, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
//                        // Aggiungi qui eventuale logica per avviare un'activity di gioco
//                    } else {
//                        Toast.makeText(this@MainActivity, "Avvicinati per giocare!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                return true
//            }
//
//            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
//        },
//        applicationContext
//    )
//    mapView.overlays.add(poiOverlay)
//
//    // Assicura che l'overlay utente sia sopra
//    mapView.overlays.remove(locationOverlay)
//    mapView.overlays.add(locationOverlay)
//}
//
//private fun generateRandomPOIs(center: GeoPoint) {
//    val currentTime = System.currentTimeMillis()
//
//    // Rimuovi quelli scaduti
//    val iterator = poiItems.iterator()
//    while (iterator.hasNext()) {
//        val poi = iterator.next()
//        if (currentTime > poi.createdAt + poi.lifespan) {
//            animateDespawn(poi)
//        }
//    }
//
//    // Controlla se aggiungere nuovi POI
//    if (currentTime - lastPoiAddTime >= poiAddInterval) { //Very very very sus
//        val currentCount = poiItems.size
//        val forceAdd = currentCount < 12
//        val allowAdd = currentCount < 28
//
//        if (forceAdd || allowAdd) {
//            val toAdd = if (forceAdd) (12 - currentCount + (0 .. 6).random()) else (0..2).random()
//            repeat(toAdd) {
//                val latOffset = (Random.nextDouble() - 0.5) / 125
//                val lonOffset = (Random.nextDouble() - 0.5) / 125
//                val location = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)
//
//                val poiItem = OverlayItem("Sudoku", "Gioca!", location)
//                val bitmap = createPoiIcon(R.drawable.sudoku_icon, 100)
//                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//                val canvas = Canvas(mutableBitmap)
//                val paint = Paint()
//
//                // Animazione Fade-In
//                ValueAnimator.ofInt(0, 255).apply {
//                    ValueAnimator.setDuration = 500
//                    addUpdateListener {
//                        paint.alpha = it.animatedValue as Int
//                        mutableBitmap.eraseColor(Color.TRANSPARENT)
//                        canvas.drawBitmap(bitmap, 0f, 0f, paint)
//                        mapView.invalidate()
//                    }
//                    start()
//                }
//
//                poiItem.setMarker(mutableBitmap.toDrawable(resources))
//                poiItem.markerHotspot = OverlayItem.HotspotPlace.CENTER
//
//                poiItems.add(
//                    TimedPOI(
//                        item = poiItem,
//                        createdAt = currentTime,
//                        lifespan = (25000L..40000L).random()
//                    )
//                )
//            }
//
//            lastPoiAddTime = currentTime
//            poiAddInterval = (2000L..30000L).random()
//        }
//    }
//
//    // Ricrea overlay
//    poiOverlay?.let { mapView.overlays.remove(it) }
//    poiOverlay = ItemizedIconOverlay(
//        poiItems.map { it.item },
//        object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
//            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
//                val userLocation = locationOverlay.myLocation
//                if (item != null && userLocation != null) {
//                    val distance = haversineDistance(userLocation, item.point)
//                    if (distance <= 120.0) {
//                        Toast.makeText(this@MainActivity, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
//                        // Aggiungi qui eventuale logica per avviare un'activity di gioco
//                    } else {
//                        Toast.makeText(this@MainActivity, "Avvicinati per giocare!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                return true
//            }
//
//            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
//        },
//        applicationContext
//    )
//    mapView.overlays.add(poiOverlay)
//
//    // Riaggiungi marker utente
//    mapView.overlays.remove(locationOverlay)
//    mapView.overlays.add(locationOverlay)
//}
//
//private fun drawUserCenteredCircle(center: GeoPoint, radiusInMeters: Double) {
//    val circle = Polygon().apply {
//        points = Polygon.pointsAsCircle(center, radiusInMeters)
//        outlinePaint.color = Color.DKGRAY
//        outlinePaint.strokeWidth = 4f
//        outlinePaint.style = Paint.Style.STROKE
//        outlinePaint.isAntiAlias = true
//
////            // Fill
////            fillPaint.style = android.graphics.Paint.Style.FILL
////            fillPaint.isAntiAlias = true
//
//        setInfoWindow(null)
//    }
//
//    // Remove old if exists
//    mapView.overlays.removeIf { it is Polygon && it.infoWindow == null }
//
//    mapView.overlays.add(circle)
//    mapView.invalidate()
//}
//
//private fun requestPermissionsIfNecessary(permissions: Array<String>) {
//    val toRequest = permissions.filter {
//        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
//    }
//    if (toRequest.isNotEmpty()) {
//        ActivityCompat.requestPermissions(
//            this, toRequest.toTypedArray(), 1
//        )
//    }
//}
//
//override fun onResume() {
//    super.onResume()
//    if (::mapView.isInitialized) mapView.onResume()
//}
//
//override fun onPause() {
//    super.onPause()
//    if (::mapView.isInitialized) mapView.onPause()
//}