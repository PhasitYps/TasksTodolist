package com.chillchillapp.tasks.todolist.dialog

import android.app.ActionBar
import android.app.Dialog
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import com.chillchillapp.tasks.todolist.BaseActivity
import com.chillchillapp.tasks.todolist.FOLDER_DRAFT
import com.chillchillapp.tasks.todolist.master.MediaRecord
import com.chillchillapp.tasks.todolist.R
import com.chillchillapp.tasks.todolist.model.ModelTaskAttach
import com.chillchillapp.tasks.todolist.widget.Timer
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet_audio_record.*
import java.io.File
import java.io.FileOutputStream

class AudioRecordBottomSheet(private var activity: BaseActivity, private var theme:Int):  BottomSheetDialog(activity, theme){

    private var mr: MediaRecord? = null
    private var timer: Timer? = null

    interface OnMyEvent{
        fun OnSave(model: ModelTaskAttach)
    }

    private var l: OnMyEvent? = null
    fun setOnMyEvent(l: OnMyEvent){
        this.l = l
    }

    init {
        setContentView(layoutInflater.inflate(R.layout.bottom_sheet_audio_record, null))
        setCancelable(true)
        show()

        //init
        bgRecordLL.visibility = View.VISIBLE
        bgStartRecordLL.visibility = View.GONE

        val amplitudes = ArrayList<Int>()

        for(i in 1..100){
            amplitudes.add(0)
        }

        val maxSpikes = waveformSeekBar.maxProgress.toInt()

        //waveformSeekBar.stopNestedScroll()

        timer = Timer(object : Timer.OnTimerTickListener{
            override fun onTimerTick(duration: Long, timeStr: String) {
                timerTV.text = timeStr

                amplitudes.add(mr!!.getMaxAmplitude())
                val amps = amplitudes.takeLast(maxSpikes).toIntArray()
                waveformSeekBar.setSampleFrom(amps)
                waveformSeekBar.invalidate()

                println(mr!!.getMaxAmplitude())
            }
        }, 100L)

        setEvent()
    }

    private fun setEvent(){

        microphoneRecordRL!!.setOnClickListener {

            bgRecordLL.visibility = View.GONE
            bgStartRecordLL.visibility = View.VISIBLE

            stateRecordIV.setImageResource(R.drawable.ic_pause_filled)
            stateRecordIV.tag = 1

            mr = MediaRecord(activity)
            mr!!.onRecord("start")
            timer!!.start()

        }

        recordSaveRL.setOnClickListener {
            addAudio()
            timer!!.stop()
            dismiss()

        }

        recordStartRL.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                when(stateRecordIV.tag){
                    0->{
                        stateRecordIV.setImageResource(R.drawable.ic_pause_filled)
                        stateRecordIV.tag = 1
                        mr!!.onRecord("resume")
                        timer!!.start()
                    }
                    1->{
                        stateRecordIV.setImageResource(R.drawable.ic_play_filled)
                        stateRecordIV.tag = 0
                        mr!!.onRecord("pasue")
                        timer!!.pause()
                    }
                }

            }else{
                showConfirmSaveAudioDialog()
            }
        }

        recordCancelRL.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stateRecordIV.setImageResource(R.drawable.ic_play_filled)
                stateRecordIV.tag = 0
                mr!!.onRecord("pasue")
                timer!!.pause()
            }

            showConfirmDeleteAudioDialog()

        }

        setOnDismissListener {
            mr?.onRecord("stop")
            timer!!.stop()
        }
    }

    private fun addAudio(){
        mr?.onRecord("stop")
        val model = ModelTaskAttach()

        val filename = "${System.currentTimeMillis()}.mp3"
        val draftFolder = File(activity.filesDir, FOLDER_DRAFT)
        val audioFile = File(draftFolder, filename)

        val byte = activity.contentResolver.openInputStream(activity.convertUri(mr!!.getDataFilePath()))?.readBytes()

        try {

            val out = FileOutputStream(audioFile)
            out.write(byte)
            out.close()
            model.path = audioFile.path
            Log.i("fewfw", "Saved to ${model.path}")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("fewfw", "Exception: " + e)
        }


        model.type = "audio"
        l?.OnSave(model)

        Log.i("fewf", "  audioRCV.adapter!!.notifyDataSetChanged()")
    }

    private fun showConfirmDeleteAudioDialog(){
        val d = Dialog(activity)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.dialog_confirm)
        d.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        d.window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        d.setCancelable(false)
        d.show()

        val contentTV = d.findViewById<TextView>(R.id.contentTV)
        val cancelTV = d.findViewById<TextView>(R.id.negativeTV)
        val selectTV = d.findViewById<TextView>(R.id.positiveTV)

        contentTV.text = activity.getString(R.string.are_you_sure_you_want_to_delete_the_recording)

        selectTV.setOnClickListener {
            mr!!.onRecord("stop")
            dismiss()
            d.dismiss()

            resetDetail()
        }

        cancelTV.setOnClickListener {
            d.dismiss()
        }
    }

    private fun showConfirmSaveAudioDialog(){
        val d = Dialog(activity)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.dialog_confirm)
        d.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        d.window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        d.setCancelable(false)
        d.show()

        val contentTV = d.findViewById<TextView>(R.id.contentTV)
        val cancelTV = d.findViewById<TextView>(R.id.negativeTV)
        val selectTV = d.findViewById<TextView>(R.id.positiveTV)

        contentTV.text = activity.getString(R.string.do_you_want_to_end_this_recording)
        selectTV.text = activity.getString(R.string.save)

        selectTV.setOnClickListener {
            mr!!.onRecord("stop")
            addAudio()

            d.dismiss()
            dismiss()

        }

        cancelTV.setOnClickListener {
            d.dismiss()
        }
    }

    private fun resetDetail(){

    }

}