package com.example.sudokugo.map.classes

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.compose.runtime.State
import com.example.sudokugo.map.functions.createLocationOverlay
import com.example.sudokugo.map.functions.createScaleGestureDetector
import com.example.sudokugo.map.functions.drawUserCenteredCircle
import com.example.sudokugo.ui.composables.configureMapView
//import com.example.sudokugo.ui.composables.saveMapInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.hypot

@SuppressLint("ClickableViewAccessibility")
class MyMap(private val context: Context, mapCenter: State<GeoPoint>, zoomLevel: State<Double>) {
    private val mapView: MapView
    private val poiManager: PoiManager
    private val locationOverlay: MyLocationNewOverlay
//    private val scaleDetector: ScaleGestureDetector
    private val inertiaAnimation: InertiaAnimation


//    private var backgroundJob: Job? = null

    init {

        mapView = MapView(this.context).apply {
            configureMapView(this)
            val center = mapCenter.value
            controller.setCenter(center)
            controller.setZoom(zoomLevel.value)
        }

        poiManager = PoiManager(this.context, mapView)
        locationOverlay = createLocationOverlay(this.context, mapView)
//        scaleDetector = createScaleGestureDetector(this.context, mapView)
        inertiaAnimation = InertiaAnimation(mapView)

        mapView.overlays.add(locationOverlay)

        setupTouchListener()
//        startBackgroundLoop()
    }

    fun getMapView(): MapView = mapView

//    private fun loadMapInfo(context: Context): Triple<Double, Double, Double> {
//        val prefs = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
//        val lat = prefs.getFloat("latitude", 45.4642f) // Default Milano
//        val lon = prefs.getFloat("longitude", 9.1900f)
//        val zoom = 18f
//        return Triple(lat.toDouble(), lon.toDouble(), zoom.toDouble())
//    }

    private fun setupTouchListener() {
        var activePointerId = MotionEvent.INVALID_POINTER_ID
        var lastKnownPositionX = 0f
        var lastKnownPositionY = 0f
        var lastAngle = 0f
        var rotating = false
        var lastRotationSpeed = 0f
        val touchSlop = 10
        val maxRotationSpeed = 5f

        mapView.setOnTouchListener { _, event ->
            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    // Se nessun dito attivo, scegli questo
                    if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                        activePointerId = event.getPointerId(event.actionIndex)
                        lastKnownPositionX = event.getX(event.actionIndex)
                        lastKnownPositionY = event.getY(event.actionIndex)

                        val centerPoint = mapView.projection.toPixels(mapView.mapCenter, null)
                        val dx = lastKnownPositionX - centerPoint.x
                        val dy = lastKnownPositionY - centerPoint.y
                        lastAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        rotating = false
                        lastRotationSpeed = 0f
                    }
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    if (pointerIndex == -1) return@setOnTouchListener true

                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)
                    val dx = x - lastKnownPositionX
                    val dy = y - lastKnownPositionY
                    val distance = hypot(dx.toDouble(), dy.toDouble())

                    if (distance > touchSlop) rotating = true

                    if (rotating) {
                        val centerPoint = mapView.projection.toPixels(mapView.mapCenter, null)
                        val dxCenter = x - centerPoint.x
                        val dyCenter = y - centerPoint.y
                        val angle = Math.toDegrees(atan2(dyCenter.toDouble(), dxCenter.toDouble())).toFloat()
                        var deltaAngle = angle - lastAngle
                        if (deltaAngle > 180) deltaAngle -= 360
                        if (deltaAngle < -180) deltaAngle += 360
                        lastRotationSpeed = deltaAngle.coerceIn(-maxRotationSpeed, maxRotationSpeed)
                        mapView.mapOrientation = (mapView.mapOrientation + deltaAngle) % 360
                        mapView.invalidate()
                        lastAngle = angle
                    }

                    lastKnownPositionX = x
                    lastKnownPositionY = y
                    true
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    val liftedPointerId = event.getPointerId(event.actionIndex)

                    if (liftedPointerId == activePointerId) {
                        // Promuovi un altro dito attivo, se ce n'è
                        for (i in 0 until event.pointerCount) {
                            val id = event.getPointerId(i)
                            if (id != liftedPointerId) {
                                activePointerId = id
                                lastKnownPositionX = event.getX(i)
                                lastKnownPositionY = event.getY(i)

                                val centerPoint = mapView.projection.toPixels(mapView.mapCenter, null)
                                val dx = lastKnownPositionX - centerPoint.x
                                val dy = lastKnownPositionY - centerPoint.y
                                lastAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                break
                            }
                        }
                    }

                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val liftedPointerId = event.getPointerId(event.actionIndex)

                    if (liftedPointerId == activePointerId) {
                        activePointerId = MotionEvent.INVALID_POINTER_ID
                        if (rotating && lastRotationSpeed != 0f) {
                            inertiaAnimation.startInertiaRotation(lastRotationSpeed)
                        }
                        rotating = false
                    }

                    mapView.performClick()
                    false
                }

                else -> false
            }
        }
    }


//    private fun startBackgroundLoop() {
//        backgroundJob?.cancel()
//
//        backgroundJob = CoroutineScope(Dispatchers.Default).launch {
//            while (true) {
//                val location = locationOverlay.myLocation
//                if (location != null) {
//                    val center = GeoPoint(location.latitude, location.longitude)
//                    saveMapInfo(
//                        context,
//                        location.latitude,
//                        location.longitude,
//                        mapView.zoomLevelDouble
//                    )
//
//                    poiManager.generateRandomPOIs(context, mapView, locationOverlay, center)
//                    drawUserCenteredCircle(mapView, center, 120.0)
//                }
//                delay(3000)
//            }
//        }
//    }

//    fun onDestroy() {
//        backgroundJob?.cancel()
//        backgroundJob = null
//    }

    fun updateWorldMap(location: GeoPoint) {
        locationOverlay.enableFollowLocation()
        drawUserCenteredCircle(mapView, location, 120.0)
        poiManager.generateRandomPOIs(context, mapView, locationOverlay, location)
    }

    fun getLocation(): GeoPoint? = locationOverlay.myLocation

    fun getZoom() = mapView.zoomLevelDouble

    fun onResume() = mapView.onResume()

    fun onPause() = mapView.onPause()
}