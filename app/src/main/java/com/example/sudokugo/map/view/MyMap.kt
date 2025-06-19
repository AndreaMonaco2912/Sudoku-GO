package com.example.sudokugo.map.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import com.example.sudokugo.map.classes.InertiaAnimation
import com.example.sudokugo.map.functions.createLocationOverlay
import com.example.sudokugo.map.functions.drawUserCenteredCircle
import com.example.sudokugo.ui.composables.configureMapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.hypot

@SuppressLint("ClickableViewAccessibility")
class MyMap(private val context: Context, mapCenter: GeoPoint, zoomLevel: Double, playSudoku: () -> Unit) {
    private val mapView: MapView = MapView(this.context).apply {
        configureMapView(this)
        val center = mapCenter
        controller.setCenter(center)
        controller.setZoom(zoomLevel)
    }
    private val poiManager: PoiManager = PoiManager(this.context, mapView, playSudoku)
    private val locationOverlay: MyLocationNewOverlay = createLocationOverlay(this.context, mapView)
    private val inertiaAnimation: InertiaAnimation = InertiaAnimation(mapView)

    init {
        mapView.overlays.add(locationOverlay)
        setupTouchListener()
    }

    fun getMapView(): MapView = mapView

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


    fun updateWorldMap(location: GeoPoint) {
        locationOverlay.enableFollowLocation()
        drawUserCenteredCircle(mapView, location, 120.0)
        poiManager.generateRandomPOIs(context, locationOverlay, location)
    }

    fun getLocation(): GeoPoint? = locationOverlay.myLocation

    fun getZoom() = mapView.zoomLevelDouble

    fun onResume() = mapView.onResume()

    fun onPause() = mapView.onPause()
}