package com.chillchillapp.tasks.todolist

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetMenu(
    private val activity: Activity,
    private val modelMenu: ArrayList<ModelMenuBottomSheet>,
    val itemClick: BottomSheetMenu.SelectListener
) {

    init {
        showBottomSheetDialog()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetView: View = activity.layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
        val bottomSheetDialog = BottomSheetDialog(activity, R.style.SheetDialog)
        bottomSheetDialog.setContentView(bottomSheetView)

        var dataRCV = bottomSheetDialog.findViewById<RecyclerView>(R.id.dataRCV)

        /*val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheetView.parent as View)
        val bottomSheetCallback: BottomSheetCallback = object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // do something
                Log.i("Jfkdjkfjdfdf","onStateChanged")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // do something
                Log.i("Jfkdjkfjdfdf","onSlide")
            }
        }

        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback)*/

        setDataMenuAdapter(dataRCV, bottomSheetDialog)
        bottomSheetDialog.show()

    }

    private fun setDataMenuAdapter(dataRCV: RecyclerView?, bottomSheetDialog: BottomSheetDialog) {
        dataRCV!!.adapter = DataAdapter(bottomSheetDialog)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        dataRCV!!.layoutManager = layoutManager
    }

    inner class DataAdapter(private val bottomSheetDialog: BottomSheetDialog) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_listview_bottomsheet_menu, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val m  = modelMenu[position]
            holder.titleTV.text = m.title
            holder.descriptionTV.text = if (m.description.isNullOrEmpty()) {
                holder.descriptionTV.visibility = View.GONE
                ""
            } else {
                holder.descriptionTV.visibility = View.VISIBLE
                m.description
            }
            holder.imageIV.setImageResource(m.icon)

            holder.itemCV.setOnClickListener {
                itemClick.onMyClick(m, position)
                bottomSheetDialog.dismiss()
            }


        }


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var itemCV: CardView = itemView.findViewById(R.id.itemCV)

            var imageIV: ImageView = itemView.findViewById(R.id.imageIV)
            var titleTV: TextView = itemView.findViewById(R.id.titleTV)
            var descriptionTV: TextView = itemView.findViewById(R.id.descriptionTV)


        }

        override fun getItemCount(): Int {
            return modelMenu.size
        }


    }

    interface SelectListener {
        fun onMyClick(m: ModelMenuBottomSheet, position: Int)
    }

    data class ModelMenuBottomSheet(
        val title:String,
        val description:String,
        val icon:Int
    )
}