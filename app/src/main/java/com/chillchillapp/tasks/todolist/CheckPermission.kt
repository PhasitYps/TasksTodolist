package com.chillchillapp.tasks.todolist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.chillchillapp.tasks.todolist.database.FunctionCategory
import com.chillchillapp.tasks.todolist.model.ModelCategory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import android.app.NotificationManager

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.chillchillapp.tasks.todolist.master.MyCustomReceiver
import java.io.File


class CheckPermission : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_check_permission)

        showPinReminderNotification()
        timerCount()

    }

    private val pinReminderChannel = "com.chillchillapp.tasks.todolist.input.tasks.activity"
    private fun showPinReminderNotification(){

        val remoteViews = RemoteViews(packageName, R.layout.view_notification_pin_reminder)

        val addTaskIntent = Intent(this, InputTasksActivity::class.java)
        addTaskIntent.putExtra(KEY_FUNCTION, KEY_INSERT)
        val discardIntent = Intent(this, MyCustomReceiver::class.java)
        discardIntent.action = KEY_DISCARD_PIN_REMINDER

        remoteViews.setOnClickPendingIntent(R.id.addTaskIV, PendingIntent.getActivity(this, 0, addTaskIntent, 0))
        remoteViews.setOnClickPendingIntent(R.id.cancelIV, PendingIntent.getBroadcast(this, 0, discardIntent, 0))
        remoteViews.setTextViewText(R.id.captionTV, getString(R.string.Every_morning_starts_a_new_page_in_your_story))
        remoteViews.setTextViewText(R.id.addTV, getString(R.string.add_task))
        val build = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                NotificationCompat.Builder(this, pinReminderChannel).apply {
                    setCustomContentView(remoteViews)
                    setSmallIcon(R.drawable.ic_todolist_stroke)
                    setContentIntent(getPendingIntent())
                    setAutoCancel(false)
                    setShowWhen(false)
                }
            }
            else -> {
                NotificationCompat.Builder(this).apply {
                    setSmallIcon(R.drawable.ic_todolist_stroke)
                    setContentIntent(getPendingIntent())
                    setCustomContentView(remoteViews)
                    setAutoCancel(false)
                    setShowWhen(false)
                }
            }
        }
        val notification = build.build()
        notification.flags = notification.flags or (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createChannel()
        notificationManager.notify(-1, notification)




    }
    private fun NotificationManager.createChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelList = mutableListOf<NotificationChannel>()
            channelList.add(NotificationChannel(pinReminderChannel, "PinReminder", NotificationManager.IMPORTANCE_LOW))
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



    var T: Timer? = null
    var count = 0
    private fun timerCount() {
        T = Timer()
        T!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (count == 1) {

                        if(!isSetCategoryDefault()){
                            val intent = Intent(applicationContext, InitiateLanguageActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else {
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    count++
                }
            }
        }, 0, 1000)
    }



    override fun onDestroy() {
        super.onDestroy()
        T?.cancel()
    }

    private fun isSetCategoryDefault(): Boolean{
        val setData = prefs!!.intInsertCategoryDefault
        return setData == 1
    }

    /*private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray, ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setData()
            timerCount()
        }else{
            finish()
        }
    }*/
}