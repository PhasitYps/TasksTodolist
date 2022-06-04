package com.chillchillapp.tasks.todolist.master

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.work.*
import com.chillchillapp.tasks.todolist.*
import com.chillchillapp.tasks.todolist.database.*
import com.chillchillapp.tasks.todolist.model.ModelTask
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import java.io.File
import java.util.concurrent.ExecutionException

class SyncHelper(private val activity: Activity) {

    fun syncing(){

        Log.d("hhjjjjjhhhhh", "syncing start")

        val uploadConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncingWorker = OneTimeWorkRequestBuilder<SyncingWorker>()
            .setConstraints(uploadConstraints)
            .build()

        WorkManager.getInstance(activity)
            .beginUniqueWork(KEY_SYNC, ExistingWorkPolicy.KEEP, syncingWorker)
            .enqueue()

        Log.d("hhjjjjjhhhhh", "syncing end")

    }

    class SyncingWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){
        override fun doWork(): Result {

            try {
                Log.d("hhjjjjjhhhhh", "SyncingWorker start")

                val driveHelper = DriveManageHelper(context)
                val fileList = driveHelper.getFileList(SQLITE_NAME, arrayOf(driveHelper.MIME_TYPE_SQLite3))

                val SyncFolder = File(context.filesDir, FOLDER_SYNC)
                if(!SyncFolder.isDirectory){
                    SyncFolder.mkdir()
                }

                val dbDrive = File(SyncFolder, SQLITE_NAME)
                if(!dbDrive.exists()){
                    dbDrive.createNewFile()
                }

                when(fileList.files.size){

                    0->{

                        val uploadConstraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()

                        val backUpWorker = OneTimeWorkRequestBuilder<BackUpWorker>()
                            .setConstraints(uploadConstraints)
                            .build()

                        WorkManager.getInstance(context)
                            .beginUniqueWork(KEY_SYNC, ExistingWorkPolicy.APPEND, backUpWorker)
                            .enqueue()

                    }
                    else->{

                        val workData = workDataOf(KEY_DATABASE_ID to fileList.files[0].id)

                        val uploadConstraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()

                        val downloadWorker = OneTimeWorkRequestBuilder<Syncing.DowloadWorker>()
                            .setConstraints(uploadConstraints)
                            .setInputData(workData)
                            .build()

                        val syncingWorker = OneTimeWorkRequestBuilder<Syncing.SyncingWorker>()
                            .setConstraints(uploadConstraints)
                            .build()

                        val uploadWorker = OneTimeWorkRequestBuilder<Syncing.UploadWorker>()
                            .setConstraints(uploadConstraints)
                            .setInputData(workData)
                            .build()

                        WorkManager.getInstance(context)
                            .beginUniqueWork(KEY_SYNC, ExistingWorkPolicy.APPEND, downloadWorker)
                            .then(syncingWorker)
                            .then(uploadWorker)
                            .enqueue()
                    }
                }

                Log.d("hhjjjjjhhhhh", "SyncingWorker end")

                return Result.success(workDataOf("Process" to 40))
            }catch (e: Exception){
                Log.d("SyncHelper", "Exception: " + e)
                return Result.failure(workDataOf(KEY_FAIL to KEY_FAIL))
            }

        }
    }

    class BackUpWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

        override fun doWork(): Result {

            Log.d("hhjjjjjhhhhh", "BackUpWorker start")

            try {
                val driveHelper = DriveManageHelper(context)
                val sqlite = context.getDatabasePath(SQLITE_NAME)

                //upload database sqlite
                Log.d("hhjjjjjhhhhh", "uploadFile sqlite start")
                driveHelper.uploadFile(sqlite, driveHelper.MIME_TYPE_SQLite3)
                Log.d("hhjjjjjhhhhh", "uploadFile sqlite end")

                //upload files attach
                val functionTaskAttach = FunctionTaskAttach(context)
                functionTaskAttach.open()
                val attachList = functionTaskAttach.getDataList()
                for(m in attachList){
                    val file = File(m.path)
                    when(m.type){
                        FOLDER_IMAGE->{
                            driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_IMAGE_JPG)
                        }
                        FOLDER_AUDIO->{
                            driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_AUDIO)
                        }
                        FOLDER_VIDEO->{
                            driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_VIDEO)
                        }
                    }
                }

                Log.d("hhjjjjjhhhhh", "BackUpWorker end")

                return Result.success(workDataOf("Process" to 100))
            }catch (e: java.lang.Exception){
                Log.d("hhjjjjjhhhhh", "e: " + e)
                return Result.failure(workDataOf(KEY_FAIL to KEY_FAIL))
            }
        }
    }

    class Syncing{

        class DowloadWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                Log.d("hhjjjjjhhhhh", "DowloadWorker start")

                try {
                    val driveHelper = DriveManageHelper(context)
                    val dbDrive = File( File(context.filesDir, FOLDER_SYNC), SQLITE_NAME)
                    val databaseId = inputData.getString(KEY_DATABASE_ID)

                    driveHelper.dowloadFileByIdTo(databaseId!!, driveHelper.MIME_TYPE_SQLite3, dbDrive)
                    return Result.success(workDataOf("Process" to 70))
                }catch (e: Exception){
                    Log.d("hhjjjjjhhhhh", "DowloadWorker: "  + e)
                    return Result.failure(workDataOf(KEY_FAIL to KEY_FAIL))
                }


            }
        }


        class SyncingWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                Log.d("hhjjjjjhhhhh", "SyncingWorker start")

                try {
                    val driveHelper = DriveManageHelper(context)
                    val dbLocal = context.getDatabasePath(SQLITE_NAME)
                    val dbDrive = File(File(context.filesDir, FOLDER_SYNC), SQLITE_NAME)

                    val dbAttach = attachSync(dbLocal, dbDrive)
                    dbAttach!!.endTransaction()

                    val localDB = DBFunctionHelper(context, dbLocal.path)
                    val driveDB = DBFunctionHelper(context, dbDrive.path)

                    val categoryExceptList = localDB.functionCategory.getModelExceptList(dbAttach!!)
                    val taskExceptList = localDB.functionTask.getModelExceptList(dbAttach!!)
                    //val attachExceptList = localDB.functionTaskAttach.getModelExceptList(dbAttach!!)

                    for(m in categoryExceptList){

                        val data = localDB.functionCategory.query("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${m.createDate}'")

                        when(data.isEmpty()){
                            true->{
                                localDB.functionCategory.insert(m)
                            }
                            false->{

                                when{
                                    m.status == KEY_REMOVE->{
                                        localDB.functionCategory.updateById(data[0].id, m)
                                    }
                                    m.status != KEY_REMOVE->{
                                        if(m.updateDate!! > data[0].updateDate!!){
                                            localDB.functionCategory.updateById(data[0].id, m)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for(m in taskExceptList){

                        val taskInLocal = localDB.functionTask.getDataByCreateDate(m.createDate!!)

                        when(taskInLocal.isEmpty()){
                            true->{
                                Log.d("jjjjjjj", "insert on Server: ")
                                insertTask(m, driveDB, localDB)
                            }
                            false->{
                                when(m.status){

                                    KEY_REMOVE->{
                                        Log.d("jjjjjjj", "update on Local: ")
                                        updateTask(taskInLocal[0].id!!, m, driveDB, localDB)
                                    }
                                    KEY_ACTIVE->{

                                        when{
                                            m.updateDate!! > taskInLocal[0].updateDate!!->{
                                                Log.d("jjjjjjj", "update on Local: ")
                                                updateTask(taskInLocal[0].id!!, m, driveDB, localDB)
                                            }

                                            m.updateDate!! < taskInLocal[0].updateDate!! || m.updateDate!! == taskInLocal[0].updateDate!!->{
                                                val attachList = localDB.functionTaskAttach.getDataByTaskId(taskInLocal[0].id)
                                                for(m in attachList){

                                                    val file = File(m.path)
                                                    when(m.type){
                                                        FOLDER_IMAGE->{
                                                            val fileList = driveHelper.getFileList(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_IMAGE_JPG, driveHelper.MIME_TYPE_FILE_IMAGE_PNG))
                                                            if(fileList.files.isEmpty()){
                                                                driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_IMAGE_JPG)
                                                            }
                                                        }
                                                        FOLDER_VIDEO->{
                                                            val fileList = driveHelper.getFileList(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_VIDEO))
                                                            if(fileList.files.isEmpty()){
                                                                driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_VIDEO)
                                                            }
                                                        }
                                                        FOLDER_AUDIO->{
                                                            val fileList = driveHelper.getFileList(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_AUDIO))
                                                            if(fileList.files.isEmpty()){
                                                                driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_AUDIO)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return Result.success(workDataOf("Process" to 95))
                }catch (e: Exception){
                    Log.d("hhjjjjjhhhhh", "SyncingWorker: "  + e)
                    return Result.failure(workDataOf(KEY_FAIL to KEY_FAIL))
                }
            }

            private val DATABASE_BookTaskTodoListAttach = "BookTaskTodoListAttach"
            private fun attachSync(dbFile: File, dbDriveFile: File): SQLiteDatabase? {

                if (!(dbFile.exists() && dbDriveFile.exists())) return null

                val dbAttach = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
                dbAttach.beginTransaction()
                dbAttach.execSQL("ATTACH '" + dbDriveFile.path + "' AS " + DATABASE_BookTaskTodoListAttach)
                return dbAttach
            }

            private fun insertTask(modelTask: ModelTask, dbDrive: DBFunctionHelper, dbLocal: DBFunctionHelper){

                val driveHelper = DriveManageHelper(context)

                //sync category
                if(modelTask.categoryId != -1L){
                    val category = dbDrive.functionCategory.getById(modelTask.categoryId)
                    val data = dbLocal.functionCategory.query("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${category.createDate}'")
                    if(data.size > 0){
                        modelTask.categoryId = data[0].id
                    }
                }

                if(dbLocal.functionTask.insert(modelTask) != 0L){
                    val lastId = dbLocal.functionTask.getLastId()

                    //1.sync subtask
                    val subTasks = dbDrive.functionTaskSub.getDataByTaskId(modelTask.id)
                    for(m in subTasks){
                        m.taskId = lastId
                        dbLocal.functionTaskSub.insert(m)
                    }//end save subTask

                    //2.sync attach
                    val attachList = dbDrive.functionTaskAttach.getDataByTaskId(modelTask.id)
                    for(m in attachList){
                        m.taskId = lastId
                        dbLocal.functionTaskAttach.insert(m)

                        val file = File(m.path)
                        file.createNewFile()
                        when(m.type){
                            FOLDER_IMAGE->{
                                driveHelper.dowloadFileTo(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_IMAGE_JPG, driveHelper.MIME_TYPE_FILE_IMAGE_PNG), file)
                            }
                            FOLDER_AUDIO->{
                                driveHelper.dowloadFileTo(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_AUDIO), file)
                            }
                            FOLDER_VIDEO->{
                                driveHelper.dowloadFileTo(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_VIDEO), file)
                            }
                        }
                    }

                    //3.sync repeat
                    val repeat = dbDrive.functionRepeat.getByTaskId(modelTask.id)
                    if(repeat.taskId != null){
                        repeat.taskId = lastId
                        dbLocal.functionRepeat.insert(repeat)
                    }

                    //4.sync reminder
                    val reminderList = dbDrive.functionReminder.getReminderByTaskId(modelTask.id)
                    for(m in reminderList){
                        m.taskId = lastId
                        dbLocal.functionReminder.insert(m)
                    }
                }
            }

            private fun updateTask(taskId: Long, newTask: ModelTask, dbDrive: DBFunctionHelper, dbLocal: DBFunctionHelper){

                val driveHelper = DriveManageHelper(context)

                //update category
                if(newTask.categoryId != -1L){
                    val category = dbDrive.functionCategory.getById(newTask.categoryId)
                    val dataCategory = dbLocal.functionCategory.query("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${category.createDate}'")
                    if(dataCategory.size > 0){
                        newTask.categoryId = dataCategory[0].id
                    }
                }

                //update task
                if(dbLocal.functionTask.updateById(taskId, newTask) != 0){
                    //1.delete subTask
                    dbLocal.functionTaskSub.deleteByTaskID(taskId)
                    //2.delete attach
                    dbLocal.functionTaskAttach.deleteByTaskId(taskId)
                    //3.delete repeat
                    dbLocal.functionRepeat.deleteByTaskId(taskId)
                    //4.delete reminder
                    dbLocal.functionReminder.deleteByTaskId(taskId)


                    //1.insert subtask
                    val subTasks = dbDrive.functionTaskSub.getDataByTaskId(newTask.id)
                    for(m in subTasks){
                        m.taskId = taskId
                        dbLocal.functionTaskSub.insert(m)
                    }//end save subTask

                    //2.insert attach
                    val attachList = dbDrive.functionTaskAttach.getDataByTaskId(newTask.id)
                    for(m in attachList){
                        m.taskId = taskId
                        dbLocal.functionTaskAttach.insert(m)

                        val file = File(m.path)
                        if(!file.exists()){
                            file.createNewFile()
                            when(m.type){
                                FOLDER_IMAGE->{
                                    driveHelper.dowloadFileTo(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_IMAGE_JPG, driveHelper.MIME_TYPE_FILE_IMAGE_PNG), file)
                                }
                                FOLDER_AUDIO->{
                                    driveHelper.dowloadFileTo(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_AUDIO), file)
                                }
                                FOLDER_VIDEO->{
                                    driveHelper.dowloadFileTo(m.name!!, arrayOf(driveHelper.MIME_TYPE_FILE_VIDEO), file)
                                }
                            }
                        }
                    }

                    //3.insert repeat
                    val repeat = dbDrive.functionRepeat.getByTaskId(newTask.id)
                    if(repeat.id != null){
                        repeat.taskId = taskId
                        dbLocal.functionRepeat.insert(repeat)
                    }

                    //4.insert reminder
                    val reminderList = dbDrive.functionReminder.getReminderByTaskId(newTask.id)
                    for(m in reminderList){
                        m.taskId = taskId
                        dbLocal.functionReminder.insert(m)
                    }
                }
            }
        }

        class UploadWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                Log.d("hhjjjjjhhhhh", "UploadWorker start")

                try {
                    val databaseId = inputData.getString(KEY_DATABASE_ID)
                    val driveHelper = DriveManageHelper(context)
                    val dbLocal = context.getDatabasePath(SQLITE_NAME)

                    driveHelper.update(databaseId!!, dbLocal, driveHelper.MIME_TYPE_SQLite3)

                    return Result.success(workDataOf("Process" to 100))
                }catch (e: Exception){
                    Log.d("hhjjjjjhhhhh", "UploadWorker: "  + e)
                    return Result.failure(workDataOf(KEY_FAIL to KEY_FAIL))
                }


            }
        }

    }

}