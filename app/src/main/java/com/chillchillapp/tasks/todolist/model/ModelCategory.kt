package com.chillchillapp.tasks.todolist.model

import android.content.Context
import com.chillchillapp.tasks.todolist.KEY_REMOVE
import com.chillchillapp.tasks.todolist.database.DBFunctionHelper
import com.chillchillapp.tasks.todolist.database.SQLITE_NAME

class ModelCategory(
    var id: Long? = null,
    var name: String? = null,
    var image: ByteArray? = null,
    var priority: Long? = null,
    var createDate: Long? = null,
    var updateDate: Long? = null,
    var status: String? = null

){
    /*fun delete(context: Context, sqlite_name: String = SQLITE_NAME): Int{
        if(id == null){
            return 0
        }

        val dbHelper = DBFunctionHelper(context, sqlite_name)
        val taskList = dbHelper.functionTask.getTaskByCategoryId(id)
        for(m in taskList){
            m.remove(context, sqlite_name)
        }

        dbHelper.functionCategory.delete(id)
        return 1

    }*/

    fun remove(context: Context): Int{
        if(id == null){
            return 0
        }

        val dbHelper = DBFunctionHelper(context, SQLITE_NAME)
        status = KEY_REMOVE
        updateDate = System.currentTimeMillis()

        val taskList = dbHelper.functionTask.getTaskByCategoryId(id)
        for(m in taskList){
            m.remove(context)
        }

        dbHelper.functionCategory.update(this)
        return 1

    }
}