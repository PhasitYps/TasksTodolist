package com.chillchillapp.gthingstodo.dialog

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.Window
import com.chillchillapp.gthingstodo.R
import kotlinx.android.synthetic.main.dialog_process_sync.*

class ProcessSyncDialog(context: Context): Dialog(context) {

    private var max: Int = 100
    private var process: Int = 20
    private var unit: Int = 0
    private var delayMillis: Long = 0
    private var animate: Long = 2000

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_process_sync)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)

        unitProcessTV.text =  "${(unit * 100/max)}%"
    }

    private var handler = Handler()

    private var runnable = object : Runnable{
        override fun run() {

            if(unit < process){
                unit += 1
                unitProcessTV.text =  "${(unit * 100/max)}%"
            }

            if(process <= max && unit < max){
                delayMillis = if(process != 0){
                    animate/process
                }else{
                    1000
                }

                handler.postDelayed(this, delayMillis)
            }else{
                dismiss()
            }

        }
    }

    fun start(){

        delayMillis = if(process != 0){
            animate/process
        }else{
            1000
        }
        handler.postDelayed(runnable, delayMillis)
    }

    fun setMax(max: Int){
        this.max = max
    }

    fun setProcess(process: Int){
        this.process = process
    }

    fun setAnimate(millis: Long){
        this.animate = millis
    }

    fun setUnit(unit: Int){
        this.unit = unit
    }
}