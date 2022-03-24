package com.chillchillapp.tasks.todolist


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.Chip
import com.google.common.base.CharMatcher
import kotlinx.android.synthetic.main.activity_select_symbol.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class SelectSymbol : BaseActivity() {

    var imageSymbolList: ArrayList<ModelSymbol> = ArrayList()
    var folderPath = ""
    var folderFile = ""
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_select_symbol)

        database()
        setWidget()
        setEvent()
        //initAds(false)

    }

    public override fun onResume() {
        super.onResume()
    }


    private fun database() {

    }
    private fun setWidget() {
        AddChipView()
    }

    inner class ModelSymbol {
        var pathName: String? = null
        var isLock = false
    }

    var SymbolName: Array<String>? = null
    var SymbolPath: Array<String>? = null
    var SymbolFile: Array<String>? = null
    private fun AddChipView() {

        SymbolName = arrayOf(
            getString(R.string.all),
            getString(R.string.finance),
            getString(R.string.car),
            getString(R.string.utilities),
            getString(R.string.family_and_children),
            getString(R.string.shopping),
            getString(R.string.food_and_drink),
            getString(R.string.education),
            getString(R.string.entertainment),
            getString(R.string.health),
            getString(R.string.home),
            getString(R.string.pet),
            getString(R.string.personal_use),
            getString(R.string.donate),
            getString(R.string.travel),
            getString(R.string.accommodation_hotel),
            getString(R.string.garden),
            getString(R.string.sport),
            getString(R.string.friends_and_lovers)
        )
        SymbolPath = arrayOf(
            "",
            "Finance",
            "Car",
            "Public_utility",
            "Family_and_children",
            "Shopping",
            "Food_and_drink",
            "Education",
            "Entertainment",
            "Health",
            "Home",
            "Pet",
            "Personal_Belongings",
            "Donate",
            "Tourism",
            "Accommodation_and_Hotels",
            "Landscaping",
            "Sport",
            "Friends_and_Lovers"
        )
        SymbolFile = arrayOf(
            "",
            "finance",
            "car",
            "Public_utility",
            "family",
            "shopping",
            "food",
            "education",
            "entertainment",
            "health",
            "home",
            "pet",
            "personal",
            "donate",
            "tourism",
            "accommodation",
            "landscaping",
            "sport",
            "friends"
        )

        for (i in SymbolName!!.indices) {
            val chip = Chip(this)
            chip.setChipBackgroundColorResource(R.color.selector_choice_state)
            val colors = ContextCompat.getColorStateList(this, R.color.selector_text_state)
            chip.setTextColor(colors)
            chip.text = SymbolName!![i]
            chip.isCheckedIconVisible = false
            chip.isCloseIconVisible = false
            chip.isClickable = true
            chip.isCheckable = true
            symbolCG.addView(chip, i)

        }

        symbolCG.setOnCheckedChangeListener { chipGroup, chipId ->
            val chip: Chip? = chipGroup.findViewById(chipId)

            if (chip != null) {
                var index = 0

                for ((i, Name) in SymbolName!!.withIndex()) {
                    if (chip.text.toString() == Name) {
                        index = i
                    }
                }
                folderPath = SymbolPath!![index]
                folderFile = SymbolFile!![index]
                AddSymbol()

            } else {
                imageSymbolList.clear()
            }
            ListGV.adapter = EAdapter(applicationContext)
        }

        (symbolCG.getChildAt(0) as Chip).isChecked = true
        ListGV.adapter = EAdapter(applicationContext)

    }

    private fun setEvent() {

/*
        createImageSymCh!!.setOnClickListener(object : View.OnClickListener() {
            fun onClick(view: View?) {
                if (isPurchases()) {
                    val intent = Intent(applicationContext, CropImage::class.java)
                    startActivityForResult(intent, SELECT_SYMBOL)
                } else {
                    val intent = Intent(applicationContext, UpgradePremium::class.java)
                    startActivity(intent)
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.alert_please_upgrade_premium_version),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
*/
    }

    private fun AddSymbol() {

        imageSymbolList.clear()

        if (folderPath.isEmpty()) {

            for (i in SymbolPath!!.indices) {
                if (!SymbolPath!![i].isEmpty()) {
                    val assetManager = assets
                    var files: Array<String?>? = arrayOfNulls(0)
                    //var fileList: ArrayList<String> = ArrayList()
                    val symbol = SymbolPath!![i]
                    try {
                        files = assetManager.list("image_symbol/" + symbol + "/Free")
                    } catch (e: Exception) {

                    }

                    Log.i("Jfkdjkfjdf", symbol + ",")
                    for (FileName in files!!) {
                        val modelSymbol: ModelSymbol = ModelSymbol()
                        modelSymbol.pathName = "file:///android_asset/image_symbol/" + symbol + "/Free/" + FileName
                        modelSymbol.isLock = false
                        imageSymbolList.add(modelSymbol)
                    }
                    try {
                        files = assetManager.list("image_symbol/" + symbol + "/Purchase")
                    } catch (e: Exception) {
                    }

                    Log.i("Jfkdjkfjdf", symbol + ",")
                    for (FileName in files!!) {
                        val modelSymbol: ModelSymbol = ModelSymbol()
                        modelSymbol.pathName = "file:///android_asset/image_symbol/" + symbol + "/Purchase/" + FileName
                        modelSymbol.isLock = true
                        imageSymbolList.add(modelSymbol)
                    }


                    /*int Index = 1;
                while (isAssetExists("image_symbol/" + SymbolPath[i] + "/sym_ic_" + SymbolFile[i] + Index + ".png")) {
                    ImageSymbolList.add("file:///android_asset/image_symbol/" + SymbolPath[i] + "/sym_ic_" + SymbolFile[i] + Index + ".png");
                    Index++;
                }*/
                }
            }

        } else {
            val assetManager = assets
            var files: Array<String?>? = arrayOfNulls(0)
            //var fileList: ArrayList<String> = ArrayList()
            try {
                files = assetManager.list("image_symbol/$folderPath/Free")

            } catch (e: Exception) {
            }


            for (FileName in files!!) {
                val modelSymbol: ModelSymbol = ModelSymbol()
                modelSymbol.pathName = "file:///android_asset/image_symbol/$folderPath/Free/$FileName"
                modelSymbol.isLock = false
                imageSymbolList.add(modelSymbol)
            }
            try {
                //fileList.add(assetManager.list("image_symbol/$FolderPath/Free").toString())
                files = assetManager.list("image_symbol/$folderPath/Purchase")
            } catch (e: Exception) {
            }

            for (FileName in files!!) {
                val modelSymbol: ModelSymbol = ModelSymbol()
                modelSymbol.pathName = "file:///android_asset/image_symbol/$folderPath/Purchase/$FileName"
                modelSymbol.isLock = true
                imageSymbolList.add(modelSymbol)
            }


            /*int i = 1;
            while (isAssetExists("image_symbol/" + FolderPath + "/sym_ic_" + FolderFile + i + ".png")) {
                ImageSymbolList.add("file:///android_asset/image_symbol/" + FolderPath + "/sym_ic_" + FolderFile + i + ".png");
                i++;
            }*/
        }

        Log.i("jjjjjjjjj", "imageSymbolList: " + imageSymbolList.size)



        Collections.sort(imageSymbolList, object : Comparator<ModelSymbol?> {

            override fun compare(modelSymbol: ModelSymbol?, t1: ModelSymbol?): Int {
                val fileName1 = modelSymbol!!.pathName
                val fileName2 = t1!!.pathName
                val fileId1 = CharMatcher.inRange('0', '9').retainFrom(fileName1).toDouble()
                val fileId2 = CharMatcher.inRange('0', '9').retainFrom(fileName2).toDouble()
                return java.lang.Double.compare(fileId1, fileId2)
            }
        })

        imageSymbolList.sortBy { it.isLock }
    }

    private fun isAssetExists(pathInAssetsDir: String): Boolean {
        val assetManager = this.applicationContext.resources.assets
        var inputStream: InputStream? = null
        try {
            inputStream = assetManager.open(pathInAssetsDir)
            if (null != inputStream) {
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    private var dataImage: ByteArray? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (resultCode == SELECT_SYMBOL) {
                dataImage = data.getByteArrayExtra("Symbol")!!
                val intent = Intent()
                intent.putExtra("Symbol", dataImage)
                setResult(SELECT_SYMBOL, intent)
                finish()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id: Int = item.getItemId()
        if (id == R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class EAdapter(context: Context) : BaseAdapter() {
        var mcontext: Context
        var mlayoutInflater: LayoutInflater

        override fun getCount(): Int {
            return imageSymbolList.size
        }

        override fun getItem(i: Int): Any? {
            return null
        }

        override fun getItemId(i: Int): Long {
            return 0
        }

        //getView จะถูกเรียกตามที่ getCount กำหนด
        override fun getView(i: Int, ConvertView: View?, viewGroup: ViewGroup?): View {
            var convertView: View? = ConvertView
            var widget = Widget()
            //check เพื่อไม่ให้เกิดเม็มโมรี่เต็ม โดยการประกาศ layout เพียงครั้งเดียว
            if (convertView == null) {
                //load layout
                convertView = mlayoutInflater.inflate(R.layout.custom_gridview_symbol, null)
                widget.itemRL = convertView.findViewById(R.id.ItemRL)
                widget.iconIV = convertView.findViewById(R.id.IconIV)
                widget.lockRL = convertView.findViewById(R.id.LockRL)
                convertView.setTag(widget) //setTag
            } else {
                widget = convertView.getTag() as Widget//getTag
            }
            Glide.with(this@SelectSymbol)
                .asBitmap()
                .apply(RequestOptions.centerCropTransform()) //                    .load(ImageSymbolList.get(i))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .load(Uri.parse(imageSymbolList[i].pathName))
                .into(widget.iconIV!!)

            if (imageSymbolList[i].isLock) {
                if (isPurchases()) {
                    widget.lockRL!!.visibility = View.GONE
                } else {
                    widget.lockRL!!.visibility = View.VISIBLE
                }
            } else {
                widget.lockRL!!.visibility = View.GONE
            }

            widget.itemRL!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    if (isPurchases()) {
                        selectSymbol(i)
                    } else if (imageSymbolList[i].isLock) {
                        val intent = Intent(mcontext, UpgradePremium::class.java)
                        startActivity(intent)
                        Toast.makeText(mcontext, getString(R.string.alert_please_upgrade_premium_version), Toast.LENGTH_LONG).show()
                    } else {
                        selectSymbol(i)
                    }
                }

            })
            return convertView!!
        }

        private fun selectSymbol(i: Int) {

            Glide.with(this@SelectSymbol)
                .asBitmap()
                .apply(RequestOptions.centerCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .load(imageSymbolList[i].pathName)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, @Nullable transition: Transition<in Bitmap?>?) {

                        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                        circularBitmapDrawable.isCircular = true

                        val stream = ByteArrayOutputStream()
                        resource.compress(Bitmap.CompressFormat.PNG, 50, stream)
                        val DataImage: ByteArray = stream.toByteArray()

                        val data = Intent()
                        data.putExtra("Symbol", DataImage)
                        setResult(SELECT_SYMBOL, data)
                        finish()
                    }

                    override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                })
        }

        init {
            mcontext = context
            mlayoutInflater = LayoutInflater.from(mcontext)
        }
    }

    inner class Widget {
        var itemRL: RelativeLayout? = null
        var iconIV: ImageView? = null
        var lockRL: RelativeLayout? = null
    }

    private fun isPurchases(): Boolean{
        return false
    }
}