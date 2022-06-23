package com.chillchillapp.gthingstodo.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.chillchillapp.gthingstodo.KEY_ACTIVE
import com.chillchillapp.gthingstodo.KEY_REMOVE
import com.chillchillapp.gthingstodo.model.ModelCategory
import java.lang.Exception
import kotlin.collections.ArrayList

class FunctionCategory(private var context: Context, private var sqlitePath: String? = null) {

    private var sqLiteData: SQLiteMaster? = null
    private var db: SQLiteDatabase? = null
    private val tableName = TABLE_CATEGORY


    init {
        sqLiteData = if(sqlitePath != null)
            SQLiteMaster(context, sqlitePath!!)
        else
            SQLiteMaster(context)

        db = sqLiteData?.writableDatabase
    }

    fun open(){

    }


    fun insert(model: ModelCategory):Boolean{

        val values = ContentValues()
        values.put(COL_CATE_NAME, model.name)
        values.put(COL_CATE_IMAGE, model.image)
        values.put(COL_CATE_PRIORITY, model.priority)
        values.put(COL_CREATEDATE, model.createDate)
        values.put(COL_UPDATEDATE, model.updateDate)
        values.put(COL_STATUS, model.status)

        val r = db!!.insert(tableName, null, values)
        return r != 0L
    }

    fun update(model: ModelCategory): Int{

        val values = ContentValues()
        val id = model.id
        values.put(COL_CATE_NAME, model.name)
        values.put(COL_CATE_IMAGE, model.image)
        values.put(COL_CATE_PRIORITY, model.priority)
        values.put(COL_CREATEDATE, model.createDate)
        values.put(COL_UPDATEDATE, model.updateDate)
        values.put(COL_STATUS, model.status)

        val r = db!!.update(tableName, values, "$COL_CATE_ID = ?", arrayOf(id.toString()))

        return r
    }

    fun updateById(cateId: Long?, model: ModelCategory): Int{

        val values = ContentValues()
        values.put(COL_CATE_NAME, model.name)
        values.put(COL_CATE_IMAGE, model.image)
        values.put(COL_CATE_PRIORITY, model.priority)
        values.put(COL_CREATEDATE, model.createDate)
        values.put(COL_UPDATEDATE, model.updateDate)
        values.put(COL_STATUS, model.status)

        val r = db!!.update(tableName, values, "$COL_CATE_ID = ?", arrayOf(cateId.toString()))

        return r
    }

    fun delete(cateId: Long?): Boolean {
        return db!!.delete(tableName, "$COL_CATE_ID = ?", arrayOf(cateId.toString())) > 0
    }

    fun getCategory(): ArrayList<ModelCategory>{

        val cursor: Cursor = db!!.rawQuery("select * from $tableName where $COL_STATUS != '$KEY_REMOVE'",null)
        return dataQuryList(cursor)
    }

    fun getCategoryDisplay(): ArrayList<ModelCategory>{
        val cursor: Cursor = db!!.rawQuery("select * from $tableName where $COL_STATUS == '$KEY_ACTIVE'",null)
        return dataQuryList(cursor)
    }

    fun getById(cateId: Long?): ModelCategory{

        val cursor = db!!.rawQuery("select * from $tableName where $COL_CATE_ID like '$cateId' and $COL_STATUS != '$KEY_REMOVE'",null)
        val m = ModelCategory()

        if(cursor.moveToFirst()){

            m.id = (cursor.getLong(cursor.getColumnIndexOrThrow(COL_CATE_ID)))
            m.name = (cursor.getString(cursor.getColumnIndexOrThrow(COL_CATE_NAME)))
            m.image = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_CATE_IMAGE))
            m.priority = (cursor.getLong(cursor.getColumnIndexOrThrow(COL_CATE_PRIORITY)))
            m.createDate = (cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATEDATE)))
            m.updateDate = (cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATEDATE)))
            m.status = (cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)))

        }

        Log.i("gewfw", "cursor: $cursor")
        return m
    }

    fun getLastPriority(): Long{

        val cursor: Cursor = db!!.rawQuery("select max($COL_CATE_PRIORITY) from $tableName;", null)
        var lastID: Long = 0

        if(cursor.moveToFirst()){
            lastID = cursor.getLong(0)
            Log.i("fewsfrdws", "getLastPriority: " + lastID)
        }
        return lastID
    }

    fun getLastId(): Long{

        val cursor: Cursor = db!!.rawQuery("select max($COL_CATE_ID) from $tableName;", null)
        var lastID: Long? = null

        if(cursor.moveToFirst()){
            lastID = cursor.getLong(0)
            Log.i("gewfw", "getLastId: $lastID")
        }
        return lastID!!
    }

    fun query(sql: String): ArrayList<ModelCategory>{
        val cursor = db!!.rawQuery(sql, null)
        return dataQuryList(cursor)
    }

    private val DATABASE_BookTaskTodoListAttach = "BookTaskTodoListAttach"
    fun getModelExceptList(dbAttach: SQLiteDatabase): ArrayList<ModelCategory>{

        dbAttach.beginTransaction()

        val cursor = dbAttach!!.rawQuery(
            "SELECT * " +
                    /*"$COL_UPDATEDATE, " +
                    "$COL_CREATEDATE, " +
                    "$COL_STATUS " +*/
                    "FROM $DATABASE_BookTaskTodoListAttach.$TABLE_CATEGORY " +
                    "EXCEPT " +
                    "SELECT * " +
                    /*"$COL_UPDATEDATE, " +
                    "$COL_CREATEDATE, " +
                    "$COL_STATUS " +*/
                    "FROM $TABLE_CATEGORY"
            , null)

        dbAttach.endTransaction()

        return dataQuryList(cursor)
    }

    private fun dataQuryList(cursor: Cursor): ArrayList<ModelCategory>{
        val list = ArrayList<ModelCategory>()

        try {

            while (cursor.moveToNext()) {

                val m = ModelCategory()
                m.id = (cursor.getLong(cursor.getColumnIndexOrThrow(COL_CATE_ID)))
                m.name = (cursor.getString(cursor.getColumnIndexOrThrow(COL_CATE_NAME)))
                m.image = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_CATE_IMAGE))
                m.priority = (cursor.getLong(cursor.getColumnIndexOrThrow(COL_CATE_PRIORITY)))
                m.createDate = (cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATEDATE)))
                m.updateDate = (cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATEDATE)))
                m.status = (cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)))

                list.add(m)
            }

        } catch (e: Exception){
            Log.d("", "function category dataQuryList:"+e.toString())

        }
        return list
    }

}