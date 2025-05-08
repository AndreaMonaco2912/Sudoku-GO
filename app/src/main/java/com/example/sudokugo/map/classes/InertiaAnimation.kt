package com.example.sudokugo.map.classes

import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import org.osmdroid.views.MapView

class InertiaAnimation(mapView: MapView) {
    private val inertiaAnimator = ValueAnimator.ofFloat(0f, 0f).apply {
        duration = 1000
        interpolator = DecelerateInterpolator()

        addUpdateListener { animation ->
            val delta = animation.animatedValue as Float
            mapView.mapOrientation = (mapView.mapOrientation + delta) % 360
            mapView.invalidate()
        }
    }


    fun startInertiaRotation(initialSpeed: Float) {
        inertiaAnimator?.cancel()
        inertiaAnimator?.setFloatValues(initialSpeed, 0f)
        inertiaAnimator?.start()
    }

    fun stopInertiaRotation() {
        inertiaAnimator?.cancel()
    }
}