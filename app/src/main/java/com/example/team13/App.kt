package com.example.team13

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.app.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker


class App : AppCompatActivity(), LocationListener{

    var lm: LocationManager? = null
   
    @SuppressLint("MissingPermission")

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)


        val ctx = applicationContext

        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        //inflate layout after loading (to make sure that app can write to cache)
        setContentView(R.layout.app)

        // map setup code
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(30.0)

        // setup location polling
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1f, this)

        // open camera taking photo
        findViewById<Button>(R.id.cmr).setOnClickListener {
            openCamera()
        }

    }

    var image_uri: Uri? = null
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION,"New location")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, 1)
    }





    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            val userLocation = GeoPoint(location.latitude, location.longitude)
            // center map to users location
            map.controller.setCenter(userLocation)

            // create user current location marker for map
            val userMarker = Marker(map)
            userMarker.position = userLocation
            val customIcon = ResourcesCompat.getDrawable(resources,
                R.drawable.location_icon, null)
            if (customIcon != null) {
                userMarker.icon = customIcon
            }

            // Clear old marker before adding new one
            map.overlays.clear()
            map.overlays.add(userMarker)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
    override fun onProviderEnabled(provider: String?) {
    }
    override fun onProviderDisabled(provider: String?) {
    }
}