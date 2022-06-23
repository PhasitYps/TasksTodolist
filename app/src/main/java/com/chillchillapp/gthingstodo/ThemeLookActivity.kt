package com.chillchillapp.gthingstodo

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.chillchillapp.gthingstodo.model.ModelTheme
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_theme.*
import kotlinx.android.synthetic.main.activity_theme_look.*
import kotlinx.android.synthetic.main.listview_theme_color.view.*
import java.util.*
import kotlin.collections.ArrayList





class ThemeLookActivity : BaseActivity() {

    private val colorThemeList = ArrayList<ModelTheme>()

    private var selectThemeColor: Int? = null
    private var indexSelect: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setContentView(R.layout.activity_theme_look)

        selectThemeColor = intent.getIntExtra("themeColorSelect", 0)

        addColorTheme()
        setEvent()

    }

    private fun restart() {
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        //viewPager2.registerOnPageChangeCallback(pageChangeCallback)
    }

    private fun setAdap(){
        val adapter = AdapThemeColor(this, colorThemeList)
        val layoutManager = GridLayoutManager(this,  1, GridLayoutManager.HORIZONTAL, false)
        colorThemeRCV.adapter = adapter
        colorThemeRCV.layoutManager = layoutManager



    }

    private fun setAdapPager(){
        val adapter = AdapPagerColorThemeLook(this, colorThemeList)
        viewPager2.adapter = adapter

        //viewPager2.setCurrentItem(indexSelect, 1000)

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.i("hhhhhhh", "position: " + position)

                //set Data
                colorThemeRCV.smoothScrollToPosition(position)
                indexSelect = position

                //update UI
                viewHolderColorList.forEach { it.bgIconSelectRL.visibility = View.GONE }
                viewHolderColorList[position].bgIconSelectRL.visibility = View.VISIBLE
                useThemeCC.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this@ThemeLookActivity, colorThemeList[position].colorSecond))
                //end update UI
            }
        })



    }

    fun ViewPager2.setCurrentItem(item: Int, duration: Long, interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
        pagePxWidth: Int = width // Default value taken from getWidth() from ViewPager2 view
    ) {
        val pxToDrag: Int = pagePxWidth * (item - currentItem)
        val animator = ValueAnimator.ofInt(0, pxToDrag)
        var previousValue = 0
        animator.addUpdateListener { valueAnimator ->
            val currentValue = valueAnimator.animatedValue as Int
            val currentPxToDrag = (currentValue - previousValue).toFloat()
            fakeDragBy(-currentPxToDrag)
            previousValue = currentValue
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) { beginFakeDrag() }
            override fun onAnimationEnd(animation: Animator?) { endFakeDrag() }
            override fun onAnimationCancel(animation: Animator?) { /* Ignored */ }
            override fun onAnimationRepeat(animation: Animator?) { /* Ignored */ }
        })
        animator.interpolator = interpolator
        animator.duration = duration
        animator.start()
    }

    private fun setEvent(){

        cancelIV.setOnClickListener {
            finish()
        }

        useThemeCC.setOnClickListener {

            val theme = colorThemeList[indexSelect].style
            prefs!!.intCurrentThemeColor = theme

            /*editor!!.putInt(KEY_CURRENT_THEME_COLOR, theme)
            editor!!.commit()*/
            restart()
        }
    }

    private fun addColorTheme(){

        colorThemeList.add(ModelTheme(R.style.BlueTheme, R.color.colorBlueAlpha, R.color.colorBlueDark))
        colorThemeList.add(ModelTheme(R.style.PinkTheme, R.color.colorPinkAlpha, R.color.colorPinkDark))
        colorThemeList.add(ModelTheme(R.style.GreenTheme, R.color.colorGreenAlpha, R.color.colorGreenDark))
        colorThemeList.add(ModelTheme(R.style.PurpleTheme, R.color.colorPurpleAlpha, R.color.colorPurpleDark))
        colorThemeList.add(ModelTheme(R.style.RedTheme, R.color.colorRedAlpha, R.color.colorRedDark))
        colorThemeList.add(ModelTheme(R.style.YellowTheme, R.color.colorYellowAlpha, R.color.colorYellowDark))
        colorThemeList.add(ModelTheme(R.style.BrownTheme, R.color.colorBrownAlpha, R.color.colorBrownDark))

        //indexSelect = colorThemeList.indexOf(colorThemeList.single { it.colorName == colorName.toString() })
        Log.i("hhhhhhh", "indexSelect: " + indexSelect)

        setAdapPager()
        setAdap()


    }

    inner class AdapPagerColorThemeLook(private val activity: Activity, private val themeList: ArrayList<ModelTheme>) : RecyclerView.Adapter<AdapPagerColorThemeLook.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listview_pager_themelook, parent, false))

        override fun getItemCount(): Int = themeList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            Log.i("hhhhhhhy", "onBindViewHolder")

            setTheme(themeList[position].style)
            setCustomChip(holder.chip1, position)
            setCustomChip(holder.chip2, position)
            setCustomChip(holder.chip3, position)
            setCustomChip(holder.chip4, position)

            holder.chip1.isChecked = true
            holder.menuTaskIV.background.setTint(themeColor(R.attr.colorAccent))
            holder.menuInputRL.background = ContextCompat.getDrawable(activity, themeList[position].colorSecond)

        }

        private fun setCustomChip(chip: Chip, position: Int){
            val states = arrayOf(
                intArrayOf(-android.R.attr.state_checked), //1 Disabled
                intArrayOf(android.R.attr.state_checked)) //2 Enabled

            val colors = intArrayOf(
                ContextCompat.getColor(activity, themeList[position].colorPrimary), //1
                ContextCompat.getColor(activity, themeList[position].colorSecond), //2
            )
            val myList = ColorStateList(states, colors)

            chip.setTextColor(myList)
            chip.chipBackgroundColor = myList
            chip.isCheckable = true
            chip.isClickable = false
            chip.isCheckedIconVisible = false
            chip.isCloseIconVisible = false
        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

            val menuInputRL = itemView.findViewById<RelativeLayout>(R.id.menuInputRL)
            val menuTaskIV = itemView.findViewById<ImageView>(R.id.menuTaskIV)
            val chip1 = itemView.findViewById<Chip>(R.id.chip1)
            val chip2 = itemView.findViewById<Chip>(R.id.chip2)
            val chip3 = itemView.findViewById<Chip>(R.id.chip3)
            val chip4 = itemView.findViewById<Chip>(R.id.chip4)

        }
    }

    private val viewHolderColorList = ArrayList<View>()
    inner class AdapThemeColor(private val activity: Activity, private val dataList: ArrayList<ModelTheme>) : RecyclerView.Adapter<AdapThemeColor.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_theme_color, parent, false)
            viewHolderColorList.add(view)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            //set color
            holder.bgColorView.background = ContextCompat.getDrawable(activity, dataList[position].colorSecond)
            holder.iconSelectIV.setColorFilter(ContextCompat.getColor(activity, dataList[position].colorSecond))

            holder.itemCV.setOnClickListener{
                viewPager2.setCurrentItem(position, 400)

            }


            if(selectThemeColor == dataList[position].style){
                viewPager2.setCurrentItem(position, true)
            }

        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val itemCV = itemView.findViewById<CardView>(R.id.itemCV)
            val bgColorView = itemView.findViewById<View>(R.id.bgColorView)
            val bgIconSelectRL = itemView.findViewById<RelativeLayout>(R.id.bgIconSelectRL)
            val iconSelectIV = itemView.findViewById<ImageView>(R.id.iconSelectIV)

            init {
                bgIconSelectRL.visibility = View.GONE
                bgIconSelectRL.background = ContextCompat.getDrawable(activity, R.drawable.bg_circle_black)
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }
    }
}