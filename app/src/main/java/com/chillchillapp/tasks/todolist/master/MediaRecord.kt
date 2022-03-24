package com.chillchillapp.tasks.todolist.master

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.widget.Timer
import java.io.File
import java.io.IOException

class MediaRecord(private var context: Context) {

    private var fileName: String = "audio_draft.mp3"
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var timer: Timer? = null
    private val LOG_TAG = "hhhhhhggg"

    private var l: Timer.OnTimerTickListener? = null
    fun setTimer(l: Timer.OnTimerTickListener){
        this.l = l
    }

    fun getMaxAmplitude(): Int{
        return if(recorder != null){
            recorder!!.maxAmplitude!!
        }else{
            -1
        }
    }

    fun onRecord(start: String){
        when(start) {
            "start" -> {//เริ่ม
                startRecording()
            }
            "stop" -> {//หยุด
                stopRecording()
            }
            "pause"->{//พัก
                pauseRecording()
            }
            "resume"->{//บันทึกต่อ
                resumeRecording()
            }
        }
    }

    fun onPlay(start: String){
        when(start) {
            "start" -> {
                startPlaying()
            }
            "stop" -> {
                stopPlaying()
            }
        }
    }

    private fun startRecording() {

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(getDataFilePath())
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
                Toast.makeText(context, context.getString(R.string.fail), Toast.LENGTH_SHORT).show()
                return
            }

        }
        recorder!!.start()

        if(l != null){
            timer = Timer(l!!, 100)
            timer?.start()
        }

        Log.e(LOG_TAG, "prepare start")
    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pause()
            timer?.pause()
            Log.e(LOG_TAG, "pause")
        }else{
            stopRecording()
            Log.e(LOG_TAG, "stop")
        }

    }

    private fun stopRecording() {
        recorder?.apply {
            //stop()
            release()
        }
        recorder = null
        timer?.stop()

        Log.e(LOG_TAG, "stop release")
    }

    private fun resumeRecording(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.resume()
            timer?.start()
            Log.e(LOG_TAG, "resume")
        }
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(getDataFilePath())
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }

        Log.e(LOG_TAG, "startPlaying")
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    fun getDataFilePath():String{
        val file = File(context!!.filesDir, fileName)
        return file.path
    }

}