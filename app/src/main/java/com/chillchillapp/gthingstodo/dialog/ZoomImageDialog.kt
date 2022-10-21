package com.chillchillapp.gthingstodo.dialog

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.view.Window
import com.chillchillapp.gthingstodo.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_zoom_image.*

class ZoomImageDialog(private var activity: Activity, private var imageUri: Uri): Dialog(activity) {

    private val TAG = "ZoomImageDialogTag"

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_zoom_image)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
        setCancelable(true)
        show()

        Picasso.get().load(imageUri).into(zoomImageZV)

        backIV.setOnClickListener {
            dismiss()
        }

    }

}