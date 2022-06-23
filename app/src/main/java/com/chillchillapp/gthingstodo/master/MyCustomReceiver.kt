package com.chillchillapp.gthingstodo.master

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.chillchillapp.gthingstodo.KEY_DISCARD_PIN_REMINDER
import com.chillchillapp.gthingstodo.KEY_NOTIFICATION
import com.chillchillapp.gthingstodo.database.*
import com.chillchillapp.gthingstodo.model.ModelTaskReminder
import kotlin.collections.ArrayList


class MyCustomReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action.equals(KEY_NOTIFICATION)){

            val functionReminder = FunctionTaskReminder(context)
            val reminderList = functionReminder.getReminderCurrent()

            sendNotification(context, reminderList)
        }

        if(intent.action.equals(KEY_DISCARD_PIN_REMINDER)){
            NotificationManagerCompat.from(context).cancel(-1)
        }

    }


    private fun sendNotification(context: Context, reminderArr: ArrayList<ModelTaskReminder>){

        val functionTask = FunctionTask(context)
        val notificationManageMaster = NotificationHelper(context)
        notificationManageMaster.createChannelTask()

        for(m in reminderArr){
            val task = functionTask.getTaskById(m.taskId!!)
            if(task.id != null){
                notificationManageMaster.startNotify(m.id!!, task)
            }
        }

        notificationManageMaster.setUpdateReminder()
    }


    /*private fun displayNotificationDialog(){
        val d = Dialog(context)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.dialog_notifications)
        d.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        d.window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        d.window!!.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        d.window!!.setGravity(Gravity.TOP)
        d.window!!.setDimAmount(0f)
        d.setCancelable(true)
        d.show()

        d.taskNameTV.text = titleTask
        d.dueTimeTV.text = dueTime

        Toast.makeText(context, "notification!! "+ d.dueTimeTV.text, Toast.LENGTH_SHORT).show()

        d.understandTV.setOnClickListener {
            d.dismiss()
        }

        d.verifyTV.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            d.dismiss()
        }
    }*/
}