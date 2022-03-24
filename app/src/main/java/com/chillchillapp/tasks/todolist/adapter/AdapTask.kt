package com.chillchillapp.tasks.todolist.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chillchillapp.tasks.todolist.*
import com.chillchillapp.tasks.todolist.database.*
import com.chillchillapp.tasks.todolist.master.RepeatHelper
import com.chillchillapp.tasks.todolist.model.ModelTask
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AdapTask(private val activity: Activity, private var taskList: ArrayList<ModelTask>) : RecyclerView.Adapter<AdapTask.ViewHolder>() {

    private val TAG = "AdapTask"

    interface OnUpdateTaskListener {
        fun onUpdateStateListenner()
        fun onUpdateRepeatListener()
    }

    private var mInterstitialAd: InterstitialAd? = null
    fun setAds(){

        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(activity, activity.getString(R.string.Ads_Interstitial_AddTask_UnitId), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }
    private fun showAds(){
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_task, parent, false)
        return ViewHolder(view)
    }

    private val functionCategory = FunctionCategory(activity)
    private val functionTask = FunctionTask(activity)
    private val functionSubTask = FunctionTaskSub(activity)
    private val functionAttachTask = FunctionTaskAttach(activity)
    private val functionRepeat = FunctionRepeat(activity)
    private val functionReminder = FunctionTaskReminder(activity)

    private val masterRepeat = RepeatHelper(activity)

    private var listener: OnUpdateTaskListener? = null
    fun setOnUpdateStateListener(listener: OnUpdateTaskListener){
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        functionTask.open()
        functionAttachTask.open()
        functionCategory.open()

        setName(holder, position)
        setDate(holder, position)
        setIcon(holder, position)
        setFavorite(holder, position)
        setState(holder, position)
        setEvent(holder, position)
    }

    private fun setName(holder: ViewHolder, position: Int){
        holder.nameTV.text = taskList[position].name
    }

    private fun setDate(holder: ViewHolder, position: Int){

        val calDueDate = Calendar.getInstance()
        calDueDate.timeInMillis = taskList[position].dueDate!!

        if(calDueDate.timeInMillis != 0L){

            val calStart = Calendar.getInstance()
            calStart[Calendar.MILLISECOND] = 0
            calStart[Calendar.SECOND] = 0
            calStart[Calendar.MINUTE] = 0
            calStart[Calendar.HOUR_OF_DAY] = calStart.getActualMinimum(Calendar.HOUR_OF_DAY)

            val calEnd = Calendar.getInstance()
            calEnd[Calendar.MILLISECOND] = calEnd.getActualMaximum(Calendar.MILLISECOND)
            calEnd[Calendar.SECOND] = 59
            calEnd[Calendar.MINUTE] = 59
            calEnd[Calendar.HOUR_OF_DAY] = calEnd.getActualMaximum(Calendar.HOUR_OF_DAY)

            val calCurrent = Calendar.getInstance()

            if(calDueDate.timeInMillis in calStart.timeInMillis until calEnd.timeInMillis){
                val hours = taskList[position].hour!!
                val minute = taskList[position].minute!!

                if(hours != -1 && minute != -1){
                    calDueDate.set(Calendar.HOUR_OF_DAY, hours)
                    calDueDate.set(Calendar.MINUTE, minute)

                    holder.textDueDateTV.text = "${formatDate("HH:mm", calDueDate.time)}"
                    holder.textDueDateTV.visibility = View.VISIBLE

                    if(calDueDate.timeInMillis < calCurrent.timeInMillis && taskList[position].state == 0L ){
                        holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorRed))
                    }else{
                        holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))
                    }
                }else{
                    holder.textDueDateTV.visibility = View.GONE
                }

            }else{
                val hours = taskList[position].hour!!
                val minute = taskList[position].minute!!

                if(hours != -1 && minute != -1){
                    calDueDate.set(Calendar.HOUR_OF_DAY, hours)
                    calDueDate.set(Calendar.MINUTE, minute)

                    holder.textDueDateTV.text = "${formatDate("MM-dd HH:mm", calDueDate.time)}"
                    holder.textDueDateTV.visibility = View.VISIBLE

                    if(calDueDate.timeInMillis < calCurrent.timeInMillis && taskList[position].state == 0L ){
                        holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorRed))
                    }else{
                        holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))
                    }

                }else{

                    holder.textDueDateTV.text = formatDate("MM-dd", calDueDate.time)
                    holder.textDueDateTV.visibility = View.VISIBLE

                    //dueDate < start
                    if(calDueDate.timeInMillis < calCurrent.timeInMillis && taskList[position].state == 0L){
                        holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorRed))
                    }else{
                        holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))
                    }
                }
            }
        } else{
            holder.textDueDateTV.visibility = View.GONE
        }
    }

    private fun setIcon(holder: ViewHolder, position: Int){
        val taskId = taskList[position].id
        val categoryId = taskList[position].categoryId
        val modelCategory = functionCategory.getById(categoryId)
        val modelRepeat = functionRepeat.getByTaskId(taskId)

        //set icon category
        if(modelCategory.id != null){
            Glide.with(activity).load(modelCategory.image).into(holder.iconCateIV)
            holder.iconCateIV.visibility = View.VISIBLE
        }else{
            holder.iconCateIV.visibility = View.GONE
        }

        //set icon attach
        if(functionAttachTask.getDataByTaskId(taskId).size > 0){
            holder.iconAttachTaskIV.visibility = View.VISIBLE
        }else{
            holder.iconAttachTaskIV.visibility = View.GONE
        }
        //set icon subtask
        if(functionSubTask.getDataByTaskId(taskId).size > 0){
            holder.iconSubTaskIV.visibility = View.VISIBLE
        }else{
            holder.iconSubTaskIV.visibility = View.GONE
        }

        //set icon reminder
        if(functionReminder.getReminderByTaskId(taskId).size > 0){
            holder.iconReminderIV.visibility = View.VISIBLE
        }else{
            holder.iconReminderIV.visibility = View.GONE
        }

        //set icon repeat
        if(modelRepeat.id != null){
            holder.iconRepeatIV.visibility = View.VISIBLE
        }else{
            holder.iconRepeatIV.visibility = View.GONE
        }

    }

    private fun setFavorite(holder: ViewHolder, position: Int){
        when(taskList[position].favorite){
            1L ->{
                holder.favoriteIV.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_star_filled))
                holder.favoriteIV.setColorFilter(themeColor(activity, R.attr.colorAccent))
            }
            0L ->{
                holder.favoriteIV.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_star))
                holder.favoriteIV.setColorFilter(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))
            }
        }
    }

    private fun setState(holder: ViewHolder, position: Int){

        when(taskList[position].state){

            1L -> updateStateTodo(position, holder, "done")

            0L -> updateStateTodo(position, holder, "don't")
        }
    }

    private fun setEvent(holder: ViewHolder, position: Int){

        holder.itemFavoriteRL.setOnClickListener {

            taskList[position].favorite = if(taskList[position].favorite == 0L){
                1
            }else{
                0
            }

            setFavorite(holder, position)

            if(updateTask(taskList[position]) != 0){
                Toast.makeText(activity, "เพิ่มในรายการโปรดเรียบร้อย", Toast.LENGTH_SHORT).show()
            }
        }

        holder.itemStateRL.setOnClickListener {

            when(taskList[position].state){
                0L->{
                    taskList[position].state = 1
                    showAds()
                }
                1L->{
                    taskList[position].state = 0
                }
            }

            setState(holder, position)

            // is repeat
            val modelRepeat = functionRepeat.getByTaskId(taskList[position].id)
            when(modelRepeat.id){
                null->{
                    updateTask(taskList[position])
                    Handler().postDelayed({
                        listener?.onUpdateStateListenner()
                    }, 500)
                }
                else->{


                    masterRepeat.copyTask(taskList[position].id)
                    masterRepeat.setUpdateRepeat(taskList[position].id)

                    Handler().postDelayed({
                        listener?.onUpdateRepeatListener()
                    }, 500)
                }
            }
        }

        holder.itemCV.setOnClickListener{
            val intent = Intent(activity, InputTasksActivity::class.java)
            intent.putExtra(COL_TASK_ID, taskList[position].id)
            intent.putExtra("function", "Update")
            activity.startActivity(intent)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemCV = itemView.findViewById<CardView>(R.id.itemCV)
        val nameTV = itemView.findViewById<TextView>(R.id.nameTV)
        val stateIV = itemView.findViewById<ImageView>(R.id.stateIV)
        val favoriteIV = itemView.findViewById<ImageView>(R.id.favoriteIV)
        val textDueDateTV = itemView.findViewById<TextView>(R.id.textDueDateTV)

        val iconSubTaskIV = itemView.findViewById<ImageView>(R.id.iconSubtaskIV)
        val iconAttachTaskIV = itemView.findViewById<ImageView>(R.id.iconAttackTaskIV)
        val iconRepeatIV = itemView.findViewById<ImageView>(R.id.iconRepeatIV)
        val iconCateIV = itemView.findViewById<ImageView>(R.id.iconCateIV)
        val iconReminderIV = itemView.findViewById<ImageView>(R.id.iconReminderIV)

        val itemFavoriteRL = itemView.findViewById<RelativeLayout>(R.id.itemFavoriteRL)
        val itemStateRL = itemView.findViewById<RelativeLayout>(R.id.itemStateRL)

    }

    private fun updateTask(modelTask: ModelTask): Int{
        when(modelTask.state){
            1L -> modelTask.completeDate = System.currentTimeMillis()
            0L -> modelTask.completeDate = null
        }

        modelTask.updateDate = System.currentTimeMillis()
        return functionTask.update(modelTask)
    }

    private fun formatDate(formatStr: String, day: Date): String{
        val format = SimpleDateFormat(formatStr)
        val dateStr = format.format(day)
        return dateStr
    }

    private fun strikethroughSpan(text: String): SpannableString {
        val ss = SpannableString(text)
        ss.setSpan(StrikethroughSpan(), 0, text.length, 0)
        return ss
    }

    private fun updateStateTodo(position: Int, holder: ViewHolder, state: String){

        when(state){
            "done"->{

                //holder.stateIV.setColorFilter(activity.getColorTheme(R.attr.colorAccent))
                holder.stateIV.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_ok_filled))
                holder.nameTV.setTextColor(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))

                val ss = strikethroughSpan(taskList[position].name!!)
                holder.nameTV.setText(ss)

            }
            "don't"->{

                holder.stateIV.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_round))
                holder.stateIV.setColorFilter(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))
                holder.nameTV.setTextColor(ContextCompat.getColor(activity, R.color.colorBlack))

                holder.nameTV.setText(taskList[position].name)

            }
        }
    }

    private fun themeColor(context: Context, @AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute (attrRes, typedValue, true)
        return typedValue.data
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

}