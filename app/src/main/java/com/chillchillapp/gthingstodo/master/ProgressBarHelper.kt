package com.chillchillapp.gthingstodo.master

import android.os.Handler
import android.widget.ProgressBar

class ProgressBarHelper(progressBar: ProgressBar) {

    private var progress: Int = 0
    private var max: Int = 100
    private var unit:Int = 0
    private var delayMillis: Long = 1000
    private var animate: Long = 2000

    init {
        progressBar.max = max
        progressBar.progress = unit

    }

    private var handler = Handler()

    private var runnable = object : Runnable{
        override fun run() {

            if(unit < progress){
                unit += 1
                val value = (unit * 100/max)
                progressBar.progress = value
            }

            if(progress < max){
                delayMillis = if(progress != 0){
                    animate/progress
                }else{
                    1000
                }

                handler.postDelayed(this, delayMillis)
            }

        }
    }

    fun start(){

        delayMillis = if(progress != 0){
            animate/progress
        }else{
            1000
        }
        handler.postDelayed(runnable, delayMillis)
    }

    fun setMax(max: Int){
        this.max = max
    }

    fun setProcess(progress: Int){
        this.progress = progress
    }

    fun setAnimate(millis: Long){
        this.animate = millis
    }
}