package com.example.sudokugo.ui.composables

import android.annotation.SuppressLint
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
fun MapScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    val poiManager = remember { PoiManager(context, mapView) }
    lateinit var locationOverlay: MyLocationNewOverlay
    lateinit var scaleDetector: ScaleGestureDetector

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
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 16.0
                maxZoomLevel = 21.0
                controller.setZoom(19.0)

                // Setup GPS + overlay
                val gpsProvider = GpsMyLocationProvider(it).apply {
                    locationUpdateMinDistance = 5.0f
                    locationUpdateMinTime = 2000
                }
                locationOverlay = MyLocationNewOverlay(gpsProvider, this).apply {
                    isDrawAccuracyEnabled = false
                    enableMyLocation()
                    enableFollowLocation()
                    setPersonAnchor(0.5f, 0.5f)
                    setDirectionAnchor(0.5f, 0.5f)
                    val drawable = ContextCompat.getDrawable(it, R.drawable.character_icon) as BitmapDrawable
                    val scaled = getCircularBitmap(drawable.bitmap).scale(100, 100)
                    setPersonIcon(scaled)
                    setDirectionIcon(scaled)
                }
                overlays.add(locationOverlay)

                // Scale detector
                scaleDetector = ScaleGestureDetector(it, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val currentZoom = zoomLevelDouble
                        val scale = detector.scaleFactor
                        val newZoom = if (scale > 1) currentZoom + 0.1 else currentZoom - 0.1
                        controller.setZoom(newZoom.coerceIn(minZoomLevel, maxZoomLevel))
                        return true
                    }
                })



            }

            val inertiaAnimation = InertiaAnimation(mapView)

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

            val poiManager = PoiManager(context, mapView)



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
                poiManager.generateRandomPOIs(context, mapView, locationOverlay, center)
                drawUserCenteredCircle(mapView, center, 120.0)
            }
            delay(3000) // aspetta 3 secondi
        }
    }

}