package com.chillchillapp.tasks.todolist.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import com.chillchillapp.tasks.todolist.KEY_ACTIVE
import com.chillchillapp.tasks.todolist.model.ModelTask
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("Range")
class FunctionTask(private var context: Context, private var sqlitePath: String? = null) {

    private var sqLiteData: SQLiteMaster? = null
    private var db: SQLiteDatabase? = null
    private val tableName = TABLE_TASK

    init {
        sqLiteData = if(sqlitePath != null)
            SQLiteMaster(context, sqlitePath!!)
        else
            SQLiteMaster(context)

        db = sqLiteData?.writableDatabase
    }


    fun open(){
    }

    fun insert(model: ModelTask):Long{

        //Log.i("fewfb", "ModelTask: " + model.favorite)

        val values = ContentValues()
        values.put(COL_CATE_ID, model.categoryId)
        values.put(COL_TASK_NAME, model.name)
        values.put(COL_TASK_STATE, model.state)
        values.put(COL_TASK_FAVORITE, model.favorite)
        values.put(COL_TASK_DUEDATE, model.dueDate)
        values.put(COL_TASK_HOUR, model.hour)
        values.put(COL_TASK_MINUTE, model.minute)
        values.put(COL_TASK_COMPLETEDATE, model.completeDate)
        values.put(COL_CREATEDATE, model.createDate)
        values.put(COL_UPDATEDATE, model.updateDate)
        values.put(COL_STATUS, model.status)

        val r = db!!.insert(tableName, null, values)

        return r
    }

    fun update(model: ModelTask): Int{

        val values = ContentValues()
        val id = model.id
        values.put(COL_CATE_ID, model.categoryId)
        values.put(COL_TASK_NAME, model.name)
        values.put(COL_TASK_STATE, model.state)
        values.put(COL_TASK_FAVORITE, model.favorite)
        values.put(COL_TASK_DUEDATE, model.dueDate)
        values.put(COL_TASK_HOUR, model.hour)
        values.put(COL_TASK_MINUTE, model.minute)
        values.put(COL_TASK_COMPLETEDATE, model.completeDate)
        values.put(COL_CREATEDATE, model.createDate)
        values.put(COL_UPDATEDATE, model.updateDate)
        values.put(COL_STATUS, model.status)

        val r = db!!.update(tableName, values, "$COL_TASK_ID = ?", arrayOf(id.toString()))

        return r

    }

    fun updateById(taskId: Long?, model: ModelTask): Int{

        val values = ContentValues()
        values.put(COL_CATE_ID, model.categoryId)
        values.put(COL_TASK_NAME, model.name)
        values.put(COL_TASK_STATE, model.state)
        values.put(COL_TASK_FAVORITE, model.favorite)
        values.put(COL_TASK_DUEDATE, model.dueDate)
        values.put(COL_TASK_HOUR, model.hour)
        values.put(COL_TASK_MINUTE, model.minute)
        values.put(COL_TASK_COMPLETEDATE, model.completeDate)
        values.put(COL_CREATEDATE, model.createDate)
        values.put(COL_UPDATEDATE, model.updateDate)
        values.put(COL_STATUS, model.status)

        val r = db!!.update(tableName, values, "$COL_TASK_ID = ?", arrayOf(taskId.toString()))

        return r

    }

    fun delete(taskId: Long?): Boolean {
        return db!!.delete(tableName, "$COL_TASK_ID = ?", arrayOf(taskId.toString())) > 0
    }

    fun getTaskById(taskID: Long): ModelTask{

        Log.i("gewfw", "taskID: ${taskID}")

        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_ID like '$taskID' and $COL_STATUS == '$KEY_ACTIVE'",null)
        val m = ModelTask()

        if(cursor.moveToFirst()){

            m.id = (cursor.getLong(cursor.getColumnIndex(COL_TASK_ID)))
            m.categoryId = (cursor.getLong(cursor.getColumnIndex(COL_CATE_ID)))
            m.favorite = (cursor.getLong(cursor.getColumnIndex(COL_TASK_FAVORITE)))
            m.name = (cursor.getString(cursor.getColumnIndex(COL_TASK_NAME)))
            m.state = cursor.getLong(cursor.getColumnIndex(COL_TASK_STATE))
            m.dueDate = (cursor.getLong(cursor.getColumnIndex(COL_TASK_DUEDATE)))
            m.hour = (cursor.getInt(cursor.getColumnIndex(COL_TASK_HOUR)))
            m.minute = (cursor.getInt(cursor.getColumnIndex(COL_TASK_MINUTE)))
            m.completeDate = (cursor.getLong(cursor.getColumnIndex(COL_TASK_COMPLETEDATE)))
            m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
            m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))
            m.status = (cursor.getString(cursor.getColumnIndex(COL_STATUS)))

        }

        Log.i("gewfw", "m: ${m.id}")

        return m
    }

    fun getLastId(): Long{

        val cursor: Cursor = db!!.rawQuery("select max($COL_TASK_ID) from $tableName;", null)
        var lastID: Long? = null

        if(cursor.moveToFirst()){
            lastID = cursor.getLong(0)
            //Log.i("gewfw", "getLastId: $lastID")
        }
        return lastID!!

    }

    fun getTaskToday(): ArrayList<ModelTask>{

        val startCal = Calendar.getInstance()
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)

        val endCal = Calendar.getInstance()
        endCal.timeInMillis = startCal.timeInMillis
        endCal.add(Calendar.DATE, 1)

        val start = startCal.timeInMillis
        val end = endCal.timeInMillis

        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_STATE == 0 and $COL_STATUS == '$KEY_ACTIVE'" +
                " or $COL_TASK_COMPLETEDATE < $end and $COL_TASK_COMPLETEDATE >= $start and $COL_STATUS == '$KEY_ACTIVE' ORDER BY $COL_CREATEDATE ASC",null)

        return dataQueryList(cursor)

    }

    fun getTaskByCategoryId(cateId: Long?): ArrayList<ModelTask>{

        val cursor = db!!.rawQuery("select * from $tableName where $COL_CATE_ID == $cateId and $COL_STATUS == '$KEY_ACTIVE'",null)
        return dataQueryList(cursor)
    }

    fun getTaskFromDate(date: Long): ArrayList<ModelTask>{

        val startCal = Calendar.getInstance()
        startCal.timeInMillis = date
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)

        val endCal = Calendar.getInstance()
        endCal.timeInMillis = startCal.timeInMillis
        endCal.add(Calendar.DATE, 1)

        val start = startCal.timeInMillis
        val end = endCal.timeInMillis

        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_DUEDATE >= $start and $COL_TASK_DUEDATE < $end and $COL_STATUS == '$KEY_ACTIVE'",null)
        return dataQueryList(cursor)
    }

    fun getDueDateFromMonth(date: Date): ArrayList<Long>{
        val list = ArrayList<Long>()

        val startCal = Calendar.getInstance()
        startCal.timeInMillis = date.time
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        startCal.set(Calendar.DATE, 1)


        val endCal = Calendar.getInstance()
        endCal.timeInMillis = startCal.timeInMillis
        endCal.add(Calendar.MONTH, 1)

        Log.i("gewghre", "startCal: " + startCal.get(Calendar.DATE)+"/"+startCal.get(Calendar.MONTH) + " or " + formatDate("dd/MMM", startCal.time))
        Log.i("gewghre", "endCal: " + endCal.get(Calendar.DATE)+"/"+endCal.get(Calendar.MONTH) + " or " + formatDate("dd/MMM", endCal.time))

        val start = startCal.timeInMillis
        val end = endCal.timeInMillis

        //Log.i("gewghre", "endCal: " + start)
        //Log.i("gewghre", "endCal: " + end)

        val cursor = db!!.rawQuery("select $COL_TASK_DUEDATE from $tableName where $COL_TASK_DUEDATE >= $start and $COL_TASK_DUEDATE < $end and $COL_STATUS == '$KEY_ACTIVE'",null)

        try {
            while (cursor.moveToNext()) {

                val dueDate = (cursor.getLong(cursor.getColumnIndex(COL_TASK_DUEDATE)))
                list.add(dueDate)
            }

        } catch (e: Exception){
            Toast.makeText(context, ""+ e.toString(), Toast.LENGTH_SHORT).show()
        }
        return list
    }

    fun getTask7NextDay(): ArrayList<ModelTask>{

        val startCal = Calendar.getInstance()
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        startCal.add(Calendar.DATE, 1)

        val endCal = Calendar.getInstance()
        endCal.timeInMillis = startCal.timeInMillis
        endCal.add(Calendar.DATE, 7)

        val start = startCal.timeInMillis
        val end = endCal.timeInMillis

        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_DUEDATE >= $start and $COL_TASK_DUEDATE < $end and $COL_STATUS == '$KEY_ACTIVE'",null)

        return dataQueryList(cursor)
    }

    fun getDataList(): ArrayList<ModelTask>{

        val cursor: Cursor = db!!.query(tableName, null, null, null, null, null, null)
        return dataQueryList(cursor)

    }

    fun getFavoriteList(): ArrayList<ModelTask>{

        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_FAVORITE like 1 and $COL_STATUS == '$KEY_ACTIVE'",null)
        return dataQueryList(cursor)

    }

    fun getHistory(): ArrayList<ModelTask>{
        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_STATE like 1 and $COL_STATUS == '$KEY_ACTIVE'",null)
        return dataQueryList(cursor)
    }

    fun getDataByCreateDate(createDate: Long): ArrayList<ModelTask>{
        val cursor = db!!.rawQuery("select * from $tableName where $COL_CREATEDATE like '$createDate'", null)
        return dataQueryList(cursor)
    }

    fun query(sql: String): ArrayList<ModelTask>{
        val cursor = db!!.rawQuery(sql, null)
        return dataQueryList(cursor)
    }

    private val DATABASE_BookTaskTodoListAttach = "BookTaskTodoListAttach"
    fun getModelExceptList(dbAttach: SQLiteDatabase): ArrayList<ModelTask>{

        dbAttach.beginTransaction()

        val cursor = dbAttach!!.rawQuery(
            "SELECT * " +
                    /*"$COL_UPDATEDATE, " +
                    "$COL_CREATEDATE, " +
                    "$COL_STATUS " +*/
                    "FROM $DATABASE_BookTaskTodoListAttach.$TABLE_TASK " +
                    "EXCEPT " +
                    "SELECT * " +
                    /*"$COL_UPDATEDATE, " +
                    "$COL_CREATEDATE, " +
                    "$COL_STATUS " +*/
                    "FROM $TABLE_TASK"
            , null)

        dbAttach.endTransaction()

        return dataQueryList(cursor)
    }

    private fun dataQueryList(cursor: Cursor): ArrayList<ModelTask>{
        val list = ArrayList<ModelTask>()

        try {
            while (cursor.moveToNext()) {
                val m = ModelTask()
                m.id = (cursor.getLong(cursor.getColumnIndex(COL_TASK_ID)))
                m.categoryId = (cursor.getLong(cursor.getColumnIndex(COL_CATE_ID)))
                m.favorite = (cursor.getLong(cursor.getColumnIndex(COL_TASK_FAVORITE)))
                m.name = (cursor.getString(cursor.getColumnIndex(COL_TASK_NAME)))
                m.state = cursor.getLong(cursor.getColumnIndex(COL_TASK_STATE))
                m.dueDate = (cursor.getLong(cursor.getColumnIndex(COL_TASK_DUEDATE)))
                m.hour = (cursor.getInt(cursor.getColumnIndex(COL_TASK_HOUR)))
                m.minute = (cursor.getInt(cursor.getColumnIndex(COL_TASK_MINUTE)))
                m.completeDate = (cursor.getLong(cursor.getColumnIndex(COL_TASK_COMPLETEDATE)))
                m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
                m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))
                m.status = (cursor.getString(cursor.getColumnIndex(COL_STATUS)))

                list.add(m)
            }

            return list

        } catch (e: Exception){
            Log.d("hhjjjjjhhhhh", "function task dataQuryList: $e")
            return list
        }
    }

    private fun formatDate(formatStr: String, day: Date): String{
        val format = SimpleDateFormat(formatStr)
        val dateStr = format.format(day)
        return dateStr
    }
}