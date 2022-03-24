package com.chillchillapp.tasks.todolist.dialog

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.CheckBox
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.model.ModelTaskReminder
import kotlinx.android.synthetic.main.dialog_setting_reminder.*
import java.util.*
import kotlin.collections.ArrayList

class SetReminderDialog(private val activity: Activity): Dialog(activity) {

    interface OnMyEvent{
        fun OnClickPositive(selectItemList: ArrayList<ModelTaskReminder>)
    }
    private var l: OnMyEvent? = null
    fun setOnMyEvent(l: OnMyEvent){
        this.l = l
    }

    private var selectItemList: ArrayList<ModelTaskReminder> = ArrayList()
    private val dataList: ArrayList<ModelTaskReminder> = ArrayList()


    init{
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_setting_reminder)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)
        show()

        //op = option
        dataList.add(ModelTaskReminder(null, null, "op1", 0, Calendar.MINUTE,null, null))
        dataList.add(ModelTaskReminder(null, null, "op2", 5, Calendar.MINUTE,null, null))
        dataList.add(ModelTaskReminder(null, null, "op3", 10, Calendar.MINUTE,null, null))
        dataList.add(ModelTaskReminder(null, null, "op4", 15, Calendar.MINUTE,null, null))
        dataList.add(ModelTaskReminder(null, null, "op5", 30, Calendar.MINUTE,null, null))
        dataList.add(ModelTaskReminder(null, null, "op6", 1, Calendar.DATE,null, null))
        dataList.add(ModelTaskReminder(null, null, "op7", 2, Calendar.DATE,null, null))

        openNotifySw.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                bgStateRL.visibility = View.GONE
            }else{
                bgStateRL.visibility = View.VISIBLE
            }
        }

        chk1.tag = 0
        chk2.tag = 1
        chk3.tag = 2
        chk4.tag = 3
        chk5.tag = 4
        chk6.tag = 5
        chk7.tag = 6

    }

    fun setDataList(selectReminderList: ArrayList<ModelTaskReminder>){
        this.selectItemList = selectReminderList

        selectItemList.forEach {
            Log.d("hhjjjjjhhhhh",
                "input id: " + it.id + " ,taskId: " + it.taskId + " , optionId: " + it.optionId)
        }

        setDetail()
        setEvent()
    }

    private fun setDetail(){

        when{
            selectItemList.isEmpty()->{

                openNotifySw.isChecked = false
                bgStateRL.visibility = View.VISIBLE
            }

            selectItemList.isNotEmpty()->{

                openNotifySw.isChecked = true
                bgStateRL.visibility = View.GONE

                for(i in selectItemList.indices){

                    when(selectItemList[i].optionId){

                        dataList[0].optionId-> chk1.isChecked = true

                        dataList[1].optionId-> chk2.isChecked = true

                        dataList[2].optionId-> chk3.isChecked = true

                        dataList[3].optionId-> chk4.isChecked = true

                        dataList[4].optionId-> chk5.isChecked = true

                        dataList[5].optionId-> chk6.isChecked = true

                        dataList[6].optionId-> chk7.isChecked = true
                    }
                }
            }
        }
    }

    private fun setEvent(){

        negativeTV.setOnClickListener {
            dismiss()
        }

        positiveTV.setOnClickListener {
            if(openNotifySw.isChecked){

                chk1.checkAdd()
                chk2.checkAdd()
                chk3.checkAdd()
                chk4.checkAdd()
                chk5.checkAdd()
                chk6.checkAdd()
                chk7.checkAdd()

                l?.OnClickPositive(selectItemList)

            }else{
                selectItemList.clear()
                l?.OnClickPositive(selectItemList)
            }

            dismiss()
        }
    }

    private fun CheckBox.checkAdd(){
        val position = tag.toString().toInt()
        if(isChecked){
            val index = selectItemList.linearSearch(dataList[position].optionId!!)
            if(index <= -1){
                selectItemList.add(dataList[position])
            }
        }else{
            val index = selectItemList.linearSearch(dataList[position].optionId!!)
            Log.i("hhjjjjjhhhhh", "index: " + index)
            if(index > -1){
                selectItemList.removeAt(index)
            }
        }
    }

    private fun ArrayList<ModelTaskReminder>.linearSearch(optionId: String): Int{
        for (i in this.indices){
            if(optionId == this[i].optionId){
                return i
            }
        }
        return -1
    }


}