package com.chillchillapp.tasks.todolist

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.util.Log.d
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.work.WorkManager
import com.chillchillapp.tasks.todolist.`interface`.Communicator
import com.chillchillapp.tasks.todolist.database.FunctionTask
import com.chillchillapp.tasks.todolist.database.FunctionTaskReminder
import com.chillchillapp.tasks.todolist.fragment.MenuCalendarFragment
import com.chillchillapp.tasks.todolist.fragment.MenuMenuFragment
import com.chillchillapp.tasks.todolist.fragment.MenuStatisticsFragment
import com.chillchillapp.tasks.todolist.fragment.MenuTaskFragment
import com.chillchillapp.tasks.todolist.master.*
import com.chillchillapp.tasks.todolist.model.ModelTaskReminder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.bgSyncingLL
import kotlinx.android.synthetic.main.activity_main.syncProgressBar
import kotlinx.android.synthetic.main.dialog_leaveapp.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity() , Communicator{

    private var categoryId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        initLanguage()

        setTheme()
        setContentView(R.layout.activity_main)

        setAutoSync()
        changeMenu("task")
        setEvent()

        d("hhjjjjjhhhhh", "app run")


//        val functionReminder = FunctionTaskReminder(this)
//        val functionTask = FunctionTask(this)
//        val model = functionTask.getTaskById(2)
//        val reminderList = functionReminder.getReminderByTaskId(model.id)
//
//        for(m in reminderList){
//            val choice = java.util.ArrayList<ModelTaskReminder>()
//            choice.add(ModelTaskReminder(optionId =  "op1", reminderCount = 0, reminderType = Calendar.MINUTE))
//            choice.add(ModelTaskReminder(optionId =  "op2", reminderCount = 5, reminderType = Calendar.MINUTE))
//            choice.add(ModelTaskReminder(optionId =  "op3", reminderCount = 10, reminderType = Calendar.MINUTE))
//            choice.add(ModelTaskReminder(optionId =  "op4", reminderCount = 15, reminderType = Calendar.MINUTE))
//            choice.add(ModelTaskReminder(optionId =  "op5", reminderCount = 30, reminderType = Calendar.MINUTE))
//            choice.add(ModelTaskReminder(optionId =  "op6", reminderCount = 1, reminderType = Calendar.DATE))
//            choice.add(ModelTaskReminder(optionId =  "op7", reminderCount = 2, reminderType = Calendar.DATE))
//
//            choice.forEachIndexed{i, mc ->
//                d("sadasdaegejr", "index: $i")
//                val cal = Calendar.getInstance()
//                cal.timeInMillis = model.dueDate!!
//                mc.setNotifyTime(cal, model.hour!!, model.minute!!)
//
//                if (mc.notifyTime == m.notifyTime) {
//                    m.optionId = mc.optionId
//                    m.reminderCount = mc.reminderCount
//                    m.reminderType = mc.reminderType
//                    d("sadasdaegejr", "return: $i")
//                    return@forEachIndexed
//                }
//            }
//
//            d("sadasdaegejr", "next task")
//
//        }
//
//        for(m in reminderList){
//            d("sadasdaegejr", "reminder: " + m.optionId)
//        }



    }


    private fun setAutoSync(){

        getDriveService().let { drive->
            if(drive != null){

                val autoSync = prefs!!.boolAutoSync
                if(autoSync){

                    if(isInternetAvailable()){

                        showProcessSyncing()
                        val syncHelper = SyncHelper(this)
                        syncHelper.syncing()

                        var message = ""
                        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData(KEY_SYNC)
                            .observe(this, { workInfoList->

                                d("ffffgdd", "Work Live Data: " + workInfoList.size)
                                val processList = ArrayList<Int>()

                                workInfoList.forEach {

                                    if(it.state.isFinished){
                                        val fail = it.outputData.getString(KEY_FAIL)

                                        if(fail == KEY_FAIL){
                                            message = KEY_FAIL
                                            processList.add(100)
                                            return@forEach
                                        }else{
                                            val process = it.outputData.getInt("Process", 0)
                                            processList.add(process)
                                            d("ffffgdd", "Process: $process")
                                        }

                                    }
                                }

                                val valueMax = max(processList)
                                progressBarHelper?.setProcess(valueMax)
                                d("ffffgdd", "valueMax: $valueMax")

                                if(valueMax == 100){

                                    if(message == KEY_FAIL){
                                        changeMenu(fragmentCurrent)
                                        hideProcessSyncing()

                                        prefs!!.longLastAutoSync = System.currentTimeMillis()
                                        Toast.makeText(this, getString(R.string.The_work_hasbeen_synced_to_your_google_dive_account), Toast.LENGTH_SHORT).show()

                                    }else{
                                        hideProcessSyncing()
                                        Toast.makeText(this, getString(R.string.Failed_to_sync_tasks_to_your_google_dive_account), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })

                    }else{
                        hideProcessSyncing()
                        Toast.makeText(this, "Network error, Pleace check your connection and try again.", Toast.LENGTH_SHORT).show()
                    }

                }else{
                    hideProcessSyncing()
                }
            }
        }

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

    private var progressBarHelper: ProgressBarHelper? = null
    private fun showProcessSyncing(){
        bgSyncingLL.visibility = View.VISIBLE
        if(progressBarHelper == null){
            progressBarHelper = ProgressBarHelper(syncProgressBar)
            progressBarHelper!!.setMax(100)
            progressBarHelper!!.setProcess(0)
            progressBarHelper!!.setAnimate(500)
            progressBarHelper!!.start()
        }

    }

    private fun hideProcessSyncing(){
        bgSyncingLL.visibility = View.GONE
        progressBarHelper = null
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

    override fun onResume() {
        super.onResume()
        setUpdateReminder()
    }

    override fun onBackPressed() {
        showLeaveDialog()
    }

    override fun OnSelectCategory(cateId: String) {
        categoryId = cateId
    }

    private fun showLeaveDialog(){
        val d = Dialog(this)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.dialog_leaveapp)
        d.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        d.window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        d.setCancelable(true)
        d.show()

        d.positiveTV.setOnClickListener {
            d.dismiss()
            finish()
        }
        d.negativeTV.setOnClickListener {
            d.dismiss()
        }

        d.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                d.cancel()
                true
            }
            false
        }

        d.setOnCancelListener {
            finish()
        }

    }

    private fun setEvent(){

        menu_taskRL.setOnClickListener {
            changeMenu("task")
        }

        menu_calendarRL.setOnClickListener {
            changeMenu("calendar")
        }
        menu_meRL.setOnClickListener {
            changeMenu("statistics")
        }

        menu_settingRL.setOnClickListener {
            changeMenu("menu")
        }

        menuInputRL.setOnClickListener {
            val intent = Intent(this, InputTasksActivity::class.java)
            intent.putExtra("categoryId", categoryId)
            intent.putExtra(KEY_FUNCTION, KEY_INSERT)
            startActivity(intent)
        }

    }

    private var fragmentCurrent = ""
    private fun changeMenu(menu: String) {
        fragmentCurrent = menu

        when (menu) {
            "task" -> {
                menuTaskIV.setBackgroundResource(R.drawable.ic_task_press)
                menuCalendarIV.setBackgroundResource(R.drawable.ic_calendar)
                menuStatisticsIV.setBackgroundResource(R.drawable.ic_statistics)
                menuMenuIV.setBackgroundResource(R.drawable.ic_menu)

                menuTaskIV.background.setTint(themeColor(R.attr.colorAccent))

                val bundle = Bundle()
                bundle.putString("categoryId", categoryId)

                val menuTaskFragment = MenuTaskFragment()
                menuTaskFragment.arguments = bundle

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, menuTaskFragment)
                    commit()
                }

            }
            "calendar" -> {
                menuTaskIV.setBackgroundResource(R.drawable.ic_task)
                menuCalendarIV.setBackgroundResource(R.drawable.ic_calendar_press)
                menuStatisticsIV.setBackgroundResource(R.drawable.ic_statistics)
                menuMenuIV.setBackgroundResource(R.drawable.ic_menu)

                menuCalendarIV.background.setTint(themeColor(R.attr.colorAccent))

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, MenuCalendarFragment())
                    commit()
                }

            }
            "statistics" -> {
                menuTaskIV.setBackgroundResource(R.drawable.ic_task)
                menuCalendarIV.setBackgroundResource(R.drawable.ic_calendar)
                menuStatisticsIV.setBackgroundResource(R.drawable.ic_statistics_press)
                menuMenuIV.setBackgroundResource(R.drawable.ic_menu)

                menuStatisticsIV.background.setTint(themeColor(R.attr.colorAccent))

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, MenuStatisticsFragment())
                    commit()
                }

            }
            "menu" -> {
                menuTaskIV.setBackgroundResource(R.drawable.ic_task)
                menuCalendarIV.setBackgroundResource(R.drawable.ic_calendar)
                menuStatisticsIV.setBackgroundResource(R.drawable.ic_statistics)
                menuMenuIV.setBackgroundResource(R.drawable.ic_menu_press)

                menuMenuIV.background.setTint(themeColor(R.attr.colorAccent))

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, MenuMenuFragment())
                    commit()
                }
            }
        }
    }

    private fun setUpdateReminder(){
        val notificationManageMaster = NotificationHelper(this)
        notificationManageMaster.setUpdateReminder()

    }
}
