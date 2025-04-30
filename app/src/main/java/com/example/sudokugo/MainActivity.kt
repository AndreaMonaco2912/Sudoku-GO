package com.example.sudokugo

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
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
import org.osmdroid.api.IGeoPoint
import org.osmdroid.views.overlay.Polygon
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    data class TimedPOI(val item: OverlayItem, val createdAt: Long, val lifespan: Long)

    private lateinit var handler: android.os.Handler
    private lateinit var poiRunnable: Runnable


    private var poiOverlay: ItemizedIconOverlay<OverlayItem>? = null

    private var lastAngle = 0f
    private var rotating = false
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var lastRotationSpeed = 0f
    private var inertiaAnimator: ValueAnimator? = null

    private var initialRotationAngle = 0f
    private var lastMultiTouchAngle = 0f


    private val touchSlop = 10  // Pixel minimo per riconoscere drag vs tap
    private val maxRotationSpeed = 5f // Limita la velocità massima di inerzia
    private val fixedZoomLevel = 15.0

    private val poiItems = mutableListOf<TimedPOI>()

    private lateinit var scaleDetector: ScaleGestureDetector


    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    private var lastPoiAddTime = 0L
    private var poiAddInterval = randomAddInterval() // 17-30 secondi
    private fun randomAddInterval() = (200L..400L).random()

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_main)

        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoom = map.zoomLevelDouble
                val scale = detector.scaleFactor

                // Use scale to adjust zoom level
                val newZoom = if (scale > 1) currentZoom + 0.1 else currentZoom - 0.1
                map.controller.setZoom(newZoom.coerceIn(map.minZoomLevel, map.maxZoomLevel))
                return true
            }
        })

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.isClickable = true

        val mapController: IMapController = map.controller
        mapController.setZoom(fixedZoomLevel)

        map.minZoomLevel = 30.0 // Example: limit zoom out to level 10


        // This line will *force-follow* after any touch
        map.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event) // Handle pinch zoom

            val pointerCount = event.pointerCount
//            if (pointerCount > 1) {
//                // Don't process rotation when multi-touch (likely pinch)
//                return@setOnTouchListener false
//            }

            val projection = map.projection
            val centerGeo = map.mapCenter
            val centerPoint = projection.toPixels(centerGeo, null)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
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
                    if (event.pointerCount == 2) {
                        val x1 = event.getX(0)
                        val y1 = event.getY(0)
                        val x2 = event.getX(1)
                        val y2 = event.getY(1)

                        val angle = Math.toDegrees(
                            atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
                        ).toFloat()

                        if (!rotating) {
                            initialRotationAngle = angle
                            lastMultiTouchAngle = map.mapOrientation
                            rotating = true
                        } else {
                            val deltaAngle = angle - initialRotationAngle
                            map.mapOrientation = (lastMultiTouchAngle + deltaAngle + 360) % 360
                            map.invalidate()
                        }

                        true
                    } else {
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
                    drawUserCenteredCircle(center, 25.0) // radius in meters
                }
                handler.postDelayed(this, 3000) // repeat after 3 seconds
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

    private fun haversineDistance(p1: IGeoPoint, p2: GeoPoint): Double {
        val R = 6371000.0 // raggio Terra in metri
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

//    private fun animateSpawn(poiItem: OverlayItem) {
//        val fadeIn = android.view.animation.AlphaAnimation(0f, 1f).apply {
//            duration = 500
//            fillAfter = true
//        }
//        val mapView = map
//        val view = android.view.View(this).apply {
//            layoutParams = android.view.ViewGroup.LayoutParams(50, 50)
//            setBackgroundColor(android.graphics.Color.TRANSPARENT)
//            startAnimation(fadeIn)
//        }
//        mapView.overlayManager.addAfter(poiOverlay, object : org.osmdroid.views.overlay.Overlay() {
//            override fun draw(c: android.graphics.Canvas?, osmv: MapView?, shadow: Boolean) {
//                osmv?.overlayManager?.remove(this)
//            }
//        })
//    }
//
private fun animateDespawn(poi: TimedPOI) {
    val item = poi.item
    val originalDrawable = item.drawable as? BitmapDrawable ?: return
    val originalBitmap = originalDrawable.bitmap

    val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = android.graphics.Canvas(mutableBitmap)
    val paint = android.graphics.Paint().apply { isAntiAlias = true }

    val animator = ValueAnimator.ofInt(255, 0).apply {
        duration = 500
        addUpdateListener {
            val alpha = it.animatedValue as Int
            paint.alpha = alpha
            mutableBitmap.eraseColor(android.graphics.Color.TRANSPARENT)
            canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
            item.setMarker(mutableBitmap.toDrawable(resources))
            map.invalidate()
        }
        doOnEnd {
            // Dopo fade out completo, rimuovi
            poiItems.remove(poi)
            recreatePoiOverlay()
        }
        start()
    }
}


    private fun recreatePoiOverlay() {
        poiOverlay?.let { map.overlays.remove(it) }
        poiOverlay = ItemizedIconOverlay(
            poiItems.map { it.item },
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    item?.let {
                        Toast.makeText(this@MainActivity, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
            },
            applicationContext
        )
        map.overlays.add(poiOverlay)

        // Assicura che l'overlay utente sia sopra
        map.overlays.remove(locationOverlay)
        map.overlays.add(locationOverlay)
    }





    private fun generateRandomPOIs(center: GeoPoint) {
        val currentTime = System.currentTimeMillis()

        // Rimuovi quelli scaduti
        val iterator = poiItems.iterator()
        while (iterator.hasNext()) {
            val poi = iterator.next()
            if (currentTime > poi.createdAt + poi.lifespan) {
                animateDespawn(poi)
            }
        }

        // Controlla se aggiungere nuovi POI
        if (currentTime - lastPoiAddTime >= poiAddInterval) {
            val currentCount = poiItems.size
            val forceAdd = currentCount < 3
            val allowAdd = currentCount < 7

            if (forceAdd || allowAdd) {
                val toAdd = if (forceAdd) (3 - currentCount) else 1
                repeat(toAdd) {
                    val latOffset = (Random.nextDouble() - 0.5) / 500
                    val lonOffset = (Random.nextDouble() - 0.5) / 500
                    val location = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)

                    val poiItem = OverlayItem("Sudoku", "Gioca!", location)
                    val bitmap = createPoiIcon(R.drawable.sudoku_icon, 100)
                    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = android.graphics.Canvas(mutableBitmap)
                    val paint = android.graphics.Paint()

                    // Animazione Fade-In
                    ValueAnimator.ofInt(0, 255).apply {
                        duration = 500
                        addUpdateListener {
                            paint.alpha = it.animatedValue as Int
                            mutableBitmap.eraseColor(android.graphics.Color.TRANSPARENT)
                            canvas.drawBitmap(bitmap, 0f, 0f, paint)
                            map.invalidate()
                        }
                        start()
                    }

                    poiItem.setMarker(mutableBitmap.toDrawable(resources))
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
                poiAddInterval = (20000L..30000L).random()
            }
        }

        // Ricrea overlay
        poiOverlay?.let { map.overlays.remove(it) }
        poiOverlay = ItemizedIconOverlay(
            poiItems.map { it.item },
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    item?.let {
                        Toast.makeText(this@MainActivity, "Cliccato: ${item.title}", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
            },
            applicationContext
        )
        map.overlays.add(poiOverlay)

        // Riaggiungi marker utente
        map.overlays.remove(locationOverlay)
        map.overlays.add(locationOverlay)
    }



    private fun drawUserCenteredCircle(center: GeoPoint, radiusInMeters: Double) {
        val circle = Polygon().apply {
            points = Polygon.pointsAsCircle(center, radiusInMeters)
            outlinePaint.color = android.graphics.Color.BLUE
            outlinePaint.strokeWidth = 2f
            outlinePaint.style = android.graphics.Paint.Style.STROKE
            outlinePaint.isAntiAlias = true

            // Fill
            fillPaint.color = android.graphics.Color.argb(50, 0, 0, 255)
            fillPaint.style = android.graphics.Paint.Style.FILL
            fillPaint.isAntiAlias = true

            setInfoWindow(null)
        }

        // Remove old if exists
        map.overlays.removeIf { it is Polygon && it.infoWindow == null }

        map.overlays.add(circle)
        map.invalidate()
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