package com.chillchillapp.tasks.todolist

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chillchillapp.tasks.todolist.database.FunctionCategory
import com.chillchillapp.tasks.todolist.model.ModelCategory
import kotlinx.android.synthetic.main.activity_initiate_language.*
import kotlinx.android.synthetic.main.activity_language.*
import kotlinx.android.synthetic.main.activity_language.dataRCV
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class InitiateLanguageActivity : BaseActivity() {

    private val languageList = ArrayList<ModelLanguage>()
    private var indexSelect = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initiate_language)
        initBase()


        setAdap()
        event()

    }

    private fun event(){
        selectRL.setOnClickListener {
            insertCatgory()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setAdap(){
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_english.png",
            "English",
            "en",
            0))
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_spain.png",
            "Español",
            "es",
            0))
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_philippines.png",
            "Filipino",
            "fil",
            0))
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_france.png",
            "Français",
            "fr",
            0))
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_thailand.png",
            "ภาษาไทย",
            "th",
            0))

        val language = prefs!!.strCurrentLanguage
        languageList.forEachIndexed{ i, m ->
            if(language == m.locale){
                indexSelect = i
            }
        }
        val adapter = AdapLanguage(languageList)
        val layoutManager = GridLayoutManager(this,  1, GridLayoutManager.VERTICAL, false)
        dataRCV.adapter = adapter
        dataRCV.layoutManager = layoutManager
    }

    private fun insertCatgory(){
        val categoryList = ArrayList<ModelCategory>()
        categoryList.add(ModelCategory(null, getString(R.string.Work), convertAssetToBytesArray("image_symbol/Home/Purchase/sym_ic_home (36).png"), 1, 1L, 0L, KEY_ACTIVE))
        categoryList.add(ModelCategory(null, getString(R.string.Personal_activities), convertAssetToBytesArray("image_symbol/Friends_and_Lovers/Purchase/sym_ic_friends_lover (40).png"),2, 2L, 1L, KEY_ACTIVE))
        categoryList.add(ModelCategory(null, getString(R.string.Education), convertAssetToBytesArray("image_symbol/Education/Purchase/sym_ic_educa18.png"), 3, 3L, 2L, KEY_ACTIVE))
        categoryList.add(ModelCategory(null, getString(R.string.Appointment), convertAssetToBytesArray("image_symbol/Friends_and_Lovers/Purchase/sym_ic_friends_lover (36).png"), 4, 4L, 3L, KEY_ACTIVE))
        categoryList.add(ModelCategory(null, getString(R.string.Shopping), convertAssetToBytesArray("image_symbol/Shopping/Purchase/sym_ic_shopping2.png"), 5, 5L, 4L, KEY_ACTIVE))
        categoryList.add(ModelCategory(null, getString(R.string.Birthday), convertAssetToBytesArray("image_symbol/Friends_and_Lovers/Purchase/sym_ic_friends_lover (27).png"), 6, 6L, 5L, KEY_ACTIVE))

//        categoryList.add(ModelCategory(null, getString(R.string.work), convertAssetToBytesArray("image_symbol/Education/Purchase/sym_ic_educa47.png"), 1, 1L, 0L, KEY_ACTIVE))
//        categoryList.add(ModelCategory(null, getString(R.string.activities_to_do), convertAssetToBytesArray("image_symbol/Education/Purchase/sym_ic_educa25.png"), 2, 2L, 1L, KEY_ACTIVE))
//        categoryList.add(ModelCategory(null, getString(R.string.wish), convertAssetToBytesArray("image_symbol/Category/Revenue/ic_category_bonus.png"), 3, 3L, 2L, KEY_ACTIVE))


        val functionCategory = FunctionCategory(this)
        functionCategory.open()
        for (m in categoryList){
            functionCategory.insert(m)
        }

        val imageDir = File(filesDir, FOLDER_IMAGE)
        val videoDir = File(filesDir, FOLDER_VIDEO)
        val audioDir = File(filesDir, FOLDER_AUDIO)

        if(!imageDir.exists()){
            imageDir.mkdir()
        }
        if(!videoDir.exists()){
            videoDir.mkdir()
        }
        if(!audioDir.exists()){
            audioDir.mkdir()
        }

        prefs!!.intInsertCategoryDefault = 1
    }

    private fun convertAssetToBytesArray(path: String): ByteArray{
        val inputStream: InputStream = assets.open(path)
        val drawable = Drawable.createFromStream(inputStream, null)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)

        return stream.toByteArray()
    }


    inner class AdapLanguage(private var languageList: ArrayList<ModelLanguage>): RecyclerView.Adapter<AdapLanguage.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapLanguage.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_selcet_language, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: AdapLanguage.ViewHolder, position: Int) {

            setIconNational(holder, position)
            setName(holder, position)
            setIconState(holder, position)

            setEvent(holder, position)

        }
        private fun setIconNational(holder: AdapLanguage.ViewHolder, position: Int){
            Glide.with(this@InitiateLanguageActivity).load(languageList[position].nationalFlag).into(holder.iconIV)
        }
        private fun setName(holder: AdapLanguage.ViewHolder, position: Int){
            holder.nameTV.text = languageList[position].name
        }
        private fun setIconState(holder: AdapLanguage.ViewHolder, position: Int){

            if(indexSelect == position){
                holder.stateIV.visibility = View.VISIBLE
            }else{
                holder.stateIV.visibility = View.GONE
            }
        }

        private fun setEvent(holder: AdapLanguage.ViewHolder, position: Int){

            holder.itemLL.setOnClickListener {
                indexSelect = position
                changeLanguage(languageList[position].locale)
                selectTV.text = getString(R.string.Select)
                selectLanguageTV.text = getString(R.string.Select_Language)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int = languageList.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val itemLL = itemView.findViewById<LinearLayout>(R.id.itemLL)
            val nameTV = itemView.findViewById<TextView>(R.id.nameTV)
            val iconIV = itemView.findViewById<ImageView>(R.id.iconIV)
            val stateIV = itemView.findViewById<ImageView>(R.id.stateIV)

        }
    }

    open class ModelLanguage(
        var nationalFlag: String? = "",
        var name: String? = "",
        var locale: String? = "",
        var state: Int? = 0
    )
}