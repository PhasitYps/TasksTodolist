package com.chillchillapp.gthingstodo

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chillchillapp.gthingstodo.adapter.AdapTask
import com.chillchillapp.gthingstodo.database.FunctionTask
import com.chillchillapp.gthingstodo.model.ModelTask
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

        backRL.setOnClickListener {
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