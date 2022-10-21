package com.chillchillapp.gthingstodo.dialog

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.location.Location
import android.net.ConnectivityManager
import android.view.KeyEvent
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chillchillapp.gthingstodo.R
import com.chillchillapp.gthingstodo.master.GPSManager
import com.chillchillapp.gthingstodo.master.Prefs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.MapsInitializer


import kotlinx.android.synthetic.main.dialog_set_location.*


class SetLocationDialog(private var activity: Activity, private var latLng: LatLng):Dialog(activity) {

    //-------------------* not use *-------------------------------------------------------------------------------------------------
    private var l:MyEvent? = null
    interface MyEvent{
        fun onMySelect(latLng: LatLng)
    }

    fun setMyEvent(l: MyEvent){
        this.l = l
    }

    private var prefs = Prefs(activity)
    private var gpsManager = GPSManager(activity)

    private lateinit var mMap: GoogleMap
    private var latitude: Double? = null
    private var longitude: Double? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_set_location)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
        setCancelable(false)
        create()

        setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dismiss()
                true
            }
            false
        }

        setOnDismissListener {
            prefs!!.floatLastLat = mMap.cameraPosition.target.latitude.toFloat()
            prefs!!.floatLastLng = mMap.cameraPosition.target.longitude.toFloat()
            gpsManager.close()
        }

        initMap()

        gpsManager.setMyEvent(object : GPSManager.MyEvent{
            override fun onLocationChanged(currentLocation: Location) {
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude

                val myLocation = LatLng(latitude!!, longitude!!)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f))
                gpsManager.close()
            }

            override fun onDissAccessGPS() {
            }

        })

        event()

    }

    private fun initMap(){
        MapsInitializer.initialize(activity)
        mapView.onCreate(onSaveInstanceState())
        mapView.onResume()

        mapView.getMapAsync{ googleMap->
            mMap = googleMap    //13.668217, 100.614021
            mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            mMap!!.uiSettings.isMapToolbarEnabled = false

            latitude = latLng.latitude
            longitude = latLng.longitude

            if(latitude != 0.0 && longitude != 0.0){
                val myLocation = LatLng(latitude!!, longitude!!)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f))
            }
        }
    }

    private fun event(){

        selectRL.setOnClickListener {
            if(isNetworkAvailable()){
                val latLng = mMap.cameraPosition.target
                latitude = latLng.latitude
                longitude = latLng.longitude

                l?.onMySelect(LatLng(latitude!!, longitude!!))
                dismiss()
            }else {
                Toast.makeText(activity, activity.getString(R.string.Please_check_your_network), Toast.LENGTH_SHORT).show()
            }
        }

        currentFab.setOnClickListener {
            gpsManager.requestLocation()
        }

        backRL.setOnClickListener {
            dismiss()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}