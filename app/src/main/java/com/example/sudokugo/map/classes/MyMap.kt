package com.example.sudokugo.map.classes

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.example.sudokugo.map.functions.createLocationOverlay
import com.example.sudokugo.map.functions.createScaleGestureDetector
import com.example.sudokugo.map.functions.drawUserCenteredCircle
import com.example.sudokugo.ui.composables.configureMapView
import com.example.sudokugo.ui.composables.saveMapInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.hypot

@SuppressLint("ClickableViewAccessibility")
class MyMap(private val context: Context) {
    private val mapView: MapView
    private val poiManager: PoiManager
    private val locationOverlay: MyLocationNewOverlay
    private val scaleDetector: ScaleGestureDetector
    private val inertiaAnimation: InertiaAnimation

    private var savedLat: Double = 0.0
    private var savedLon: Double = 0.0
    private var savedZoom: Double = 0.0

    private var backgroundJob: Job? = null

    init {

        val (lat, lon, zoom) = loadMapInfo(this.context)
        savedLat = lat
        savedLon = lon
        savedZoom = zoom

        mapView = MapView(this.context).apply {
            configureMapView(this)
            val center = GeoPoint(savedLat, savedLon)
            controller.setCenter(center)
            controller.setZoom(savedZoom)
        }

        poiManager = PoiManager(this.context, mapView)
        locationOverlay = createLocationOverlay(this.context, mapView)
        scaleDetector = createScaleGestureDetector(this.context, mapView)
        inertiaAnimation = InertiaAnimation(mapView)

        mapView.overlays.add(locationOverlay)

        setupTouchListener()
        startBackgroundLoop()
    }

    fun getMapView(): MapView = mapView

    private fun loadMapInfo(context: Context): Triple<Double, Double, Double> {
        val prefs = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("latitude", 45.4642f) // Default Milano
        val lon = prefs.getFloat("longitude", 9.1900f)
        val zoom = prefs.getFloat("zoom", 18f)
        return Triple(lat.toDouble(), lon.toDouble(), zoom.toDouble())
    }

    private fun setupTouchListener(){
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
    }

    private fun startBackgroundLoop() {
        backgroundJob?.cancel()

        backgroundJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val location = locationOverlay.myLocation
                if (location != null) {
                    val center = GeoPoint(location.latitude, location.longitude)
                    saveMapInfo(
                        context,
                        location.latitude,
                        location.longitude,
                        mapView.zoomLevelDouble
                    )

                    poiManager.generateRandomPOIs(context, mapView, locationOverlay, center)
                    drawUserCenteredCircle(mapView, center, 120.0)
                }
                delay(3000)
            }
        }
    }

    fun onDestroy() {
        backgroundJob?.cancel()
        backgroundJob = null
    }

    fun onResume() = mapView.onResume()

    fun onPause() = mapView.onPause()
}