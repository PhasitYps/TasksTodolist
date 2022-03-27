package com.chillchillapp.tasks.todolist

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chillchillapp.tasks.todolist.database.*
import com.chillchillapp.tasks.todolist.dialog.AddCategoryDialog
import com.chillchillapp.tasks.todolist.model.ModelCategory
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.backIV
import java.util.*
import kotlin.collections.ArrayList


class CategoryActivity : BaseActivity() {

    private lateinit var functionCategory: FunctionCategory
    private lateinit var functionTask: FunctionTask
    private lateinit var functionTaskSub: FunctionTaskSub
    private lateinit var functionAttach: FunctionTaskAttach

    private var categoryList = ArrayList<ModelCategory>()

    private var dataNameCategoty = ""
    private var dataImageInByte: ByteArray? = null
    private lateinit var categoryDialog: AddCategoryDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_category)

        init()
        addDataCategory()
        setAdap()
        setEvent()
    }

    private fun init(){

        functionCategory = FunctionCategory(this)
        functionTask = FunctionTask(this)
        functionTaskSub = FunctionTaskSub(this)
        functionAttach = FunctionTaskAttach(this)

        functionTask.open()
        functionCategory.open()
        functionAttach.open()

    }

    private fun setEvent(){

        newCategoryRL.setOnClickListener {
            showAddCategoryDialog()
        }

        backIV.setOnClickListener {
            finish()
        }
    }

    private fun addDataCategory(){

        categoryList.clear()
        categoryList.addAll(functionCategory.getCategory())
        categoryList.sortBy { it.priority }

        //Log.i("fewfw", "categoryList: " + categoryList.size)
    }

    private var helper: ItemTouchHelper? = null

    private fun setAdap(){

        val adapter = AdapCategoryManager(this, categoryList)
        val layoutManager = GridLayoutManager(this,  1, GridLayoutManager.VERTICAL, false)
        categoryRCV.adapter = adapter
        categoryRCV.layoutManager = layoutManager

        helper?.attachToRecyclerView(null)

        helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

                val positionDragged = viewHolder.adapterPosition
                val positionTarget = target.adapterPosition

                Collections.swap(categoryList, positionDragged, positionTarget)
                adapter.notifyItemMoved(positionDragged, positionTarget)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    //drop
                    Log.i("ggge", "onSelectedChanged: ")

                    for(i in categoryList.indices){
                        val model = categoryList[i]
                        model.priority = i.toLong()
                        functionCategory.update(model)

                    }
                    categoryRCV.adapter!!.notifyDataSetChanged()
                    notifyUpdate()
                }
            }
        })

        helper!!.attachToRecyclerView(categoryRCV)
    }

    private fun showAddCategoryDialog(){
        categoryDialog = AddCategoryDialog(this)
        categoryDialog.setOnAddCategoryListener(object: AddCategoryDialog.OnAddCategoryListener{
            override fun OnAddCategoryListener(name: String, image: ByteArray) {
                dataNameCategoty = name
                dataImageInByte = image

                addCategory()
            }
        })
    }

    private fun addCategory(){

        val model = ModelCategory()
        model.name = dataNameCategoty
        model.image = dataImageInByte
        model.updateDate = System.currentTimeMillis()
        model.createDate = System.currentTimeMillis()
        model.status = KEY_ACTIVE
        model.priority = (functionCategory.getLastPriority() + 1L)

        if(functionCategory.insert(model)){
            Toast.makeText(this, getString(R.string.category_added), Toast.LENGTH_SHORT).show()
            model.id = functionCategory.getLastId()

            //Log.i("fewfr", "priority: "+ functionCategory.getLastPriority())

            categoryList.add(model)
            categoryRCV.adapter!!.notifyDataSetChanged()
            notifyUpdate()
        }else{
            Toast.makeText(this, getString(R.string.add_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun notifyUpdate(){
        prefs!!.boolUpdateTask = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            when (requestCode) {

                SELECT_SYMBOL -> if (data.extras != null) {
                    val imageInByteArray = data.getByteArrayExtra("Symbol")
                    categoryDialog.setResuil(imageInByteArray!!)

                }
            }
        }
    }

    inner class AdapCategoryManager(private var activity: BaseActivity, private var cateList: ArrayList<ModelCategory>):  RecyclerView.Adapter<AdapCategoryManager.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapCategoryManager.ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.listview_category_manager, parent, false)
            return ViewHolder(v)
        }

        private var positionSelect: Int? = null

        @SuppressLint("RecyclerView")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val taskList = functionTask.getTaskByCategoryId(cateList[position].id)
            val countTask = taskList.size

            holder.nameTV.text = cateList[position].name
            holder.countTaskTV.text = countTask.toString()
            Glide.with(activity).load(cateList[position].image).into(holder.imageIV)

            holder.itemMoveIV.setOnTouchListener { view, motionEvent ->
                if (motionEvent.metaState == MotionEvent.ACTION_DOWN){
                    helper!!.startDrag(holder)
                }
                false
            }

            holder.menuIV.setOnClickListener {
                positionSelect = position
                showMenuPopup(holder)
            }

            when(cateList[position].status){
                KEY_HIDE->{
                    holder.hideIV.visibility = View.VISIBLE
                }
                KEY_ACTIVE->{
                    holder.hideIV.visibility = View.GONE
                }
            }

        }


        override fun getItemCount(): Int {
            return cateList.size
        }


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            val imageIV = itemView.findViewById<ImageView>(R.id.imageIV)
            val nameTV = itemView.findViewById<TextView>(R.id.nameTV)
            val countTaskTV = itemView.findViewById<TextView>(R.id.countTaskTV)
            val itemMoveIV = itemView.findViewById<ImageView>(R.id.itemMoveIV)
            val menuIV = itemView.findViewById<ImageView>(R.id.menuIV)
            val hideIV = itemView.findViewById<ImageView>(R.id.hideIV)

            init {
                hideIV.visibility = View.GONE
            }
        }

        private fun showMenuPopup(v: ViewHolder){

            val popupMenu = PopupMenu(activity!!, v.menuIV)
            popupMenu.inflate(R.menu.popup_menu_category)
            popupMenu.show()

            val item2 = popupMenu.menu.getItem(1)

            item2.title = if(cateList[positionSelect!!].status == KEY_HIDE){
                getString(R.string.show)
            }else{
                getString(R.string.hide)
            }

            popupMenu.setOnMenuItemClickListener {

                when(it.itemId){
                    R.id.item1 ->{
                        showUpdateCategoryDialog()
                    }
                    R.id.item2 ->{

                        val model = cateList[positionSelect!!]

                        if(model.status != KEY_ACTIVE){
                            model.status = KEY_ACTIVE
                            model.updateDate = System.currentTimeMillis()
                            v.hideIV.visibility = View.GONE
                        }else{
                            model.status = KEY_HIDE
                            model.updateDate = System.currentTimeMillis()
                            v.hideIV.visibility = View.VISIBLE
                        }

                        functionCategory.update(model)
                        notifyUpdate()
                    }
                    R.id.item3 ->{

                        showConfirmDeleteCategoryDialog()
                    }
                }
                popupMenu.dismiss()
                true
            }
        }

        private fun showConfirmDeleteCategoryDialog(){
            val d = Dialog(activity)
            d.requestWindowFeature(Window.FEATURE_NO_TITLE)
            d.setContentView(R.layout.dialog_confirm)
            d.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            d.window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
            d.setCancelable(true)
            d.show()

            val contentTV = d.findViewById<TextView>(R.id.contentTV)
            val cancelTV = d.findViewById<TextView>(R.id.negativeTV)
            val selectTV = d.findViewById<TextView>(R.id.positiveTV)

            contentTV.text = getString(R.string.delete_category)

            selectTV.setOnClickListener {
                deleteCategoryInDatabase(cateList[positionSelect!!])
                d.dismiss()

            }

            cancelTV.setOnClickListener {
                d.dismiss()
            }
        }

        private fun deleteCategoryInDatabase(modelCategory: ModelCategory){

            if(modelCategory.remove(this@CategoryActivity) != 0){
                cateList.remove(modelCategory)
                this.notifyDataSetChanged()
                Toast.makeText(activity!!, getString(R.string.the_category_has_been_deleted) , Toast.LENGTH_SHORT).show()
            }

            notifyUpdate()
        }

        private fun showUpdateCategoryDialog(){
            categoryDialog = AddCategoryDialog(activity!!)

            val modelCategory = cateList[positionSelect!!]

            dataImageInByte = modelCategory.image
            categoryDialog.setInitValue(modelCategory.name!!, modelCategory.image!!)
            categoryDialog.setAddTextView("บันทึก")

            categoryDialog.setOnAddCategoryListener(object: AddCategoryDialog.OnAddCategoryListener{
                override fun OnAddCategoryListener(name: String, image: ByteArray) {
                    dataNameCategoty = name
                    dataImageInByte = image

                    if(dataNameCategoty != ""){

                        cateList[positionSelect!!].name = dataNameCategoty
                        cateList[positionSelect!!].image = dataImageInByte
                        cateList[positionSelect!!].updateDate = System.currentTimeMillis()
                        updateCategory(cateList[positionSelect!!])

                        categoryDialog.dismiss()
                    }else{
                        Toast.makeText(activity, "ป้อนข้อมูลก่อนเพิ่ม",Toast.LENGTH_SHORT ).show()
                    }
                }
            })

        }

        private fun updateCategory(model: ModelCategory){

            if(functionCategory.update(model) != 0){
                Toast.makeText(activity, "เเก้ไขเรียบร้อย", Toast.LENGTH_SHORT).show()
                notifyUpdate()
                this.notifyDataSetChanged()
                notifyUpdate()
            }else{
                Toast.makeText(activity, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show()
            }
        }


    }
}