package com.chillchillapp.tasks.todolist.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.chillchillapp.tasks.todolist.BaseFragment
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.adapter.AdapTaskInCalendar
import com.chillchillapp.tasks.todolist.database.*
import com.chillchillapp.tasks.todolist.model.ModelRepeat
import com.chillchillapp.tasks.todolist.model.ModelTask
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import kotlinx.android.synthetic.main.fragment_calendar.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("UseRequireInsteadOfGet")
class MenuCalendarFragment : BaseFragment(R.layout.fragment_calendar)  {

    private lateinit var functionTask: FunctionTask
    private lateinit var functionTaskSub: FunctionTaskSub
    private lateinit var functionCategory: FunctionCategory
    private lateinit var functionAttach: FunctionTaskAttach
    private lateinit var functionRepeat: FunctionRepeat

    private var currenSelectCal: Calendar = Calendar.getInstance()

    private val FORMAT_DATE = "dd MM yyyy"
    private val FORMAT_DATE_CALENDAR = "MMMM yyyy"

    private var currentDayTaskList = ArrayList<ModelTask>()
    private var timeCount = ArrayList<Long>()
    private var repeatList: ArrayList<ModelRepeat>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBase()

        init()
        addEventInCalendar()
        setAdap()
        functionChangeDate(FUNCTION_TODAY)
        setEvent()

    }

    override fun onResume() {
        super.onResume()

        checkUpdate()
    }

    private var calToday: Calendar? = null
    private var calTomorrow: Calendar? = null
    private var calThreeDaysLater: Calendar? = null
    private var calSunday: Calendar? = null
    private var calYesterDay: Calendar? = null


    private fun init(){

        functionTask = FunctionTask(requireContext())
        functionTaskSub= FunctionTaskSub(requireContext())
        functionCategory = FunctionCategory(requireContext())
        functionAttach = FunctionTaskAttach(requireContext())
        functionRepeat = FunctionRepeat(requireContext())

        functionTask.open()
        functionCategory.open()
        functionAttach.open()

        currenSelectCal!!.timeInMillis = System.currentTimeMillis()

        calToday = Calendar.getInstance()

        calYesterDay = Calendar.getInstance()
        calYesterDay!!.add(Calendar.DATE, -1)

        calTomorrow = Calendar.getInstance()
        calTomorrow!!.add(Calendar.DATE, 1)

        calThreeDaysLater = Calendar.getInstance()
        calThreeDaysLater!!.add(Calendar.DATE, 3)

        calSunday = Calendar.getInstance()
        while (calSunday!!.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
            calSunday!!.add(Calendar.DATE, 1)
        }

        calCCV.setListener(object : CompactCalendarView.CompactCalendarViewListener{

            override fun onDayClick(dateClicked: Date) {
                currenSelectCal!!.timeInMillis = dateClicked.time
                dateTV.setTextDate(currenSelectCal!!.time)

                calCCV.setCurrentSelectedDayBackgroundColor(themeColor(activity, R.attr.colorAccent))
                calCCV.setCurrentSelectedDayTextColor(ContextCompat.getColor(activity!!, R.color.colorWhite))

                addDataCurrentDay()
                selectDayCG.clearCheck()
                setChangeSelectDate()

            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {

                currenSelectCal!!.timeInMillis = firstDayOfNewMonth.time
                dateTV.setTextDate(currenSelectCal!!.time)

                calCCV.setCurrentSelectedDayBackgroundColor(themeColor(activity, R.attr.colorAccent))
                calCCV.setCurrentSelectedDayTextColor(ContextCompat.getColor(activity!!, R.color.colorWhite))

                addDataCurrentDay()

                if(firstDayOfNewMonth.time !in timeCount){
                    timeCount.add(firstDayOfNewMonth.time)
                    addEventInCalendar()

                    Log.i("hhhjjgg", "addEventInCalendar")
                }

                selectDayCG.clearCheck()
                setChangeSelectDate()

            }
        })

        dateTV.setTextDate(currenSelectCal!!.time)

    }

    private fun checkUpdate(){

        val update = prefs!!.boolUpdateTask
        //val update = sp?.getLong("update", 0)

        if(update){
            Handler().postDelayed( {

                try {
                    if(!requireActivity().isDestroyed){
                        addEventInCalendar()
                        addDataCurrentDay()
                    }
                }catch (e:Exception){
                    Log.i("MenuCalendarFragment", "e: $e")
                }

            }, 500)

            prefs!!.boolUpdateTask = false

            /*editor?.putLong("update", 0)
            editor?.commit()*/
        }
    }


    /*private fun addEventInCalendar(){

        val currentMonthTaskRepeatList = getCurrentMonthTaskRepeat()
        currentMonthTaskList = functionTask.getTaskFromMonth(currenSelectCal!!.timeInMillis)
        currentMonthTaskList.addAll(currentMonthTaskRepeatList)

        for (m in currentMonthTaskList){

            val list = eventList.filter { formatDate("dd/MM/yyyy", Date(it)) == formatDate("dd/MM/yyyy", Date(m.dueDate!!))}
            if(list.isEmpty()){
                eventList.add(m.dueDate!!)
                val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Event(themeColor(activity!!, R.attr.colorAccent), m.dueDate!!, null)
                } else {
                    Event(Color.GRAY, m.dueDate!!, null)
                }
                calCCV.addEvent(event)
                //Log.i("gewgsdgr", "date: " + formatDate("dd/MM/yyyy", Date(m.dueDate!!)))
            }
        }

    }*/

    private val eventList = ArrayList<Long>()
    private fun addEventInCalendar(){

        Log.i("hhhjjgg", "addEventInCalendar")

        val calStartMonth = Calendar.getInstance()
        calStartMonth.timeInMillis = currenSelectCal!!.timeInMillis
        calStartMonth[Calendar.MILLISECOND] = 0
        calStartMonth[Calendar.SECOND] = 0
        calStartMonth[Calendar.MINUTE] = 0
        calStartMonth[Calendar.HOUR_OF_DAY] = calStartMonth.getActualMinimum(Calendar.HOUR_OF_DAY)
        calStartMonth[Calendar.DATE] = 1

        val calEndMonth = Calendar.getInstance()
        calEndMonth.timeInMillis = calStartMonth.timeInMillis
        calEndMonth.add(Calendar.MONTH, 1)

        val calCount = Calendar.getInstance()
        calCount.timeInMillis = calStartMonth.timeInMillis

        Log.i("hhhjjgg", "getDueDateFromMonth start")
        val dueDateList = functionTask.getDueDateFromMonth(currenSelectCal!!.time) //getTask best check
        Log.i("hhhjjgg", "getCurrentMonthDueDateRepeat start")
        dueDateList.addAll(getCurrentMonthDueDateRepeat())// mix task
        dueDateList.sort()
        Log.i("hhhjjgg", "dueDateList: " + dueDateList.size)


        //run 28-31 day
        while (calCount.timeInMillis < calEndMonth.timeInMillis){

            val index = binarySearch(dueDateList.toLongArray(), 0, (dueDateList.size-1), calCount.timeInMillis)
            Log.i("hhhjjgg", "date: " + formatDate("dd/MM", calCount.time) + " index: " + index)


            if(index != -1){

                val i = binarySearch(eventList.toLongArray(), 0, (eventList.size-1), calCount.timeInMillis)
                if(i == -1){
                    eventList.add(calCount.timeInMillis)
                    //Log.i("hhhjjgg", "eventList: " + eventList.size)
                    val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Event(themeColor(activity!!, R.attr.colorAccent), calCount.timeInMillis, null)
                    } else {
                        Event(Color.GRAY, calCount.timeInMillis, null)
                    }
                    calCCV.addEvent(event)
                }
            }
            calCount.add(Calendar.DATE, 1)
        }
    }

    private fun setEvent(){

        calLeftIV.setOnClickListener {
            calCCV.scrollLeft()
        }
        calRightIV.setOnClickListener {
            calCCV.scrollRight()
        }

        notDueDateChip.setOnClickListener {
            functionChangeDate(FUNCTION_YESTERDAY)
        }

        selectDay2CC.setOnClickListener {
            functionChangeDate(FUNCTION_TODAY)
        }

        selectDay3CC.setOnClickListener {
            functionChangeDate(FUNCTION_TOMORROW)
        }

        selectDay4CC.setOnClickListener {
            functionChangeDate(FUNCTION_THREEDAY)
        }

        selectDay5CC.setOnClickListener {
            functionChangeDate(FUNCTION_SUNDAY)
        }

    }

    private fun addDataCurrentDay(){

        currentDayTaskList.clear()
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = currenSelectCal!!.timeInMillis
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)

        val endCal = Calendar.getInstance()
        endCal.timeInMillis = startCal.timeInMillis
        endCal.add(Calendar.DATE, 1)

        val start = startCal.timeInMillis
        val end = endCal.timeInMillis

        for(m in functionTask.getTaskFromDate(currenSelectCal!!.timeInMillis)){
            currentDayTaskList.add(m)
        }

        /*for(m in currentMonthTaskList){
            if(m.dueDate!! in start until end){
                currentDayTaskList.add(m)
            }
        }*/

        val currentDayTaskRepeat = getCurrentDayTaskRepeat()
        currentDayTaskList.addAll(currentDayTaskRepeat)
        currentDayTaskList.sortBy { it.dueDate }

        dataRCV.adapter?.notifyDataSetChanged()
    }

    private fun setAdap(){
        currentDayTaskList = functionTask.getTaskFromDate(currenSelectCal!!.timeInMillis)

        val adapter = AdapTaskInCalendar(activity!!, currentDayTaskList)
        val layoutManager = GridLayoutManager(activity!!,  1, GridLayoutManager.VERTICAL, false)
        dataRCV.adapter = adapter
        dataRCV.layoutManager = layoutManager

    }

    private val FUNCTION_YESTERDAY = 1
    private val FUNCTION_TODAY = 2
    private val FUNCTION_TOMORROW = 3
    private val FUNCTION_THREEDAY = 4
    private val FUNCTION_SUNDAY = 5
    private fun functionChangeDate(functionDate: Int){
        calCCV.setCurrentSelectedDayBackgroundColor(themeColor(activity!!, R.attr.colorAccent))
        calCCV.setCurrentSelectedDayTextColor(ContextCompat.getColor(activity!!, R.color.colorWhite))
        val c = Calendar.getInstance()

        when(functionDate){
            FUNCTION_YESTERDAY ->{
                c.add(Calendar.DATE, -1)
            }
            FUNCTION_TODAY ->{
                c.add(Calendar.DATE, 0)
            }
            FUNCTION_TOMORROW ->{
                c.add(Calendar.DATE, 1)
            }
            FUNCTION_THREEDAY ->{
                c.add(Calendar.DATE, 3)
            }
            FUNCTION_SUNDAY ->{
                while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
                    c.add(Calendar.DATE, 1)
                }
            }
        }

        currenSelectCal!!.timeInMillis = c.timeInMillis
        calCCV.setCurrentDate(c.time)
        dateTV.setTextDate(c.time)

        addDataCurrentDay()
        setChangeSelectDate()
    }

    private fun setChangeSelectDate(){

        selectDayCG.clearCheck()

        if(formatDate(FORMAT_DATE, currenSelectCal!!.time) == formatDate(FORMAT_DATE, calYesterDay!!.time)){
            notDueDateChip.isChecked = true
        }
        if(formatDate(FORMAT_DATE, currenSelectCal!!.time) == formatDate(FORMAT_DATE, calToday!!.time)){
            selectDay2CC.isChecked = true
        }
        if(formatDate(FORMAT_DATE, currenSelectCal!!.time) == formatDate(FORMAT_DATE, calTomorrow!!.time)){
            selectDay3CC.isChecked = true
        }
        if(formatDate(FORMAT_DATE, currenSelectCal!!.time) == formatDate(FORMAT_DATE, calThreeDaysLater!!.time)){
            selectDay4CC.isChecked = true
        }
        if(formatDate(FORMAT_DATE, currenSelectCal!!.time) == formatDate(FORMAT_DATE, calSunday!!.time)){
            selectDay5CC.isChecked = true
        }

    }

    private val TYPE_HOUR = "hour"
    private val TYPE_DAY = "day"
    private val TYPE_WEEK = "week"
    private val TYPE_MONTH = "month"
    private val TYPE_YEAR = "year"
    private fun getCurrentMonthDueDateRepeat(): ArrayList<Long>{

        val calStartMonth = Calendar.getInstance()
        calStartMonth.timeInMillis = currenSelectCal!!.timeInMillis
        calStartMonth[Calendar.MILLISECOND] = 0
        calStartMonth[Calendar.SECOND] = 0
        calStartMonth[Calendar.MINUTE] = 0
        calStartMonth[Calendar.HOUR_OF_DAY] = calStartMonth.getActualMinimum(Calendar.HOUR_OF_DAY)
        calStartMonth[Calendar.DATE] = 1

        val calEndMonth = Calendar.getInstance()
        calEndMonth.timeInMillis = calStartMonth.timeInMillis
        calEndMonth.add(Calendar.MONTH, 1)

        val dataList = ArrayList<Long>()

        if(repeatList == null){
            repeatList = functionRepeat.getDataList()
        }

        Log.i("hhhjjgg", "loop in repeat start")
        for(i in repeatList!!.indices){

            val modelRepeat = repeatList!![i]

            Log.i("hhhjjgg", "i: " + i + ", " + modelRepeat.id+", " + modelRepeat.taskId + ", " + modelRepeat.createDate+ ", " + modelRepeat.updateDate)

            val calDue = Calendar.getInstance()
            val modelTask = functionTask.getTaskById(modelRepeat.taskId!!)

            if(modelTask.id == null){
                continue
            }

            modelRepeat.programCount = modelRepeat.programCount!! + 1

            calDue.timeInMillis = modelTask.dueDate!!
            if(modelTask.hour != -1 && modelTask.minute != -1){
                calDue.set(Calendar.HOUR_OF_DAY, modelTask.hour!!)
                calDue.set(Calendar.MINUTE, modelTask.minute!!)
            }

            var repeatType = 0
            when(modelRepeat.repeatType){
                TYPE_HOUR-> {
                    repeatType = Calendar.HOUR_OF_DAY
                }
                TYPE_DAY-> {
                    repeatType = Calendar.DATE
                }
                TYPE_WEEK-> {
                    repeatType = Calendar.WEEK_OF_YEAR
                }
                TYPE_MONTH-> {
                    repeatType = Calendar.MONTH
                }
                TYPE_YEAR-> {
                    repeatType = Calendar.YEAR
                }
            }

            if(modelRepeat.numberOfRepeat == 0L){
                //ไม่รู้จบ
                do {
                    calDue.add(repeatType!!, modelRepeat.repeatNext!!)

                    if(calDue.timeInMillis in calStartMonth.timeInMillis until calEndMonth.timeInMillis){
                        val dueDate = Calendar.getInstance()
                        dueDate.timeInMillis = calDue.timeInMillis
                        dueDate[Calendar.HOUR_OF_DAY] = 0
                        dueDate[Calendar.MINUTE] = 0
                        dueDate[Calendar.SECOND] = 0
                        dueDate[Calendar.MILLISECOND] = 0

                        dataList.add(dueDate.timeInMillis)
                    }

                }while (calDue.timeInMillis < calEndMonth.timeInMillis)

            }else{
                //กำหนดจำนวน
                do {
                    calDue.add(repeatType!!, modelRepeat.repeatNext!!)

                    if(calDue.timeInMillis in calStartMonth.timeInMillis until calEndMonth.timeInMillis){
                        val dueDate = Calendar.getInstance()
                        dueDate.timeInMillis = calDue.timeInMillis
                        dueDate[Calendar.HOUR_OF_DAY] = 0
                        dueDate[Calendar.MINUTE] = 0
                        dueDate[Calendar.SECOND] = 0
                        dueDate[Calendar.MILLISECOND] = 0

                        dataList.add(dueDate.timeInMillis)
                    }
                    modelRepeat.programCount = modelRepeat.programCount!! + 1

                }while (modelRepeat.programCount!! < modelRepeat.numberOfRepeat!! && calDue.timeInMillis < calEndMonth.timeInMillis)
            }
        }

        Log.i("hhhjjgg", "loop in repeat end")

        dataList.sortBy { it }

        Log.i("hhhhhhhjk", "task.size: " + dataList.size)

        /*dataList.forEachIndexed { index, dueDate ->

            Log.i("hhhhhhhjk", "$index - DueDate: " + formatDate("dd MM yyyy - HH:mm:ss", Date(dueDate)))
        }*/
        return dataList
    }

    /*private fun getCurrentMonthDueDateRepeat(s: String): ArrayList<Long>{

        val calStartMonth = Calendar.getInstance()
        calStartMonth.timeInMillis = currenSelectCal!!.timeInMillis
        calStartMonth[Calendar.MILLISECOND] = 0
        calStartMonth[Calendar.SECOND] = 0
        calStartMonth[Calendar.MINUTE] = 0
        calStartMonth[Calendar.HOUR_OF_DAY] = calStartMonth.getActualMinimum(Calendar.HOUR_OF_DAY)
        calStartMonth[Calendar.DATE] = 1

        //1.get Date  1-30
        //2.for Date 1-30
        //3.take date 1-30 to check repeat if(date == repeatDate) return

        val calEndMonth = Calendar.getInstance()
        calEndMonth.timeInMillis = calStartMonth.timeInMillis
        calEndMonth.add(Calendar.MONTH, 1)
    }*/

    private fun getCurrentDayTaskRepeat(): ArrayList<ModelTask>{

        val calStartDay = Calendar.getInstance()
        calStartDay.timeInMillis = currenSelectCal!!.timeInMillis
        calStartDay[Calendar.MILLISECOND] = 0
        calStartDay[Calendar.SECOND] = 0
        calStartDay[Calendar.MINUTE] = 0
        calStartDay[Calendar.HOUR_OF_DAY] = calStartDay.getActualMinimum(Calendar.HOUR_OF_DAY)

        val calEndDay = Calendar.getInstance()
        calEndDay.timeInMillis = currenSelectCal!!.timeInMillis
        calEndDay[Calendar.MILLISECOND] = calStartDay.getActualMaximum(Calendar.MILLISECOND)
        calEndDay[Calendar.SECOND] = 59
        calEndDay[Calendar.MINUTE] = 59
        calEndDay[Calendar.HOUR_OF_DAY] = calStartDay.getActualMaximum(Calendar.HOUR_OF_DAY)

        /*Log.i("hhhhhhhjk", "startCal: " + formatDate("dd MM yyyy - HH:mm:ss", startCal.time))
        Log.i("hhhhhhhjk", "endCal: " + formatDate("dd MM yyyy - HH:mm:ss", endCal.time))*/

        val dataList = ArrayList<ModelTask>()
        val repeatList = functionRepeat.getDataList()

        for(i in repeatList.indices){

            val modelTask = functionTask.getTaskById(repeatList[i].taskId!!)
            if(modelTask.id == null){
                continue
            }

            val calDue = Calendar.getInstance()
            repeatList[i].programCount = repeatList[i].programCount!! + 1

            calDue.timeInMillis = modelTask.dueDate!!
            if(modelTask.hour != -1 && modelTask.minute != -1){
                calDue.set(Calendar.HOUR_OF_DAY, modelTask.hour!!)
                calDue.set(Calendar.MINUTE, modelTask.minute!!)
            }

            var repeatType = 0
            when(repeatList[i].repeatType){
                TYPE_HOUR-> {
                    repeatType = Calendar.HOUR_OF_DAY
                }
                TYPE_DAY-> {
                    repeatType = Calendar.DATE
                }
                TYPE_WEEK-> {
                    repeatType = Calendar.WEEK_OF_YEAR
                }
                TYPE_MONTH-> {
                    repeatType = Calendar.MONTH
                }
                TYPE_YEAR-> {
                    repeatType = Calendar.YEAR
                }
            }

            if(repeatList[i].numberOfRepeat == 0L){
                //ไม่รู้จบ
                do {
                    calDue.add(repeatType!!, repeatList[i].repeatNext!!)

                    if(calDue.timeInMillis in calStartDay.timeInMillis until calEndDay.timeInMillis){
                        var dueDate = Calendar.getInstance()
                        dueDate.timeInMillis = calDue.timeInMillis
                        dueDate[Calendar.HOUR_OF_DAY] = 0
                        dueDate[Calendar.MINUTE] = 0
                        dueDate[Calendar.SECOND] = 0
                        dueDate[Calendar.MILLISECOND] = 0

                        var hour = calDue.get(Calendar.HOUR_OF_DAY)
                        var minute = calDue.get(Calendar.MINUTE)

                        val model = functionTask.getTaskById(repeatList[i].taskId!!)
                        model.dueDate = dueDate.timeInMillis
                        model.hour = hour
                        model.minute = minute
                        dataList.add(model)
                    }

                }while (calDue.timeInMillis < calEndDay.timeInMillis)

            }else{
                //กำหนดจำนวน
                do {
                    calDue.add(repeatType!!, repeatList[i].repeatNext!!)
                    val model = functionTask.getTaskById(repeatList[i].taskId!!)
                    model.dueDate = calDue.timeInMillis

                    if(model.dueDate!! in calStartDay.timeInMillis until calEndDay.timeInMillis){
                        var dueDate = Calendar.getInstance()
                        dueDate.timeInMillis = calDue.timeInMillis
                        dueDate[Calendar.HOUR_OF_DAY] = 0
                        dueDate[Calendar.MINUTE] = 0
                        dueDate[Calendar.SECOND] = 0
                        dueDate[Calendar.MILLISECOND] = 0

                        var hour = calDue.get(Calendar.HOUR_OF_DAY)
                        var minute = calDue.get(Calendar.MINUTE)

                        val model = functionTask.getTaskById(repeatList[i].taskId!!)
                        model.dueDate = dueDate.timeInMillis
                        model.hour = hour
                        model.minute = minute
                        dataList.add(model)
                    }
                    repeatList[i].programCount = repeatList[i].programCount!! + 1

                }while (repeatList[i].programCount!! < repeatList[i].numberOfRepeat!! && calDue.timeInMillis < calEndDay.timeInMillis)
            }
        }

        dataList.sortBy { it.dueDate }
        return dataList
    }

    private fun getCurrentMonthTaskRepeat(): ArrayList<ModelTask>{

        val calStartMonth = Calendar.getInstance()
        calStartMonth.timeInMillis = currenSelectCal!!.timeInMillis
        calStartMonth[Calendar.MILLISECOND] = 0
        calStartMonth[Calendar.SECOND] = 0
        calStartMonth[Calendar.MINUTE] = 0
        calStartMonth[Calendar.HOUR_OF_DAY] = calStartMonth.getActualMinimum(Calendar.HOUR_OF_DAY)

        while (calStartMonth.get(Calendar.DATE) != 1){
            calStartMonth.add(Calendar.DATE, -1)
        }

        val calEndMonth = Calendar.getInstance()
        calEndMonth.timeInMillis = calStartMonth.timeInMillis
        calEndMonth.add(Calendar.MONTH, 1)

        /*Log.i("hhhhhhhjk", "startCal: " + formatDate("dd MM yyyy - HH:mm:ss", startCal.time))
        Log.i("hhhhhhhjk", "endCal: " + formatDate("dd MM yyyy - HH:mm:ss", endCal.time))*/

        val dataList = ArrayList<ModelTask>()
        val repeatList = functionRepeat.getDataList()

        for(i in repeatList.indices){

            val calDue = Calendar.getInstance()
            val modelTask = functionTask.getTaskById(repeatList[i].taskId!!)
            repeatList[i].programCount = repeatList[i].programCount!! + 1

            calDue.timeInMillis = modelTask.dueDate!!
            if(modelTask.hour != -1 && modelTask.minute != -1){
                calDue.set(Calendar.HOUR_OF_DAY, modelTask.hour!!)
                calDue.set(Calendar.MINUTE, modelTask.minute!!)
            }

            var repeatType = 0
            when(repeatList[i].repeatType){
                TYPE_HOUR-> {
                    repeatType = Calendar.HOUR_OF_DAY
                }
                TYPE_DAY-> {
                    repeatType = Calendar.DATE
                }
                TYPE_WEEK-> {
                    repeatType = Calendar.WEEK_OF_YEAR
                }
                TYPE_MONTH-> {
                    repeatType = Calendar.MONTH
                }
                TYPE_YEAR-> {
                    repeatType = Calendar.YEAR
                }
            }

            if(repeatList[i].numberOfRepeat == 0L){
                //ไม่รู้จบ
                do {
                    calDue.add(repeatType!!, repeatList[i].repeatNext!!)

                    if(calDue.timeInMillis in calStartMonth.timeInMillis until calEndMonth.timeInMillis){
                        var dueDate = Calendar.getInstance()
                        dueDate.timeInMillis = calDue.timeInMillis
                        dueDate[Calendar.HOUR_OF_DAY] = 0
                        dueDate[Calendar.MINUTE] = 0
                        dueDate[Calendar.SECOND] = 0
                        dueDate[Calendar.MILLISECOND] = 0

                        var hour = calDue.get(Calendar.HOUR_OF_DAY)
                        var minute = calDue.get(Calendar.MINUTE)

                        val model = functionTask.getTaskById(repeatList[i].taskId!!)
                        model.dueDate = dueDate.timeInMillis
                        model.hour = hour
                        model.minute = minute
                        dataList.add(model)
                    }

                }while (calDue.timeInMillis < calEndMonth.timeInMillis)

            }else{
                //กำหนดจำนวน
                do {
                    calDue.add(repeatType!!, repeatList[i].repeatNext!!)
                    val model = functionTask.getTaskById(repeatList[i].taskId!!)
                    model.dueDate = calDue.timeInMillis

                    if(model.dueDate!! in calStartMonth.timeInMillis until calEndMonth.timeInMillis){
                        var dueDate = Calendar.getInstance()
                        dueDate.timeInMillis = calDue.timeInMillis
                        dueDate[Calendar.HOUR_OF_DAY] = 0
                        dueDate[Calendar.MINUTE] = 0
                        dueDate[Calendar.SECOND] = 0
                        dueDate[Calendar.MILLISECOND] = 0

                        var hour = calDue.get(Calendar.HOUR_OF_DAY)
                        var minute = calDue.get(Calendar.MINUTE)

                        val model = functionTask.getTaskById(repeatList[i].taskId!!)
                        model.dueDate = dueDate.timeInMillis
                        model.hour = hour
                        model.minute = minute
                        dataList.add(model)
                    }
                    repeatList[i].programCount = repeatList[i].programCount!! + 1

                }while (repeatList[i].programCount!! < repeatList[i].numberOfRepeat!! && calDue.timeInMillis < calEndMonth.timeInMillis)
            }
        }

        dataList.sortBy { it.dueDate }

        /*dataList.forEachIndexed { index, modelTask ->
            if(modelTask.hour != -1 && modelTask.minute != -1){

            }
            Log.i("hhhhhhhjk", "$index - DueDate: " + formatDate("dd MM yyyy - HH:mm:ss", Date(modelTask.dueDate!!)))
        }*/
        return dataList
    }

    private fun binarySearch(arr: LongArray, left: Int, right: Int, x: Long): Int {

        if (right >= left) {
            val mid = left + (right - left) / 2
            if (formatDate("dd/MM", Date(arr[mid])) == formatDate("dd/MM", Date(x))) return mid

            return if (arr[mid] > x) binarySearch(arr, left, mid - 1, x) else binarySearch(arr,
                mid + 1,
                right,
                x)
        }
        return -1
    }

    private fun TextView.setTextDate(date: Date?){

        val format_date = FORMAT_DATE_CALENDAR
        val format = SimpleDateFormat(format_date)
        val dateStr = format.format(date)
        this.text = dateStr

        val monthCurrent = Date().month
        val monthSelect = date!!.month

        if(monthCurrent == monthSelect){
            this.setTextColor(ContextCompat.getColor(activity!!, R.color.colorBlack))
        }else{
            this.setTextColor(ContextCompat.getColor(activity!!, R.color.colorWhiteDarkDark))
        }
    }
}