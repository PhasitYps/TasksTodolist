package com.chillchillapp.tasks.todolist.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import com.chillchillapp.tasks.todolist.model.ModelRepeat

@SuppressLint("Range")
class FunctionRepeat(private var context: Context, private var sqlitePath: String? = null) {

    private var sqLiteData: SQLiteMaster? = null
    private var db: SQLiteDatabase? = null
    private val tableName = TABLE_REPEAT

    init {
        sqLiteData = if(sqlitePath != null)
            SQLiteMaster(context, sqlitePath!!)
        else
            SQLiteMaster(context)
        db = sqLiteData!!.writableDatabase
    }


    fun deleteTable(){
        db!!.delete(tableName, null, null)
    }

    fun createTable(){
        db!!.execSQL(DATABASE_CREATERepeat)
    }

    fun insert(model: ModelRepeat):Boolean{

        val values = ContentValues()
        values.put(COL_TASK_ID, model.taskId)
        values.put(COL_REPEAT_TYPE, model.repeatType)
        values.put(COL_REPEAT_NEXT, model.repeatNext)
        values.put(COL_REPEAT_NUMBER_OF_TIME, model.numberOfRepeat)
        values.put(COL_REPEAT_PROGRAMCOUNT, model.programCount)
        values.put(COL_CREATEDATE, model.createDate)
        values.put(COL_UPDATEDATE, model.updateDate)

        val r = db!!.insert(tableName, null, values)
        return r != 0L
    }

    fun update(model: ModelRepeat): Int{

        val values = ContentValues()
        val id = model.id
        values.put(COL_TASK_ID, model.taskId)
        values.put(COL_REPEAT_TYPE, model.repeatType)
        values.put(COL_REPEAT_NEXT, model.repeatNext)
        values.put(COL_REPEAT_NUMBER_OF_TIME, model.numberOfRepeat)
        values.put(COL_REPEAT_PROGRAMCOUNT, model.programCount)
        //values.put(COL_TASK_DUEDATE, model.dueDate)
        values.put(COL_UPDATEDATE, model.updateDate)

        val r = db!!.update(tableName, values, "$COL_REPEAT_ID = ?", arrayOf(id.toString()))

        return r

    }

    fun delete(repeatID: Long?): Boolean {
        return db!!.delete(tableName, "$COL_REPEAT_ID = ?", arrayOf(repeatID.toString())) > 0
    }

    fun deleteByTaskId(taskId: Long?): Boolean {
        return db!!.delete(tableName, "$COL_TASK_ID = ?", arrayOf(taskId.toString())) > 0
    }


    fun getByTaskId(taskId: Long?): ModelRepeat{

        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_ID like '$taskId'",null)
        val m = ModelRepeat()

        if (cursor.moveToFirst()) {

            m.id = (cursor.getLong(cursor.getColumnIndex(COL_REPEAT_ID)))
            m.taskId = (cursor.getLong(cursor.getColumnIndex(COL_TASK_ID)))
            m.repeatType = (cursor.getString(cursor.getColumnIndex(COL_REPEAT_TYPE)))
            m.repeatNext = (cursor.getInt(cursor.getColumnIndex(COL_REPEAT_NEXT)))
            m.numberOfRepeat = (cursor.getLong(cursor.getColumnIndex(COL_REPEAT_NUMBER_OF_TIME)))
            m.programCount = (cursor.getLong(cursor.getColumnIndex(COL_REPEAT_PROGRAMCOUNT)))
            //m.dueDate = (cursor.getLong(cursor.getColumnIndex(COL_TASK_DUEDATE)))
            m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
            m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))
        }
        return m
    }

    fun getDataList(): ArrayList<ModelRepeat>{
        val list = ArrayList<ModelRepeat>()
        val cursor: Cursor = db!!.query(tableName, null, null, null, null, null, null)
        try {
            while (cursor.moveToNext()) {
                val m = ModelRepeat()
                m.id = (cursor.getLong(cursor.getColumnIndex(COL_REPEAT_ID)))
                m.taskId = (cursor.getLong(cursor.getColumnIndex(COL_TASK_ID)))
                m.repeatType = (cursor.getString(cursor.getColumnIndex(COL_REPEAT_TYPE)))
                m.repeatNext = (cursor.getInt(cursor.getColumnIndex(COL_REPEAT_NEXT)))
                m.numberOfRepeat = (cursor.getLong(cursor.getColumnIndex(COL_REPEAT_NUMBER_OF_TIME)))
                m.programCount = (cursor.getLong(cursor.getColumnIndex(COL_REPEAT_PROGRAMCOUNT)))
                m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
                m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))

                list.add(m)
            }

            return list

        } catch (e: Exception){
            Toast.makeText(context, ""+ e.toString(), Toast.LENGTH_SHORT).show()
            return list
        }
    }

}