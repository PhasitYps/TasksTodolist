package com.chillchillapp.gthingstodo.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.chillchillapp.gthingstodo.InputTasksActivity
import com.chillchillapp.gthingstodo.R
import com.chillchillapp.gthingstodo.database.*
import com.chillchillapp.gthingstodo.model.ModelTask
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdapTaskHistory(val activity: Activity, private val dataList: ArrayList<ModelTask>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val TYPE_DATE = 1
    private val TYPE_TASK = 2

    private val functionTask = FunctionTask(activity)
    private val functionAttachTask = FunctionTaskAttach(activity)
    private val functionCategory = FunctionCategory(activity)
    private val functionSubTask = FunctionTaskSub(activity)

    private val sp: SharedPreferences = activity.getSharedPreferences("Setting", Context.MODE_PRIVATE)
    private val editer: SharedPreferences.Editor = sp!!.edit()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType){
            TYPE_DATE->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_date_history, parent, false)
                ViewHolderDate(view)
            }
            TYPE_TASK->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_task_history, parent, false)
                ViewHolderTask(view)
            }
            else->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_task_history, parent, false)
                ViewHolderTask(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(dataList[position].typeView){
            TYPE_TASK->{
                ViewHolderTask(holder.itemView).setDetail(position)
            }
            TYPE_DATE->{
                ViewHolderDate(holder.itemView).setDetail(position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        Log.i("gefwehhhhh", "getItemViewType typeView [$position]: " + dataList[position].typeView!!)
        return dataList[position].typeView!!

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolderDate(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val dateTV = itemView.findViewById<TextView>(R.id.dateTV)

        fun setDetail(position: Int){
            dateTV.text = formatDate("MM/dd - HH", Date(dataList[position].updateDate!!))+":00 "+ activity.getString(R.string.clock)
        }
    }

    inner class ViewHolderTask(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemCV = itemView.findViewById<CardView>(R.id.itemCV)
        val nameTV = itemView.findViewById<TextView>(R.id.nameTV)
        val stateIV = itemView.findViewById<ImageView>(R.id.stateIV)
        val favoriteIV = itemView.findViewById<ImageView>(R.id.favoriteIV)
        val imageCateIV = itemView.findViewById<ImageView>(R.id.iconCateIV)

        val textDueDateTV = itemView.findViewById<TextView>(R.id.textDueDateTV)
        val imageSubTaskIV = itemView.findViewById<ImageView>(R.id.imageSubTaskIV)
        val imageAttackTaskIV = itemView.findViewById<ImageView>(R.id.imageAttackTaskIV)

        val itemFavoriteRL = itemView.findViewById<RelativeLayout>(R.id.itemFavoriteRL)
        val itemStateRL = itemView.findViewById<RelativeLayout>(R.id.itemStateRL)

        init {
            imageAttackTaskIV.visibility = View.GONE
            imageSubTaskIV.visibility = View.GONE
            textDueDateTV.visibility = View.GONE
        }

        fun setDetail(position: Int){
            setName(this, position)
            setDate(this, position)
            setIcon(this, position)
            setState(this, position)
            setFavorite(this, position)
            setEvent(this, position)
        }

        private fun setName(holder: AdapTaskHistory.ViewHolderTask, position: Int){
            holder.nameTV.text = dataList[position].name
        }

        private fun setDate(holder: AdapTaskHistory.ViewHolderTask, position: Int){

            holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))

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

            val calDueDate = Calendar.getInstance()
            calDueDate.timeInMillis = dataList[position].dueDate!!

            var dueDateStr = ""
            if(calDueDate.timeInMillis != 0L){

                if(calDueDate.timeInMillis < calStart.timeInMillis && dataList[position].state == 0L){
                    dueDateStr = formatDate("MM-dd", calDueDate.time) + " "
                    holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorRed))
                }
                if(calDueDate.timeInMillis > calEnd.timeInMillis){
                    dueDateStr = formatDate("MM-dd", calDueDate.time) + " "
                }

                val hours = dataList[position].hour!!
                val minute = dataList[position].minute!!
                if(hours != -1 && minute != -1){
                    calDueDate.set(Calendar.HOUR_OF_DAY, hours)
                    calDueDate.set(Calendar.MINUTE, minute)
                    dueDateStr += "${formatDate("HH:mm", calDueDate.time)}"
                    if(dataList[position].state == 0L && calDueDate.timeInMillis <= System.currentTimeMillis()){
                        holder.textDueDateTV.setTextColor(ContextCompat.getColor(activity, R.color.colorRed))
                    }
                }

                if(dueDateStr != ""){
                    holder.textDueDateTV.visibility = View.VISIBLE
                    holder.textDueDateTV.text = dueDateStr
                }else{
                    holder.textDueDateTV.visibility = View.GONE
                }
            }
        }

        private fun setIcon(holder: AdapTaskHistory.ViewHolderTask, position: Int){
            val taskId = dataList[position].id
            val categoryId = dataList[position].categoryId
            val modelCategory = functionCategory.getById(categoryId)

            Glide.with(activity).load(modelCategory.image).into(holder.imageCateIV)

            //set icon
            if(functionAttachTask.getDataByTaskId(taskId).size > 0){
                holder.imageAttackTaskIV.visibility = View.VISIBLE
            }
            if(functionSubTask.getDataByTaskId(taskId).size > 0L){
                holder.imageSubTaskIV.visibility = View.VISIBLE
            }
        }

        private fun setFavorite(holder: AdapTaskHistory.ViewHolderTask, position: Int){
            when(dataList[position].favorite){
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

        private fun setState(holder: AdapTaskHistory.ViewHolderTask, position: Int){

            when(dataList[position].state){

                1L ->{updateStateTodo(position, holder, "done")}

                0L ->{updateStateTodo(position, holder, "don't")}
            }
        }

        private fun setEvent(holder: AdapTaskHistory.ViewHolderTask, position: Int){

            holder.itemFavoriteRL.setOnClickListener {

                dataList[position].favorite = if(dataList[position].favorite == 0L){
                    1
                }else{
                    0
                }

                setFavorite(holder, position)

                if(updateTask(dataList[position]) != 0){
                    Toast.makeText(activity, activity.getString(R.string.added_to_star), Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemStateRL.setOnClickListener {
                dataList[position].state = if(dataList[position].state != 1L){
                    1
                }else{
                    0
                }

                setState(holder, position)

                updateTask(dataList[position])
                dataList.removeAt(position)
                notifyItemRemoved(position)
                notifyDataSetChanged()

                editer!!.putLong("update", 1)
                editer!!.commit()
            }

            holder.itemCV.setOnClickListener{
                val intent = Intent(activity, InputTasksActivity::class.java)
                intent.putExtra(COL_TASK_ID, dataList[position].id)
                intent.putExtra("function", "Update")
                activity.startActivity(intent)
            }
        }

        private fun updateTask(modelTask: ModelTask): Int{
            when(modelTask.state){
                1L -> modelTask.completeDate = System.currentTimeMillis()
                0L -> modelTask.completeDate = null
            }
            modelTask.updateDate = System.currentTimeMillis()
            return functionTask.update(modelTask)
        }

        private fun strikethroughSpan(text: String): SpannableString {
            val ss = SpannableString(text)
            ss.setSpan(StrikethroughSpan(), 0, text.length, 0)
            return ss
        }

        private fun updateStateTodo(position: Int, holder: AdapTaskHistory.ViewHolderTask, state: String){

            when(state){
                "done"->{

                    //holder.stateIV.setColorFilter(activity.getColorTheme(R.attr.colorAccent))
                    holder.stateIV.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_ok_filled))
                    holder.nameTV.setTextColor(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))

                    val ss = strikethroughSpan(dataList[position].name!!)
                    holder.nameTV.setText(ss)

                }
                "don't"->{

                    holder.stateIV.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_round))
                    holder.stateIV.setColorFilter(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))
                    holder.nameTV.setTextColor(ContextCompat.getColor(activity, R.color.colorBlack))

                    holder.nameTV.setText(dataList[position].name)

                }
            }
        }


    }

    private fun formatDate(formatStr: String, day: Date): String{
        val format = SimpleDateFormat(formatStr)
        val dateStr = format.format(day)
        return dateStr
    }


    private fun themeColor(context: Context, @AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute (attrRes, typedValue, true)
        return typedValue.data
    }

}