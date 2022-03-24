package com.chillchillapp.tasks.todolist.master

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.database.*
import java.io.File
import java.util.*

class RepeatHelper(private val activity: Activity){

    private var dbHelper: DBFunctionHelper = DBFunctionHelper(activity, SQLITE_NAME)

    private val TYPE_HOUR = 0
    private val TYPE_DAY = 1
    private val TYPE_WEEK = 2
    private val TYPE_MONTH = 3
    private val TYPE_YEAR = 4
    private val FOLDER_IMAGE = "image"
    private val FOLDER_VIDEO = "video"
    private val FOLDER_AUDIO = "audio"


    fun copyTask(taskId: Long?){

        if(taskId == null){
            Toast.makeText(activity, activity.getString(R.string.fail), Toast.LENGTH_SHORT).show()
            return
        }

        val newTask = dbHelper.functionTask.getTaskById(taskId!!)
        newTask.state = 1L
        newTask.completeDate = System.currentTimeMillis()
        newTask.updateDate = System.currentTimeMillis()
        newTask.createDate = System.currentTimeMillis()

        if(dbHelper.functionTask.insert(newTask) != 0L){
            val lastId = dbHelper.functionTask.getLastId()

            //1.copy subtask
            val subTaskList = dbHelper.functionTaskSub.getDataByTaskId(newTask.id)
            for(m in subTaskList){
                m.taskId = lastId
                m.createDate = System.currentTimeMillis()
                m.updateDate = System.currentTimeMillis()
                dbHelper.functionTaskSub.insert(m)
            }

            //2.copy attach
            val attachList = dbHelper.functionTaskAttach.getDataByTaskId(newTask.id)
            for(m in attachList){
                m.createDate = System.currentTimeMillis()
                m.updateDate = System.currentTimeMillis()
                m.taskId = lastId

                val folder = File(activity.filesDir, m.type)

                when(m.type){
                    FOLDER_IMAGE->{
                        val targetFile = File(folder, System.currentTimeMillis().toString()+".jpg")
                        File(m.path).copyTo(targetFile)
                        m.name = targetFile.name
                        m.path = targetFile.path
                    }
                    FOLDER_VIDEO->{
                        val targetFile = File(folder, System.currentTimeMillis().toString()+".mp4")
                        File(m.path).copyTo(targetFile)
                        m.name = targetFile.name
                        m.path = targetFile.path
                    }
                    FOLDER_AUDIO->{
                        val targetFile = File(folder, System.currentTimeMillis().toString()+".mp3")
                        File(m.path).copyTo(targetFile)
                        m.name = targetFile.name
                        m.path = targetFile.path
                    }
                }

                dbHelper.functionTaskAttach.insert(m)

            }

            //3.copy reminder
            val reminderList = dbHelper.functionReminder.getReminderByTaskId(newTask.id)
            for (m in reminderList){
                m.taskId = lastId
                m.updateDate = System.currentTimeMillis()
                m.createDate = System.currentTimeMillis()

                val cal = Calendar.getInstance()
                cal.timeInMillis = newTask.dueDate!!
                m.setTime(cal, newTask.hour!!, newTask.minute!!)

                dbHelper.functionReminder.insert(m)
            }

            //4.not repeat


        }else{
            Toast.makeText(activity, activity.getString(R.string.fail), Toast.LENGTH_SHORT).show()
        }
    }

    fun setUpdateRepeat(taskId: Long?){

        if(taskId == null){
            Toast.makeText(activity, activity.getString(R.string.fail), Toast.LENGTH_SHORT).show()
            return
        }

        val model = dbHelper.functionTask.getTaskById(taskId!!)

        if(model.id == null){
            Toast.makeText(activity, activity.getString(R.string.fail), Toast.LENGTH_SHORT).show()
            return
        }
        model.state = 0L
        model.completeDate = null
        model.updateDate = System.currentTimeMillis()

        val modelRepeat = dbHelper.functionRepeat.getByTaskId(taskId)

        val calCurrent = Calendar.getInstance()
        val cal = Calendar.getInstance()

        cal.timeInMillis = model.dueDate!!
        if(model.hour != -1 && model.minute != -1){
            cal[Calendar.HOUR_OF_DAY] = model.hour!!
            cal[Calendar.MINUTE] = model.minute!!
        }
        var repeatType: Int? = when(modelRepeat.repeatType){
            TYPE_HOUR-> {
                Calendar.HOUR_OF_DAY
            }
            TYPE_DAY-> {
                Calendar.DATE
            }
            TYPE_WEEK-> {
                Calendar.WEEK_OF_YEAR
            }
            TYPE_MONTH-> {
                Calendar.MONTH
            }
            TYPE_YEAR-> {
                Calendar.YEAR
            }
            else->null
        }

        do {
            cal.add(repeatType!!, modelRepeat.repeatNext!!)

        }while (calCurrent.timeInMillis > cal.timeInMillis)

        var calDueDate = Calendar.getInstance()

        //set value
        calDueDate.timeInMillis = cal.timeInMillis
        calDueDate[Calendar.HOUR_OF_DAY] = 0
        calDueDate[Calendar.MINUTE] = 0
        calDueDate[Calendar.SECOND] = 0
        calDueDate[Calendar.MILLISECOND] = 0
        if(model.hour != -1 && model.minute != -1){
            val hour = cal[Calendar.HOUR_OF_DAY]
            val minute = cal[Calendar.MINUTE]

            model.hour = hour
            model.minute = minute
        }

        model.dueDate = cal.timeInMillis
        modelRepeat.programCount = modelRepeat.programCount!! + 1L
        modelRepeat.updateDate = System.currentTimeMillis()

        Log.i("fhhhhh", "programCount: " + modelRepeat.programCount)
        Log.i("fhhhhh", "numberOfTime: " + modelRepeat.numberOfTime)

        //update value
        val subTaskList = dbHelper.functionTaskSub.getDataByTaskId(model.id)
        for (m in subTaskList){
            m.state = 0L
            m.updateDate = System.currentTimeMillis()
            dbHelper.functionTaskSub.update(m)
        }

        when {
            modelRepeat.numberOfTime == 0L -> { //repeat not know end
                dbHelper.functionTask.update(model)
                dbHelper.functionRepeat.update(modelRepeat)

            }
            modelRepeat.programCount!! < modelRepeat.numberOfTime!! -> {//repeat know end
                dbHelper.functionTask.update(model)
                dbHelper.functionRepeat.update(modelRepeat)
            }
            else -> {//repeat finish
                model.remove(activity)
            }
        }

    }

}