package com.chillchillapp.gthingstodo.master

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.chillchillapp.gthingstodo.R
import com.chillchillapp.gthingstodo.database.*
import com.chillchillapp.gthingstodo.model.ModelTaskReminder
import java.io.File
import java.util.*

class RepeatHelper(private val activity: Activity){

    private var dbHelper: DBFunctionHelper = DBFunctionHelper(activity, SQLITE_NAME)

    private val TYPE_HOUR = "hour"
    private val TYPE_DAY = "day"
    private val TYPE_WEEK = "week"
    private val TYPE_MONTH = "month"
    private val TYPE_YEAR = "year"
    private val FOLDER_IMAGE = "image"
    private val FOLDER_VIDEO = "video"
    private val FOLDER_AUDIO = "audio"


    fun insertByRepeat(taskId: Long?){

        if(taskId == null){
            Toast.makeText(activity, activity.getString(R.string.fail), Toast.LENGTH_SHORT).show()
            return
        }

        //create new task is done
        val newTask = dbHelper.functionTask.getTaskById(taskId!!)
        newTask.state = 1L
        newTask.completeDate = System.currentTimeMillis()
        newTask.updateDate = System.currentTimeMillis()
        newTask.createDate = System.currentTimeMillis()

        if(dbHelper.functionTask.insert(newTask) != 0L){
            val lastId = dbHelper.functionTask.getLastId()

            //1.copy subtask
            val subTaskList = dbHelper.functionTaskSub.getDataByTaskId(taskId)
            for(m in subTaskList){
                m.taskId = lastId
                m.createDate = System.currentTimeMillis()
                m.updateDate = System.currentTimeMillis()
                dbHelper.functionTaskSub.insert(m)
            }

            //2.copy attach
            val attachList = dbHelper.functionTaskAttach.getDataByTaskId(taskId)
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
            val reminderList = dbHelper.functionReminder.getReminderByTaskId(taskId)
            for (m in reminderList){
                m.taskId = lastId
                m.updateDate = System.currentTimeMillis()
                m.createDate = System.currentTimeMillis()

                dbHelper.functionReminder.insert(m)
            }

            //4.not repeat

        }else{
            Toast.makeText(activity, activity.getString(R.string.fail), Toast.LENGTH_SHORT).show()
        }

        val model = dbHelper.functionTask.getTaskById(taskId!!)
        model.state = 0L
        model.completeDate = null
        model.updateDate = System.currentTimeMillis()

        val reminderList = dbHelper.functionReminder.getReminderByTaskId(model.id)
        for(m in reminderList){
            val choice = ArrayList<ModelTaskReminder>()
            choice.add(ModelTaskReminder(optionId =  "op1", reminderCount = 0, reminderType = Calendar.MINUTE))
            choice.add(ModelTaskReminder(optionId =  "op2", reminderCount = 5, reminderType = Calendar.MINUTE))
            choice.add(ModelTaskReminder(optionId =  "op3", reminderCount = 10, reminderType = Calendar.MINUTE))
            choice.add(ModelTaskReminder(optionId =  "op4", reminderCount = 15, reminderType = Calendar.MINUTE))
            choice.add(ModelTaskReminder(optionId =  "op5", reminderCount = 30, reminderType = Calendar.MINUTE))
            choice.add(ModelTaskReminder(optionId =  "op6", reminderCount = 1, reminderType = Calendar.DATE))
            choice.add(ModelTaskReminder(optionId =  "op7", reminderCount = 2, reminderType = Calendar.DATE))
            choice.forEach { mc ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = model.dueDate!!
                mc.setNotifyTime(cal, model.hour!!, model.minute!!)

                if (mc.notifyTime == m.notifyTime) {
                    m.optionId = mc.optionId
                    m.reminderCount = mc.reminderCount
                    m.reminderType = mc.reminderType
                    return@forEach
                }
            }
        }

        val modelRepeat = dbHelper.functionRepeat.getByTaskId(taskId)
        val calCurrent = Calendar.getInstance()
        val calDueDate = Calendar.getInstance()

        calDueDate.timeInMillis = model.dueDate!!
        if(model.hour != -1 && model.minute != -1){
            calDueDate[Calendar.HOUR_OF_DAY] = model.hour!!
            calDueDate[Calendar.MINUTE] = model.minute!!
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
            calDueDate.add(repeatType!!, modelRepeat.repeatNext!!)

        }while (calCurrent.timeInMillis > calDueDate.timeInMillis)


        //set value
        if(model.hour != -1 && model.minute != -1){
            val hour = calDueDate[Calendar.HOUR_OF_DAY]
            val minute = calDueDate[Calendar.MINUTE]

            model.hour = hour
            model.minute = minute
        }

        calDueDate[Calendar.HOUR_OF_DAY] = 0
        calDueDate[Calendar.MINUTE] = 0
        calDueDate[Calendar.SECOND] = 0
        calDueDate[Calendar.MILLISECOND] = 0

        model.dueDate = calDueDate.timeInMillis
        modelRepeat.programCount = modelRepeat.programCount!! + 1L //is done +1
        modelRepeat.updateDate = System.currentTimeMillis()

        Log.i("fhhhhh", "programCount: " + modelRepeat.programCount)
        Log.i("fhhhhh", "numberOfTime: " + modelRepeat.numberOfRepeat)

        //update subtask
        val subTaskList = dbHelper.functionTaskSub.getDataByTaskId(model.id)
        for (m in subTaskList){
            m.state = 0L
            m.updateDate = System.currentTimeMillis()
            dbHelper.functionTaskSub.update(m)
        }

        Log.i("fhhhhh", "numberOfTime: " + modelRepeat.numberOfRepeat)
        //update reminder
        for (m in reminderList){
            m.setNotifyTime(calDueDate, model.hour!!, model.minute!!)
            dbHelper.functionReminder.update(m)
        }

        //way repeat?
        when {
            modelRepeat.numberOfRepeat == -1L -> { //repeat not know end
                dbHelper.functionTask.update(model)
                dbHelper.functionRepeat.update(modelRepeat)
            }
            modelRepeat.programCount!! == (modelRepeat.numberOfRepeat!! - 1) -> {//repeat know end
                dbHelper.functionTask.update(model)
                dbHelper.functionRepeat.deleteByTaskId(taskId)
            }
            modelRepeat.programCount!! < (modelRepeat.numberOfRepeat!! - 1) -> {//repeat know end
                dbHelper.functionTask.update(model)
                dbHelper.functionRepeat.update(modelRepeat)
            }
        }
    }
}