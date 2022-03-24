package com.chillchillapp.tasks.todolist.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.chillchillapp.tasks.todolist.model.ModelTaskReminder
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("Range")
class FunctionTaskReminder(private var context: Context, private var sqlitePath: String? = null) {

    private var sqLiteData: SQLiteMaster? = null
    private var db: SQLiteDatabase? = null
    private val tableName = TABLE_REMINDER

    init {
        sqLiteData = if(sqlitePath != null)
            SQLiteMaster(context, sqlitePath!!)
        else
            SQLiteMaster(context)
        db = sqLiteData!!.writableDatabase
    }

    fun insert(model: ModelTaskReminder):Long{

        val values = ContentValues()

        values.put(COL_TASK_ID, model.taskId)
        values.put(COL_REMINDER_OPTION, model.optionId)
        values.put(COL_REMINDER_TYPE, model.reminderType)
        values.put(COL_REMINDER_COUNT, model.reminderCount)
        values.put(COL_REMINDER_BASETIME, model.baseTime)
        values.put(COL_UPDATEDATE, model.updateDate)
        values.put(COL_CREATEDATE, model.createDate)

        val r = db!!.insert(tableName, null, values)

        return r
    }

    fun update(model: ModelTaskReminder){

        val values = ContentValues()
        val id = model.id
        values.put(COL_TASK_ID, model.taskId)
        values.put(COL_REMINDER_OPTION, model.optionId)
        values.put(COL_REMINDER_TYPE, model.reminderType)
        values.put(COL_REMINDER_COUNT, model.reminderCount)
        values.put(COL_REMINDER_BASETIME, model.baseTime)
        values.put(COL_UPDATEDATE, model.updateDate)
        values.put(COL_CREATEDATE, model.createDate)

        db!!.update(tableName, values, "$COL_REMINDER_ID = ?", arrayOf(id.toString()))

    }

    fun deleteByTaskId(taskId: Long?): Int{
        val r = db!!.delete(tableName, "$COL_TASK_ID = ?", arrayOf(taskId.toString()))
        return r
    }

    fun delete(model: ModelTaskReminder): Int{
        val r = db!!.delete(tableName, "$COL_REMINDER_ID = ?", arrayOf(model.id.toString()))
        return r
    }

    fun getReminder(): ArrayList<ModelTaskReminder>{
        val calStart = Calendar.getInstance()
        val calEnd = Calendar.getInstance()
        calEnd.add(Calendar.DATE, 1)

        val cursor = db!!.rawQuery("select * from $tableName where $COL_REMINDER_BASETIME >= ${calStart.timeInMillis} and $COL_REMINDER_BASETIME < ${calEnd.timeInMillis}",null)
        return query(cursor)
    }

    fun getDataList(): ArrayList<ModelTaskReminder>{
        val cursor: Cursor = db!!.query(tableName, null, null, null, null, null, null)
        return query(cursor)
    }

    fun getReminderCurrent(): ArrayList<ModelTaskReminder>{
        val cal = Calendar.getInstance()
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val cursor = db!!.rawQuery("select * from $tableName where $COL_REMINDER_BASETIME like ${cal.timeInMillis}",null)
        return query(cursor)
    }

    fun getReminderByTaskId(taskId: Long?): ArrayList<ModelTaskReminder>{
        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_ID like '$taskId'",null)
        return query(cursor)
    }

    private fun query(cursor: Cursor): ArrayList<ModelTaskReminder>{
        val list = ArrayList<ModelTaskReminder>()
        try {
            while (cursor.moveToNext()) {

                val m = ModelTaskReminder()
                m.id = (cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)))
                m.taskId = cursor.getLong(cursor.getColumnIndex(COL_TASK_ID))
                m.optionId = cursor.getString(cursor.getColumnIndex(COL_REMINDER_OPTION))
                m.reminderType = (cursor.getInt(cursor.getColumnIndex(COL_REMINDER_TYPE)))
                m.reminderCount = (cursor.getLong(cursor.getColumnIndex(COL_REMINDER_COUNT)))
                m.baseTime = cursor.getLong(cursor.getColumnIndex(COL_REMINDER_BASETIME))
                m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
                m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))

                list.add(m)
            }

            return list

        } catch (e: Exception){
            return list
        }
    }


}