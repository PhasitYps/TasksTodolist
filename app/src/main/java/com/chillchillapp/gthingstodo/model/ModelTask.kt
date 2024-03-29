package com.chillchillapp.gthingstodo.model

import android.content.Context
import com.chillchillapp.gthingstodo.KEY_REMOVE
import com.chillchillapp.gthingstodo.database.DBFunctionHelper
import com.chillchillapp.gthingstodo.database.SQLITE_NAME
import java.io.File

class ModelTask(
    var id: Long? = null, // เขียนบันทึกทั่วไป
    var name: String? = null,
    var categoryId: Long? = null,
    var favorite: Long? = null,
    var state: Long? = null,
    var completeDate: Long? = null,
    var createDate: Long? = null,
    var updateDate: Long? = null,
    var dueDate: Long? = null, //วันครบกำหนด
    var hour: Int? = null,
    var minute: Int? = null,
    var place: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var status: String? = null,
    var typeView: Int? = null
//12
){
    /*fun delete(context: Context, name: String = SQLITE_NAME): Int{

        if(id == null){
            return 0
        }

        val dbHelper = DBFunctionHelper(context, name)

        if(dbHelper.functionTask.delete(id)){
            //1.remove subtask
            dbHelper.functionTaskSub.deleteByTaskID(id!!)

            //2.remove attach
            val attachTaskList = dbHelper.functionTaskAttach.getDataByTaskId(id)
            for(m in attachTaskList){

                File(m.path).delete()
                dbHelper.functionTaskAttach.delete(m.id)
            }
            //3.remove repeat
            val modelRepeat = dbHelper.functionRepeat.getByTaskId(id)
            if(modelRepeat.id != null){
                dbHelper.functionRepeat.delete(modelRepeat.id)
            }

            //4.remove reminder
            dbHelper.functionReminder.deleteByTaskId(id)

            return 1
        }else{
            return 0
        }

    }*/

    fun remove(context: Context): Int{

        if(id == null){
            return 0
        }

        val dbHelper = DBFunctionHelper(context, SQLITE_NAME)
        status = KEY_REMOVE
        updateDate = System.currentTimeMillis()

        if(dbHelper.functionTask.update(this) != 0){
            //1.remove subtask
            dbHelper.functionTaskSub.deleteByTaskID(id!!)

            //2.remove attach
            val attachTaskList = dbHelper.functionTaskAttach.getDataByTaskId(id)
            for(m in attachTaskList){
                File(m.path).delete()
                dbHelper.functionTaskAttach.delete(m.id)
            }
            //3.remove repeat
            dbHelper.functionRepeat.deleteByTaskId(id)

            //4.remove reminder
            dbHelper.functionReminder.deleteByTaskId(id)

            return 1
        }else{
            return 0
        }
    }
}