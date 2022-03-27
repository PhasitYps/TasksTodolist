package com.chillchillapp.tasks.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chillchillapp.tasks.todolist.model.ModelTheme
import kotlinx.android.synthetic.main.activity_theme.*
import kotlin.collections.ArrayList

class ThemeActivity : BaseActivity() {

    private val colorThemeList = ArrayList<ModelTheme>()
    private var currentThemeColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_theme)

        currentThemeColor = prefs!!.intCurrentThemeColor

        addThemeColor()
        setAdapColorTheme()
        setEvent()

    }

    private fun init(){
    }

    private fun setEvent(){

        backIV.setOnClickListener {
            finish()
        }
    }

    private fun addThemeColor(){
        colorThemeList.add(ModelTheme(R.style.BlueTheme, R.color.colorBlueAlpha, R.color.colorBlue))
        colorThemeList.add(ModelTheme(R.style.PinkTheme, R.color.colorPinkAlpha, R.color.colorPink))
        colorThemeList.add(ModelTheme(R.style.GreenTheme, R.color.colorGreenAlpha, R.color.colorGreen))
        colorThemeList.add(ModelTheme(R.style.PurpleTheme, R.color.colorPurpleAlpha, R.color.colorPurple))
        colorThemeList.add(ModelTheme(R.style.RedTheme, R.color.colorRedAlpha, R.color.colorRed))
        colorThemeList.add(ModelTheme(R.style.YellowTheme, R.color.colorYellowAlpha, R.color.colorYellow))
        colorThemeList.add(ModelTheme(R.style.BrownTheme, R.color.colorBrownAlpha, R.color.colorBrown))
    }

    private fun setAdapColorTheme(){
        val adapter = AdapThemeColor(this, colorThemeList)
        val layoutManager = GridLayoutManager(this,  4, GridLayoutManager.VERTICAL, false)
        themeRCV.adapter = adapter
        themeRCV.layoutManager = layoutManager

    }

    inner class AdapThemeColor(private val activity: Activity, private val dataList: ArrayList<ModelTheme>) : RecyclerView.Adapter<AdapThemeColor.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_theme_color, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            //set color
            holder.bgColorView.background = ContextCompat.getDrawable(activity, dataList[position].colorSecond)
            holder.iconSelectIV.setColorFilter(ContextCompat.getColor(activity, dataList[position].colorSecond))

            holder.itemCV.setOnClickListener{
                val intent = Intent(activity , ThemeLookActivity::class.java)
                intent.putExtra("themeColorSelect", dataList[position].style)
                activity.startActivity(intent)

            }


            if(dataList[position].style == currentThemeColor){
                holder.bgIconSelectRL.visibility = View.VISIBLE
            }

        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val itemCV = itemView.findViewById<CardView>(R.id.itemCV)
            val bgColorView = itemView.findViewById<View>(R.id.bgColorView)
            val bgIconSelectRL = itemView.findViewById<RelativeLayout>(R.id.bgIconSelectRL)
            val iconSelectIV = itemView.findViewById<ImageView>(R.id.iconSelectIV)

            init {
                bgIconSelectRL.visibility = View.GONE
                bgIconSelectRL.background = ContextCompat.getDrawable(activity, R.drawable.bg_circle_white)
            }

        }

        override fun getItemCount(): Int {
            return dataList.size
        }

    }


}