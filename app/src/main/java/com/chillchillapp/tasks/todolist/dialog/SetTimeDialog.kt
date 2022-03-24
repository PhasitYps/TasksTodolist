package com.chillchillapp.tasks.todolist.dialog

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.view.Window
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import com.chillchillapp.tasks.todolist.R
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.dialog_set_time.*
import java.util.*
import kotlin.collections.ArrayList

class SetTimeDialog(private var activity: Activity): Dialog(activity) {

    interface OnPositiveListener{
        fun OnPositiveListener(hour: Int, minute: Int)
    }

    private var l: OnPositiveListener? = null
    fun setOnPositiveListener(l: OnPositiveListener){
        this.l = l
    }

    private val timeList = ArrayList<ModelTime>()
    private val commandChipList = ArrayList<Chip>()


    //data
    private val cal = Calendar.getInstance()
    private var currentHour: Int = cal.get(Calendar.HOUR_OF_DAY)
    private var currentMinute: Int = cal.get(Calendar.MINUTE)

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_set_time)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)
        show()

        timeList.add(ModelTime(activity.getString(R.string.no_time), 0, 0))
        timeList.add(ModelTime("07:00", 7, 0))
        timeList.add(ModelTime("09:00", 9, 0))
        timeList.add(ModelTime("12:00", 12, 0))
        timeList.add(ModelTime("14:00", 14, 0))
        timeList.add(ModelTime("16:00", 16, 0))
        timeList.add(ModelTime("18:00", 18, 0))

        clockTP.setIs24HourView(true)

        clockTP.setTime(currentHour, currentMinute)

        //setAdapChip
        commandChipList.clear()
        for (i in timeList!!.indices) {
            val chip = Chip(activity)
            chip.setChipBackgroundColorResource(R.color.selector_choice_state)
            val colors = ContextCompat.getColorStateList(activity, R.color.selector_text_state)
            chip.setTextColor(colors)
            chip.text = timeList[i].name
            chip.tag = i
            chip.isCheckedIconVisible = false
            chip.isCloseIconVisible = false
            chip.isClickable = true
            chip.isCheckable = true
            selectTimeCG.addView(chip, i)

            chip.setOnClickListener {
                if (i == 0){
                    currentHour = -1
                    currentMinute = -1
                }else{
                    currentHour = timeList[i].hour!!
                    currentMinute = timeList[i].minute!!
                    clockTP.setTime( timeList[i].hour!!, timeList[i].minute!!)
                }

            }

            //updateCheckChip
            if(i != 0 && currentHour == timeList[i].hour && currentMinute == timeList[i].minute){
                chip.isChecked = true
            }

            commandChipList.add(chip)
        }

        setEvent()

    }

    fun setInitValue(hour: Int, minute: Int){
        currentHour = hour
        currentMinute = minute

        if(!isSetTime()){
            currentHour = cal.get(Calendar.HOUR_OF_DAY)
            currentMinute = cal.get(Calendar.MINUTE)
        }

        clockTP.setTime(currentHour, currentMinute)
        updateCheckChip()
    }

    private fun updateCheckChip(){

        selectTimeCG.clearCheck()
        for(chip in commandChipList){
            val index = chip.tag.toString().toInt()

            if(index != 0 && currentHour == timeList[index].hour && currentMinute == timeList[index].minute){
                chip.isChecked = true
                break
            }
        }
    }

    private fun setEvent(){

        clockTP.setOnTimeChangedListener { timePicker, h, m ->
            currentHour = h
            currentMinute = m

            updateCheckChip()
        }

        positiveTV.setOnClickListener {
            l?.OnPositiveListener(currentHour, currentMinute)
            dismiss()
        }

        negativeTV.setOnClickListener {
            dismiss()
        }
    }

    private fun isSetTime(): Boolean{
        return currentHour != -1 && currentMinute != -1
    }

    private fun TimePicker.setTime(hours: Int, minutes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.hour = hours
            this.minute = minutes
        } else {
            this.currentHour = hours
            this.currentMinute = minutes
        }
    }

    inner class ModelTime(
        var name: String? = null,
        var hour: Int? = null,
        var minute: Int? = null
    )
}