package com.chillchillapp.gthingstodo.widget

import android.os.Handler
import android.os.Looper

open class Timer(listener: OnTimerTickListener,private val delay: Long){

    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var isTimer = false
    private var duration = 0L

    interface OnTimerTickListener{
        fun onTimerTick(duration: Long, timeStr: String)
    }

    init {

        isTimer = false
        duration = 0L

        runnable = Runnable {
            duration += delay
            listener.onTimerTick(duration, format(duration))

            handler.postDelayed(runnable, delay)
        }
    }

    fun start(){
        if(!isTimer){
            handler.postDelayed(runnable, delay)
            isTimer = true
        }
    }

    fun pause(){
        if(isTimer){
            handler.removeCallbacks(runnable)
            isTimer = false
        }
    }

    fun stop(){
        if(isTimer){
            handler.removeCallbacks(runnable)
            duration = 0L
            isTimer = false
        }
    }

    private fun format(duration: Long): String {
        val millis = duration % 1000
        val sec = (duration / 1000) % 60
        val min = (duration / (1000 * 60)) % 60
        val hours = (duration / (1000 * 60 * 60))

        return if (hours > 0)
            "%02d:%02d:%02d.%02d".format(hours, min, sec, millis / 10)
        else
            "%02d:%02d.%02d".format(min, sec, millis / 10)

    }

}