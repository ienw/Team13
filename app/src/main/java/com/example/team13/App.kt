package com.example.team13

import android.annotation.SuppressLint
import android.app.Activity
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
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.getbase.floatingactionbutton.FloatingActionButton
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
        map.controller.setZoom(100.0)

        // setup location polling
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 170f, this)

        // open camera taking photo gallery

        findViewById<FloatingActionButton>(R.id.cmr).setOnClickListener {
            openCamera()
            Toast.makeText(this, "Go to Camera", Toast.LENGTH_SHORT).show()

        }
        findViewById<FloatingActionButton>(R.id.gallery).setOnClickListener {
            openGallery()
        }

    }


    //open gallery
    val GALLERY_REQUEST = 1
    private fun openGallery() {
        val gintent = Intent(Intent.ACTION_PICK)
        gintent.type = "image/*"
        startActivityForResult(gintent, GALLERY_REQUEST)
    }
    var REQUEST_CODE = 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            findViewById<ImageView>(R.id.ImageView).setImageURI(data?.data)
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
        startActivityForResult(cameraIntent, 1)

    }



    override fun onLocationChanged(location: Location?) {
        val cmr = findViewById<FloatingActionButton>(R.id.cmr)
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
            val mIcon = ResourcesCompat.getDrawable(resources,R.drawable.marker_default, null)

            if (customIcon != null ) {

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