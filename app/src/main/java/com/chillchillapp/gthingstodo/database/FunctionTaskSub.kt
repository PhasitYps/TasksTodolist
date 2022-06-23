package com.chillchillapp.gthingstodo.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.chillchillapp.gthingstodo.model.ModelTaskSub

@SuppressLint("Range")
class FunctionTaskSub(private var context: Context, private var sqlitePath: String? = null) {

    private var sqLiteData: SQLiteMaster? = null
    private var db: SQLiteDatabase? = null
    private val tableName = TABLE_TASKSUB

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
        db!!.execSQL(DATABASE_CREATETaskSub)
    }

    fun insert(modelSub: ModelTaskSub):Boolean{

        val values = ContentValues()
        values.put(COL_TASK_ID, modelSub.taskId)
        values.put(COL_CATE_ID, modelSub.categoryId)
        values.put(COL_SUBTASK_TODO, modelSub.todo)
        values.put(COL_SUBTASK_STATE, modelSub.state)
        values.put(COL_SUBTASK_PRIORITY, modelSub.priority)
        values.put(COL_CREATEDATE, modelSub.createDate)
        values.put(COL_UPDATEDATE, modelSub.updateDate)

        val r = db!!.insert(tableName, null, values)
        return r != 0L
    }

    fun update(model: ModelTaskSub): Int{

        val values = ContentValues()
        val id = model.id
        values.put(COL_TASK_ID, model.taskId)
        values.put(COL_CATE_ID, model.categoryId)
        values.put(COL_SUBTASK_TODO, model.todo)
        values.put(COL_SUBTASK_STATE, model.state)
        values.put(COL_SUBTASK_PRIORITY, model.priority)
        values.put(COL_CREATEDATE, model.createDate)
        values.put(COL_UPDATEDATE, model.updateDate)

        val r = db!!.update(tableName, values, "$COL_SUBTASK_ID = ?", arrayOf(id.toString()))
        return r
    }

    fun delete(subtaskId: Long?): Boolean {
        return db!!.delete(tableName, "$COL_SUBTASK_ID = ?", arrayOf(subtaskId.toString())) > 0
    }

    fun deleteByTaskID(taskId: Long): Boolean{
        return db!!.delete(tableName, "$COL_TASK_ID = ?", arrayOf(taskId.toString())) > 0
    }


    fun getDataByTaskId(taskId: Long?): ArrayList<ModelTaskSub>{
        val list = ArrayList<ModelTaskSub>()

        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_ID like '$taskId'",null)

        while (cursor.moveToNext()) {

            val m = ModelTaskSub()
            m.id = cursor.getLong(cursor.getColumnIndex(COL_SUBTASK_ID))
            m.taskId = cursor.getLong(cursor.getColumnIndex(COL_TASK_ID))
            m.categoryId = cursor.getLong(cursor.getColumnIndex(COL_CATE_ID))
            m.todo = cursor.getString(cursor.getColumnIndex(COL_SUBTASK_TODO))
            m.state = (cursor.getLong(cursor.getColumnIndex(COL_SUBTASK_STATE)))
            m.priority = (cursor.getLong(cursor.getColumnIndex(COL_SUBTASK_PRIORITY)))
            m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
            m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))

            list.add(m)
        }

        return list
    }

    fun getDataList(): ArrayList<ModelTaskSub>{

        val cursor: Cursor = db!!.query(TABLE_TASKSUB, null, null, null, null, null, null)
        return dataQuryList(cursor)
    }

    private fun dataQuryList(cursor: Cursor): ArrayList<ModelTaskSub>{

        val list = ArrayList<ModelTaskSub>()

        while (cursor.moveToNext()) {

            val m = ModelTaskSub()
            m.id = cursor.getLong(cursor.getColumnIndex(COL_SUBTASK_ID))
            m.taskId = cursor.getLong(cursor.getColumnIndex(COL_TASK_ID))
            m.categoryId = cursor.getLong(cursor.getColumnIndex(COL_CATE_ID))
            m.todo = cursor.getString(cursor.getColumnIndex(COL_SUBTASK_TODO))
            m.state = (cursor.getLong(cursor.getColumnIndex(COL_SUBTASK_STATE)))
            m.priority = (cursor.getLong(cursor.getColumnIndex(COL_SUBTASK_PRIORITY)))
            m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
            m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))

            list.add(m)
        }

        return list
    }

}