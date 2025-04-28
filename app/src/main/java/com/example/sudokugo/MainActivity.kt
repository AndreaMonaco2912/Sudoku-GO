package com.example.sudokugo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.log
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val mapController: IMapController = map.controller
        mapController.setZoom(15.0)

        // Crea gpsProvider personalizzato
        val gpsProvider = GpsMyLocationProvider(this).apply {
            locationUpdateMinDistance = 5.0F // metri
            locationUpdateMinTime = 2000    // millisecondi
        }

        // Passa il provider a MyLocationNewOverlay
        locationOverlay = MyLocationNewOverlay(gpsProvider, map)
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
    }

    private fun generateRandomPOIs(center: GeoPoint) {
        val items = mutableListOf<OverlayItem>()

        repeat(3) { i ->
            val latOffset = (Random.nextDouble() - 0.5) / 500
            val lonOffset = (Random.nextDouble() - 0.5) / 500
            val poiLocation = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)

            val item = OverlayItem("POI #${i + 1}", "Punto di interesse", poiLocation)
            items.add(item)
        }

        val poiOverlay = ItemizedIconOverlay(
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
}