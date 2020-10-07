package com.example.team13

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.getbase.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.app.*
import kotlinx.android.synthetic.main.image_layout.*
import kotlinx.android.synthetic.main.image_layout.view.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

// class that contains image and location information
class PhotoLocation(
    val latitude: Double,
    val longitude: Double,
    val image: Uri) {
}

val GALLERY_REQUEST = 1
val REQUEST_IMAGE_CAPTURE = 3

class App : AppCompatActivity(), LocationListener{

    var lm: LocationManager? = null
    var photoLocations = mutableListOf<PhotoLocation>()

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
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.controller.setZoom(9.5)


        // setup location polling
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 170f, this)

        // open camera taking photo image_layout

        findViewById<FloatingActionButton>(R.id.cmr).setOnClickListener {
            openCamera()
            Toast.makeText(this, "Go to Camera", Toast.LENGTH_SHORT).show()

        }
        findViewById<FloatingActionButton>(R.id.gallery).setOnClickListener {
            openGallery()
        }

    }

    private fun showImage(uri: Uri) {
        val settingsDialog = Dialog(this)
        settingsDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val modalView = layoutInflater.inflate(
            R.layout.image_layout
            , null
        )
        settingsDialog.setContentView(modalView)
        settingsDialog.setCanceledOnTouchOutside(true);
        modalView.image.setImageURI(uri)
        settingsDialog.show()
    }

    //open image_layout
    private fun openGallery() {
        val gintent = Intent(Intent.ACTION_PICK)
        gintent.type = "image/*"
        startActivityForResult(gintent, GALLERY_REQUEST)
    }
    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST){
            if (data?.data != null) {
                showImage(data.data!!)
            }
        }
        Log.d("asd", "ACTIVITYRESULT code ${requestCode} result ${resultCode} is ok ${resultCode == Activity.RESULT_OK}")
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.d("asd", "RECEIVING IMAGE ${data.toString()}")
            val latitude = lm!!.getLastKnownLocation(LocationManager.GPS_PROVIDER).latitude
            val longitude = lm!!.getLastKnownLocation(LocationManager.GPS_PROVIDER).longitude
            if (image_uri != null) {
                Log.d("asd", "RECDEIVED IMAGE ${image_uri.toString()}")
                val marker = PhotoLocation(latitude, longitude, image_uri!!)
                photoLocations.add(marker)
                addPhotoMarker(marker)
            }
        }
    }

    //open camera
    var image_uri: Uri? = null
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun addPhotoMarker(it: PhotoLocation) {
        val location = GeoPoint(it.latitude, it.longitude)
        var marker = Marker(map)
        val icon = ResourcesCompat.getDrawable(resources,R.drawable.marker_default, null)
        if (icon != null) {
            marker.icon = icon
        }
        marker.setOnMarkerClickListener(Marker.OnMarkerClickListener { marker, map ->
            showImage(it.image)
            true
        })
        marker.position = location
        map.overlays.add(marker)
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            val userLocation = GeoPoint(location.latitude, location.longitude)
            // center map to users location
            map.controller.setCenter(userLocation)

            // create user current location marker for map
            val userMarker = Marker(map)
            userMarker.position = userLocation
            val customIcon = ResourcesCompat.getDrawable(
                resources,
                R.drawable.location_icon, null
            )
            if (customIcon != null ) {
                userMarker.icon = customIcon
            }
            // Clear old marker before adding new one
            map.overlays.clear()
            map.overlays.add(userMarker)
            photoLocations.forEach {
                addPhotoMarker(it)
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
    override fun onProviderEnabled(provider: String?) {
    }
    override fun onProviderDisabled(provider: String?) {
    }
}