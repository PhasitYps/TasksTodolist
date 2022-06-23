package com.chillchillapp.gthingstodo.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.chillchillapp.gthingstodo.model.ModelTaskAttach
import kotlin.collections.ArrayList

@SuppressLint("Range")
class FunctionTaskAttach(private var context: Context, private var sqlitePath: String? = null) {

    private var sqLiteData: SQLiteMaster? = null
    private var db: SQLiteDatabase? = null
    private val tableName = TABLE_TASKATTACH

    init {
        sqLiteData = if(sqlitePath != null)
            SQLiteMaster(context, sqlitePath!!)
        else
            SQLiteMaster(context)

        db = sqLiteData?.writableDatabase
    }

    fun open(){

    }

    fun insert(modelAttach: ModelTaskAttach):Long{

        val values = ContentValues()

        values.put(COL_TASK_ID, modelAttach.taskId)
        values.put(COL_CATE_ID, modelAttach.categoryId)
        values.put(COL_ATTACH_NAME, modelAttach.name)
        values.put(COL_ATTACH_PATH, modelAttach.path)
        values.put(COL_ATTACH_TYPE, modelAttach.type)
        values.put(COL_CREATEDATE, modelAttach.createDate)
        values.put(COL_UPDATEDATE, modelAttach.updateDate)

        val r = db!!.insert(tableName, null, values)
        Log.i("fehhhhh", "insert: "+ r)
        return r
    }

    fun update(modelAttach: ModelTaskAttach): Int{

        val values = ContentValues()
        val id = modelAttach.id
        values.put(COL_TASK_ID, modelAttach.taskId)
        values.put(COL_CATE_ID, modelAttach.categoryId)
        values.put(COL_ATTACH_NAME, modelAttach.name)
        values.put(COL_ATTACH_PATH, modelAttach.path)
        values.put(COL_ATTACH_TYPE, modelAttach.type)
        values.put(COL_CREATEDATE, modelAttach.createDate)
        values.put(COL_UPDATEDATE, modelAttach.updateDate)

        val r = db!!.update(tableName, values, "$COL_ATTACH_ID = ?", arrayOf(id.toString()))

        return r

    }


    fun delete(attachId: Long?): Int{
        val r = db!!.delete(tableName, "$COL_ATTACH_ID = ?", arrayOf(attachId.toString()))
        return r
    }

    fun getLastId(): Long{

        val cursor: Cursor = db!!.rawQuery("select max($COL_ATTACH_ID) from $tableName;", null)
        var lastID: Long? = null

        if(cursor.moveToFirst()){
            lastID = cursor.getLong(0)
            Log.i("gewfw", "getLastId: $lastID")
        }
        return lastID!!

    }

    fun getDataList(): ArrayList<ModelTaskAttach>{
        val cursor: Cursor = db!!.query(tableName, null, null, null, null, null, null)
        return dataQuryList(cursor)
    }

    fun getDataByTaskId(taskId: Long?): ArrayList<ModelTaskAttach>{
        val cursor = db!!.rawQuery("select * from $tableName where $COL_TASK_ID like '$taskId'",null)
        return dataQuryList(cursor)
    }

    fun deleteByTaskId(taskId: Long?):Int{
        val r = db!!.delete(tableName, "$COL_TASK_ID = ?", arrayOf(taskId.toString()))
        return r
    }

    fun getDataByCreateDate(createDate: Long): ModelTaskAttach?{
        val cursor = db!!.rawQuery("select * from $tableName where $COL_CREATEDATE like '$createDate'", null)

        if(cursor.moveToFirst()){
            val m = ModelTaskAttach()
            m.id = (cursor.getLong(cursor.getColumnIndex(COL_ATTACH_ID)))
            m.taskId = (cursor.getLong(cursor.getColumnIndex(COL_TASK_ID)))
            m.categoryId = (cursor.getLong(cursor.getColumnIndex(COL_CATE_ID)))
            m.name = (cursor.getString(cursor.getColumnIndex(COL_ATTACH_NAME)))
            m.path = (cursor.getString(cursor.getColumnIndex(COL_ATTACH_PATH)))
            m.type = (cursor.getString(cursor.getColumnIndex(COL_ATTACH_TYPE)))
            m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
            m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))

            return m
        }
        return null
    }

    fun query(sql: String): ArrayList<ModelTaskAttach>{
        val cursor = db!!.rawQuery(sql, null)
        return dataQuryList(cursor)
    }

    private val DATABASE_BookTaskTodoListAttach = "BookTaskTodoListAttach"
    fun getModelExceptList(dbAttach: SQLiteDatabase): ArrayList<ModelTaskAttach>{

        dbAttach.beginTransaction()

        val cursor = dbAttach!!.rawQuery(
            "SELECT * " +
                    /*"$COL_UPDATEDATE, " +
                    "$COL_CREATEDATE " +*/
                    "FROM $DATABASE_BookTaskTodoListAttach.$TABLE_TASKATTACH " +
                    "EXCEPT " +
                    "SELECT * " +
                    /*"$COL_UPDATEDATE, " +
                    "$COL_CREATEDATE " +*/
                    "FROM $TABLE_TASKATTACH"
            , null)

        dbAttach.endTransaction()

        return dataQuryList(cursor)
    }


    private fun dataQuryList(cursor: Cursor): ArrayList<ModelTaskAttach>{
        val list = ArrayList<ModelTaskAttach>()
        try {
            while (cursor.moveToNext()) {

                val m = ModelTaskAttach()
                m.id = (cursor.getLong(cursor.getColumnIndex(COL_ATTACH_ID)))
                m.taskId = (cursor.getLong(cursor.getColumnIndex(COL_TASK_ID)))
                m.categoryId = (cursor.getLong(cursor.getColumnIndex(COL_CATE_ID)))
                m.name = (cursor.getString(cursor.getColumnIndex(COL_ATTACH_NAME)))
                m.path = (cursor.getString(cursor.getColumnIndex(COL_ATTACH_PATH)))
                m.type = (cursor.getString(cursor.getColumnIndex(COL_ATTACH_TYPE)))
                m.createDate = (cursor.getLong(cursor.getColumnIndex(COL_CREATEDATE)))
                m.updateDate = (cursor.getLong(cursor.getColumnIndex(COL_UPDATEDATE)))


                list.add(m)
            }
        } catch (e: Exception){
            Log.i("hhhhh", "getDataTaskId : " + e)
        }
        return list
    }

}