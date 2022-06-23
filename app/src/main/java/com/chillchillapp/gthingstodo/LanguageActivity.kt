package com.chillchillapp.gthingstodo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_language.*
import java.util.*
import kotlin.collections.ArrayList


class LanguageActivity : BaseActivity() {

    private val languageList = ArrayList<ModelLanguage>()

    private var indexSelect = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_language)

        setDatabase()
        setAdap()
        setEvent()

    }
    private fun setDatabase(){
        languageList.clear()
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_english.png", "English", "en", 0))
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_spain.png", "Español", "es", 0))
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_philippines.png", "Filipino", "fil", 0))
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_france.png", "Français", "fr", 0))
        languageList.add(ModelLanguage("file:///android_asset/flag/ic_flag_thailand.png", "ภาษาไทย", "th", 0))


        val language = prefs!!.strCurrentLanguage
        languageList.forEachIndexed{ i, m ->
            if(language == m.locale){
                indexSelect = i
            }
        }
    }
    private fun setAdap(){
        val adapter = AdapLanguage(languageList)
        val layoutManager = GridLayoutManager(this,  1, GridLayoutManager.VERTICAL, false)
        dataRCV.adapter = adapter
        dataRCV.layoutManager = layoutManager
    }

    private fun setEvent(){

        doneTV.setOnClickListener {
            setCurrentLanguage(languageList[indexSelect].locale!!)
        }

        backRL.setOnClickListener {
            finish()
        }
    }

    private fun setCurrentLanguage(land: String){
        prefs!!.strCurrentLanguage = land
        restart()
    }

    private fun restart() {
        val intent = Intent(this@LanguageActivity, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    inner class AdapLanguage(private var languageList: ArrayList<ModelLanguage>): RecyclerView.Adapter<AdapLanguage.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapLanguage.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_language, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: AdapLanguage.ViewHolder, position: Int) {

            setIconNational(holder, position)
            setName(holder, position)
            setIconState(holder, position)

            setEvent(holder, position)

        }
        private fun setIconNational(holder: AdapLanguage.ViewHolder, position: Int){
            Glide.with(this@LanguageActivity).load(languageList[position].nationalFlag).into(holder.iconIV)
        }
        private fun setName(holder: AdapLanguage.ViewHolder, position: Int){
            holder.nameTV.text = languageList[position].name
        }
        private fun setIconState(holder: AdapLanguage.ViewHolder, position: Int){
            holder.stateIV.visibility = View.GONE
            if(indexSelect == position){
                holder.stateIV.visibility = View.VISIBLE
            }
        }

        private fun setEvent(holder: AdapLanguage.ViewHolder, position: Int){

            holder.itemLL.setOnClickListener {
                indexSelect = position
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