package com.example.sudokugo.map.classes

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.example.sudokugo.R
import com.example.sudokugo.map.functions.haversineDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.random.Random

class PoiManager(private val context: Context, private val mapView: MapView) {

    data class TimedPOI(val item: OverlayItem, val createdAt: Long, val lifespan: Long)

    private val poiItems = mutableListOf<TimedPOI>()
    private var poiOverlay: ItemizedIconOverlay<OverlayItem>? = null
    private var lastPoiAddTime = 0L
    private var poiAddInterval = randomAddInterval() // 17-30 secondi
    private fun randomAddInterval() = (200L..400L).random()

    private fun createPoiIcon(drawableId: Int, size: Int): Bitmap {
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

    private fun animateDespawn(poi: TimedPOI, onAnimationEnd: () -> Unit) {
        val item = poi.item
        val originalDrawable = item.drawable as? BitmapDrawable ?: return
        val originalBitmap = originalDrawable.bitmap

        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply { isAntiAlias = true }

        val animator = ValueAnimator.ofInt(255, 0).apply {
            duration = 500
            addUpdateListener {
                val alpha = it.animatedValue as Int
                paint.alpha = alpha
                mutableBitmap.eraseColor(Color.TRANSPARENT)
                canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
                item.setMarker(mutableBitmap.toDrawable(context.resources))
                mapView.invalidate()
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
                        if (distance <= 120.0) {
                            Toast.makeText(context, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
                            // Aggiungi qui eventuale logica per avviare un'activity di gioco
                        } else {
                            Toast.makeText(context, "Avvicinati per giocare!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
            },
            context
        )
        mapView.overlays.add(poiOverlay)

        // Assicura che l'overlay utente sia sopra
        mapView.overlays.remove(locationOverlay)
        mapView.overlays.add(locationOverlay)
    }

    fun generateRandomPOIs(context: Context, mapView: MapView, locationOverlay: MyLocationNewOverlay, center: GeoPoint) {
        val currentTime = System.currentTimeMillis()

        // Rimuovi quelli scaduti
        val expiredPOIs = poiItems.filter { currentTime > it.createdAt + it.lifespan }
        expiredPOIs.forEach { expiredPoi ->
            animateDespawn(expiredPoi) {
                poiItems.remove(expiredPoi)
                recreatePoiOverlay(locationOverlay)
            }
        }




        // Controlla se aggiungere nuovi POI
        if (currentTime - lastPoiAddTime >= poiAddInterval) { //Very very very sus
            val currentCount = poiItems.size
            val forceAdd = currentCount < 12
            val allowAdd = currentCount < 28

            if (forceAdd || allowAdd) {
                val toAdd = if (forceAdd) (12 - currentCount + (0 .. 6).random()) else (0..2).random()
                repeat(toAdd) {
                    val latOffset = (Random.nextDouble() - 0.5) / 125
                    val lonOffset = (Random.nextDouble() - 0.5) / 125
                    val location = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)

                    val poiItem = OverlayItem("Sudoku", "Gioca!", location)
                    val bitmap = createPoiIcon(R.drawable.sudoku_icon, 100)
                    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = Canvas(mutableBitmap)
                    val paint = Paint()

                    // Animazione Fade-In
                    ValueAnimator.ofInt(0, 255).apply {
                        duration = 500
                        addUpdateListener {
                            paint.alpha = it.animatedValue as Int
                            mutableBitmap.eraseColor(Color.TRANSPARENT)
                            canvas.drawBitmap(bitmap, 0f, 0f, paint)
                            mapView.invalidate()
                        }
                        Handler(Looper.getMainLooper()).post {
                            start()
                        }
                    }

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

                lastPoiAddTime = currentTime
                poiAddInterval = (2000L..30000L).random()
            }
        }

        // Ricrea overlay
        poiOverlay?.let { mapView.overlays.remove(it) }
        poiOverlay = ItemizedIconOverlay(
            poiItems.map { it.item },
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    val userLocation = locationOverlay.myLocation
                    if (item != null && userLocation != null) {
                        val distance = haversineDistance(userLocation, item.point)
                        if (distance <= 120.0) {
                            Toast.makeText(context, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
                            // Aggiungi qui eventuale logica per avviare un'activity di gioco
                        } else {
                            Toast.makeText(context, "Avvicinati per giocare!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
            },
            context
        )
        mapView.overlays.add(poiOverlay)

        // Riaggiungi marker utente
        mapView.overlays.remove(locationOverlay)
        mapView.overlays.add(locationOverlay)
    }
}