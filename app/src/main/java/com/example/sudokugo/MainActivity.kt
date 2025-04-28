package com.example.sudokugo

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.random.Random
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.drawable.toDrawable
import kotlin.math.atan2
import kotlin.math.hypot

class MainActivity : AppCompatActivity() {
    private lateinit var handler: android.os.Handler
    private lateinit var poiRunnable: Runnable


    private var poiOverlay: ItemizedIconOverlay<OverlayItem>? = null

    private var lastAngle = 0f
    private var rotating = false
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var lastRotationSpeed = 0f
    private var inertiaAnimator: ValueAnimator? = null

    private val touchSlop = 10  // Pixel minimo per riconoscere drag vs tap
    private val maxRotationSpeed = 5f // Limita la velocità massima di inerzia
    private val fixedZoomLevel = 15.0



    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(false)
        map.isClickable = true

        val mapController: IMapController = map.controller
        mapController.setZoom(fixedZoomLevel)


        // This line will *force-follow* after any touch
        map.setOnTouchListener { _, event ->
            val projection = map.projection
            val centerGeo = map.mapCenter
            val centerPoint = projection.toPixels(centerGeo, null)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Stop any previous inertia
                    inertiaAnimator?.cancel()

                    touchStartX = event.x
                    touchStartY = event.y

                    val dx = event.x - centerPoint.x
                    val dy = event.y - centerPoint.y
                    lastAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

                    rotating = false
                    lastRotationSpeed = 0f
                    locationOverlay.enableFollowLocation()
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val moveX = event.x - touchStartX
                    val moveY = event.y - touchStartY
                    val distanceMoved = hypot(moveX.toDouble(), moveY.toDouble())

                    if (distanceMoved > touchSlop) {
                        rotating = true
                    }

                    if (rotating) {
                        val dx = event.x - centerPoint.x
                        val dy = event.y - centerPoint.y
                        val currentAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

                        var deltaAngle = currentAngle - lastAngle
                        if (deltaAngle > 180) deltaAngle -= 360
                        if (deltaAngle < -180) deltaAngle += 360

                        // Limit rotation speed
                        lastRotationSpeed = deltaAngle.coerceIn(-maxRotationSpeed, maxRotationSpeed)

                        map.mapOrientation = (map.mapOrientation + deltaAngle) % 360
                        map.invalidate()

                        lastAngle = currentAngle
                        true
                    } else {
                        locationOverlay.enableFollowLocation()
                        false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (rotating && lastRotationSpeed != 0f) {
                        startInertiaRotation(lastRotationSpeed)
                    }
                    rotating = false
                    locationOverlay.enableFollowLocation()
                    false
                }
                else -> false
            }


        }

//        val rotationGestureOverlay = RotationGestureOverlay(map)
//        rotationGestureOverlay.isEnabled = true
//        map.overlays.add(rotationGestureOverlay)

        // Crea gpsProvider personalizzato
        val gpsProvider = GpsMyLocationProvider(this).apply {
            locationUpdateMinDistance = 5.0F // metri
            locationUpdateMinTime = 2000    // millisecondi
        }

        // Passa il provider a MyLocationNewOverlay
        locationOverlay = MyLocationNewOverlay(gpsProvider, map)

        locationOverlay.isDrawAccuracyEnabled = false

        // Carica l'immagine da drawable
        val drawable = ContextCompat.getDrawable(this, R.drawable.character_icon) as BitmapDrawable
        val originalBitmap = drawable.bitmap

// Ritaglia a forma circolare
        val circularBitmap = getCircularBitmap(originalBitmap)

// Ridimensiona l'immagine (es: 100x100 pixel)
        val scaledBitmap = circularBitmap.scale(100, 100)

// Imposta l'icona personalizzata
        locationOverlay.setPersonIcon(scaledBitmap)
        locationOverlay.setDirectionIcon(scaledBitmap)

// Centra l'icona
        locationOverlay.setPersonAnchor(0.5f, 0.5f)
        locationOverlay.setDirectionAnchor(0.5f, 0.5f)


        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

//        locationOverlay.myLocationProvider?.locationSource?.let { provider ->
//            if (provider is GpsMyLocationProvider) {
//                provider.addLocationSource(object : GpsMyLocationProvider.LocationSource {
//                    override fun onLocationChanged(location: Location, providerName: String?) {
//                        runOnUiThread {
//                            val userLocation = GeoPoint(location.latitude, location.longitude)
//                            map.controller.animateTo(userLocation)
//                        }
//                    }
//                })
//            }
//        }
        // Quando trova per la prima volta la posizione
//        locationOverlay.runOnFirstFix {
//            val location = locationOverlay.myLocation
//            location?.let {
//                runOnUiThread {
//                    val userLocation = GeoPoint(it.latitude, it.longitude)
//                    map.controller.animateTo(userLocation)
//                    generateRandomPOIs(userLocation)
//                }
//            }
//        }

        locationOverlay.enableFollowLocation()

        requestPermissionsIfNecessary(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        ))

        handler = android.os.Handler(mainLooper)

        poiRunnable = object : Runnable {
            override fun run() {
                val location = locationOverlay.myLocation
                if (location != null) {
                    val center = GeoPoint(location.latitude, location.longitude)
                    generateRandomPOIs(center)
                }
                handler.postDelayed(this, 1000) // repeat after 5 seconds
            }
        }
        handler.post(poiRunnable)
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = createBitmap(size, size)

        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        val srcRect = android.graphics.Rect(
            (bitmap.width - size) / 2,
            (bitmap.height - size) / 2,
            (bitmap.width + size) / 2,
            (bitmap.height + size) / 2
        )
        val destRect = android.graphics.Rect(0, 0, size, size)
        canvas.drawBitmap(bitmap, srcRect, destRect, paint)

        return output
    }



    private fun startInertiaRotation(initialSpeed: Float) {
        inertiaAnimator = ValueAnimator.ofFloat(initialSpeed, 0f).apply {
            duration = 1000 // 1 secondo di inerzia
            interpolator = DecelerateInterpolator()

            addUpdateListener { animation ->
                val delta = animation.animatedValue as Float
                map.mapOrientation = (map.mapOrientation + delta) % 360
                map.invalidate()
            }

            start()
        }
    }

//    private fun generateRandomPOIs(center: GeoPoint) {
//
//        poiOverlay?.let {
//            map.overlays.remove(it)
//        }
//        val items = mutableListOf<OverlayItem>()
//
//        repeat(3) { i ->
//            val latOffset = (Random.nextDouble() - 0.5) / 500
//            val lonOffset = (Random.nextDouble() - 0.5) / 500
//            val poiLocation = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)
//
//            val item = OverlayItem("POI #${i + 1}", "Punto di interesse", poiLocation)
//            items.add(item)
//        }
//
//         poiOverlay = ItemizedIconOverlay(
//            items,
//            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
//                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
//                    item?.let {
//                        Toast.makeText(this@MainActivity, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
//                    }
//                    return true
//                }
//
//                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
//                    return false
//                }
//            },
//            applicationContext
//        )
//
//        map.overlays.add(poiOverlay)
//    }

    private fun createPoiIcon(drawableId: Int, size: Int): Bitmap {
        val output = createBitmap(size, size)
        val canvas = android.graphics.Canvas(output)

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE // Sfondo bianco pieno
            style = android.graphics.Paint.Style.FILL
        }

        // Disegna lo sfondo bianco interamente
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        // Carica l'immagine dell'icona
        val drawable = ContextCompat.getDrawable(this, drawableId) as BitmapDrawable
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

        val destRect = android.graphics.Rect(left, top, left + newWidth, top + newHeight)

        // Disegna l'immagine sopra
        canvas.drawBitmap(originalBitmap, null, destRect, null)

        return output
    }



    private fun generateRandomPOIs(center: GeoPoint) {
        poiOverlay?.let {
            map.overlays.remove(it)
        }

        val items = mutableListOf<OverlayItem>()

        repeat(3) { i ->
            val latOffset = (Random.nextDouble() - 0.5) / 500
            val lonOffset = (Random.nextDouble() - 0.5) / 500
            val poiLocation = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)

            val poiItem = OverlayItem("POI #${i + 1}", "Punto di interesse", poiLocation)

            val poiBitmap = createPoiIcon(R.drawable.poi_image, 100) // Immagine quadrata
            val markerDrawable = poiBitmap.toDrawable(resources)

            poiItem.setMarker(markerDrawable)
            poiItem.markerHotspot = OverlayItem.HotspotPlace.CENTER // <--- ECCO QUI!

            items.add(poiItem)
        }

        poiOverlay = ItemizedIconOverlay(
            items,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    item?.let {
                        Toast.makeText(this@MainActivity, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    return false
                }
            },
            applicationContext
        )

        map.overlays.add(poiOverlay)

        // Riaggiungo il personaggio sopra
        map.overlays.remove(locationOverlay)
        map.overlays.add(locationOverlay)
    }




    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }
}