package com.chillchillapp.tasks.todolist.model

import android.net.Uri
import java.util.*

class ModelTaskReminder (
    var id: Long? = null,
    var taskId: Long? = null,
    var optionId: String? = null,
    var reminderCount: Long? = null,
    var reminderType: Int? = null,
    var baseTime: Long? = null,
    var updateDate: Long? = null,
    var createDate: Long? = null
){
    fun setTime(calDueDate: Calendar, h: Int, m: Int){
        val calNotify = Calendar.getInstance()
        calNotify.timeInMillis = calDueDate.timeInMillis
        calNotify.set(Calendar.HOUR_OF_DAY, h)
        calNotify.set(Calendar.MINUTE, m)
        calNotify.set(Calendar.SECOND, 0)
        calNotify.set(Calendar.MILLISECOND, 0)
        calNotify.add(reminderType!!, -(reminderCount)!!.toInt())

        this.baseTime = calNotify.timeInMillis
    }
}