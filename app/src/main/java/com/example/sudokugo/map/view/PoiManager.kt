package com.example.sudokugo.map.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.material3.NavigationBar
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.NavController
import com.example.sudokugo.R
import com.example.sudokugo.map.functions.haversineDistance
import com.example.sudokugo.ui.SudokuGORoute
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.random.Random

class PoiManager(private val context: Context, private val mapView: MapView, private val playSudoku: () -> Unit) {

    data class TimedPOI(val item: OverlayItem, val createdAt: Long, val lifespan: Long)

    private val poiItems = mutableListOf<TimedPOI>()
    private var poiOverlay: ItemizedIconOverlay<OverlayItem>? = null
    private var invalidateScheduled = false
    private val minDistance = 120.0
    private val maxDistance = 600.0
    private val minSudoku = 12
    private val maxSudoku = 16

    private fun animateDespawn(poi: TimedPOI, onAnimationEnd: () -> Unit) {
        val item = poi.item
        val originalDrawable = item.drawable as? BitmapDrawable ?: return
        val originalBitmap = originalDrawable.bitmap

        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        ValueAnimator.ofInt(255, 0).apply {
            duration = 500
            addUpdateListener {
                val alpha = it.animatedValue as Int
                paint.alpha = alpha
                mutableBitmap.eraseColor(Color.TRANSPARENT)
                canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
                item.setMarker(mutableBitmap.toDrawable(context.resources))
                requestInvalidate(mapView)
            }
            doOnEnd {
                onAnimationEnd()
            }
            Handler(Looper.getMainLooper()).post {
                start()
            }
        }
    }

    private fun recreatePoiOverlay(locationOverlay: MyLocationNewOverlay) {
        poiOverlay?.let { mapView.overlays.remove(it) }
        poiOverlay = ItemizedIconOverlay(
            poiItems.map { it.item },
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    val userLocation = locationOverlay.myLocation
                    if (item != null && userLocation != null) {
                        val distance = haversineDistance(userLocation, item.point)
                        if (distance <= minDistance) {
                            Handler(Looper.getMainLooper()).post {
                                playSudoku()
                            }
                        } else {
                            Toast.makeText(context, "Avvicinati per giocare!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
            },
            context
        )
        mapView.overlays.add(poiOverlay)
        mapView.overlays.remove(locationOverlay)
        mapView.overlays.add(locationOverlay)
    }

    private fun animateSpawn(mutableBitmap: Bitmap, originalBitmap: Bitmap) {
        val canvas = Canvas(mutableBitmap)
        val paint = Paint()

        // Animazione Fade-In
        ValueAnimator.ofInt(0, 255).apply {
            duration = 500
            addUpdateListener {
                paint.alpha = it.animatedValue as Int
                mutableBitmap.eraseColor(Color.TRANSPARENT)
                canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
                requestInvalidate(mapView)
            }
            Handler(Looper.getMainLooper()).post {
                start()
            }
        }
    }

    fun generateRandomPOIs(
        context: Context,
        locationOverlay: MyLocationNewOverlay,
        center: GeoPoint
    ) {
        val currentTime = System.currentTimeMillis()

        poiItems.forEach { poi ->
            val isExpired = currentTime > poi.createdAt + poi.lifespan
            val isTooFar = haversineDistance(center, poi.item.point) > maxDistance
            if (isExpired || isTooFar) {
                animateDespawn(poi) {
                    poiItems.remove(poi)
                }
            }
        }

        val currentCount = poiItems.size
        val forceAdd = currentCount < minSudoku
        val allowAdd = currentCount < maxSudoku

        if (forceAdd || allowAdd) {
            val toAdd = if (forceAdd) (1) else (0..1).random()
            if (toAdd > 0) {
                val latOffset = (Random.nextDouble() - 0.5) / 125
                val lonOffset = (Random.nextDouble() - 0.5) / 125
                val location =
                    GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)

                val poiItem = OverlayItem("Sudoku", "Gioca!", location)
                val drawable = ContextCompat.getDrawable(context, R.drawable.poi_sudoku_w)
                val bitmap = (drawable as? BitmapDrawable)?.bitmap!!

                val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                animateSpawn(mutableBitmap, bitmap)

                poiItem.setMarker(mutableBitmap.toDrawable(context.resources))
                poiItem.markerHotspot = OverlayItem.HotspotPlace.CENTER

                poiItems.add(
                    TimedPOI(
                        item = poiItem,
                        createdAt = currentTime,
                        lifespan = (25000L..40000L).random()
                    )
                )
            }

        }

        recreatePoiOverlay(locationOverlay)
    }

    private fun requestInvalidate(mapView: MapView) {
        if (!invalidateScheduled) {
            invalidateScheduled = true
            Handler(Looper.getMainLooper()).post {
                mapView.invalidate()
                invalidateScheduled = false
            }
        }
    }
}