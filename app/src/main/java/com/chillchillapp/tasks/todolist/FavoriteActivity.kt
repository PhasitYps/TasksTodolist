package com.chillchillapp.tasks.todolist

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chillchillapp.tasks.todolist.adapter.AdapTask
import com.chillchillapp.tasks.todolist.database.FunctionTask
import com.chillchillapp.tasks.todolist.model.ModelTask
import kotlinx.android.synthetic.main.activity_favorite.*

class FavoriteActivity : BaseActivity() {

    private lateinit var functionTask: FunctionTask

    private val taskList = ArrayList<ModelTask>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_favorite)

        init()
        setAdap()
        setEvent()
    }

    private fun setEvent(){

        backIV.setOnClickListener {
            finish()
        }
    }

    private fun init(){
        functionTask = FunctionTask(this)
        functionTask.open()

        taskList.addAll(functionTask.getFavoriteList())

    }

    private fun setAdap(){
        dataRCV.adapter = AdapTask(this, taskList)
        dataRCV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}