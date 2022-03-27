package com.chillchillapp.tasks.todolist

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_synchronizetion.*

import android.util.Log.d
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.work.WorkManager
import com.chillchillapp.tasks.todolist.dialog.ProcessSyncDialog
import com.chillchillapp.tasks.todolist.dialog.SignOutDialog
import com.chillchillapp.tasks.todolist.master.SyncHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.collections.ArrayList


class SynchronizetionActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setContentView(R.layout.activity_synchronizetion)

        val autoSync = prefs!!.boolAutoSync
        if(autoSync){
            autoSyncSW.isChecked = true
        }

        updateUser()
        setTextLastSyncTime()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        updateUser()
    }

    private var valueMax = 0
    private var message = ""
    private var startTime = System.currentTimeMillis()
    private fun setWorkDataLive(){

        valueMax = 0
        message = ""
        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData(KEY_SYNC).observe(
            this, { workInfoList->

                d("ffffgdd", "Work Live Data: " + workInfoList.size)
                val processList = ArrayList<Int>()

                workInfoList.forEach {

                    if(it.state.isFinished){

                        val fail = it.outputData.getString(KEY_FAIL)

                        if(fail != null){
                            message = fail
                            processList.add(100)
                            return@forEach
                        }else{
                            val process = it.outputData.getInt("Process", 0)
                            processList.add(process)

                            d("ffffgdd", "Process: $process")
                        }

                    }
                }

                val value = max(processList)

                if(value > valueMax){
                    valueMax = max(processList)
                    dialog_sync_process?.setProcess(valueMax)

                    d("ffffgdd", "valueMax: $valueMax")
                }

                if(valueMax == 100){

                    if(message != KEY_FAIL){

                        prefs!!.longLastAutoSync = System.currentTimeMillis()
                        /*editor!!.putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                        editor!!.commit()*/

                        setTextLastSyncTime()
                        Toast.makeText(this, "ซิงค์งานเข้ากับบัญชี google ไดฟ์เรียบร้อย", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "ซิงค์งานเข้ากับบัญชี google ไดฟ์ของคุณไม่สำเร็จ โปรดตรวจสอบเครือข่ายของคุณเเละลองอีกครั้ง", Toast.LENGTH_SHORT).show()
                    }

                    var endTime = System.currentTimeMillis()
                    d("hhjjjjjhhhhh", "start time: " + formatDate("HH:mm:ss", Date(startTime)))
                    d("hhjjjjjhhhhh", "end time: " + formatDate("HH:mm:ss", Date(endTime)))
                    d("hhjjjjjhhhhh", "Time: " + (endTime - startTime)/1000 + " sec." )
                }
            }
        )

    }

    private fun setEvent(){

        signLL.setOnClickListener {
            val user = auth.currentUser
            if(user == null){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        syncSafetyLL.setOnClickListener {
            if(isInternetAvailable()){
                syncing()
            }else{
                Toast.makeText(this, "Network error, Pleace check your connection and try again.", Toast.LENGTH_SHORT).show()
            }
        }

        autoSyncSW.setOnClickListener {
            if(autoSyncSW.isChecked){
                if(isInternetAvailable()){
                    //set auto sync
                    syncing()
                    

                }else{
                    Toast.makeText(this, "Network error, Pleace check your connection and try again.", Toast.LENGTH_SHORT).show()
                }
                prefs!!.boolAutoSync = true
                /*editor!!.putBoolean(KEY_AUTO_SYNC, true)
                editor!!.commit()*/
            }else{
                //set not auto sync
                prefs!!.boolAutoSync = false
                /*editor!!.putBoolean(KEY_AUTO_SYNC, false)
                editor!!.commit()*/
            }
        }

        menuIV.setOnClickListener {
            showMenuPopup(it)
        }

        backIV.setOnClickListener {
            finish()
        }

    }

    private fun showMenuPopup(v: View){

        val popupMenu = PopupMenu(this, v)
        popupMenu.menu.add(getString(R.string.sign_out))
        popupMenu.show()

        popupMenu.menu.getItem(0).setOnMenuItemClickListener {
            showSignOutDialog()
            true
        }

    }

    private fun showSignOutDialog(){
        val d = SignOutDialog(this)
        d.setOnMyEvent(object : SignOutDialog.OnMyEvent{
            override fun OnPositiveListener(it: Task<Void>) {
                if(it.isSuccessful){
                    updateUser()
                }
            }
        })

    }

    private fun syncing() {
        //synchronize

        getDriveService().let { drive ->
            if(drive != null){

                startTime = System.currentTimeMillis()
                valueMax = 0
                showProcessSyncDialog()
                val syncHelper = SyncHelper(this)
                syncHelper.syncing()

                setWorkDataLive()
            }
        }
    }


    private fun max(arrayList: ArrayList<Int>):Int{
        var value = 0
        arrayList.forEach {
            if(it > value){
                value = it
            }
        }
        return value
    }

    private fun getDriveService() : Drive?{
        GoogleSignIn.getLastSignedInAccount(this)?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(this, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA))
            credential.selectedAccount = googleAccount.account!!
            return Drive.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(getString(R.string.app_name))
                .build()
        }
        return null
    }

    private fun updateUser(){

        val user = auth.currentUser
        if(user != null){
            emailTV.text = user!!.email
            menuIV.visibility = View.VISIBLE
        }else{
            emailTV.text = getString(R.string.click_for_login_google)
            menuIV.visibility = View.GONE
        }
    }

    private fun setTextLastSyncTime(){
        val syncLastTime = prefs!!.longLastAutoSync

        syncLastTV.text = if(syncLastTime != null && syncLastTime != 0L){
            "Last sync time: " + formatDate("dd/MM/yyyy HH:mm:ss", Date(syncLastTime))
        }else{
            "ยังไม่ได้ซิงค์"
        }
    }


    private var dialog_sync_process: ProcessSyncDialog? = null
    private fun showProcessSyncDialog(){
        if(dialog_sync_process == null){
            dialog_sync_process = ProcessSyncDialog(this)
        }

        dialog_sync_process!!.setMax(100)
        dialog_sync_process!!.setProcess(40)
        dialog_sync_process!!.setAnimate(500)
        dialog_sync_process!!.setUnit(0)
        dialog_sync_process!!.start()
        dialog_sync_process!!.show()

    }

    private fun hideProcessSyncDialog(){
        dialog_sync_process?.dismiss()
        if(dialog_sync_process != null){
            dialog_sync_process = null
        }
    }

}