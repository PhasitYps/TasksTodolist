package com.chillchillapp.tasks.todolist.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.DashPathEffect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chillchillapp.tasks.todolist.BaseFragment
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.database.FunctionCategory
import com.chillchillapp.tasks.todolist.database.FunctionTask
import com.chillchillapp.tasks.todolist.model.ModelTask
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import kotlinx.android.synthetic.main.fragment_statistics.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@SuppressLint("UseRequireInsteadOfGet")
class MenuStatisticsFragment : BaseFragment(R.layout.fragment_statistics)  {

    private lateinit var functionCategory: FunctionCategory
    private lateinit var functionTask: FunctionTask

    private lateinit var calStart: Calendar
    private lateinit var calEnd: Calendar

    private var mTaskOfWeekList: ArrayList<ModelDataOfWeek> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBase()

        init()
        setAdap()
        currentDate()
        setEvent()
    }

    override fun onResume() {
        super.onResume()

        checkUpdate()

    }

    /*private fun setAds(){
        val adLoader = AdLoader.Builder(requireContext(), "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { nativeAd : NativeAd ->
                // Show the ad.
                if (requireActivity().isDestroyed) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                nativeAd(nativeAd)


            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                // Methods in the NativeAdOptions.Builder class can be
                // used here to specify individual options settings.
                .build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }*/


    private fun checkUpdate(){
        val update = sp?.getLong("update", 0)

        if(update != 0L){
            Handler().postDelayed( {
                currentDate()
            }, 500)

            editor?.putLong("update", 0)
            editor?.commit()
        }
    }

    private fun init(){
        functionCategory = FunctionCategory(requireContext())
        functionTask = FunctionTask(requireActivity())

        functionTask.open()
        functionCategory.open()


        val taskList = functionTask.getDataList()

        numDoneTV.text = taskList.filter { it.state == 1L }.size.toString()
        numNotDoneTV.text = taskList.filter { it.state == 0L }.size.toString()

    }

    private fun setEvent(){

        dateToLeftIV.setOnClickListener {
            left()
        }

        dateToRightIV.setOnClickListener {
            right()
        }
    }

    private fun currentDate(){

        calStart = Calendar.getInstance()
        calStart[Calendar.MILLISECOND] = 0
        calStart[Calendar.SECOND] = 0
        calStart[Calendar.MINUTE] = 0
        calStart[Calendar.HOUR_OF_DAY] = calStart.getActualMinimum(Calendar.HOUR_OF_DAY)

        calEnd = Calendar.getInstance()
        calEnd[Calendar.MILLISECOND] = calStart.getActualMaximum(Calendar.MILLISECOND)
        calEnd[Calendar.SECOND] = 59
        calEnd[Calendar.MINUTE] = 59
        calEnd[Calendar.HOUR_OF_DAY] = calStart.getActualMaximum(Calendar.HOUR_OF_DAY)

        while (calStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calStart.add(Calendar.DATE, -1)
            Log.d("ttt", "calStart: "+  calStart.time.toString())
        }

        while (calEnd.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            calEnd.add(Calendar.DATE, 1)
            Log.d("ttt", "calEnd: " + calEnd.time.toString())
        }

        setDetail()

    }

    private fun right(){
        calStart.add(Calendar.DATE, 7)
        calEnd.add(Calendar.DATE, 7)

        while (calStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calStart.add(Calendar.DATE, -1)
            Log.d("ttt", "calStart: "+  calStart.time.toString())
        }

        while (calEnd.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            calEnd.add(Calendar.DATE, 1)
            Log.d("ttt", "calEnd: " + calEnd.time.toString())
        }

        setDetail()
    }

    private fun left(){
        calStart.add(Calendar.DATE, -7)
        calEnd.add(Calendar.DATE, -7)

        while (calStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calStart.add(Calendar.DATE, -1)
            Log.d("ttt", "calStart: "+  calStart.time.toString())
        }

        while (calEnd.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            calEnd.add(Calendar.DATE, 1)
            Log.d("ttt", "calEnd: " + calEnd.time.toString())
        }

        setDetail()
    }

    private fun setDetail() {
        val sdfShow = SimpleDateFormat("d/MMM")
        rangeDateTV.text = "${sdfShow.format(calStart.time)} - ${sdfShow.format(calEnd.time)}"

        initData()
        setBarChart()
        //setView()*/

    }

    private fun initData() {

        mTaskOfWeekList.clear()

        val sdfShow = SimpleDateFormat("EEE")

        var calCountDate =  Calendar.getInstance()
        calCountDate.time = calStart.time

        while (calCountDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calCountDate.add(Calendar.DATE, -1)
        }

        //this week
        for (i in 1..7) {

            val dataTaskList = functionTask.getTaskFromDate(calCountDate.timeInMillis)
            val countTask = dataTaskList.filter { it.state == 1L }.size.toDouble()

            var m = ModelDataOfWeek(calCountDate.time, sdfShow.format(calCountDate.time), countTask)

            mTaskOfWeekList.add(m)
            calCountDate.add(Calendar.DATE, 1)

            Log.d("ttt", "calCountDate: "+  calCountDate.time.toString())
        }

    }

    private fun setBarChart() {

        var xAxisLabel = ArrayList<String?>()

        val taskEntries: MutableList<BarEntry> = ArrayList()

        for ((index, value) in mTaskOfWeekList.withIndex()) {

            // add x name
            xAxisLabel.add(value.dateName)
            // add index and value
            taskEntries.add(BarEntry(index.toFloat(), value.countTask!!.toFloat()))
        }

        //Log.i("fewzf", "yAxisLeft.labelCount: " + yAxisLeft.labelCount)

        val barData = BarData(setInit(taskEntries, "task"))
        val description = Description()
        description.text = ""
        dataPlotBC!!.legend.isEnabled = false
        dataPlotBC!!.data = barData
        dataPlotBC!!.description = description
        dataPlotBC!!.setTouchEnabled(true)
        dataPlotBC!!.animateXY(2000, 2000)
        dataPlotBC!!.setDrawMarkers(true)

        val barSpace = 0.8f
        val groupSpace = 0.4f
        val groupCount = xAxisLabel.size

        dataPlotBC!!.barData.barWidth = barSpace

        //IMPORTANT *****
        val xAxis = dataPlotBC!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.textSize = 12f
        xAxis.textColor = ContextCompat.getColor(requireActivity(), R.color.colorBlack) // color text down
        xAxis.enableGridDashedLine(16f, 12f, 0f)
        xAxis.granularity = 1f
        xAxis.labelCount = groupCount
        xAxis.setCenterAxisLabels(false)
        xAxis.isGranularityEnabled = true
        xAxis.setDrawLabels(true) //ตัวอักษร
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.axisLineColor = ContextCompat.getColor(activity!!, R.color.colorBlack)
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabel)

        val yAxisLeft = dataPlotBC!!.axisLeft
        yAxisLeft.setDrawLabels(true)
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.setDrawZeroLine(false)
        yAxisLeft.axisMinimum = 0f // start at zero
        yAxisLeft.labelCount = 4 // count labal left
        yAxisLeft.axisLineColor = ContextCompat.getColor(activity!!, R.color.colorBlack)
        yAxisLeft.valueFormatter = IAxisValueFormatter { value, axis ->
            "${value.toInt()}"
        }

        var max = mTaskOfWeekList.maxByOrNull{ it.countTask!! }!!.countTask!! // < 8
        if(max < 8){
            yAxisLeft.axisMaximum = 8f // the axis maximum is 8
        }else{

            while(max.toFloat() % yAxisLeft.labelCount != 0f){
                max += 1
            }
            yAxisLeft.axisMaximum = max.toFloat()
        }

        val yAxisRight = dataPlotBC!!.axisRight
        yAxisRight.isEnabled = false // no right axis

        dataPlotBC!!.setPinchZoom(false)
        dataPlotBC!!.isDragEnabled = false
        dataPlotBC!!.setVisibleXRangeMaximum(mTaskOfWeekList.size.toFloat())
        dataPlotBC!!.moveViewToX(xAxisLabel.size.toFloat())
        dataPlotBC!!.setScaleEnabled(false)
        dataPlotBC!!.setDrawGridBackground(false)
        dataPlotBC!!.setDrawValueAboveBar(true) //ตั้งค่า value เหนือบาร์
        //dataPlotBC!!.setViewPortOffsets(0f,0f,0f,0f)
        dataPlotBC!!.isDoubleTapToZoomEnabled = true
        dataPlotBC!!.invalidate()


    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setInit(entries: List<BarEntry>, enType: String): BarDataSet {
        val barDataSet = BarDataSet(entries, null)
        when (enType) {
            "task" -> {
                barDataSet.valueFormatter = IValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
                    "" + value.toInt().toString()
                }
            }
        }


        barDataSet.color = themeColor(activity, R.attr.colorAccent)
        barDataSet.valueTextSize = 12f
        barDataSet.setDrawValues(true)
        barDataSet.valueTextColor = ContextCompat.getColor(activity!!, R.color.colorBlack)
        barDataSet.formLineWidth = 1f
        barDataSet.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        barDataSet.highLightColor = themeColor(activity, R.attr.colorAccent)
        barDataSet.barBorderWidth = 0f
        barDataSet.isHighlightEnabled = true //bar click

        return barDataSet
    }


    private fun setAdap(){

        val list = functionTask.getTask7NextDay()

        taskRCV.adapter = AdapTask7NextDay(activity!!, list)
        taskRCV.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    }

    inner class AdapTask7NextDay(private val activity: Activity, private var taskList: ArrayList<ModelTask>) : RecyclerView.Adapter<AdapTask7NextDay.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_task_statistics, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.nameTV.text = taskList[position].name

            holder.dateTV.text = formatDate("dd/MMM", Date(taskList[position].dueDate!!))

            val modelCategory = functionCategory.getById(taskList[position].categoryId)

            if(modelCategory.id != null){
                Glide.with(activity).load(modelCategory.image).into(holder.imageCateIV)
                holder.imageCateIV.visibility = View.VISIBLE
            }else{
                holder.imageCateIV.visibility = View.GONE
            }

        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val itemRL = itemView.findViewById<RelativeLayout>(R.id.itemRL)
            val nameTV = itemView.findViewById<TextView>(R.id.nameTV)
            val dateTV = itemView.findViewById<TextView>(R.id.dateTV)
            val imageCateIV = itemView.findViewById<ImageView>(R.id.iconCateIV)

            init {
                itemRL.isClickable = false
            }

        }

        override fun getItemCount(): Int {
            return taskList.size
        }

    }

    data class ModelDataOfWeek(
        var startDate: Date? = null,
        var dateName: String? = "",
        var countTask: Double? = 0.0

    )

}