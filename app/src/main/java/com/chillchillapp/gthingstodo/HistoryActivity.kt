package com.chillchillapp.gthingstodo

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chillchillapp.gthingstodo.adapter.AdapTaskHistory
import com.chillchillapp.gthingstodo.database.*
import com.chillchillapp.gthingstodo.model.ModelTask
import kotlinx.android.synthetic.main.activity_history.*
import java.util.*
import kotlin.collections.ArrayList

class HistoryActivity : BaseActivity() {

    private var functionTask: FunctionTask? = null

    private val dataTaskList = ArrayList<ModelTask>()

    private val TYPE_DATE = 1
    private val TYPE_TASK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_history)

        init()
        database()
        setEvent()
    }

    private fun init(){
        functionTask = FunctionTask(this)
        functionTask!!.open()

    }

    private fun database(){
        val historyTaskList = functionTask!!.getHistory()
        var dateStr = ""

        historyTaskList.sortBy { it.completeDate }
        historyTaskList.reverse()

        for(m in historyTaskList){
            val completeDateStr = formatDate("MM/dd/HH", Date(m.completeDate!!))
            if(dateStr != completeDateStr){
                dateStr = completeDateStr
                val model = ModelTask()
                model.typeView = TYPE_DATE
                model.updateDate = m.updateDate!!
                dataTaskList.add(model)
            }
            m.typeView = TYPE_TASK
            dataTaskList.add(m)
        }

        setAdap()

    }

    private fun setAdap(){
        val adapter = AdapTaskHistory(this, dataTaskList)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dataRCV.adapter = adapter
        dataRCV.layoutManager = layoutManager

    }

    private fun setEvent(){

        backRL.setOnClickListener {
            finish()
        }
    }

}