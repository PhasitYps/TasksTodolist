package com.chillchillapp.tasks.todolist.master

import android.content.Context
import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.*
import java.util.*
import com.chillchillapp.tasks.todolist.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.DriveScopes


class DriveManageHelper(private val context: Context) {

    val MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"
    val MIME_TYPE_SQLite3 = "application/x-sqlite3"
    val MIME_TYPE_FILE_IMAGE_JPG = "image/jpeg"
    val MIME_TYPE_FILE_IMAGE_PNG = "image/png"
    val MIME_TYPE_FILE_VIDEO = "video/mp4"
    val MIME_TYPE_FILE_AUDIO = "audio/mp3"
    val MIME_TYPE_FILE_TEXT = "text/plain"

    private val KEY_APPDATAFOLDER = "appDataFolder"
    private val KEY_DRIVE = "drive"

    val driveService = driveService()!!

    fun getFileList(fileName: String?, selectMimeType: Array<String>): FileList{

        var mimeType = ""
        for(mime in selectMimeType){
            if(mimeType == ""){
                mimeType += " mimeType = '$mime'"
            }else{
                mimeType += " or mimeType = '$mime'"
            }
        }

        val result = driveService.files().list()
            .setQ("name = '$fileName' and $mimeType")
            .setSpaces(KEY_APPDATAFOLDER) // Supported values are 'drive' and 'appDataFolder'.
            .execute()

        return result
    }

    fun createFolerInDrive(name: String): File {
        val fileMetadata = File()
        fileMetadata.name = name
        fileMetadata.mimeType = MIME_TYPE_FOLDER
        fileMetadata.parents = Collections.singletonList(KEY_APPDATAFOLDER) // Supported values are 'root' and 'appDataFolder' and fileId.

        val file: File = driveService.files().create(fileMetadata)
            .setFields("id, name")
            .execute()

        return file
    }

    fun uploadFile(file: java.io.File, mimeType: String): File{
        val fileMetadata = File()
        fileMetadata.parents = Collections.singletonList(KEY_APPDATAFOLDER)
        fileMetadata.name = file.name
        fileMetadata.mimeType = mimeType

        val mediaContent = FileContent(mimeType, file)
        val googleFile: File = driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, name, parents")
            .execute()

        return googleFile
    }

    fun uploadFileInFolder(folderId: String, fileName: String, filePath: java.io.File, type: String): File{
        val fileMetadata = File()
        fileMetadata.parents = Collections.singletonList(folderId)
        fileMetadata.name = fileName
        fileMetadata.mimeType = type

        val mediaContent = FileContent(type, filePath)
        val googleFile: File = driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, name, parents")
            .execute()

        Log.i("hhjjjjjhhhhh", "file: " + googleFile.name + " size "
                + (filePath.length() / 1024).toFloat() + "KB --> Up load finish. is " + googleFile.name)

        return googleFile
    }

    fun update(fileId: String, filePath: java.io.File, mimeType: String){

        val fileMetadata = File()
        fileMetadata.name = filePath.name
        fileMetadata.mimeType = mimeType

        val mediaContent = FileContent(mimeType, filePath)

        driveService.files().update(fileId, fileMetadata, mediaContent)
            .setFields("id, name")
            .execute()
    }

    fun dowloadFile(fileName: String, mimeType: String): ByteArrayOutputStream? {
        return try {
            val result: FileList = driveService.files().list()
                .setQ("name = '$fileName' and mimeType = '$mimeType'")
                .setSpaces(KEY_APPDATAFOLDER)
                .setFields("nextPageToken, files(id, name, parents)")
                .execute()

            Log.d("hhjjjjjhhhhh", "result: " + result.files.size)

            val outputStream = ByteArrayOutputStream()
            driveService.files().get(result.files[0].id)
                .executeMediaAndDownloadTo(outputStream)

            outputStream
        }catch (e: Exception){
            Log.d("hhjjjjjhhhhh", "e: $e")
            null
        }
    }

    fun dowloadFileByIdTo(fileId: String, mimeType: String, toPath: java.io.File){

        val inputStream = driveService.files().get(fileId).executeMediaAsInputStream()

        val bis = BufferedInputStream(inputStream)
        val buffer = ByteArray(1024)
        var bytesread = 0

        try {
            val outStream = FileOutputStream(toPath)
            while (bis.read(buffer).also { bytesread = it } != -1) {
                outStream.write(buffer, 0, bytesread)
            }
            outStream.flush()
            bis.close()
            outStream.close()
        } catch (e: FileNotFoundException) {
            Log.i("hhjjjjjhhhhh", "FileNotFoundException: "+e)
        } catch (e: IOException) {
            Log.i("hhjjjjjhhhhh", "IOException: "+ e)
        } finally {

        }
    }

    fun dowloadFileTo(fileName: String, selectMimeType: Array<String>, downloadTo:java.io.File): File?{
        try {
            var mimeType = ""
            for(mime in selectMimeType){
                if(mimeType == ""){
                    mimeType += " mimeType = '$mime'"
                }else{
                    mimeType += " or mimeType = '$mime'"
                }
            }
            val result: FileList = driveService.files().list()
                .setQ("name = '$fileName' and $mimeType")
                .setSpaces(KEY_APPDATAFOLDER)
                .setFields("nextPageToken, files(id, name, parents)")
                .execute()

            Log.d("hhjjjjjhhhhh", "result: " + result.files.size)


            driveService.files().get(result.files[0].id)
                .executeMediaAndDownloadTo(FileOutputStream(downloadTo))


            return result.files[0]

        }catch (e: Exception){
            Log.d("hhjjjjjhhhhh", "e: $e")
            return null
        }
    }

    fun delete(fileId: String){
        driveService.files().delete(fileId).execute()
    }

    private fun driveService() : Drive?{
        GoogleSignIn.getLastSignedInAccount(context)?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA))
            credential.selectedAccount = googleAccount.account!!
            return Drive.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        }
        return null
    }

}