package com.chillchillapp.tasks.todolist.dialog

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.util.TypedValue
import android.view.Window
import android.widget.*
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.model.ModelTaskReminder
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import kotlinx.android.synthetic.main.dialog_due_date.*
import kotlinx.android.synthetic.main.dialog_due_date.stateNotifyTV
import kotlinx.android.synthetic.main.dialog_due_date.stateRepeatTV
import kotlinx.android.synthetic.main.dialog_due_date.stateTimeTV
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SetDueDateDialog(private var activity: Activity) : Dialog(activity){

    interface OnPositiveListener{
        fun OnPositiveListener(calDue: Calendar, hour: Int, minute: Int, reminderList: ArrayList<ModelTaskReminder>)
    }

    private var l: OnPositiveListener? = null
    fun setOnPositiveListener(l: OnPositiveListener){
        this.l = l
    }

    private val calToday = Calendar.getInstance()
    private val calTomorrow = Calendar.getInstance()
    private val calThreeDaysLater = Calendar.getInstance()
    private val calSunday = Calendar.getInstance()
    private val FORMAT_DATE = "dd MM yyyy"
    private val FORMAT_DATE_CALENDAR = "MMM yyyy"

    //data
    private var currenCalDueDate: Calendar = Calendar.getInstance()
    private var currentHour: Int = -1
    private var currentMinute: Int = -1
    private var selectReminderList = ArrayList<ModelTaskReminder>()

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_due_date)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)
        show()

        //set function choice date
        calTomorrow.add(Calendar.DATE, 1)
        calThreeDaysLater.add(Calendar.DATE, 3)
        while (calSunday.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
            calSunday.add(Calendar.DATE, 1)
        }

        setEvent()
        setUpdateUI()

    }

    fun setInitValue(calDue: Calendar, hour: Int, minute: Int, reminderList: ArrayList<ModelTaskReminder>){
        currenCalDueDate.timeInMillis = calDue.timeInMillis
        currentHour = hour
        currentMinute = minute
        selectReminderList.addAll(reminderList)

        if(!isSetDueDate()){
            currenCalDueDate = Calendar.getInstance()
            currentHour = -1
            currentMinute = -1
        }

        setUpdateUI()
    }

    private fun setUpdateUI(){

        //check update Duedate
        updateDueDate()

        //check update Time
        updateTime()

        //check update Reminder
        updateReminder()

    }

    private fun setEvent(){

        addTimeLL.setOnClickListener {
            if(isSetDueDate()){
                setTimeDialog()
            }else{
                Toast.makeText(activity, activity.getString(R.string.please_set_a_date_first), Toast.LENGTH_SHORT).show()
            }
        }

        setReminderLL.setOnClickListener {
            if(isSetTime()){
                setReminderDialog()
            }else{
                Toast.makeText(activity, activity.getString(R.string.please_set_the_task_time_first), Toast.LENGTH_SHORT).show()
            }
        }

        positiveTV.setOnClickListener {

            if(currenCalDueDate.timeInMillis != 0L){
                val calDueDate = Calendar.getInstance()
                calDueDate.timeInMillis = currenCalDueDate.timeInMillis
                calDueDate[Calendar.MILLISECOND] = calDueDate.getActualMinimum(Calendar.MILLISECOND)
                calDueDate[Calendar.SECOND] = 0
                calDueDate[Calendar.MINUTE] = 0
                calDueDate[Calendar.HOUR_OF_DAY] = calDueDate.getActualMinimum(Calendar.HOUR_OF_DAY)

                l?.OnPositiveListener(calDueDate, currentHour, currentMinute, selectReminderList)
            }else{
                l?.OnPositiveListener(currenCalDueDate, currentHour, currentMinute, selectReminderList)
            }


            dismiss()
        }

        negativeTV.setOnClickListener {
            dismiss()
        }

        calCCV.setListener(object : CompactCalendarView.CompactCalendarViewListener{

            override fun onDayClick(dateClicked: Date) {
                currenCalDueDate!!.time = dateClicked
                dateTV.setTextDate(dateClicked)

                updateDueDate()

            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                currenCalDueDate.time = firstDayOfNewMonth
                dateTV.setTextDate(firstDayOfNewMonth)

                updateDueDate()
            }
        })

        notDueDateChip.setOnClickListener {
            currenCalDueDate.timeInMillis = 0
            currentHour = -1
            currentMinute = -1

            updateDueDate()
        }

        selectDay2CC.setOnClickListener {
            currenCalDueDate.timeInMillis = calToday.timeInMillis
            updateDueDate()

        }

        selectDay3CC.setOnClickListener {
            currenCalDueDate.timeInMillis = calTomorrow.timeInMillis
            updateDueDate()

        }

        selectDay4CC.setOnClickListener {
            currenCalDueDate.timeInMillis = calThreeDaysLater.timeInMillis
            updateDueDate()

        }

        selectDay5CC.setOnClickListener {
            currenCalDueDate.timeInMillis = calSunday.timeInMillis
            updateDueDate()

        }

        calendarLeftIV.setOnClickListener {
            calCCV.scrollLeft()
        }

        calendarRightIV.setOnClickListener {
            calCCV.scrollRight()
        }
    }
    private fun setReminderDialog(){
        val d = SetReminderDialog(activity)
        d.setDataList(selectReminderList)
        d.setOnMyEvent(object : SetReminderDialog.OnMyEvent{
            override fun OnClickPositive(itemSelect: ArrayList<ModelTaskReminder>) {
                selectReminderList = itemSelect

                updateReminder()
            }
        })

    }

    private fun setTimeDialog(){
        val d = SetTimeDialog(activity)
        d.setInitValue(currentHour, currentMinute)
        d.setOnPositiveListener(object : SetTimeDialog.OnPositiveListener{
            override fun OnPositiveListener(hour: Int, minute: Int) {
                currentHour = hour
                currentMinute = minute

                if(isSetTime()){
                    if(selectReminderList.size == 0){
                        selectReminderList.add(ModelTaskReminder(null, null, "op2", 5, Calendar.MINUTE,null, null))
                    }
                }else{
                    selectReminderList.clear()
                }

                updateTime()
                updateReminder()
            }
        })
    }

    private fun updateDueDate(){
        when(isSetDueDate()){

            true->{
                setCustomTime(1)
                setCustomNotify(0)
                setCustomRepeat(1)

                val currentDate = currenCalDueDate.time
                dateTV.setTextDate(currentDate)
                calCCV.setCurrentDate(currentDate)
                calCCV.setCurrentSelectedDayBackgroundColor(getColor(activity, R.attr.colorAccent))
                calCCV.setCurrentSelectedDayTextColor(getColor(R.color.colorWhite))

                selectDayCG.clearCheck()

                if(formatDate(FORMAT_DATE, currentDate) == formatDate(FORMAT_DATE, calToday.time)){
                    selectDay2CC.isChecked = true
                }
                if(formatDate(FORMAT_DATE, currentDate) == formatDate(FORMAT_DATE, calTomorrow.time)){
                    selectDay3CC.isChecked = true
                }
                if(formatDate(FORMAT_DATE, currentDate) == formatDate(FORMAT_DATE, calThreeDaysLater.time)){
                    selectDay4CC.isChecked = true
                }
                if(formatDate(FORMAT_DATE, currentDate) == formatDate(FORMAT_DATE, calSunday.time)){
                    selectDay5CC.isChecked = true
                }
            }

            false->{
                setCustomTime(0)
                setCustomNotify(0)
                setCustomRepeat(0)

                stateTimeTV.text = activity.getString(R.string.do_not_have)
                stateNotifyTV.text = activity.getString(R.string.do_not_have)
                stateRepeatTV.text = activity.getString(R.string.no_loop)

                selectDayCG.clearCheck()
                selectReminderList.clear()

                notDueDateChip.isChecked = true

                calCCV.setCurrentSelectedDayBackgroundColor(getColor(R.color.colorWhite))
                calCCV.setCurrentSelectedDayTextColor(getColor(R.color.colorBlack))
            }
        }
    }

    private fun updateTime(){
        when(isSetTime()){
            true->{
                val c = Calendar.getInstance()
                c.set(Calendar.HOUR_OF_DAY, currentHour!!)
                c.set(Calendar.MINUTE, currentMinute!!)
                stateTimeTV.text = formatDate("HH:mm", c.time)
                setCustomNotify(1)
            }
            false->{
                stateTimeTV.text = activity.getString(R.string.do_not_have)
                selectReminderList.clear()
                setCustomNotify(0)
            }
        }
    }

    private fun updateReminder(){
        when(isSetReminder()){
            true->{
                var reminderStr = ""
                for(m in selectReminderList){
                    val calNotify = Calendar.getInstance()
                    calNotify.timeInMillis = currenCalDueDate.timeInMillis
                    calNotify.set(Calendar.HOUR_OF_DAY, currentHour)
                    calNotify.set(Calendar.MINUTE, currentMinute)
                    calNotify.add(m.reminderType!!, -(m.reminderCount!!).toInt())

                    if(reminderStr.isEmpty()){
                        reminderStr = if(formatDate("yyyy/MM/dd", calNotify.time) == formatDate("yyyy/MM/dd", currenCalDueDate.time)){
                            formatDate("HH:mm", calNotify.time)
                        }else{
                            formatDate("yyyy/MM/dd HH:mm", calNotify.time)
                        }
                    }else{
                        reminderStr = if(formatDate("yyyy/MM/dd", calNotify.time) == formatDate("yyyy/MM/dd", currenCalDueDate.time)){
                            formatDate("HH:mm", calNotify.time) + ", " + reminderStr
                        }else{
                            formatDate("yyyy/MM/dd HH:mm", calNotify.time) + ", " + reminderStr
                        }
                    }
                }
                stateNotifyTV.text = reminderStr
            }
            false->{
                stateNotifyTV.text = activity.getString(R.string.do_not_have)
            }
        }

    }

    /*
    private fun updateRepeat(){
        when(isSetRepeat()){
            true->{
                stateRepeatTV.text = activity.getString(R.string.have)
            }
            false->{
                stateRepeatTV.text = activity.getString(R.string.do_not_have)
            }
        }
    }*/

    private fun setCustomTime(open: Int){
        when(open){
            0->{
                timeIV.setColorFilter(getColor(R.color.colorWhiteDarkDark))
                textTimeTV.setTextColor(getColor(R.color.colorWhiteDarkDark))
                stateTimeTV.setTextColor(getColor(R.color.colorWhiteDarkDark))
            }
            1->{
                timeIV.setColorFilter(getColor(R.color.colorBlack))
                textTimeTV.setTextColor(getColor(R.color.colorBlack))
                stateTimeTV.setTextColor(getColor(R.color.colorBlack))
            }
        }
    }

    private fun setCustomNotify(open: Int){
        when(open){
            0->{
                notifyIV.setColorFilter(getColor(R.color.colorWhiteDarkDark))
                textNotifyTV.setTextColor(getColor(R.color.colorWhiteDarkDark))
                stateNotifyTV.setTextColor(getColor(R.color.colorWhiteDarkDark))
            }
            1->{
                notifyIV.setColorFilter(getColor(R.color.colorBlack))
                textNotifyTV.setTextColor(getColor(R.color.colorBlack))
                stateNotifyTV.setTextColor(getColor(R.color.colorBlack))
            }
        }
    }

    private fun setCustomRepeat(open: Int){
        when(open){
            0->{
                repeatIV.setColorFilter(getColor(R.color.colorWhiteDarkDark))
                textRepeatTV.setTextColor(getColor(R.color.colorWhiteDarkDark))
                stateRepeatTV.setTextColor(getColor(R.color.colorWhiteDarkDark))
            }
            1->{
                repeatIV.setColorFilter(getColor(R.color.colorBlack))
                textRepeatTV.setTextColor(getColor(R.color.colorBlack))
                stateRepeatTV.setTextColor(getColor(R.color.colorBlack))
            }
        }
    }

    private fun formatDate(formatStr: String, day: Date): String{
        val format = SimpleDateFormat(formatStr)
        val dateStr = format.format(day)
        return dateStr
    }

    private fun TextView.setTextDate(date: Date?){

        val format = try {
            SimpleDateFormat(FORMAT_DATE_CALENDAR, Locale.getDefault())
        }catch (e: Exception){
            null
        }

        if(format != null){
            val dateStr = format!!.format(date)
            this.text = dateStr

            val monthCur = Date().month
            val monthSel = date!!.month

            if(monthCur == monthSel){
                this.setTextColor(getColor(R.color.colorBlack))
            }else{
                this.setTextColor(getColor(R.color.colorWhiteDarkDark))
            }
        }
    }

    private fun isSetDueDate(): Boolean{
        return currenCalDueDate.timeInMillis != 0L
    }
    private fun isSetTime(): Boolean{
        return currentHour != -1 && currentMinute != -1
    }
    private fun isSetReminder(): Boolean{
        return selectReminderList.size != 0
    }
    private fun isSetRepeat(): Boolean{
        return false
    }

    private fun getColor(color: Int):Int{
        return ContextCompat.getColor(activity, color)
    }

    private fun getColor(activity: Activity, @AttrRes attrRes: Int): Int{
        val typedValue = TypedValue()
        activity.theme.resolveAttribute (attrRes, typedValue, true)
        return typedValue.data
    }

    open class ModelSetDueDate(
        var calDue: Calendar? = null,
        var hour: Int? = null,
        var minute: Int? = null
    )

}