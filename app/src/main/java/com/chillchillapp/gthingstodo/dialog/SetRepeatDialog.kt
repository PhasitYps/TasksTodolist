package com.chillchillapp.gthingstodo.dialog

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.get
import com.chillchillapp.gthingstodo.R
import com.chillchillapp.gthingstodo.model.ModelRepeat
import com.chillchillapp.gthingstodo.model.ModelTask
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.dialog_set_repeat.*
import java.util.*
import kotlin.collections.ArrayList

class SetRepeatDialog(private var activity: Context): Dialog(activity) {

    interface OnChangeDataListener{
        fun OnChangeDataListener(m: ModelRepeat)
    }

    private var l: OnChangeDataListener? = null
    fun setChangeDataListener(l: OnChangeDataListener){
        this.l = l
    }

    private var repeatType: String? = null //type
    private var repeatNextTime: Int? = null //num next
    private var repeatNum: Long? = null //num repeat

    private var calDueDate = Calendar.getInstance()
    private var hour = -1
    private var minute = -1

    private val TYPE_HOUR = "hour"
    private val TYPE_DAY = "day"
    private val TYPE_WEEK = "week"
    private val TYPE_MONTH = "month"
    private val TYPE_YEAR = "year"

    private var indexSelect: Int = 1

    private val typeList = ArrayList<ModelSetting>()
    private val nextList = ArrayList<ModelSetting>()
    private val endList = ArrayList<ModelSetting>()

    fun setInit(m0: ModelRepeat, m1: ModelTask){
        repeatType = m0.repeatType
        repeatNextTime = m0.repeatNext
        repeatNum = m0.numberOfRepeat

        hour = m1.hour!!
        minute = m1.minute!!
        calDueDate.timeInMillis = m1.dueDate!!

        addEndList()
        addChipType()
        setEvent()

        if(repeatNum == null){
            repeatNum = endList[0].value!!.toLong()
            valueEndDateTV.text = endList.single { it.value == repeatNum!!.toInt()}.text
        }

    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_set_repeat)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)
        show()

        bgSettingRepeatRL.visibility = View.GONE
        openRepeatSw.isChecked = false
    }

    private fun addChipType(){
        typeRepeatCG.removeAllViews()
        typeList.clear()

        typeList.add(ModelSetting( activity.getString(R.string.hour), null, TYPE_HOUR))
        typeList.add(ModelSetting( activity.getString(R.string.daily), null, TYPE_DAY))
        typeList.add(ModelSetting( activity.getString(R.string.weekly),null, TYPE_WEEK))
        typeList.add(ModelSetting( activity.getString(R.string.monthly),null, TYPE_MONTH))
        typeList.add(ModelSetting( activity.getString(R.string.yearly),null, TYPE_YEAR))

        for (i in typeList.indices) {
            val chip = Chip(activity)
            chip.setChipBackgroundColorResource(R.color.selector_choice_state)
            val colors = ContextCompat.getColorStateList(activity, R.color.selector_text_state)
            chip.setTextColor(colors)
            chip.text = typeList[i].text
            chip.isCheckedIconVisible = false
            chip.isCloseIconVisible = false
            chip.isClickable = true
            chip.isCheckable = true
            chip.tag = i

            if(repeatType == typeList[i].repeatType){
                indexSelect = i

                chip.isChecked = true
                openRepeatSw.isChecked = true
                bgSettingRepeatRL.visibility = View.VISIBLE

                when(repeatType){

                    TYPE_HOUR-> addDataHour()

                    TYPE_DAY-> addDataDay()

                    TYPE_WEEK-> addDataWeek()

                    TYPE_MONTH-> addDataMonth()

                    TYPE_YEAR-> addDataYear()
                }

                valueNextTV.text = nextList.single { it.value == repeatNextTime!! }.text
                valueEndDateTV.text = endList.single { it.value == repeatNum!!.toInt()}.text
            }
            typeRepeatCG.addView(chip)
        }
    }

    private fun setEvent(){

        openRepeatSw.setOnCheckedChangeListener { _, check ->
            when(check){
                true->{
                    bgSettingRepeatRL.visibility = View.VISIBLE

                    (typeRepeatCG[indexSelect] as Chip).apply {
                        if (!isChecked){
                            isChecked = true
                            val i = tag.toString().toInt()
                            repeatType = typeList[i].repeatType

                            changeEventType()
                        }
                    }
                }
                false->{
                    bgSettingRepeatRL.visibility = View.GONE
                    typeRepeatCG.clearCheck()
                }
            }
        }

        typeRepeatCG.forEach {
            val chip: Chip = it as Chip
            val i = chip.tag.toString().toInt()

            chip.setOnClickListener {
                chip.isChecked = true
                repeatType = typeList[i].repeatType
                indexSelect = i

                changeEventType()

                if(!openRepeatSw.isChecked){
                    openRepeatSw.isChecked = true
                }
            }
        }

        (typeRepeatCG.getChildAt(0) as Chip).setOnClickListener {
            val i = it.tag.toString().toInt()

            if(!isSetTime()){

                if(openRepeatSw.isChecked){
                    (typeRepeatCG.getChildAt(indexSelect) as Chip).isChecked = true
                }else{
                    (it as Chip).isChecked = false
                }
                Toast.makeText(activity, activity.getString(R.string.please_set_the_task_time_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            (it as Chip).isChecked = true
            repeatType = typeList[i].repeatType
            indexSelect = i

            changeEventType()

            if(!openRepeatSw.isChecked){
                openRepeatSw.isChecked = true
            }
        }

        //every
        selectNextLL.setOnClickListener {
            showMenuNextRepeatPopup(it)
        }

        //end
        selectEndDateLL.setOnClickListener {
            showMenuEndDatePopup(it)
        }

        positiveTV.setOnClickListener {

            when(isSetRepeat()){
                true->{
                    val modelRepeat = ModelRepeat()
                    modelRepeat.repeatType = repeatType
                    modelRepeat.repeatNext = repeatNextTime
                    modelRepeat.numberOfRepeat = repeatNum

                    l?.OnChangeDataListener(modelRepeat)
                }
                false->{
                    val modelRepeat = ModelRepeat()
                    modelRepeat.repeatType = null
                    modelRepeat.repeatNext = null
                    modelRepeat.numberOfRepeat = null

                    l?.OnChangeDataListener(modelRepeat)
                }
            }
            dismiss()
        }

        negativeTV.setOnClickListener {
            dismiss()
        }

        setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK){
                dismiss()
                true
            }
            false
        }
    }

    private fun changeEventType(){

        if(repeatType == null){
            return
        }

        when(repeatType){

            TYPE_HOUR-> addDataHour()

            TYPE_DAY-> addDataDay()

            TYPE_WEEK-> addDataWeek()

            TYPE_MONTH-> addDataMonth()

            TYPE_YEAR-> addDataYear()
        }

        repeatNextTime = nextList[0].value
        valueNextTV.text = nextList[0].text

        Log.i("fhhhhh", "repeatNextTime: " + repeatNextTime)
        Log.i("fhhhhh", "endDate: " + repeatNum)
    }

    private fun addEndList(){
        endList.clear()
        endList.add(ModelSetting(activity.getString(R.string.endlessly), -1))

        for(i in 1..10){
            endList.add(ModelSetting("$i ${activity.getString(R.string.time)}", i))
        }
    }

    private fun addDataHour(){
        nextList.clear()
        for(i in 1..24){
            nextList.add(ModelSetting("$i ${activity.getString(R.string.hour)}", i))
        }
    }

    private fun addDataDay(){

        nextList.clear()
        for(i in 1..30){
            nextList.add(ModelSetting("$i ${activity.getString(R.string.day)}", i))
        }

    }

    private fun addDataWeek(){
        nextList.clear()
        for(i in 1..4){
            nextList.add(ModelSetting("$i ${activity.getString(R.string.week)}", i))
        }

    }

    private fun addDataMonth(){
        nextList.clear()
        for(i in 1..4){
            nextList.add(ModelSetting("$i ${activity.getString(R.string.month)}", i))
        }
    }

    private fun addDataYear(){
        nextList.clear()
        for(i in 1..4){
            nextList.add(ModelSetting("$i ${activity.getString(R.string.year)}", i))
        }

    }

    private fun showMenuNextRepeatPopup(v: View){

        val popupMenu = PopupMenu(activity!!, v)
        //popupMenu.inflate(R.menu.popup_menu_category)
        //set Data
        nextList.forEachIndexed { i, m ->
            val view = View(activity)
            view.tag = i

            popupMenu.menu.add(m.text)
            popupMenu.menu[i].actionView = view
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            val i = it.actionView!!.tag.toString().toInt()
            repeatNextTime = nextList[i].value
            Log.i("fhhhhh", "repeatNextTime: " + repeatNextTime)

            valueNextTV.text = it.title
            popupMenu.dismiss()
            true
        }
    }

    private fun showMenuEndDatePopup(v: View){

        val popupMenu = PopupMenu(activity!!, v)
        //popupMenu.inflate(R.menu.popup_menu_category)
        endList.forEachIndexed { i, m ->
            val view = View(activity)
            view.tag = i

            popupMenu.menu.add(m.text)
            popupMenu.menu[i].actionView = view
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            val i = it.actionView!!.tag.toString().toInt()
            repeatNum = endList[i].value!!.toLong()
            Log.i("fhhhhh", "endDate: " + repeatNum)

            valueEndDateTV.text = it.title
            popupMenu.dismiss()
            true
        }
    }



    private fun isSetDueDate(): Boolean{
        return calDueDate.timeInMillis != 0L
    }

    private fun isSetTime(): Boolean{
        return hour != -1 && minute != -1
    }

    private fun isSetRepeat(): Boolean{
        return openRepeatSw.isChecked && repeatType != null
    }

    inner class ModelSetting(
        var text: String = "",
        var value: Int? = 0,
        var repeatType: String? = ""
    )
}