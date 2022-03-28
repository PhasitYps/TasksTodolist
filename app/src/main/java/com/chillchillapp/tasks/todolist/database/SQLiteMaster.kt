package com.chillchillapp.tasks.todolist.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

//version
val DATABASE_VERSION = 1
val SQLITE_NAME = "TaskTodolist.db"

//COL GLOBAL
val COL_CREATEDATE = "CreateDate"
val COL_UPDATEDATE = "UpdateDate"
val COL_STATUS = "Status"

//Table Category
val TABLE_CATEGORY = "CategoryTB"
val COL_CATE_ID = "CateID" //primary key
val COL_CATE_NAME = "CateName"
val COL_CATE_IMAGE = "CateImage"
val COL_CATE_PRIORITY = "Priority"

val DATABASE_CREATECategory = ("create table IF NOT EXISTS " + TABLE_CATEGORY + "( "
        + COL_CATE_ID + " integer primary key autoincrement, "
        + COL_CATE_NAME + " text,"
        + COL_CATE_IMAGE + " BLOB,"
        + COL_CATE_PRIORITY + " integer,"
        + COL_CREATEDATE + " integer,"
        + COL_UPDATEDATE + " integer,"
        + COL_STATUS + " text" + ");"
        )

//Table Task
val TABLE_TASK = "TaskTB"
val COL_TASK_ID = "TaskId"
val COL_TASK_NAME = "TaskName"
val COL_TASK_FAVORITE = "TaskFavorite"
val COL_TASK_STATE = "TaskState"
val COL_TASK_DUEDATE = "TaskDuedate"
val COL_TASK_HOUR = "TaskHour"
val COL_TASK_MINUTE = "TaskMinute"
val COL_TASK_COMPLETEDATE = "TaskCompleteDate"

val DATABASE_CREATETask = ("create table IF NOT EXISTS " + TABLE_TASK + "( "
        + COL_TASK_ID + " integer primary key autoincrement, "
        + COL_CATE_ID + " integer,"
        + COL_TASK_NAME + " text,"
        + COL_TASK_FAVORITE + " integer,"
        + COL_TASK_STATE + " integer,"
        + COL_TASK_DUEDATE + " integer,"
        + COL_TASK_HOUR + " integer,"
        + COL_TASK_MINUTE + " integer,"
        + COL_TASK_COMPLETEDATE + " integer,"
        + COL_CREATEDATE + " integer,"
        + COL_UPDATEDATE + " integer,"
        + COL_STATUS + " text"+ ");"
        )

//Table SubTask
val TABLE_TASKSUB = "TaskSubTB"
val COL_SUBTASK_ID = "SubTaskId"
val COL_SUBTASK_TODO = "SubTaskTodo"
val COL_SUBTASK_STATE = "SubTaskState"
val COL_SUBTASK_PRIORITY = "SubTaskPriority"

val DATABASE_CREATETaskSub = ("create table IF NOT EXISTS " + TABLE_TASKSUB + "( "
        + COL_SUBTASK_ID + " integer primary key autoincrement, "
        + COL_TASK_ID + " integer,"
        + COL_CATE_ID + " integer,"
        + COL_SUBTASK_TODO + " text,"
        + COL_SUBTASK_STATE + " integer,"
        + COL_SUBTASK_PRIORITY + " integer,"
        + COL_CREATEDATE + " integer,"
        + COL_UPDATEDATE + " integer" + ");"
        )

//Table Attach
val TABLE_TASKATTACH = "TaskAttachTB"
val COL_ATTACH_ID = "AttachId"
val COL_ATTACH_PATH = "AttachPath"
val COL_ATTACH_TYPE = "AttachType"
val COL_ATTACH_NAME = "AttachName"

val DATABASE_CREATETaskAttach = ("create table IF NOT EXISTS " + TABLE_TASKATTACH + "( "
        + COL_ATTACH_ID + " integer primary key autoincrement, "
        + COL_TASK_ID + " integer,"
        + COL_CATE_ID + " integer,"
        + COL_ATTACH_NAME + " text,"
        + COL_ATTACH_PATH + " text,"
        + COL_ATTACH_TYPE + " text,"
        + COL_CREATEDATE + " integer,"
        + COL_UPDATEDATE + " integer" + ");"
        )

//Table Reminder
val TABLE_REMINDER = "ReminderTB"
val COL_REMINDER_ID = "ReminderId"
val COL_REMINDER_TYPE = "ReminderType"
val COL_REMINDER_COUNT = "ReminderCount"
val COL_REMINDER_BASETIME = "ReminderBaseTime"
val COL_REMINDER_OPTION = "ReminderOption"

val DATABASE_CREATETaskReminder = ("create table IF NOT EXISTS " + TABLE_REMINDER + "( "
        + COL_REMINDER_ID + " integer primary key autoincrement, "
        + COL_TASK_ID + " integer,"
        + COL_REMINDER_OPTION + " text,"
        + COL_REMINDER_COUNT + " integer,"
        + COL_REMINDER_TYPE + " integer,"
        + COL_REMINDER_BASETIME + " integer,"
        + COL_UPDATEDATE + " integer,"
        + COL_CREATEDATE + " integer" + ");"
        )


// Table Repeat
val TABLE_REPEAT = "RepeatTB"
val COL_REPEAT_ID = "RepeatID"
val COL_REPEAT_TYPE = "RepeatType"
val COL_REPEAT_NEXT = "RepeatNext"
val COL_REPEAT_NUMBER_OF_TIME = "NumberOfTime"
val COL_REPEAT_PROGRAMCOUNT = "ProgramCount"

val DATABASE_CREATERepeat = ("create table IF NOT EXISTS " + TABLE_REPEAT + "( "
        + COL_REPEAT_ID + " integer primary key autoincrement,"
        + COL_TASK_ID + " integer,"
        + COL_REPEAT_TYPE + " text,"
        + COL_REPEAT_NEXT + " integer,"
        + COL_REPEAT_NUMBER_OF_TIME + " integer,"
        + COL_REPEAT_PROGRAMCOUNT + " integer,"
        + COL_UPDATEDATE + " integer,"
        + COL_CREATEDATE + " integer" + ");"
        )

/*val TABLE_SYNC = "SyncTB"
val COL_SYNC_ID = "SyncID"
val COL_SYNC_FILE_ID = "SyncFileID"
val COL_SYNC_UID = "SyncUID"
val COL_SYNC_DATABASE_ID = "SyncDatabaseID"

val DATABASE_CREATESync = ("create table IF NOT EXISTS " + TABLE_SYNC + "( "
        + COL_SYNC_ID + " integer primary key autoincrement,"
        + COL_SYNC_FILE_ID + " text,"
        + COL_SYNC_UID + " text,"
        + COL_SYNC_DATABASE_ID + " text,"
        + COL_UPDATEDATE + " integer,"
        + COL_CREATEDATE + " integer" + ");"
        )*/


class SQLiteMaster(var context: Context, private var sqliteName: String = SQLITE_NAME): SQLiteOpenHelper(context, sqliteName, null, DATABASE_VERSION){

    //Environment.getExternalStorageDirectory().toString() + "/" + SQLite_Name

    override fun onCreate(db: SQLiteDatabase?) {

        db!!.execSQL(DATABASE_CREATECategory)
        db!!.execSQL(DATABASE_CREATETask)
        db!!.execSQL(DATABASE_CREATETaskSub)
        db!!.execSQL(DATABASE_CREATETaskAttach)
        db!!.execSQL(DATABASE_CREATERepeat)
        db!!.execSQL(DATABASE_CREATETaskReminder)
        //db!!.execSQL(DATABASE_CREATESync)
        Log.i("ttttt", "onCreate Table")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.i("ttttt", "onUpgrade Table")

        db!!.execSQL(DATABASE_CREATECategory)
        db!!.execSQL(DATABASE_CREATETask)
        db!!.execSQL(DATABASE_CREATETaskSub)
        db!!.execSQL(DATABASE_CREATETaskAttach)
        db!!.execSQL(DATABASE_CREATERepeat)
        db!!.execSQL(DATABASE_CREATETaskReminder)

    }
}