package com.chillchillapp.tasks.todolist.dialog

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.master.Prefs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.MapsInitializer


import com.google.android.gms.maps.MapView
import kotlinx.android.synthetic.main.dialog_set_coordinate.*


class SetLocationDialog(private var activity: Activity, private var latLng: LatLng):Dialog(activity) {

    private var l:MyEvent? = null
    interface MyEvent{
        fun onMySelect(latLng: LatLng)
    }

    fun setMyEvent(l: MyEvent){
        this.l = l
    }

    private var prefs = Prefs(activity)
    private lateinit var mMap: GoogleMap
    private var latitude: Double? = null
    private var longitude: Double? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_set_coordinate)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
        setCancelable(false)
        create()

        setOnDismissListener {
            prefs!!.floatLastLat = mMap.cameraPosition.target.latitude.toFloat()
            prefs!!.floatLastLng = mMap.cameraPosition.target.longitude.toFloat()
        }

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
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 18f))
            }
        }

        event()

    }

    private fun event(){

        selectRL.setOnClickListener {
            val latLng = mMap.cameraPosition.target
            latitude = latLng.latitude
            longitude = latLng.longitude

            l?.onMySelect(LatLng(latitude!!, longitude!!))
            dismiss()
        }

        backIV.setOnClickListener {
            dismiss()
        }
    }
}