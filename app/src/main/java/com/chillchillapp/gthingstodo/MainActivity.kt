package com.chillchillapp.gthingstodo

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.chillchillapp.gthingstodo.`interface`.Communicator
import com.chillchillapp.gthingstodo.dialog.AdsDialog
import com.chillchillapp.gthingstodo.fragment.MenuCalendarFragment
import com.chillchillapp.gthingstodo.fragment.MenuMenuFragment
import com.chillchillapp.gthingstodo.fragment.MenuStatisticsFragment
import com.chillchillapp.gthingstodo.fragment.MenuTaskFragment
import com.chillchillapp.gthingstodo.master.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.bgSyncingLL
import kotlinx.android.synthetic.main.activity_main.syncProgressBar
import kotlinx.android.synthetic.main.dialog_leaveapp.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import kotlin.collections.ArrayList

class MainActivity : BaseActivity() , Communicator{

    private var categoryId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        initLanguage()
        setTheme()
        setContentView(R.layout.activity_main)
        showPinReminderNotification()


        checkLerningApp()
        checkAutoSync()
        changeMenu("task")
        setEvent()

        d("hhjjjjjhhhhh", "app run")
    }

    override fun onResume() {
        super.onResume()
        setUpdateReminder()
        checkForUpdateAvailability()
    }

    override fun onBackPressed() {
        showLeaveDialog()
    }

    private fun checkLerningApp(){
        if(prefs!!.strLerningAddTask == "No"){//new user app
            showLerningAddTaskTarget()
        }else if(prefs!!.strLerningAddTask == "Yes" || prefs!!.strLerningCategory == "Yes"){
            showAdsDialog()
        }
    }

    private fun showLerningAddTaskTarget(){
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.menuInputRL)
            .setPrimaryText(getString(R.string.add_task))
            .setSecondaryText(getString(R.string.Record_your_to_do_list_here))
            .setPromptStateChangeListener { prompt, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                    prefs!!.strLerningAddTask = "Yes"
                }
            }
            .show()
    }

    private val pinReminderChannel = "com.chillchillapp.gthingstodo.input.tasks.activity"
    private val NOTIFICATION_ID = -1
    private fun showPinReminderNotification(){

        val remoteViews = RemoteViews(packageName, R.layout.view_notification_pin_reminder)

        val addTaskIntent = Intent(this, InputTasksActivity::class.java)
        addTaskIntent.putExtra(KEY_FUNCTION, KEY_INSERT)
        val discardIntent = Intent(this, MyCustomReceiver::class.java)
        discardIntent.action = KEY_DISCARD_PIN_REMINDER

        remoteViews.setOnClickPendingIntent(R.id.addTaskIV, PendingIntent.getActivity(this, 0, addTaskIntent, 0))
        remoteViews.setOnClickPendingIntent(R.id.cancelIV, PendingIntent.getBroadcast(this, 0, discardIntent, 0))
        remoteViews.setTextViewText(R.id.captionTV, getString(R.string.Do_it_in_yourself))
        remoteViews.setTextViewText(R.id.addTV, getString(R.string.add_task))
        val build = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                NotificationCompat.Builder(this, pinReminderChannel).apply {
                    setCustomContentView(remoteViews)
                    setSmallIcon(R.drawable.ic_todolist_stroke)
                    setContentIntent(getPendingIntent())
                    setAutoCancel(false)
                    setShowWhen(false)
                    setVibrate(null)
                    setSound(null)
                }
            }
            else -> {
                NotificationCompat.Builder(this).apply {
                    setSmallIcon(R.drawable.ic_todolist_stroke)
                    setContentIntent(getPendingIntent())
                    setCustomContentView(remoteViews)
                    setAutoCancel(false)
                    setShowWhen(false)
                    setVibrate(null)
                    setSound(null)
                }
            }
        }
        val notification = build.build()
        notification.flags = notification.flags or (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createChannel()
        notificationManager.notify(NOTIFICATION_ID, notification)




    }
    private fun NotificationManager.createChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelList = mutableListOf<NotificationChannel>()
            val channel = NotificationChannel(pinReminderChannel, "PinReminder", NotificationManager.IMPORTANCE_DEFAULT).apply {
                setSound(null, null)
                vibrationPattern = null
            }
            channelList.add(channel)
            this.createNotificationChannels(channelList)
        }
        //val descriptionText = "channel_description"
        //notificationChannel.description = descriptionText
    }
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        /*FLAG_ONE_SHOT คือ ใช้ได้ครั้งเดียว ถ้าเรียกซ้ำมันจะไม่ทำงาน
        FLAG_NO_CREATE คือ ไปเช็คก่อนว่า มีอยู่มัยถ้า มีอยู่แล้วมันจะไม่สร้างใหม่ พร้อมกับ return null
        FLAG_CANCEL_CURRENT คือ อันก่อนหน้า ถ้ามีอยู่แล้วจะถูกยกเลิก แล้วสร้างอันใหม่
        FLAG_UPDATE_CURRENT คือ ถ้ามีอยู่แล้ว จะทำการไปอัพเดท*/
    }

    private fun checkAutoSync(){

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
                            .observe(this) { workInfoList ->

                                d("ffffgdd", "Work Live Data: " + workInfoList.size)
                                val processList = ArrayList<Int>()

                                workInfoList.forEach {

                                    if (it.state.isFinished) {
                                        val fail = it.outputData.getString(KEY_FAIL)

                                        if (fail == KEY_FAIL) {
                                            message = KEY_FAIL
                                            processList.add(100)
                                            return@forEach
                                        } else {
                                            val process = it.outputData.getInt("Process", 0)
                                            processList.add(process)
                                            d("ffffgdd", "Process: $process")
                                        }

                                    }
                                }

                                val valueMax = max(processList)
                                progressBarHelper?.setProcess(valueMax)
                                d("ffffgdd", "valueMax: $valueMax")

                                if (valueMax == 100) {

                                    if (message != KEY_FAIL) {
                                        changeMenu(fragmentCurrent)
                                        hideProcessSyncing()

                                        prefs!!.longLastAutoSync = System.currentTimeMillis()
                                        Toast.makeText(this,
                                            getString(R.string.The_work_hasbeen_synced_to_your_google_dive_account),
                                            Toast.LENGTH_SHORT).show()

                                    } else {
                                        hideProcessSyncing()
                                        Toast.makeText(this,
                                            getString(R.string.Failed_to_sync_tasks_to_your_google_dive_account),
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

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

    private fun showAdsDialog(){
        val dialog = AdsDialog(this)
        dialog.setMyEvent(object : AdsDialog.MyEvent{
            override fun onMyEventListener(status: String) {
                if(status == "showAds"){
                    dialog.show()
                }
            }
        })
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

            if(prefs!!.strLerningAddTask == "No"){//new user app
                prefs!!.strLerningAddTask = "Yes"
            }

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

                //set color image
                menuTaskIV.background.setTint(themeColor(R.attr.colorAccent))
                menuCalendarIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuStatisticsIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuMenuIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))

                //set color text
                menuTaskTV.setTextColor(themeColor(R.attr.colorAccent))
                menuCalendarTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuStatisticsTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuMenuTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))

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


                menuTaskIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuCalendarIV.background.setTint(themeColor(R.attr.colorAccent))
                menuStatisticsIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuMenuIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))

                menuTaskTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuCalendarTV.setTextColor(themeColor(R.attr.colorAccent))
                menuStatisticsTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuMenuTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))

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

                menuTaskIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuCalendarIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuStatisticsIV.background.setTint(themeColor(R.attr.colorAccent))
                menuMenuIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))

                menuTaskTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuCalendarTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuStatisticsTV.setTextColor(themeColor(R.attr.colorAccent))
                menuMenuTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))


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


                menuTaskIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuCalendarIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuStatisticsIV.background.setTint(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuMenuIV.background.setTint(themeColor(R.attr.colorAccent))

                menuTaskTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuCalendarTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuStatisticsTV.setTextColor(ContextCompat.getColor(this, R.color.colorWhiteDarkDark))
                menuMenuTV.setTextColor(themeColor(R.attr.colorAccent))

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

    private val REQUEST_UPDATE_APP_AVAILABILITY = 1001
    private fun checkForUpdateAvailability(){
        val appUpdateManager = AppUpdateManagerFactory.create(this)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                d("hhjjjjjhhhhh", "UPDATE_AVAILABLE")

                try{
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this,
                        REQUEST_UPDATE_APP_AVAILABILITY)
                }catch (e: IntentSender.SendIntentException){
                    d("hhjjjjjhhhhh", "checkForUpdateAvailability: " + e.message)
                }
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_UPDATE_APP_AVAILABILITY) {
            if (resultCode != RESULT_OK) {
                Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }
}
