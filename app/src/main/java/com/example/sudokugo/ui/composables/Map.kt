package com.example.sudokugo.ui.composables

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sudokugo.R
import com.example.sudokugo.map.classes.InertiaAnimation
import com.example.sudokugo.map.classes.PoiManager
import com.example.sudokugo.map.functions.drawUserCenteredCircle
import com.example.sudokugo.map.functions.getCircularBitmap
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.hypot

@SuppressLint("ClickableViewAccessibility")
@Composable
fun Map() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val (savedLat, savedLon, savedZoom) = remember { loadMapInfo(context) }
    val mapView = remember {
        MapView(context).apply {
        configureMapView(this)
        val center = GeoPoint(savedLat, savedLon)
        controller.setCenter(center)
        controller.setZoom(savedZoom)
    } }
    val poiManager = remember { PoiManager(context, mapView) }
    val locationOverlay = remember { createLocationOverlay(context, mapView) }
    val scaleDetector = remember { createScaleGestureDetector(context, mapView) }
    val inertiaAnimation = remember { InertiaAnimation(mapView) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        factory = {
            locationOverlay.enableFollowLocation()
            mapView.overlays.add(locationOverlay)

            // Gesture rotation handler (direttamente qui dentro)
            var lastAngle = 0f
            var rotating = false
            var lastRotationSpeed = 0f
            var touchStartX = 0f
            var touchStartY = 0f
            var ignoreTouchUntil = 0L
            val touchSlop = 10
            val maxRotationSpeed = 5f

            mapView.setOnTouchListener { _, event ->
                if (System.currentTimeMillis() < ignoreTouchUntil) return@setOnTouchListener true

                scaleDetector.onTouchEvent(event)
                val projection = mapView.projection
                val center = mapView.mapCenter
                val centerPoint = projection.toPixels(center, null)

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        inertiaAnimation.stopInertiaRotation()
                        touchStartX = event.x
                        touchStartY = event.y
                        val dx = event.x - centerPoint.x
                        val dy = event.y - centerPoint.y
                        lastAngle =
                            Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        rotating = false
                        lastRotationSpeed = 0f
                        locationOverlay.enableFollowLocation()
                        false
                    }

                    MotionEvent.ACTION_POINTER_UP -> {
                        // Ignora input per 200ms quando si passa da 2 a 1 dito
                        if (event.pointerCount == 2) {
                            ignoreTouchUntil = System.currentTimeMillis() + 200
                        }
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (event.pointerCount == 2) {
                            val x1 = event.getX(0)
                            val y1 = event.getY(0)
                            val x2 = event.getX(1)
                            val y2 = event.getY(1)
                            val angle = Math.toDegrees(
                                atan2(
                                    (y2 - y1).toDouble(),
                                    (x2 - x1).toDouble()
                                )
                            ).toFloat()
                            if (!rotating) {
                                rotating = true
                                lastAngle = angle
                            } else {
                                val deltaAngle = angle - lastAngle
                                mapView.mapOrientation =
                                    (mapView.mapOrientation + deltaAngle + 360) % 360
                                mapView.invalidate()
                                lastAngle = angle
                            }
                            true
                        } else {
                            val moveX = event.x - touchStartX
                            val moveY = event.y - touchStartY
                            val distance = hypot(moveX.toDouble(), moveY.toDouble())
                            if (distance > touchSlop) rotating = true
                            if (rotating) {
                                val dx = event.x - centerPoint.x
                                val dy = event.y - centerPoint.y
                                val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                                    .toFloat()
                                var deltaAngle = angle - lastAngle
                                if (deltaAngle > 180) deltaAngle -= 360
                                if (deltaAngle < -180) deltaAngle += 360
                                lastRotationSpeed =
                                    deltaAngle.coerceIn(-maxRotationSpeed, maxRotationSpeed)
                                mapView.mapOrientation =
                                    (mapView.mapOrientation + deltaAngle) % 360
                                mapView.invalidate()
                                lastAngle = angle
                                true
                            } else {
                                locationOverlay.enableFollowLocation()
                                false
                            }
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (rotating && lastRotationSpeed != 0f) {
                            inertiaAnimation.startInertiaRotation(lastRotationSpeed)
                        }
                        rotating = false
                        locationOverlay.enableFollowLocation()

                        mapView.performClick()
                        false
                    }

                    else -> false
                }
            }
            mapView
        },
        modifier = Modifier.fillMaxSize()
    )

    // Start POI loop
    LaunchedEffect(Unit) {
        while (true) {
            val location = locationOverlay.myLocation
            if (location != null) {
                val center = GeoPoint(location.latitude, location.longitude)
                saveMapInfo(context, location.latitude, location.longitude, mapView.zoomLevelDouble)


                poiManager.generateRandomPOIs(context, mapView, locationOverlay, center)
                drawUserCenteredCircle(mapView, center, 120.0)
            }
            delay(3000) // aspetta 3 secondi
        }
    }
}

fun configureMapView(mapView: MapView) {
    mapView.setTileSource(TileSourceFactory.MAPNIK)
    mapView.setMultiTouchControls(true)
    mapView.minZoomLevel = 16.0
    mapView.maxZoomLevel = 21.0
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
        setPersonAnchor(0.5f, 0.5f)
        setDirectionAnchor(0.5f, 0.5f)

        val drawable = ContextCompat.getDrawable(context, R.drawable.character_icon) as BitmapDrawable
        val scaled = getCircularBitmap(drawable.bitmap).scale(100, 100)
        setPersonIcon(scaled)
        setDirectionIcon(scaled)
    }
}

fun createScaleGestureDetector(context: Context, mapView: MapView): ScaleGestureDetector {
    return ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val currentZoom = mapView.zoomLevelDouble
            val scale = detector.scaleFactor
            val newZoom = if (scale > 1) currentZoom + 0.1 else currentZoom - 0.1
            mapView.controller.setZoom(newZoom.coerceIn(mapView.minZoomLevel, mapView.maxZoomLevel))
            return true
        }
    })
}

fun saveMapInfo(context: Context, latitude: Double, longitude: Double, zoom: Double) {
    val prefs = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
    prefs.edit().apply {
        putFloat("latitude", latitude.toFloat())
        putFloat("longitude", longitude.toFloat())
        putFloat("zoom", zoom.toFloat())
        apply()
    }
}

fun loadMapInfo(context: Context): Triple<Double, Double, Double> {
    val prefs = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
    val lat = prefs.getFloat("latitude", 45.4642f) // Default Milano
    val lon = prefs.getFloat("longitude", 9.1900f)
    val zoom = prefs.getFloat("zoom", 18f)
    return Triple(lat.toDouble(), lon.toDouble(), zoom.toDouble())
}
