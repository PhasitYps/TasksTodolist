package com.chillchillapp.tasks.todolist.master

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.chillchillapp.tasks.todolist.MainActivity
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.database.FunctionCategory
import com.chillchillapp.tasks.todolist.model.ModelTask
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory

import android.util.Log
import com.chillchillapp.tasks.todolist.KEY_NOTIFICATION
import com.chillchillapp.tasks.todolist.database.COL_REMINDER_BASETIME
import com.chillchillapp.tasks.todolist.database.FunctionTaskReminder


class NotificationHelper(private var context: Context) {

    private val taskReminderChannel = "com.chillchillapp.tasks.todolist.task.reminder"
    private val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannelTask(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelList = mutableListOf<NotificationChannel>()
            channelList.add(NotificationChannel(taskReminderChannel, "TaskReminder", NotificationManager.IMPORTANCE_HIGH))
            notificationManager.createNotificationChannels(channelList)
        }
    }

    fun startNotify(reminderId: Long, model: ModelTask){

        Log.i("hhhhhhhhhhhhf", "ModelTask id = " + model.id)
        if(model.id != null){
            val id = hash(reminderId)
            val notification = createNotificationTask(model)
            notificationManager.notify(id, notification.build())

            Log.i("hhhhhhhhhhhhf", "notify: $id, " + model.name)
        }
    }

    fun setUpdateReminder(){
        val functionTaskReminder = FunctionTaskReminder(context)
        val reminderList = functionTaskReminder.getReminder()

        Log.i("hhhhhhhhhhhhf", "number of notification: " + reminderList.size)

        if(reminderList.isNotEmpty()){
            reminderList.sortBy { it.baseTime }

            val model = reminderList.first()
            val intent = Intent(context, MyCustomReceiver::class.java)
            intent.action = KEY_NOTIFICATION
            intent.putExtra(COL_REMINDER_BASETIME, model.baseTime)
            val pendingIntent = PendingIntent.getBroadcast(context, 0 , intent, 0)

            val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, model.baseTime!!, pendingIntent)

            Log.i("hhhhhhhhhhhhf", "set time alarm at: " + formatDate("HH:mm:ss", Date(model.baseTime!!)))

        }
    }

    private fun createNotificationTask(model: ModelTask): NotificationCompat.Builder{
        val calDueDate = Calendar.getInstance()
        calDueDate.timeInMillis = model.dueDate!!
        calDueDate.set(Calendar.HOUR_OF_DAY, model.hour!!)
        calDueDate.set(Calendar.MINUTE, model.minute!!)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val functionCategory = FunctionCategory(context)
        val modelCategory = functionCategory.getById(model.categoryId)
        val bitmap = BitmapFactory.decodeByteArray(modelCategory.image, 0, modelCategory.image!!.size)

        val build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, taskReminderChannel)
                .setSmallIcon(R.drawable.ic_todolist)
                .setLargeIcon(bitmap)
                .setContentTitle(model.name)
                .setContentText(formatDate("HH:mm", calDueDate.time))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        } else {
            NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_todolist)
                .setLargeIcon(bitmap)
                .setContentTitle(model.name)
                .setContentText(formatDate("HH:mm", calDueDate.time))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        }

        return build
    }

    private fun formatDate(formateStr: String, date: Date): String{
        val format = SimpleDateFormat(formateStr)
        return format.format(date)
    }

    private fun hash(x: Long): Int{
        return (x % 100).toInt()
    }

}