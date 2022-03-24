package com.chillchillapp.tasks.todolist.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chillchillapp.tasks.todolist.*
import com.chillchillapp.tasks.todolist.`interface`.Communicator
import com.chillchillapp.tasks.todolist.adapter.AdapTask
import com.chillchillapp.tasks.todolist.database.FunctionCategory
import com.chillchillapp.tasks.todolist.database.FunctionTaskSub
import com.chillchillapp.tasks.todolist.database.FunctionTask
import com.chillchillapp.tasks.todolist.model.ModelCategory
import com.chillchillapp.tasks.todolist.model.ModelTask
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_task.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
@SuppressLint("UseRequireInsteadOfGet")
class MenuTaskFragment : BaseFragment(R.layout.fragment_task)  {

    private var categoryId = ""
    private var communicator: Communicator? = null

    private var categoryDisplayList = ArrayList<ModelCategory>()
    private var allTaskList = ArrayList<ModelTask>()
    private var todayTaskIsNotDoneList = ArrayList<ModelTask>()
    private var otherTaskList = ArrayList<ModelTask>()

    private lateinit var functionCategory: FunctionCategory
    private lateinit var functionTask: FunctionTask
    private lateinit var functionTaskSub: FunctionTaskSub

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBase()

        init()
        database()
        addChipCategoryView()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        checkUpdate()
    }

    private fun checkUpdate(){
        val update = sp?.getLong("update", 0)

        if(update != 0L){
            Handler().postDelayed( {
                uploadData()
            }, 500)

            editor?.putLong("update", 0)
            editor?.commit()
        }
    }

    private fun init(){

        functionCategory = FunctionCategory(activity!!)
        functionTask = FunctionTask(activity!!)
        functionTaskSub = FunctionTaskSub(activity!!)

        functionTask.open()
        functionCategory.open()

        historyTV.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        categoryId = arguments!!.getString("categoryId", "")
        communicator = activity as Communicator

        val displayTaskOther = sp!!.getInt("displayTaskOther", 1)
        when(displayTaskOther){
            1->{
                otherTaskRCV.visibility = View.VISIBLE
                iconDisplayOtherIV.setImageDrawable(getDrawable(context!!, R.drawable.ic_sort_up_filled))
            }
            0->{
                otherTaskRCV.visibility = View.GONE
                iconDisplayOtherIV.setImageDrawable(getDrawable(context!!, R.drawable.ic_sort_down_filled))
            }
        }
    }


    private fun database(){
        allTaskList = functionTask.getTaskToday()

        setTaskAdap()
        setTaskOtherAdap()
    }

    private fun updateTaskUI(){
        setData()
        setView()
    }

    private var numTaskIsDone = 0
    private var numTaskIsDoneNot = 0
    private fun setData(){

        val calStart = Calendar.getInstance()
        calStart[Calendar.MILLISECOND] = 0
        calStart[Calendar.SECOND] = 0
        calStart[Calendar.MINUTE] = 0
        calStart[Calendar.HOUR_OF_DAY] = calStart.getActualMinimum(Calendar.HOUR_OF_DAY)

        val calEnd = Calendar.getInstance()
        calEnd[Calendar.MILLISECOND] = calEnd.getActualMaximum(Calendar.MILLISECOND)
        calEnd[Calendar.SECOND] = 59
        calEnd[Calendar.MINUTE] = 59
        calEnd[Calendar.HOUR_OF_DAY] = calEnd.getActualMaximum(Calendar.HOUR_OF_DAY)

        val startDay = calStart.timeInMillis
        val endDay = calEnd.timeInMillis

        todayTaskIsNotDoneList.clear()
        otherTaskList.clear()

        var currentTaskList = ArrayList<ModelTask>()
        if(categoryId.toInt() >= 0){
            val taskCateList = allTaskList.filter { it.categoryId.toString() == categoryId } as ArrayList<ModelTask>

            currentTaskList = taskCateList.filter {it.dueDate!! <= endDay && it.dueDate!! > 0} as ArrayList<ModelTask>

            todayTaskIsNotDoneList.addAll(taskCateList.filter {it.state == 0L && it.dueDate!! <= endDay && it.dueDate!! > 0})
            otherTaskList.addAll(taskCateList.filter {it.state == 1L || it.dueDate!! > endDay || it.dueDate!! == 0L})
        }else{

            val taskInCategory = ArrayList<ModelTask>()
            for(m in categoryDisplayList){
                val categoryID = m.id
                taskInCategory.addAll(allTaskList.filter { it.categoryId == categoryID })
            }

            currentTaskList.addAll(taskInCategory.filter {it.dueDate!! <= endDay && it.dueDate!! > 0})
            todayTaskIsNotDoneList.addAll(taskInCategory.filter {it.state == 0L && it.dueDate!! <= endDay && it.dueDate!! > 0})
            otherTaskList.addAll(taskInCategory.filter { it.state == 1L || it.dueDate!! > endDay || it.dueDate!! == 0L })
        }

        numTaskIsDone = currentTaskList.filter { it.state == 1L}.size
        numTaskIsDoneNot = currentTaskList.filter { it.state == 0L }.size
    }

    private fun setView(){
        otherTaskList.sortBy { it.state }

        textTodayTV.visibility = if(todayTaskIsNotDoneList.size == 0){
            View.GONE
        }else{
            View.VISIBLE
        }
        textOtherLL.visibility = if(otherTaskList.size == 0){
            View.GONE
        }else{
            View.VISIBLE
        }

        bgNotTaskLL.visibility = if(otherTaskList.size == 0 && todayTaskIsNotDoneList.size == 0){
            View.VISIBLE
        }else{
            View.GONE
        }

        historyTV.visibility = if(otherTaskList.size == 0 && todayTaskIsNotDoneList.size == 0){
            View.GONE
        }else{
            View.VISIBLE
        }

        taskRCV.adapter!!.notifyDataSetChanged()
        otherTaskRCV.adapter!!.notifyDataSetChanged()

        numDoneTV.text = numTaskIsDone.toString()
        numNotDoneTV.text = numTaskIsDoneNot.toString()
    }

    private fun uploadData(){

        allTaskList = functionTask.getTaskToday()

        try {
            addChipCategoryView()
        }catch (e: Exception){

        }
        updateTaskUI()

    }

    private fun setTaskAdap(){

        val adapter = AdapTask(activity!! , todayTaskIsNotDoneList)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        taskRCV.adapter = adapter
        taskRCV.layoutManager = layoutManager

        adapter.setOnUpdateStateListener(object : AdapTask.OnUpdateTaskListener {
            override fun onUpdateStateListenner() {
                updateTaskUI()
            }

            override fun onUpdateRepeatListener() {
                uploadData()
            }
        })

    }

    private fun setTaskOtherAdap(){
        val adapter = AdapTask(activity!! , otherTaskList)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        otherTaskRCV.adapter = adapter
        otherTaskRCV.layoutManager = layoutManager

        adapter.setOnUpdateStateListener(object : AdapTask.OnUpdateTaskListener {
            override fun onUpdateStateListenner() {
                updateTaskUI()
            }

            override fun onUpdateRepeatListener() {
                uploadData()
            }
        })
    }

    private fun setEvent(){

        categoryRL.setOnClickListener {
            val intent = Intent(activity, CategoryActivity::class.java)
            activity!!.startActivity(intent)

        }

        addTaskTV.setOnClickListener {
            val intent = Intent(activity, InputTasksActivity::class.java)
            intent.putExtra(KEY_FUNCTION, KEY_INSERT)
            startActivity(intent)
        }

        historyTV.setOnClickListener {
            val intent = Intent(activity, HistoryActivity::class.java)
            startActivity(intent)
        }

        textOtherLL.setOnClickListener {

            when(otherTaskRCV.visibility){
                View.GONE->{
                    otherTaskRCV.visibility = View.VISIBLE
                    iconDisplayOtherIV.setImageDrawable(getDrawable(context!!, R.drawable.ic_sort_up_filled))
                    editor!!.putInt("displayTaskOther", 1)
                    editor!!.commit()
                }
                View.VISIBLE->{
                    otherTaskRCV.visibility = View.GONE
                    iconDisplayOtherIV.setImageDrawable(getDrawable(context!!, R.drawable.ic_sort_down_filled))
                    editor!!.putInt("displayTaskOther", 0)
                    editor!!.commit()
                }
            }
        }

    }

    private var selectChipId = 0

    private fun addChipCategoryView() {
        var indexSelect = 0

        categoryCG?.removeAllViews()
        categoryDisplayList.clear()
        categoryDisplayList.add(ModelCategory(-1, getString(R.string.all), null, -1, 1, null, null))
        categoryDisplayList.addAll(functionCategory.getCategoryDisplay())

        categoryDisplayList.sortBy { it.priority!!.toInt() }

        for (i in categoryDisplayList.indices) {
            val chip = Chip(activity!!)
            chip.setChipBackgroundColorResource(R.color.selector_choice_state)
            val colors = ContextCompat.getColorStateList(activity!!, R.color.selector_text_state)
            chip.setTextColor(colors)
            chip.text = categoryDisplayList[i].name
            chip.chipIconSize = 50f

            Glide.with(this).asBitmap().apply(RequestOptions.centerCropTransform()).load(categoryDisplayList[i].image)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                        circularBitmapDrawable.isCircular = false
                        chip.chipIcon = circularBitmapDrawable
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            chip.isCheckedIconVisible = false
            chip.isCloseIconVisible = false
            chip.isClickable = true
            chip.isCheckable = true
            chip.tag = categoryDisplayList[i].id
            categoryCG.addView(chip)

            categoryCG.setOnCheckedChangeListener { chipGroup, chipId ->

                var chipSelect: Chip? = chipGroup.findViewById(chipId)

                if (chipSelect != null) {
                    var model = categoryDisplayList.single { it.id == chipSelect!!.tag }
                    categoryId = model.id.toString()
                    communicator!!.OnSelectCategory(categoryId)

                    selectChipId = chipId
                } else {
                    chipSelect = chipGroup.findViewById(selectChipId)
                    chipSelect!!.isChecked = true

                }

                updateTaskUI()
            }

            if (categoryId == categoryDisplayList[i].id.toString() ) {
                indexSelect = i
            }
        }

        if (categoryDisplayList.size > 0) {
            (categoryCG!!.getChildAt(indexSelect) as Chip).apply {
                isChecked = true
                selectChipId = id
            }
        }
    }


}