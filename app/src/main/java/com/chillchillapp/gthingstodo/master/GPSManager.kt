package com.chillchillapp.gthingstodo.master

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chillchillapp.gthingstodo.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse


class GPSManager(private var activity: Activity) {

    interface MyEvent{
        fun onLocationChanged(currentLocation: Location)
        fun onDissAccessGPS()

    }
    private var l: MyEvent? = null
    fun setMyEvent(l: MyEvent){
        this.l = l
    }
    private var lat: Double? = null
    private var lng: Double? = null

    private var isGPS: Boolean = false
    private var locationManager: LocationManager? = null
    private val TAG = "GPSManagerTag"
    private var isNetwork: Boolean = false

    companion object{
        val PERMISSION_REQUEST_GPS = 1001
    }


    init {
        locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "Latitude:" + location.latitude + ", Longitude:" + location.longitude)
            l?.onLocationChanged(location)
        }
        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
            Log.d(TAG,"disable")
        }
        override fun onProviderEnabled(provider: String) {
            super.onProviderEnabled(provider)
            Log.d(TAG,"enabled")
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            super.onStatusChanged(provider, status, extras)
            Log.d(TAG,"status")

        }
    }
    fun requestLocation(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                //ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_GPS)


            } else {

                try {
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 3f, locationListener)
                    locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 3f, locationListener)

                }catch (e: Exception){
                    Log.i(TAG, "requestLocationUpdates e: " + e.message)
                }

                if (isNetwork) {

                    locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 3f, locationListener)

                    val loc = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (loc != null) {
                        lat = loc.latitude
                        lng = loc.longitude
                    }
                }
                if (isGPS) {
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 3f, locationListener)
                    val loc = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                    if (loc != null) {
                        lat = loc.latitude
                        lng = loc.longitude
                    }
                }else{

                    showEnableLocationSetting()
                    Toast.makeText(activity, activity.getString(R.string.Please_open_location), Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "checkLocationSettingsAndShowPopup")
                }
            }
        }

        if (lat != null && lng != null) {
            val currentLocation = Location("MyLocation")
            currentLocation.latitude = lat!!
            currentLocation.longitude = lng!!
            l?.onLocationChanged(currentLocation)
        }
    }

    private val requestPermissionLauncher =  (activity as AppCompatActivity).registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.i(TAG, "This is requestPermissionLauncher in GPSManager: $isGranted")
        if (isGranted) {
            // PERMISSION GRANTED
            requestLocation()

        } else {
            // PERMISSION NOT GRANTED
        }
    }

    private val REQUEST_CHECK_SETTINGS = 1005
    private fun checkLocationSettingsAndShowPopup(
        activity: Activity,
        onSuccess: ((locationSettingsResponse: LocationSettingsResponse?) -> Unit)? = null
    ) {

        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest.interval = 10000;
        locationRequest.fastestInterval = 10000/2

        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder();

        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(activity);
        val task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
        task.addOnSuccessListener {
            Log.i(TAG, "it: $it")
        }
        task.addOnFailureListener {
            Log.i(TAG, "e: " + it.message)
        }
    }

    private fun showEnableLocationSetting() {
        activity?.let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(it)
                .checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                Log.i(TAG, "addOnSuccessListener: $response")
                val states = response.locationSettingsStates
                if (states!!.isLocationPresent) {
                    //Do something
                    Log.i(TAG, "states: $states")
                }
            }
            task.addOnFailureListener { e ->
                Log.i(TAG, "addOnFailureListener: $e")
                if (e is ResolvableApiException) {
                    try {
                        // Handle result in onActivityResult()
                        e.startResolutionForResult(it, 999)
                    } catch (sendEx: IntentSender.SendIntentException) { }
                }
            }
        }
    }

    fun close(){
        locationManager?.removeUpdates(locationListener)
    }
}

//private var isNetwork: Boolean = false
//isNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)