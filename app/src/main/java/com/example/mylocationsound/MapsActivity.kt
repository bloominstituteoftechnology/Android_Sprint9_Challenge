package com.example.mylocationsound

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.NETWORK_PROVIDER
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    lateinit var audioExoPlayer: SimpleExoPlayer

    val URL = "https://ia601504.us.archive.org/25/items/Surco2019-10-05.oktava.flac16/surco2019-10-05d1t01.mp3"

    private  lateinit var buttonFindLocation: Button
    private lateinit var  mapFragment: SupportMapFragment
    private lateinit var gpsTracker: GpsTracker
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private lateinit var location: Location
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 1
    private val MIN_TIME_BET_UPDATES = (1000 * 60 * 1).toLong()
    private lateinit var locationManager: LocationManager
    lateinit var handler: Handler
    val ACCESS_FINE_LOCATION = 1
    val ACCESS_COARSE_LOCATION= 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        createAideoPlayer()

        setupVideoPlayerWithURL()

       // playerView.player = videoExoPlayer
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        buttonFindLocation= findViewById<Button>(R.id.btn_find_location)
        handler= Handler()
        buttonFindLocation.setOnClickListener {
            loadMapData()
            audioExoPlayer.playWhenReady = true
        }


        /*  // Obtain the SupportMapFragment and get notified when the map is ready to be used.
          val mapFragment = supportFragmentManager
              .findFragmentById(R.id.map) as SupportMapFragment
          mapFragment.getMapAsync(this)*/
    }
    fun setupVideoPlayerWithURL() {
        audioExoPlayer.prepare(createUrlMediaSource(URL))
    }
    fun createUrlMediaSource(url: String): MediaSource {
        val userAgent = Util.getUserAgent(this, getString(R.string.app_name))
        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(this, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(Uri.parse(url))
    }
    fun createAideoPlayer() {
        // Need a track selector
        val trackSelector = DefaultTrackSelector()
        // Need a load control
        val loadControl = DefaultLoadControl()
        // Need a renderers factory
        val renderFact = DefaultRenderersFactory(this)
        // Set up the ExoPlayer
        audioExoPlayer = ExoPlayerFactory.newSimpleInstance(this, renderFact, trackSelector, loadControl)

        // Set up the scaling mode to crop and fit the video to the screen
        audioExoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

    }
    override fun onStop() {
        super.onStop()
        audioExoPlayer.stop()
    }
    private fun loadMapData() {
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        gpsTracker = GpsTracker(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_FINE_LOCATION
            )
        } else {
            gpsTracker(googleMap)
            //googleMap= googleMap1
        }
    }

    private fun gpsTracker(googleMap1: GoogleMap){
        if (gpsTracker.canGetLoaction()) {
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            location = locationManager.getLastKnownLocation(NETWORK_PROVIDER)
            gpsTracker.onLocationChanged(location)

            latitude = gpsTracker.getLatitude()
            longitude = gpsTracker.getLongitude()

            mMap = googleMap1

            // Add a marker in Sydney and move the camera
            Handler().postDelayed({
                val pune = LatLng(latitude, longitude)
                mMap.addMarker(MarkerOptions().position(pune).title("Marker in Pune"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pune))
                mMap.animateCamera(CameraUpdateFactory.zoomIn())
                mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f))
                //mMap.setMaxZoomPreference(14.0f);
                mMap.maxZoomLevel
            }, 1500)

            val builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setTitle("Location")
            builder.setMessage("This is your current location: Latitude: $latitude Longitude: $longitude")
            builder.setPositiveButton("OK") { dialogInterface, i -> dialogInterface.dismiss() }
            builder.show()
        } else {
            gpsTracker.openSettings()//Open the settings alert to enable the GPS sevice

        }

    }
    override fun onRequestPermissionsResult(permsRequestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (permsRequestCode) {
            0 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), ACCESS_COARSE_LOCATION)        //gpsTracker()
                }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //Show an explanation to the user *asynchronously*
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)        //gpsTracker()
                    }else{
                        val builder = AlertDialog.Builder(this)
                        builder.setCancelable(true)
                        builder.setTitle("Alert")
                        builder.setMessage("Location permission is required")
                        builder.setPositiveButton("OK") { dialogInterface, i ->
                            dialogInterface.dismiss() }
                        builder.show()
                    }
                }
            }
            1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadMapData()
                }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        //Show an explanation to the user *asynchronously*
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), ACCESS_COARSE_LOCATION)        //gpsTracker()
                    }else{
                        val builder = AlertDialog.Builder(this)
                        builder.setCancelable(true)
                        builder.setTitle("Alert")
                        builder.setMessage("Location permission is required")
                        builder.setPositiveButton("OK") { dialogInterface, i ->
                            dialogInterface.dismiss()
                        }
                        builder.show()
                    }
                }
            }
        }
    }
}

