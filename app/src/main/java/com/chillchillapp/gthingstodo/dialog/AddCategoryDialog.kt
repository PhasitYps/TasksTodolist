package com.chillchillapp.gthingstodo.dialog

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chillchillapp.gthingstodo.BaseActivity
import com.chillchillapp.gthingstodo.R
import com.chillchillapp.gthingstodo.SELECT_SYMBOL
import com.chillchillapp.gthingstodo.SelectSymbol
import kotlinx.android.synthetic.main.dialog_new_category.*
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AddCategoryDialog(private val activity: BaseActivity): Dialog(activity) {

    interface OnAddCategoryListener{
        fun OnAddCategoryListener(name: String, image: ByteArray)
    }

    var l: OnAddCategoryListener? = null
    fun setOnAddCategoryListener(l: OnAddCategoryListener){
        this.l = l
    }

    private var dataImageInByte: ByteArray? = null
    private var dataNameCategoty = ""

    init {

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_new_category)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)
        show()

        warnTextLengthTV.visibility = View.GONE

        val inputStream: InputStream = activity.assets.open("image_symbol/Education/Free/sym_ic_educa26.png")
        val drawable = Drawable.createFromStream(inputStream, null)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
        dataImageInByte = stream.toByteArray()

        nameEDT.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val num = p0?.length
                textLengthTV.text = "$num/50"

                if(num == 50){
                    warnTextLengthTV.visibility = View.VISIBLE
                }else{
                    warnTextLengthTV.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        Glide.with(activity)
            .asBitmap()
            .apply(RequestOptions.centerCropTransform())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .load(dataImageInByte)
            .into(imageIV)

        setEvent()

    }

    private fun setEvent(){
        chooseImageLL.setOnClickListener {
            val intent = Intent(activity, SelectSymbol::class.java)
            activity.startActivityForResult(intent, SELECT_SYMBOL)
        }

        addTV.setOnClickListener {
            dataNameCategoty = nameEDT.text.toString()

            if(dataNameCategoty !=""){
                l?.OnAddCategoryListener(dataNameCategoty, dataImageInByte!!)
                dismiss()
            }else{
                Toast.makeText(activity, "ป้อนข้อมูลก่อนเพิ่ม", Toast.LENGTH_SHORT ).show()
            }
        }

        negativeTV.setOnClickListener {
            dismiss()
        }
    }

    fun setInitValue(name: String, imageByteArray: ByteArray){
        nameEDT.setText(name)

        dataImageInByte = imageByteArray
        Glide.with(activity)
            .asBitmap()
            .apply(RequestOptions.centerCropTransform())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .load(dataImageInByte)
            .into(imageIV)
    }

    fun setAddTextView(name: String){
        addTV.text = name
    }

    fun setResuil(dataImageBtye: ByteArray){
        dataImageInByte = dataImageBtye
        Glide.with(activity)
            .asBitmap()
            .apply(RequestOptions.centerCropTransform())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .load(dataImageInByte)
            .into(imageIV)
    }


}