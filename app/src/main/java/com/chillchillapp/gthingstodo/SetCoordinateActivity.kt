package com.chillchillapp.gthingstodo

import android.content.Intent
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import com.chillchillapp.gthingstodo.master.GPSManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_set_coordinate.*


class SetCoordinateActivity : BaseActivity() {

    private lateinit var gpsManager: GPSManager

    private lateinit var mMap: GoogleMap
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_set_coordinate)

        init()
        initMap()
        event()
    }

    private fun init(){

        val lat = intent.getDoubleExtra(KEY_LAT, 0.0)
        val lng = intent.getDoubleExtra(KEY_LNG, 0.0)

        latitude = if(lat != 0.0) lat else null
        longitude = if (lng != 0.0) lng else null

        gpsManager = GPSManager(this)
        gpsManager.setMyEvent(object : GPSManager.MyEvent{
            override fun onLocationChanged(currentLocation: Location) {
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude

                val myLocation = LatLng(latitude!!, longitude!!)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17f))
                gpsManager.close()
            }

            override fun onDissAccessGPS() {}
        })
    }

    private fun initMap(){

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync {
            mMap = it    //13.668217, 100.614021
            mMap!!.uiSettings.isMapToolbarEnabled = false
            mMap!!.uiSettings.isCompassEnabled = false
            mMap!!.uiSettings.isZoomControlsEnabled = false

            mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID

            if(latitude != null && longitude != null){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!, longitude!!), 17f))
            }
        }

    }

    private fun event(){

        selectRL.setOnClickListener {
            if(isNetworkAvailable()){
                val latLng = mMap.cameraPosition.target
                latitude = latLng.latitude
                longitude = latLng.longitude

                val intent = Intent()
                intent.putExtra(KEY_LAT, latitude)
                intent.putExtra(KEY_LNG, longitude)
                setResult(RESULT_OK, intent)
                finish()
            }else {
                Toast.makeText(this, getString(R.string.Please_check_your_network), Toast.LENGTH_SHORT).show()
            }
        }

        currentFab.setOnClickListener {
            gpsManager.requestLocation()
        }

        backRL.setOnClickListener {
            finish()
        }

        layerFab.setOnClickListener {
            //showLayersMapDialog()
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}