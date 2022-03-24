package com.chillchillapp.tasks.todolist.master

import android.app.Activity
import android.content.Context
import android.util.Log.d
import android.util.Log.i
import androidx.work.*
import com.chillchillapp.tasks.todolist.*
import com.chillchillapp.tasks.todolist.database.*
import com.chillchillapp.tasks.todolist.model.*
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import kotlin.collections.ArrayList
import android.database.sqlite.SQLiteDatabase




class SyncHelper(private var activity: Activity) {

    /*private val workManager = WorkManager.getInstance(activity)

    fun setSyncSafety(){

        val uploadConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val start = OneTimeWorkRequestBuilder<SearchSyncWorker>()
            .setConstraints(uploadConstraints)
            .build()


        workManager.beginUniqueWork(KEY_SYNC, ExistingWorkPolicy.KEEP, start).enqueue()

    }

    class SearchSyncWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

        private val Process = "Process"

        override fun doWork(): Result {

            try {
                d("hhjjjjjhhhhh", "CheckFile start.")
                val outputData = lookForSyncPath()
                d("hhjjjjjhhhhh", "CheckFile finish.")

                return Result.success(outputData)

            }catch (e: Exception){
                d("hhjjjjjhhhhh", "e: " + e)
                return Result.failure(workDataOf(KEY_FILES to "fail"))
            }

        }

        private fun lookForSyncPath(): Data{
            val driveServiceHelper = DriveManageHelper(context)
            val user = FirebaseAuth.getInstance().currentUser

            val functionSync = FunctionSync(context)
            val modelSync = functionSync.getByUid(user.uid)

            if(modelSync.id == null){

                val fileList = driveServiceHelper.getFileList(SQLITE_NAME, driveServiceHelper.MIME_TYPE_SQLite3)

                if(fileList.files.size == 0){

                    i("hhjjjjjhhhhh", "backUpFirstTime.")
                    backUpFirstTime()
                    return workDataOf(Process to 40)

                }else{
                    i("hhjjjjjhhhhh", "syncRestore.")
                    syncRestore(fileList.files[0].id)
                    return workDataOf(Process to 40)
                }

            }else{

                val fileList = driveServiceHelper.getFileList(SQLITE_NAME, driveServiceHelper.MIME_TYPE_SQLite3)

                if(fileList.files.size == 0){

                    i("hhjjjjjhhhhh", "backUpUpdate.")
                    backUpUpdate()
                    return workDataOf(Process to 40)

                }else{
                    i("hhjjjjjhhhhh", "syncUpdate.")
                    if(modelSync.databaseId != fileList.files[0].id){
                        functionSync.delete(modelSync)
                        syncRestore(fileList.files[0].id)
                    }else{

                        syncUpdate()
                    }

                    return workDataOf(Process to 40)
                }
            }
        }

        private fun backUpFirstTime(){
            val upload_constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val upload = OneTimeWorkRequestBuilder<BackUpFirstTime.UploadWorker>()
                .setConstraints(upload_constraints)
                .build()

            val finish = OneTimeWorkRequestBuilder<FinishWorker>()
                .setConstraints(upload_constraints)
                .build()

            WorkManager.getInstance(context).beginUniqueWork(KEY_SYNC, ExistingWorkPolicy.APPEND, upload)
                .then(finish)
                .enqueue()

        }

        private fun backUpUpdate(){
            val upload_constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val upload = OneTimeWorkRequestBuilder<BackUpUpdate.UploadWorker>()
                .setConstraints(upload_constraints)
                .build()

            val finish = OneTimeWorkRequestBuilder<FinishWorker>()
                .setConstraints(upload_constraints)
                .build()

            WorkManager.getInstance(context).beginUniqueWork(KEY_SYNC, ExistingWorkPolicy.APPEND, upload)
                .then(finish)
                .enqueue()
        }

        private fun syncUpdate(){

            val upload_constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val downlod = OneTimeWorkRequestBuilder<SyncSafety.DownloadWorker>()
                .setConstraints(upload_constraints)
                .build()

            val sync = OneTimeWorkRequestBuilder<SyncSafety.SyncWorker>()
                .build()

            val update = OneTimeWorkRequestBuilder<SyncSafety.UpdateWorker>()
                .setConstraints(upload_constraints)
                .build()

            val finish = OneTimeWorkRequestBuilder<FinishWorker>()
                .setConstraints(upload_constraints)
                .build()

            WorkManager.getInstance(context).beginUniqueWork(KEY_SYNC, ExistingWorkPolicy.APPEND, downlod)
                .then(sync)
                .then(update)
                .then(finish)
                .enqueue()
        }

        private fun syncRestore(databaseId: String){

            val upload_constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val downlod = OneTimeWorkRequestBuilder<SyncNewDevice.DownloadWorker>()
                .setInputData(workDataOf(KEY_DATABASE_ID to databaseId))
                .setConstraints(upload_constraints)
                .build()

            val sync = OneTimeWorkRequestBuilder<SyncNewDevice.SyncWorker>()
                .build()

            val update = OneTimeWorkRequestBuilder<SyncNewDevice.UpdateWorker>()
                .setConstraints(upload_constraints)
                .build()

            val finish = OneTimeWorkRequestBuilder<FinishWorker>()
                .setConstraints(upload_constraints)
                .build()

            WorkManager.getInstance(context).beginUniqueWork(KEY_SYNC, ExistingWorkPolicy.APPEND, downlod)
                .then(sync)
                .then(update)
                .then(finish)
                .enqueue()
        }

    }

    class BackUpFirstTime{

        class UploadWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                try {
                    val driveHelper = DriveManageHelper(context)
                    val user = FirebaseAuth.getInstance().currentUser
                    val sqlite = context.getDatabasePath(SQLITE_NAME)

                    val sqliteDatabase = driveHelper.uploadFile(sqlite, driveHelper.MIME_TYPE_SQLite3)
                    val files = driveHelper.createFolerInDrive(KEY_FILES)


                    val functionTaskAttach = FunctionTaskAttach(context)
                    val attachList = functionTaskAttach.getDataList()
                    for(m in attachList){
                        val file = File(m.path)
                        when(m.type){
                            FOLDER_IMAGE->{
                                driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_IMAGE_JPG, files.id)
                            }
                            FOLDER_AUDIO->{
                                driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_AUDIO, files.id)
                            }
                            FOLDER_VIDEO->{
                                driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_VIDEO, files.id)
                            }
                        }
                    }

                    val functionSync = FunctionSync(context)
                    val m = ModelSync()
                    m.filesId = files.id
                    m.uid = user.uid
                    m.databaseId = sqliteDatabase.id
                    m.createDate = System.currentTimeMillis()
                    m.updateDate = System.currentTimeMillis()

                    functionSync.insert(m)

                    driveHelper.update(m.databaseId!!, sqlite, driveHelper.MIME_TYPE_SQLite3)

                    return Result.success(workDataOf("Process" to 99, "UpdateDate" to m.updateDate))
                }catch (e: java.lang.Exception){
                    d("hhjjjjjhhhhh", "e: " + e)
                    return Result.failure(workDataOf(KEY_FILES to "fail"))
                }


            }
        }
    }

    class BackUpUpdate{

        class UploadWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                val driveHelper = DriveManageHelper(context)
                val user = FirebaseAuth.getInstance().currentUser
                val sqlite = context.getDatabasePath(SQLITE_NAME)

                val sqliteDatabase = driveHelper.uploadFile(sqlite, driveHelper.MIME_TYPE_SQLite3)
                val files = driveHelper.createFolerInDrive(KEY_FILES)

                val functionTaskAttach = FunctionTaskAttach(context)
                val attachList = functionTaskAttach.getDataList()
                for(m in attachList){
                    val file = File(m.path)
                    when(m.type){
                        FOLDER_IMAGE->{
                            driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_IMAGE_JPG, files.id)
                        }
                        FOLDER_AUDIO->{
                            driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_AUDIO, files.id)
                        }
                        FOLDER_VIDEO->{
                            driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_VIDEO, files.id)
                        }
                    }
                }

                val functionSync = FunctionSync(context)
                val m = functionSync.getByUid(user.uid)
                m.filesId = files.id
                m.uid = user.uid
                m.databaseId = sqliteDatabase.id
                m.createDate = System.currentTimeMillis()
                m.updateDate = System.currentTimeMillis()
                functionSync.update(m)

                driveHelper.update(m.databaseId!!, sqlite, driveHelper.MIME_TYPE_SQLite3)

                return Result.success(workDataOf("Process" to 99, "UpdateDate" to m.updateDate))
            }

        }

    }

    class SyncSafety{

        class DownloadWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                val driveHelper = DriveManageHelper(context)
                val user = FirebaseAuth.getInstance().currentUser

                val syncFolder = File(context.filesDir, FOLDER_SYNC)
                if(!syncFolder.isDirectory) syncFolder.mkdir()

                val sqliteDirectory = File(syncFolder, SQLITE_NAME)
                if(!sqliteDirectory.exists()){
                    sqliteDirectory.createNewFile()
                }
                val funtionSync = FunctionSync(context)
                val m = funtionSync.getByUid(user.uid)

                try {
                    driveHelper.dowloadFileByIdTo(m.databaseId!!, driveHelper.MIME_TYPE_SQLite3, sqliteDirectory)
                    return Result.success(workDataOf("Process" to 80))
                }catch (e: Exception){

                    d("hhjjjjjhhhhh", "SyncUpdate.DownloadWorker e: $e")
                    return Result.failure(workDataOf(KEY_FILES to "fail"))

                }
            }
        }

        class SyncWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                val driveHelper = DriveManageHelper(context)

                val syncDirectory = File(context.filesDir, FOLDER_SYNC)
                val databaseServer = File(syncDirectory, SQLITE_NAME)

                val deiveDB = DBFunctionHelper(context, SQLITE_NAME)
                val serverDB = DBFunctionHelper(context, databaseServer.path)

                val user = FirebaseAuth.getInstance().currentUser
                val syncA = deiveDB.functionSync.getByUid(user.uid)
                val syncB = serverDB.functionSync.getByUid(user.uid)

                val uploadFiles = ArrayList<ModelTaskAttach>()

                //sync safety
                if(syncA.updateDate != syncB.updateDate){

                    //sync attach
                    val newAttachList = deiveDB.functionTaskAttach.getDataList()
                    for(m in newAttachList){

                        val data = serverDB.functionTaskAttach.qury("select * from $TABLE_TASKATTACH where $COL_CREATEDATE like '${m.createDate}'")
                        if(data.isEmpty()){
                            uploadFiles.add(m)
                        }
                    }

                    //up new category server to device
                    val newCategoryInServer = serverDB.functionCategory.getDataList()
                    for(mCategory in newCategoryInServer){

                        val data = deiveDB.functionCategory.qury("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${mCategory.createDate}'")
                        if(data.isEmpty()){
                            if(mCategory.remove == null){
                                deiveDB.functionCategory.insert(mCategory)
                            }

                        }else {
                            if(mCategory.remove == null){
                                if(mCategory.updateDate!! > data[0].updateDate!!){
                                    deiveDB.functionCategory.updateById(data[0].id, mCategory)
                                }

                            }else{
                                data[0].delete(context)
                            }
                        }
                    }

                    //up new category device to server
                    val newCategoryInDevice = deiveDB.functionCategory.getDataList()
                    for(mCategory in newCategoryInDevice){

                        val data = serverDB.functionCategory.qury("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${mCategory.createDate}'")
                        if(data.isEmpty()){
                            serverDB.functionCategory.insert(mCategory)
                        }else {

                            if(mCategory.remove == null){
                                if(mCategory.updateDate!! > data[0].updateDate!!){
                                    serverDB.functionCategory.updateById(data[0].id, mCategory)
                                }
                            }else{
                                serverDB.functionCategory.updateById(data[0].id, mCategory)
                                mCategory.delete(context)
                            }
                        }
                    }

                    //sync task
                    val newTaskInServer = serverDB.functionTask.getDataList()
                    d("jjjjjjj", "updateDate newTaskInServer: " + newTaskInServer.size)

                    for(taskInServer in newTaskInServer){
                        val mTask = deiveDB.functionTask.qury("select * from $TABLE_TASK where $COL_CREATEDATE like '${taskInServer.createDate}'")

                        if(mTask.isEmpty()){
                            d("jjjjjjj", "insert on Device: ")
                            if(taskInServer.remove == null){
                                DataManager().insertTask(taskInServer, serverDB, deiveDB)
                            }
                        }else {
                            if(taskInServer.remove == null){
                                if(taskInServer.updateDate!! > mTask[0].updateDate!!){
                                    d("jjjjjjj", "update on Device: ")
                                    DataManager().updateTask(mTask[0].id!!, taskInServer, serverDB, deiveDB)
                                }
                            }else{
                                mTask[0].delete(context)
                            }
                        }
                    }

                    //sync task
                    val newTaskInDevice = deiveDB.functionTask.getDataList()
                    d("jjjjjjj", "updateDate newTaskInDevice: " + newTaskInDevice.size)

                    for(taskInDevice in newTaskInDevice){

                        val mTask = serverDB.functionTask.qury("select * from $TABLE_TASK where $COL_CREATEDATE like '${taskInDevice.createDate}'")

                        if(mTask.isEmpty()){
                            d("jjjjjjj", "insert on Server: ")
                            DataManager().insertTask(taskInDevice, deiveDB, serverDB)
                        }else {

                            if(taskInDevice.remove == null){
                                if(taskInDevice.updateDate!! > mTask[0].updateDate!!){
                                    d("jjjjjjj", "update on Server: ")
                                    DataManager().updateTask(mTask[0].id!!, taskInDevice, deiveDB, serverDB)
                                }
                            }else{
                                DataManager().updateTask(mTask[0].id!!, taskInDevice, deiveDB, serverDB)
                                taskInDevice.delete(context)
                            }
                        }
                    }

                }else{

                    //sync quick
                    val newAttachList = deiveDB.functionTaskAttach.qury("select * from $TABLE_TASKATTACH where $COL_CREATEDATE > ${syncA.updateDate}")
                    for(m in newAttachList){

                        val data = serverDB.functionTaskAttach.qury("select * from $TABLE_TASKATTACH where $COL_CREATEDATE like '${m.createDate}'")
                        if(data.isEmpty()){
                            uploadFiles.add(m)
                        }
                    }

                    //up new category server to device
                    val newCategoryInServer = serverDB.functionCategory.qury("select * from $TABLE_CATEGORY where $COL_UPDATEDATE > ${syncA.updateDate}")
                    for(mCategory in newCategoryInServer){

                        val data = deiveDB.functionCategory.qury("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${mCategory.createDate}'")
                        if(data.isEmpty()){
                            if(mCategory.remove == null){
                                deiveDB.functionCategory.insert(mCategory)
                            }

                        }else {
                            if(mCategory.remove == null){
                                if(mCategory.updateDate!! > data[0].updateDate!!){
                                    deiveDB.functionCategory.updateById(data[0].id, mCategory)
                                }

                            }else{
                                data[0].delete(context)
                            }
                        }
                    }

                    //up new category device to server
                    val newCategoryInDevice = deiveDB.functionCategory.qury("select * from $TABLE_CATEGORY where $COL_UPDATEDATE > ${syncA.updateDate}")
                    for(mCategory in newCategoryInDevice){

                        val data = serverDB.functionCategory.qury("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${mCategory.createDate}'")
                        if(data.isEmpty()){
                            serverDB.functionCategory.insert(mCategory)
                        }else {

                            if(mCategory.remove == null){
                                if(mCategory.updateDate!! > data[0].updateDate!!){
                                    serverDB.functionCategory.updateById(data[0].id, mCategory)
                                }
                            }else{
                                serverDB.functionCategory.updateById(data[0].id, mCategory)
                                mCategory.delete(context)
                            }
                        }
                    }

                    //sync task
                    val newTaskInServer = serverDB.functionTask.qury("select * from $TABLE_TASK where $COL_UPDATEDATE > ${syncA.updateDate}")
                    d("jjjjjjj", "updateDate newTaskInServer: " + newTaskInServer.size)
                    for(taskInServer in newTaskInServer){
                        val mTask = deiveDB.functionTask.qury("select * from $TABLE_TASK where $COL_CREATEDATE like '${taskInServer.createDate}'")

                        if(mTask.isEmpty()){
                            d("jjjjjjj", "insert on Device: ")
                            if(taskInServer.remove == null){
                                DataManager().insertTask(taskInServer, serverDB, deiveDB)
                            }
                        }else {
                            if(taskInServer.remove == null){
                                if(taskInServer.updateDate!! > mTask[0].updateDate!!){
                                    d("jjjjjjj", "update on Device: ")
                                    DataManager().updateTask(mTask[0].id!!, taskInServer, serverDB, deiveDB)
                                }
                            }else{
                                mTask[0].delete(context)
                            }
                        }
                    }

                    //sync task
                    val newTaskInDevice = deiveDB.functionTask.qury("select * from $TABLE_TASK where $COL_UPDATEDATE > ${syncA.updateDate}")
                    d("jjjjjjj", "updateDate newTaskInDevice: " + newTaskInDevice.size)
                    for(taskInDevice in newTaskInDevice){

                        val mTask = serverDB.functionTask.qury("select * from $TABLE_TASK where $COL_CREATEDATE like '${taskInDevice.createDate}'")

                        if(mTask.isEmpty()){
                            d("jjjjjjj", "insert on Server: ")
                            DataManager().insertTask(taskInDevice, deiveDB, serverDB)
                        }else {

                            if(taskInDevice.remove == null){
                                if(taskInDevice.updateDate!! > mTask[0].updateDate!!){
                                    d("jjjjjjj", "update on Server: ")
                                    DataManager().updateTask(mTask[0].id!!, taskInDevice, deiveDB, serverDB)
                                }
                            }else{
                                DataManager().updateTask(mTask[0].id!!, taskInDevice, deiveDB, serverDB)
                                taskInDevice.delete(context)
                            }
                        }
                    }
                }

                try {

                    val attachList = deiveDB.functionTaskAttach.getDataList()
                    for(m in attachList){

                        val file = File(m.path)

                        if(!file.exists()){
                            file.createNewFile()
                            d("jjjjjjj", "${m.type} insert on device: " + m.name)
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

                    for(m in uploadFiles){
                        val file = File(m.path)

                        val data = deiveDB.functionTaskAttach.qury("select * from $TABLE_TASKATTACH where $COL_CREATEDATE like '${m.createDate}'")
                        if(data.isEmpty()){
                            file.delete()
                        }else{
                            d("jjjjjjj", "${m.type} insert on Server: ")
                            when(m.type){
                                FOLDER_IMAGE->{
                                    driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_IMAGE_JPG, syncA.filesId)
                                }
                                FOLDER_AUDIO->{
                                    driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_AUDIO, syncA.filesId)
                                }
                                FOLDER_VIDEO->{
                                    driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_VIDEO, syncA.filesId)
                                }
                            }
                        }
                    }

                    val updateDate = System.currentTimeMillis()
                    syncB.updateDate = updateDate
                    serverDB.functionSync.update(syncB)

                    return Result.success(workDataOf("Process" to 95, "UpdateDate" to updateDate))
                }catch (e: Exception){
                    d("hhjjjjjhhhhh", "SyncUpdate.SyncWorker e: $e")
                    return Result.failure(workDataOf(KEY_FILES to "fail"))
                }
            }

        }

        class UpdateWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){
            override fun doWork(): Result {

                val updateDate = inputData.getLong("UpdateDate", 0)

                val user = FirebaseAuth.getInstance().currentUser
                val functionSync = FunctionSync(context)

                val modelSync = functionSync.getByUid(user.uid)
                val syncFolder = File(context.filesDir, FOLDER_SYNC)
                val dbB = File(syncFolder, SQLITE_NAME)

                try {
                    val driveHelper = DriveManageHelper(context)
                    driveHelper.update(modelSync.databaseId!!, dbB, driveHelper.MIME_TYPE_SQLite3)

                    modelSync.updateDate = updateDate
                    functionSync.update(modelSync)

                    return Result.success(workDataOf("Process" to 99, "UpdateDate" to updateDate))

                }catch (e: Exception){
                    d("hhjjjjjhhhhh", "SyncUpdate.UpdateWorker e: $e")
                    return Result.failure(workDataOf(KEY_FILES to "fail"))
                }
            }
        }

    }

    class SyncNewDevice{

        class DownloadWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                val syncFolder = File(context.filesDir, FOLDER_SYNC)
                if(!syncFolder.isDirectory) syncFolder.mkdir()

                val sqliteDirectory = File(syncFolder, SQLITE_NAME)
                if(!sqliteDirectory.exists()){
                    sqliteDirectory.createNewFile()
                }

                try {
                    val driveHelper = DriveManageHelper(context)
                    val databaseId = inputData.getString(KEY_DATABASE_ID)
                    driveHelper.dowloadFileByIdTo(databaseId!!, driveHelper.MIME_TYPE_SQLite3, sqliteDirectory)

                    return Result.success(workDataOf("Process" to 80))
                }catch (e: Exception){
                    d("hhjjjjjhhhhh", "SyncNewDevice.DownloadWorker e: $e")
                    return Result.failure(workDataOf(KEY_FILES to "fail"))
                }


            }

        }

        class SyncWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

            override fun doWork(): Result {

                val syncDirectory = File(context.filesDir, FOLDER_SYNC)
                val sqliteDirectory = File(syncDirectory, SQLITE_NAME)

                val deviceDB = DBFunctionHelper(context, SQLITE_NAME)
                val serverDB = DBFunctionHelper(context, sqliteDirectory.path)

                val user = FirebaseAuth.getInstance().currentUser
                val syncB = serverDB.functionSync.getByUid(user.uid)


                try {

                    d("hhjjjjjhhhhh", "syncB: " + syncB.id + " , " + syncB.databaseId)
                    val driveHelper = DriveManageHelper(context)

                    val uploadFiles = ArrayList<ModelTaskAttach>()
                    val newAttachList = deviceDB.functionTaskAttach.getDataList()
                    for(m in newAttachList){

                        val data = serverDB.functionTaskAttach.qury("select * from $TABLE_TASKATTACH where $COL_CREATEDATE like '${m.createDate}'")
                        if(data.isEmpty()){
                            uploadFiles.add(m)
                        }
                    }

                    val newCategoryInServer = serverDB.functionCategory.getDataList()
                    for(mCategory in newCategoryInServer){

                        val data = deviceDB.functionCategory.qury("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${mCategory.createDate}'")
                        if(data.isEmpty()){
                            deviceDB.functionCategory.insert(mCategory)

                        }else {
                            if(mCategory.remove == null){
                                if(mCategory.updateDate!! > data[0].updateDate!!){
                                    deviceDB.functionCategory.updateById(data[0].id, mCategory)
                                }
                            }else{
                                data[0].delete(context)
                            }
                        }
                    }

                    val newCategoryInDevice = deviceDB.functionCategory.getDataList()
                    for(mCategory in newCategoryInDevice){

                        val data = serverDB.functionCategory.qury("select * from $TABLE_CATEGORY where $COL_CREATEDATE like '${mCategory.createDate}'")
                        if(data.isEmpty()){
                            serverDB.functionCategory.insert(mCategory)
                        }else {
                            if(mCategory.remove == null){
                                if(mCategory.updateDate!! > data[0].updateDate!!){
                                    serverDB.functionCategory.updateById(data[0].id, mCategory)
                                }
                            }else{
                                serverDB.functionCategory.updateById(data[0].id, mCategory)
                                data[0].delete(context)
                            }
                        }
                    }

                    val newTaskInServer = serverDB.functionTask.getDataList()
                    for(taskInServer in newTaskInServer){

                        val mTask = deviceDB.functionTask.qury("select * from $TABLE_TASK where $COL_CREATEDATE like '${taskInServer.createDate}'")

                        if(mTask.isEmpty()){
                            d("jjjjjjj", "insert on Device: ")
                            if(taskInServer.remove == null){
                                DataManager().insertTask(taskInServer, serverDB, deviceDB)
                            }

                        }else {
                            if(taskInServer.remove == null){
                                if(taskInServer.updateDate!! > mTask[0].updateDate!!){
                                    d("jjjjjjj", "update on Device: ")
                                    DataManager().updateTask(mTask[0].id!!, taskInServer, serverDB, deviceDB)
                                }
                            }else{
                                mTask[0].flagDelete(context)
                            }
                        }
                    }

                    val newTaskInDevice = deviceDB.functionTask.getDataList()
                    for(taskInDevice in newTaskInDevice){

                        val mTask = serverDB.functionTask.qury("select * from $TABLE_TASK where $COL_CREATEDATE like '${taskInDevice.createDate}'")
                        if(mTask.isEmpty()){
                            d("jjjjjjj", "insert on Server: ")
                            DataManager().insertTask(taskInDevice, deviceDB, serverDB)
                        }else {

                            if(taskInDevice.remove == null){
                                if(taskInDevice.updateDate!! > mTask[0].updateDate!!){
                                    d("jjjjjjj", "update on Server: ")
                                    DataManager().updateTask(mTask[0].id!!, taskInDevice, deviceDB, serverDB)
                                }
                            }else{
                                DataManager().updateTask(mTask[0].id!!, taskInDevice, deviceDB, serverDB)
                                taskInDevice.delete(context)
                            }
                        }
                    }

                    val attachList = deviceDB.functionTaskAttach.getDataList()
                    for(m in attachList){
                        val file = File(m.path)

                        if(!file.exists()){
                            file.createNewFile()
                            d("jjjjjjj", "${m.type} insert on device: ")
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

                    for(m in uploadFiles){
                        val file = File(m.path)

                        val data = deviceDB.functionTaskAttach.qury("select * from $TABLE_TASKATTACH where $COL_CREATEDATE like '${m.createDate}'")
                        if(data.isEmpty()){
                            if(file.exists()){
                                file.delete()
                            }
                        }else{
                            d("jjjjjjj", "${m.type} insert on Server: ")
                            when(m.type){
                                FOLDER_IMAGE->{
                                    driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_IMAGE_JPG, syncB.filesId)
                                }
                                FOLDER_AUDIO->{
                                    driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_AUDIO, syncB.filesId)
                                }
                                FOLDER_VIDEO->{
                                    driveHelper.uploadFile(file, driveHelper.MIME_TYPE_FILE_VIDEO, syncB.filesId)
                                }
                            }
                        }
                    }

                    val updateDate = System.currentTimeMillis()
                    syncB.updateDate = updateDate
                    serverDB.functionSync.update(syncB)

                    return Result.success(workDataOf("Process" to 90, "UpdateDate" to updateDate))
                }catch (e: Exception){
                    d("hhjjjjjhhhhh", "SyncNewDevice.SyncWorker e: $e")
                    return Result.failure(workDataOf(KEY_FILES to "fail"))
                }

            }

        }

        class UpdateWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){
            override fun doWork(): Result {

                val updateDate = inputData.getLong("UpdateDate", 0)
                val user = FirebaseAuth.getInstance().currentUser


                try {

                    val driveHelper = DriveManageHelper(context)

                    val syncFolder = File(context.filesDir, FOLDER_SYNC)
                    val dbB = File(syncFolder, SQLITE_NAME)

                    val functionSyncServer = FunctionSync(context, dbB.path)
                    val syncServer = functionSyncServer.getByUid(user.uid)

                    d("hhjjjjjhhhhh", "modelSync: " + syncServer.databaseId)
                    driveHelper.update(syncServer.databaseId!!, dbB, driveHelper.MIME_TYPE_SQLite3)

                    val functionSync = FunctionSync(context)
                    functionSync.insert(syncServer)

                    return Result.success(workDataOf("Process" to 99, "UpdateDate" to updateDate))
                }catch (e: Exception){
                    d("hhjjjjjhhhhh", "SyncNewDevice.UpdateWorker e: $e")
                    return Result.failure(workDataOf(KEY_FILES to "fail"))
                }


            }
        }
    }

    class FinishWorker(private var context: Context, workerParams: WorkerParameters): Worker(context, workerParams){

        override fun doWork(): Result {

            //val user = FirebaseAuth.getInstance().currentUser
            //val myRef = Firebase.database.getReference(KEY_USER).child(user.uid)

            //val updateDate = inputData.getLong("UpdateDate", 0L)
            //myRef.child(KEY_LAST_SYNC_TIME).setValue(updateDate)
            //myRef.child(KEY_STATE_WRITE).setValue(NOT_WRITE)

            val syncFolder = File(context.filesDir, FOLDER_SYNC)
            if(syncFolder.isDirectory){
                syncFolder.listFiles().forEach {
                    it.delete()
                }
                syncFolder.delete()
            }

            return Result.success(workDataOf("Process" to 100))
        }
    }*/

}