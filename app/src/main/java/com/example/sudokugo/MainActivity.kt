package com.example.sudokugo

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.random.Random
import androidx.core.graphics.scale
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.compose.rememberNavController
import com.example.sudokugo.map.classes.InertiaAnimation
import com.example.sudokugo.map.classes.PoiManager
import com.example.sudokugo.map.functions.getCircularBitmap
import com.example.sudokugo.map.functions.drawUserCenteredCircle
import com.example.sudokugo.map.functions.haversineDistance
import com.example.sudokugo.ui.SudokuGONavGraph
import com.example.sudokugo.ui.composables.MapScreen
import org.osmdroid.views.overlay.Polygon
import kotlin.math.atan2
import kotlin.math.hypot
import com.example.sudokugo.ui.theme.SudokuGOTheme


class MainActivity : ComponentActivity() {

//    private lateinit var mapView: MapView
//    private lateinit var locationOverlay: MyLocationNewOverlay

//    data class TimedPOI(val item: OverlayItem, val createdAt: Long, val lifespan: Long)



//    private lateinit var scaleDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Configuration.getInstance().userAgentValue = packageName

        requestPermissionsIfNecessary(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        ))

        setContent {
            SudokuGOTheme {
//                val navController = rememberNavController()
//                SudokuGONavGraph(navController)
                MapScreen()
            }
        }
    }


//    @SuppressLint("ClickableViewAccessibility")
//    @Composable
//    fun MapScreen() {
//        val context = LocalContext.current
//
//        AndroidView(
//            factory = {
//                mapView = MapView(it).apply {
//                    setTileSource(TileSourceFactory.MAPNIK)
//                    setMultiTouchControls(true)
//                    minZoomLevel = 16.0
//                    maxZoomLevel = 21.0
//                    controller.setZoom(19.0)
//
//                    // Setup GPS + overlay
//                    val gpsProvider = GpsMyLocationProvider(it).apply {
//                        locationUpdateMinDistance = 5.0f
//                        locationUpdateMinTime = 2000
//                    }
//                    locationOverlay = MyLocationNewOverlay(gpsProvider, this).apply {
//                        isDrawAccuracyEnabled = false
//                        enableMyLocation()
//                        enableFollowLocation()
//                        setPersonAnchor(0.5f, 0.5f)
//                        setDirectionAnchor(0.5f, 0.5f)
//                        val drawable = ContextCompat.getDrawable(it, R.drawable.character_icon) as BitmapDrawable
//                        val scaled = getCircularBitmap(drawable.bitmap).scale(100, 100)
//                        setPersonIcon(scaled)
//                        setDirectionIcon(scaled)
//                    }
//                    overlays.add(locationOverlay)
//
//                    // Scale detector
//                    scaleDetector = ScaleGestureDetector(it, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//                        override fun onScale(detector: ScaleGestureDetector): Boolean {
//                            val currentZoom = zoomLevelDouble
//                            val scale = detector.scaleFactor
//                            val newZoom = if (scale > 1) currentZoom + 0.1 else currentZoom - 0.1
//                            controller.setZoom(newZoom.coerceIn(minZoomLevel, maxZoomLevel))
//                            return true
//                        }
//                    })
//
//
//
//                }
//
//                val inertiaAnimation = InertiaAnimation(mapView)
//
//                // Gesture rotation handler (direttamente qui dentro)
//                var lastAngle = 0f
//                var rotating = false
//                var lastRotationSpeed = 0f
//                var touchStartX = 0f
//                var touchStartY = 0f
//                var ignoreTouchUntil = 0L
//                val touchSlop = 10
//                val maxRotationSpeed = 5f
//
//                mapView.setOnTouchListener { _, event ->
//                    if (System.currentTimeMillis() < ignoreTouchUntil) return@setOnTouchListener true
//
//                    scaleDetector.onTouchEvent(event)
//                    val projection = mapView.projection
//                    val center = mapView.mapCenter
//                    val centerPoint = projection.toPixels(center, null)
//
//                    when (event.actionMasked) {
//                        MotionEvent.ACTION_DOWN -> {
//                            inertiaAnimation.stopInertiaRotation()
//                            touchStartX = event.x
//                            touchStartY = event.y
//                            val dx = event.x - centerPoint.x
//                            val dy = event.y - centerPoint.y
//                            lastAngle =
//                                Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
//                            rotating = false
//                            lastRotationSpeed = 0f
//                            locationOverlay.enableFollowLocation()
//                            false
//                        }
//
//                        MotionEvent.ACTION_POINTER_UP -> {
//                            // Ignora input per 200ms quando si passa da 2 a 1 dito
//                            if (event.pointerCount == 2) {
//                                ignoreTouchUntil = System.currentTimeMillis() + 200
//                            }
//                            true
//                        }
//
//                        MotionEvent.ACTION_MOVE -> {
//                            if (event.pointerCount == 2) {
//                                val x1 = event.getX(0)
//                                val y1 = event.getY(0)
//                                val x2 = event.getX(1)
//                                val y2 = event.getY(1)
//                                val angle = Math.toDegrees(
//                                    atan2(
//                                        (y2 - y1).toDouble(),
//                                        (x2 - x1).toDouble()
//                                    )
//                                ).toFloat()
//                                if (!rotating) {
//                                    rotating = true
//                                    lastAngle = angle
//                                } else {
//                                    val deltaAngle = angle - lastAngle
//                                    mapView.mapOrientation =
//                                        (mapView.mapOrientation + deltaAngle + 360) % 360
//                                    mapView.invalidate()
//                                    lastAngle = angle
//                                }
//                                true
//                            } else {
//                                val moveX = event.x - touchStartX
//                                val moveY = event.y - touchStartY
//                                val distance = hypot(moveX.toDouble(), moveY.toDouble())
//                                if (distance > touchSlop) rotating = true
//                                if (rotating) {
//                                    val dx = event.x - centerPoint.x
//                                    val dy = event.y - centerPoint.y
//                                    val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
//                                        .toFloat()
//                                    var deltaAngle = angle - lastAngle
//                                    if (deltaAngle > 180) deltaAngle -= 360
//                                    if (deltaAngle < -180) deltaAngle += 360
//                                    lastRotationSpeed =
//                                        deltaAngle.coerceIn(-maxRotationSpeed, maxRotationSpeed)
//                                    mapView.mapOrientation =
//                                        (mapView.mapOrientation + deltaAngle) % 360
//                                    mapView.invalidate()
//                                    lastAngle = angle
//                                    true
//                                } else {
//                                    locationOverlay.enableFollowLocation()
//                                    false
//                                }
//                            }
//                        }
//
//                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                            if (rotating && lastRotationSpeed != 0f) {
//                                inertiaAnimation.startInertiaRotation(lastRotationSpeed)
//                            }
//                            rotating = false
//                            locationOverlay.enableFollowLocation()
//
//                            mapView.performClick()
//                            false
//                        }
//
//                        else -> false
//                    }
//                }
//
//                val poiManager = PoiManager(context, mapView)
//
//                // Start POI loop
//                val handler = android.os.Handler(mainLooper)
//                handler.post(object : Runnable {
//                    override fun run() {
//                        locationOverlay.myLocation?.let {
//                            val center = GeoPoint(it.latitude, it.longitude)
//                            poiManager.generateRandomPOIs(context, mapView, locationOverlay, center)
//                            drawUserCenteredCircle(mapView, center, 120.0)
//                        }
//                        handler.postDelayed(this, 3000)
//                    }
//                })
//
//                mapView
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//
//    }

//    private fun createPoiIcon(drawableId: Int, size: Int): Bitmap {
//        val output = createBitmap(size, size)
//        val canvas = Canvas(output)
//
//        val paint = Paint().apply {
//            isAntiAlias = true
//            color = Color.WHITE // Sfondo bianco pieno
//            style = Paint.Style.FILL
//        }
//
//        // Disegna lo sfondo bianco interamente
//        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
//
//        // Carica l'immagine dell'icona
//        val drawable = ContextCompat.getDrawable(this, drawableId) as BitmapDrawable
//        val originalBitmap = drawable.bitmap
//
//        // Calcola proporzioni corrette per scalare l'immagine
//        val scale = minOf(
//            size.toFloat() / originalBitmap.width,
//            size.toFloat() / originalBitmap.height
//        )
//
//        val newWidth = (originalBitmap.width * scale).toInt()
//        val newHeight = (originalBitmap.height * scale).toInt()
//
//        val left = (size - newWidth) / 2
//        val top = (size - newHeight) / 2
//
//        val destRect = Rect(left, top, left + newWidth, top + newHeight)
//
//        // Disegna l'immagine sopra
//        canvas.drawBitmap(originalBitmap, null, destRect, null)
//
//        return output
//    }

//    private fun haversineDistance(p1: IGeoPoint, p2: IGeoPoint): Double {
//        val R = 6371000.0 // raggio Terra in metri
//        val dLat = Math.toRadians(p2.latitude - p1.latitude)
//        val dLon = Math.toRadians(p2.longitude - p1.longitude)
//        val lat1 = Math.toRadians(p1.latitude)
//        val lat2 = Math.toRadians(p2.latitude)
//
//        val a = sin(dLat / 2) * sin(dLat / 2) +
//                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
//        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
//
//        return R * c
//    }

//    private fun animateDespawn(poi: TimedPOI) {
//        val item = poi.item
//        val originalDrawable = item.drawable as? BitmapDrawable ?: return
//        val originalBitmap = originalDrawable.bitmap
//
//        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
//        val canvas = Canvas(mutableBitmap)
//        val paint = Paint().apply { isAntiAlias = true }
//
//        val animator = ValueAnimator.ofInt(255, 0).apply {
//            duration = 500
//            addUpdateListener {
//                val alpha = it.animatedValue as Int
//                paint.alpha = alpha
//                mutableBitmap.eraseColor(Color.TRANSPARENT)
//                canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
//                item.setMarker(mutableBitmap.toDrawable(resources))
//                mapView.invalidate()
//            }
//            doOnEnd {
//                // Dopo fade out completo, rimuovi
//                poiItems.remove(poi)
//                recreatePoiOverlay()
//            }
//            start()
//        }
//    }
//
//    private fun recreatePoiOverlay() {
//        poiOverlay?.let { mapView.overlays.remove(it) }
//        poiOverlay = ItemizedIconOverlay(
//            poiItems.map { it.item },
//            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
//                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
//                    val userLocation = locationOverlay.myLocation
//                    if (item != null && userLocation != null) {
//                        val distance = haversineDistance(userLocation, item.point)
//                        if (distance <= 120.0) {
//                            Toast.makeText(this@MainActivity, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
//                            // Aggiungi qui eventuale logica per avviare un'activity di gioco
//                        } else {
//                            Toast.makeText(this@MainActivity, "Avvicinati per giocare!", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    return true
//                }
//
//                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
//            },
//            applicationContext
//        )
//        mapView.overlays.add(poiOverlay)
//
//        // Assicura che l'overlay utente sia sopra
//        mapView.overlays.remove(locationOverlay)
//        mapView.overlays.add(locationOverlay)
//    }
//
//    private fun generateRandomPOIs(context: Context, center: GeoPoint) {
//        val currentTime = System.currentTimeMillis()
//
//        // Rimuovi quelli scaduti
//        val iterator = poiItems.iterator()
//        while (iterator.hasNext()) {
//            val poi = iterator.next()
//            if (currentTime > poi.createdAt + poi.lifespan) {
//                animateDespawn(poi)
//            }
//        }
//
//        // Controlla se aggiungere nuovi POI
//        if (currentTime - lastPoiAddTime >= poiAddInterval) { //Very very very sus
//            val currentCount = poiItems.size
//            val forceAdd = currentCount < 12
//            val allowAdd = currentCount < 28
//
//            if (forceAdd || allowAdd) {
//                val toAdd = if (forceAdd) (12 - currentCount + (0 .. 6).random()) else (0..2).random()
//                repeat(toAdd) {
//                    val latOffset = (Random.nextDouble() - 0.5) / 125
//                    val lonOffset = (Random.nextDouble() - 0.5) / 125
//                    val location = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)
//
//                    val poiItem = OverlayItem("Sudoku", "Gioca!", location)
//                    val bitmap = createPoiIcon(context, R.drawable.sudoku_icon, 100)
//                    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//                    val canvas = Canvas(mutableBitmap)
//                    val paint = Paint()
//
//                    // Animazione Fade-In
//                    ValueAnimator.ofInt(0, 255).apply {
//                        duration = 500
//                        addUpdateListener {
//                            paint.alpha = it.animatedValue as Int
//                            mutableBitmap.eraseColor(Color.TRANSPARENT)
//                            canvas.drawBitmap(bitmap, 0f, 0f, paint)
//                            mapView.invalidate()
//                        }
//                        start()
//                    }
//
//                    poiItem.setMarker(mutableBitmap.toDrawable(resources))
//                    poiItem.markerHotspot = OverlayItem.HotspotPlace.CENTER
//
//                    poiItems.add(
//                        TimedPOI(
//                            item = poiItem,
//                            createdAt = currentTime,
//                            lifespan = (25000L..40000L).random()
//                        )
//                    )
//                }
//
//                lastPoiAddTime = currentTime
//                poiAddInterval = (2000L..30000L).random()
//            }
//        }
//
//        // Ricrea overlay
//        poiOverlay?.let { mapView.overlays.remove(it) }
//        poiOverlay = ItemizedIconOverlay(
//            poiItems.map { it.item },
//            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
//                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
//                    val userLocation = locationOverlay.myLocation
//                    if (item != null && userLocation != null) {
//                        val distance = haversineDistance(userLocation, item.point)
//                        if (distance <= 120.0) {
//                            Toast.makeText(this@MainActivity, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
//                            // Aggiungi qui eventuale logica per avviare un'activity di gioco
//                        } else {
//                            Toast.makeText(this@MainActivity, "Avvicinati per giocare!", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    return true
//                }
//
//                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
//            },
//            applicationContext
//        )
//        mapView.overlays.add(poiOverlay)
//
//        // Riaggiungi marker utente
//        mapView.overlays.remove(locationOverlay)
//        mapView.overlays.add(locationOverlay)
//    }
//
//    private fun drawUserCenteredCircle(center: GeoPoint, radiusInMeters: Double) {
//        val circle = Polygon().apply {
//            points = Polygon.pointsAsCircle(center, radiusInMeters)
//            outlinePaint.color = android.graphics.Color.DKGRAY
//            outlinePaint.strokeWidth = 4f
//            outlinePaint.style = Paint.Style.STROKE
//            outlinePaint.isAntiAlias = true
//
////            // Fill
////            fillPaint.style = android.graphics.Paint.Style.FILL
////            fillPaint.isAntiAlias = true
//
//            setInfoWindow(null)
//        }
//
//        // Remove old if exists
//        mapView.overlays.removeIf { it is Polygon && it.infoWindow == null }
//
//        mapView.overlays.add(circle)
//        mapView.invalidate()
//    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, toRequest.toTypedArray(), 1
            )
        }
    }

//    override fun onResume() {
//        super.onResume()
//        if (::mapView.isInitialized) mapView.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if (::mapView.isInitialized) mapView.onPause()
//    }
}
