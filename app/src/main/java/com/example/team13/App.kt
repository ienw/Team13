package com.example.team13

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat

import kotlinx.android.synthetic.main.app.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

private lateinit var photoFile: File
class App : AppCompatActivity(), LocationListener {

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
        findViewById<Button>(R.id.cmr).setOnClickListener { view ->
            TakePicture()
        }



    }


    lateinit var currentPhotoPath: String

    @Throws(IOException::class)

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    private fun TakePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, 1)
        val photoFile: File? = try {
            createImageFile()
        }catch (ex: IOException) {
            // Error occurred while creating the File

            null
        }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.team13",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 1)
                }
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