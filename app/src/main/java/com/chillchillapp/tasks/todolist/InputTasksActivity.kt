package com.chillchillapp.tasks.todolist

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.util.Log
import android.util.Log.d
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chillchillapp.tasks.todolist.BottomSheetMenu.SelectListener
import com.chillchillapp.tasks.todolist.database.*
import com.chillchillapp.tasks.todolist.dialog.*
import com.chillchillapp.tasks.todolist.master.RepeatHelper
import com.chillchillapp.tasks.todolist.master.MediaRecord
import com.chillchillapp.tasks.todolist.model.*
import com.chillchillapp.tasks.todolist.widget.Timer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.masoudss.lib.WaveformSeekBar
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_input_tasks.*
import kotlinx.android.synthetic.main.activity_input_tasks.backRL
import kotlinx.android.synthetic.main.activity_input_tasks.stateNotifyTV
import kotlinx.android.synthetic.main.activity_input_tasks.stateTimeTV
import kotlinx.android.synthetic.main.activity_select_symbol.*
import kotlinx.android.synthetic.main.listview_attach_audio.*
import kotlinx.android.synthetic.main.listview_attach_audio.view.*
import kotlinx.android.synthetic.main.listview_attach_image.*
import kotlinx.android.synthetic.main.listview_attach_video.*
import kotlinx.android.synthetic.main.listview_subtask.*
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class InputTasksActivity : BaseActivity() {

    private val TAG = "InputTasksActivity"

    //variable user input
    private var taskId: Long? = null
    private var categoryId = ""
    private var calDueDate = Calendar.getInstance()
    private var currentHour: Int = 0
    private var currentMinute: Int = 0
    private var createDate: Long =  System.currentTimeMillis()
    private var updateDate: Long = System.currentTimeMillis()
    private var completeDate: Long? =  null
    private var state: Long = 0
    private var favorite: Long = 0
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var place: String? = null

    private var reminderList = ArrayList<ModelTaskReminder>()
    //variable repeat
    private var repeatId: Long? = null
    private var repeatType: String? = null
    private var repeatNext: Int? = null
    private var repeatNumberOfTime: Long? = null
    private var programCount: Long? = null

    private val subTaskList = ArrayList<ModelTaskSub>()
    private val attachImageList = ArrayList<ModelInputAttach>()
    private val attachVideoList = ArrayList<ModelInputAttach>()
    private val attachAudioList = ArrayList<ModelInputAttach>()

    //variable in class
    private var categoryList = ArrayList<ModelCategory>()
    private val menuAttachList = ArrayList<BottomSheetMenu.ModelMenuBottomSheet>()

    private lateinit var repeatHelper: RepeatHelper

    private lateinit var functionCategory: FunctionCategory
    private lateinit var functionTask: FunctionTask
    private lateinit var functionTaskSub: FunctionTaskSub
    private lateinit var functionAttach: FunctionTaskAttach
    private lateinit var functionRepeat: FunctionRepeat
    private lateinit var functionReminder: FunctionTaskReminder

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBase()
        setTheme()
        setContentView(R.layout.activity_input_tasks)
        setAds()

        init()
        setAdap()
        addChipCategoryView()
        setEvent()

    }

    private var mInterstitialAd: InterstitialAd? = null
    private fun setAds(){

        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,getString(R.string.Ads_Interstitial_AddTask_UnitId), adRequest, object : InterstitialAdLoadCallback() {
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
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycle = ON_RESUME

        val function = intent.getStringExtra(KEY_FUNCTION)
        when(function){
            KEY_UPDATE->{
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                nameEDT.clearFocus()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.i("fewfw", "onBackPressed")

        checkUpdate()

    }

    override fun onPause() {
        super.onPause()
        lifecycle = ON_PAUSE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mr?.onRecord("pause")
        }else{
            mr?.onRecord("stop")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycle = ON_DESTROY
        val function = intent.getStringExtra(KEY_FUNCTION)

        when(function){
            KEY_INSERT ->{
                for(m in attachAudioList){
                    if(m.model.id == null){
                        File(m.model.path).delete()
                    }
                }
            }
        }
        mr?.onRecord("stop")
    }

    private val SELECT_IMAGE = 1
    private val SELECT_VEDIO = 2
    private val REQUEST_AUDIO_PERMISSION = 200
    private val REQUEST_IMAGE_PERMISSION = 201
    private val REQUEST_VIDEO_PERMISSION = 202
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            REQUEST_AUDIO_PERMISSION ->if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showAudioRecordBottomSheet()
            }

            REQUEST_IMAGE_PERMISSION ->if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            }

            REQUEST_VIDEO_PERMISSION ->if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openVideo()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {

            when (requestCode) {

                SELECT_IMAGE-> if (resultCode == Activity.RESULT_OK) {

                    val imageUri = data.data as Uri
                    //val imageInByte = contentResolver.openInputStream(imageUri)?.readBytes()

                    val model = ModelInputAttach()
                    model.uri = imageUri
                    model.model.type = "image"
                    attachImageList.add(model)
                    imageRCV.adapter!!.notifyDataSetChanged()

                }

                SELECT_VEDIO ->if (resultCode == Activity.RESULT_OK) {

                    val videoInUri = data.data as Uri
                    //val videoInByte = contentResolver.openInputStream(videoInUri)?.readBytes()

                    val model = ModelInputAttach()
                    model.uri = videoInUri
                    model.model.type = "video"

                    attachVideoList.add(model)
                    videoRCV.adapter!!.notifyDataSetChanged()

                    Log.i("dsada", "SELECT_VEDIO: " + videoInUri.path)

                }

                SELECT_SYMBOL -> if (data.extras != null) {
                    val imageInByteArray = data.getByteArrayExtra("Symbol")
                    addCategoryDialog.setResuil(imageInByteArray!!)
                }
            }
        }
    }

    private fun init(){
        createFolder()

        repeatHelper = RepeatHelper(this)
        functionCategory = FunctionCategory(this)
        functionTaskSub = FunctionTaskSub(this)
        functionTask = FunctionTask(this)
        functionAttach = FunctionTaskAttach(this)
        functionRepeat = FunctionRepeat(this)
        functionReminder = FunctionTaskReminder(this)

        val function = intent.getStringExtra(KEY_FUNCTION)

        when(function){
            KEY_INSERT ->{
                bgAddTaskRL.visibility = View.VISIBLE
                menuIV.visibility = View.GONE

                initValueInsert()
                updateUI()
            }
            KEY_UPDATE->{
                bgAddTaskRL.visibility = View.GONE //save auto
                menuIV.visibility = View.VISIBLE

                initValueUpdate()
                updateUI()
            }
        }
    }

    private fun initMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync{
            mMap = it
            mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            mMap!!.uiSettings.isMapToolbarEnabled = false
            mMap!!.uiSettings.isScrollGesturesEnabled = false
            mMap!!.uiSettings.isZoomGesturesEnabled = false

            val myLocation = LatLng(latitude!!, longitude!!)
            mMap!!.addMarker(MarkerOptions().position(myLocation).icon(bitmapDescriptorFromDrawable(this, R.drawable.ic_pin_100)))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f))


        }

    }



    private fun initValueInsert(){

        categoryId = intent.getStringExtra("categoryId").toString()

        calDueDate.set(Calendar.HOUR_OF_DAY, 0)
        calDueDate.set(Calendar.MINUTE, 0)
        calDueDate.set(Calendar.SECOND, 0)
        calDueDate.set(Calendar.MILLISECOND, 0)

        currentHour = -1
        currentMinute = -1
        createDate = System.currentTimeMillis()
        updateDate = System.currentTimeMillis()
        state = 0
        favorite = 0

        subTaskList.clear()
        attachAudioList.clear()
        attachImageList.clear()
        attachVideoList.clear()
    }

    private fun initValueUpdate(){

        subTaskList.clear()
        attachImageList.clear()
        attachVideoList.clear()
        attachAudioList.clear()
        reminderList.clear()

        taskId = intent.getLongExtra(COL_TASK_ID, 0)
        val modelTask = functionTask.getTaskById(taskId!!)

        if(modelTask.id != null){
            categoryId = modelTask.categoryId.toString()
            calDueDate.timeInMillis = modelTask.dueDate!!
            currentHour = modelTask.hour!!
            currentMinute = modelTask.minute!!
            createDate = modelTask.createDate!!
            updateDate = modelTask.updateDate!!
            state = modelTask.state!!
            favorite = modelTask.favorite!!
            completeDate = modelTask.completeDate
            latitude = modelTask.latitude
            longitude = modelTask.longitude
            place = modelTask.place

            val modelRepeat = functionRepeat.getByTaskId(taskId)
            if(modelRepeat.id != null){
                repeatId = modelRepeat.id
                repeatType = modelRepeat.repeatType
                repeatNext = modelRepeat.repeatNext
                repeatNumberOfTime = modelRepeat.numberOfRepeat
                programCount = modelRepeat.programCount
            }

            subTaskList.addAll(functionTaskSub.getDataByTaskId(taskId))
            subTaskList.sortBy { it.priority }
            functionReminder.getReminderByTaskId(taskId).forEach { m->
                val choice = ArrayList<ModelTaskReminder>()
                choice.add(ModelTaskReminder(optionId =  "op1", reminderCount = 0, reminderType = Calendar.MINUTE))
                choice.add(ModelTaskReminder(optionId =  "op2", reminderCount = 5, reminderType = Calendar.MINUTE))
                choice.add(ModelTaskReminder(optionId =  "op3", reminderCount = 10, reminderType = Calendar.MINUTE))
                choice.add(ModelTaskReminder(optionId =  "op4", reminderCount = 15, reminderType = Calendar.MINUTE))
                choice.add(ModelTaskReminder(optionId =  "op5", reminderCount = 30, reminderType = Calendar.MINUTE))
                choice.add(ModelTaskReminder(optionId =  "op6", reminderCount = 1, reminderType = Calendar.DATE))
                choice.add(ModelTaskReminder(optionId =  "op7", reminderCount = 2, reminderType = Calendar.DATE))
                choice.forEach { mc ->
                    mc.setNotifyTime(calDueDate, currentHour, currentMinute)

                    if (mc.notifyTime == m.notifyTime) {
                        m.optionId = mc.optionId
                        m.reminderCount = mc.reminderCount
                        m.reminderType = mc.reminderType
                        reminderList.add(m)

                        return@forEach

                    }
                }

            }
            functionAttach.getDataByTaskId(taskId).forEach { m->

                val modelAttach = ModelInputAttach(m, convertUri(m.path!!))
                when(m.type){
                    FOLDER_IMAGE->{
                        attachImageList.add(modelAttach)
                    }
                    FOLDER_VIDEO->{
                        attachVideoList.add(modelAttach)
                    }
                    FOLDER_AUDIO->{
                        attachAudioList.add(modelAttach)
                    }
                }
            }

            nameEDT.setText(modelTask.name!!)
            nameEDT.hint = modelTask.name!!
        }else{
            Toast.makeText(this, "มีบางอย่างผิดพลาด", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private val TYPE_HOUR = "hour"
    private val TYPE_DAY = "day"
    private val TYPE_WEEK = "week"
    private val TYPE_MONTH = "month"
    private val TYPE_YEAR = "year"
    private fun updateUI(){

        when(isSetDueDate()){
            true->{
                stateDueDateTV.text = formatDate("yyyy/MM/dd", calDueDate.time)
            }
            false->{
                stateDueDateTV.text = getString(R.string.do_not_have)
            }
        }

        when(isSetTime()){
            true->{
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, currentHour)
                cal.set(Calendar.MINUTE, currentMinute)
                stateTimeTV.text = formatDate("HH:mm", cal.time)
                settingNotifyRL.visibility = View.VISIBLE
            }
            false->{
                stateTimeTV.text = getString(R.string.do_not_have)
                settingNotifyRL.visibility = View.GONE
            }
        }

        when(isSetReminder()){
            true->{
                var reminderStr = ""
                for(m in reminderList){
                    val calNotify = Calendar.getInstance()
                    calNotify.timeInMillis = calDueDate.timeInMillis
                    calNotify.set(Calendar.HOUR_OF_DAY, currentHour)
                    calNotify.set(Calendar.MINUTE, currentMinute)
                    calNotify.add(m.reminderType!!, -(m.reminderCount!!).toInt())

                    if(reminderStr.isEmpty()){
                        reminderStr = if(formatDate("yyyy/MM/dd", calNotify.time) == formatDate("yyyy/MM/dd", calDueDate.time)){
                            formatDate("HH:mm", calNotify.time)
                        }else{
                            formatDate("yyyy/MM/dd HH:mm", calNotify.time)
                        }
                    }else{
                        reminderStr = if(formatDate("yyyy/MM/dd", calNotify.time) == formatDate("yyyy/MM/dd", calDueDate.time)){
                            formatDate("HH:mm", calNotify.time) + ", " + reminderStr
                        }else{
                            formatDate("yyyy/MM/dd HH:mm", calNotify.time) + ", " + reminderStr
                        }
                    }
                }
                stateNotifyTV.text = reminderStr
            }
            false->{
                stateNotifyTV.text = getString(R.string.do_not_have)
            }
        }

        when(isSetRepeat()){
            true->{
                val type = when(repeatType){
                    TYPE_HOUR->{
                        getString(R.string.hour)
                    }
                    TYPE_DAY->{
                        getString(R.string.day)
                    }
                    TYPE_WEEK->{
                        getString(R.string.sunday)
                    }
                    TYPE_MONTH->{
                        getString(R.string.month)
                    }
                    TYPE_YEAR->{
                        getString(R.string.year)
                    }
                    else-> ""
                }
                stateRepeatTV.text = "${getString(R.string.repeat)} $repeatNext$type/${getString(R.string.time)}"
            }
            false->{
                stateRepeatTV.text = getString(R.string.no_loop)
            }
        }

        when(state){
            0L ->{
                stateView.visibility = View.GONE
            }
            1L ->{
                stateView.visibility = View.VISIBLE
            }
        }

        when(isSetMap()){
            true->{
                mapViewCV.visibility = View.VISIBLE
                placeCV.visibility = View.VISIBLE
                if(mMap == null){
                    initMap()
                }else{
                    mMap!!.clear()
                    val myLocation = LatLng(latitude!!, longitude!!)
                    mMap!!.addMarker(MarkerOptions().position(myLocation).icon(bitmapDescriptorFromDrawable(this, R.drawable.ic_pin_100)))
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f))
                }
                stateLocationTV.text = getString(R.string.Edit)
                placeTV.text = place
            }
            false->{
                mapViewCV.visibility = View.GONE
                placeCV.visibility = View.GONE
                stateLocationTV.text = getString(R.string.Add)
                placeTV.text = ""
            }
        }
    }

    private fun setEvent(){

        addTaskRL.setOnClickListener {
            insertTask()
        }

        addSubtaskRL.setOnClickListener {
            addSubTask()
        }

        addAttachRL.setOnClickListener {
            attachBottomSheetMenu()
        }

        addCategoryCC.setOnClickListener {
            createCategoryDialog()
        }

        settingNotifyRL.setOnClickListener {
            setReminderDialog()
        }

        settingDueDateRL.setOnClickListener {
            setDueDateDialog()
        }

        settingTimeRL.setOnClickListener {
            if(isSetDueDate()){
                setTimeDialog()
            }else{
                Toast.makeText(this, getString(R.string.please_set_a_date_first), Toast.LENGTH_SHORT).show()
            }
        }

        settingLocationRL.setOnClickListener {
            setLocationDialog()

        }

        settingRepeatRL.setOnClickListener {
            if(isSetDueDate()){
                setRepeatDialog()
            }else{
                Toast.makeText(this, getString(R.string.please_set_a_date_first), Toast.LENGTH_SHORT).show()
            }
        }


        backRL.setOnClickListener {
            checkUpdate()
            finish()
        }

        menuIV.setOnClickListener {
            showMenuPopup(it)
        }

        deleteLocationRL.setOnClickListener {
            latitude = null
            longitude = null
            place = null
            updateUI()
        }
    }

    private fun checkUpdate(){

        val function = intent.getStringExtra(KEY_FUNCTION)
        when(function){
            KEY_UPDATE ->{
                updateTask()
            }
        }
    }

    private fun showMenuPopup(v: View){

        //init
        val popupMenu = PopupMenu(this, v)
        if(state == 0L){
            popupMenu.menu.add(getString(R.string.mark_as_finished))
        }else{
            popupMenu.menu.add(getString(R.string.mark_undo))
        }
        popupMenu.menu.add(getString(R.string.delete))
        //popupMenu.menu.add(getString(R.string.share))
        popupMenu.show()

        //val item2 = popupMenu.menu.getItem(1)

        popupMenu.setOnMenuItemClickListener {

            when(it.title){
                popupMenu.menu.getItem(0).title ->{
                    //state
                    changeState()
                }
                popupMenu.menu.getItem(1).title ->{
                    //delete
                    deleteThisTask()

                }
                popupMenu.menu.getItem(2).title ->{
                    //share

                }
            }

            false
        }
    }

    private fun changeState(){

        if(isSetRepeat()){
            if(taskId != null){

                //insert new Task
                repeatHelper.insertByRepeat(taskId)
                Toast.makeText(this, getString(R.string.this_task_is_marked_complete), Toast.LENGTH_SHORT).show()
            }
        }else{
            updateDate = System.currentTimeMillis()
            state = if(state != 0L){ 0L }else { 1L }

            when(state){
                0L ->{
                    stateView.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.canceled_successfully), Toast.LENGTH_SHORT).show()
                }
                1L ->{
                    stateView.visibility = View.VISIBLE
                    Toast.makeText(this, getString(R.string.this_task_is_marked_complete), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteThisTask(){
        val modelTask = functionTask.getTaskById(taskId!!)

        if(modelTask.remove(this) != 0){
            notifyUpdate()
            finish()
        }else{
            Toast.makeText(this, getString(R.string.fail), Toast.LENGTH_SHORT).show()
        }
    }

    private var indexSelect = 0
    private fun addChipCategoryView() {

        categoryList.clear()
        categoryList.add(ModelCategory(-1, getString(R.string.no_category), null, -1))
        categoryList.addAll(functionCategory.getCategoryDisplay())

        categoryList.sortBy { it.priority!!.toInt() }
        categoryCG.removeAllViews()

        for (i in categoryList.indices) {
            //Log.i("dsada", "index: " +i )
            val chip = Chip(this)
            chip.setChipBackgroundColorResource(R.color.selector_choice_state)
            val colors = ContextCompat.getColorStateList(this, R.color.selector_text_state)
            chip.setTextColor(colors)
            chip.tag = categoryList[i].id
            chip.text = categoryList[i].name
            chip.chipIconSize = 50f
            chip.isCheckedIconVisible = false
            chip.isCloseIconVisible = false
            chip.isClickable = true
            chip.isCheckable = true


            Glide.with(this).asBitmap().apply(RequestOptions.centerCropTransform()).load(categoryList[i].image)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                        circularBitmapDrawable.isCircular = false
                        chip.chipIcon = circularBitmapDrawable
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })

            categoryCG!!.addView(chip)

            if (categoryId == categoryList[i].id.toString() ) {
                indexSelect = i
            }
        }

        categoryCG!!.setOnCheckedChangeListener { chipGroup, chipId ->
            val chip: Chip? = chipGroup.findViewById(chipId)

            if (chip != null) {
                indexSelect = categoryCG.indexOfChild(chip)
                val model = categoryList.single { it.id == chip.tag }
                categoryId = model.id.toString()
            } else {
                (categoryCG.getChildAt(indexSelect) as Chip).isChecked = true
            }
        }

        if (categoryList.size > 0) {
            (categoryCG!!.getChildAt(indexSelect) as Chip).isChecked = true
        }
    }

    private fun insertTask(){

        if(nameEDT.text.isEmpty()){
            Toast.makeText(this, getString(R.string.please_enter_a_job), Toast.LENGTH_SHORT).show()
            return
        }

        createDate = System.currentTimeMillis()
        updateDate = System.currentTimeMillis()

        val modelTask = ModelTask()
        modelTask.name = nameEDT.text.toString()
        modelTask.createDate = createDate
        modelTask.updateDate = updateDate
        modelTask.categoryId = categoryId.toLong()
        modelTask.dueDate = calDueDate.timeInMillis
        modelTask.hour = currentHour
        modelTask.minute = currentMinute
        modelTask.favorite = favorite
        modelTask.state = state
        modelTask.status = KEY_ACTIVE
        modelTask.latitude = latitude
        modelTask.longitude = longitude
        modelTask.place = place

        if(functionTask.insert(modelTask) != 0L){

            val lastID = functionTask.getLastId()
            insertSubTask(lastID)
            insertAttach(lastID)

            if(isSetRepeat()){
                insertRepeat(lastID)
            }

            if(isSetReminder()){
                insertReminder(lastID)
            }


            Toast.makeText(this, getString(R.string.successfully_added), Toast.LENGTH_SHORT).show()
            notifyUpdate()

            showAds()
            finish()
        }else{
            Toast.makeText(this, getString(R.string.fail), Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertSubTask(taskId: Long){
        //save subTask
        val dataSubTask = subTaskList.filter { it.todo!!.isNotEmpty() }
        for(i in dataSubTask.indices){
            dataSubTask[i].categoryId = categoryId.toLong()
            dataSubTask[i].priority = i.toLong()
            dataSubTask[i].taskId = taskId

            functionTaskSub.insert(dataSubTask[i])

        }//end save subTask
    }

    private fun insertAttach(taskId: Long){
        //save attach image
        for(m in attachImageList){
            m.model.createDate = System.currentTimeMillis()
            m.model.updateDate = System.currentTimeMillis()
            m.model.taskId = taskId
            m.model.categoryId = categoryId.toLong()
            m.model.name = System.currentTimeMillis().toString()+".jpg"

            val imageByte = contentResolver.openInputStream(m.uri!!)?.readBytes()
            val imageDir = File(filesDir, FOLDER_IMAGE)
            val filename = File(imageDir, m.model.name)

            try {

                val out = FileOutputStream(filename)
                out.write(imageByte)
                out.close()

                m.model.path = filename.path
                Log.i("fewfw", "Saved to ${filename.path}")


            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("fewfw", "Exception: " + e)
            }

            functionAttach.insert(m.model)

        } //end save attach image

        //save attach video
        for(m in attachVideoList){
            m.model.createDate = System.currentTimeMillis()
            m.model.updateDate = System.currentTimeMillis()
            m.model.taskId = taskId
            m.model.categoryId = categoryId.toLong()
            m.model.name = System.currentTimeMillis().toString()+".mp4"

            val byteArray = contentResolver.openInputStream(m.uri!!)?.readBytes()
            val videoDir = File(filesDir, FOLDER_VIDEO)
            val filename = File(videoDir, m.model.name)

            try {

                val out = FileOutputStream(filename)
                out.write(byteArray)
                out.close()

                m.model.path = filename.path
                Log.i("fewfw", "Saved to ${filename.path}")


            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("fewfw", "Exception: " + e)
            }

            functionAttach.insert(m.model)
        }//end save video

        //save audio
        for(m in attachAudioList){
            m.model.createDate = System.currentTimeMillis()
            m.model.updateDate = System.currentTimeMillis()
            m.model.taskId = taskId
            m.model.categoryId = categoryId.toLong()

            val filename = "${System.currentTimeMillis()}.mp3"
            val audioDir = File(filesDir, FOLDER_AUDIO)
            val audioFile = File(audioDir, filename)

            m.model.name = filename

            val draftPath = m.model.path!!
            val byteArray = contentResolver.openInputStream(convertUri(draftPath))?.readBytes()

            try {

                val out = FileOutputStream(audioFile)
                out.write(byteArray)
                out.close()

                File(draftPath).delete()
                m.model.path = audioFile.path

                Log.i("fewfw", "Saved to ${audioFile.path}")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("fewfw", "Exception: " + e)
            }

            if(functionAttach.insert(m.model) != 0L){
                m.model.id = functionAttach.getLastId()
            }

        }//end save audio
    }

    private fun insertRepeat(taskId: Long){

        val modelRepeat = ModelRepeat()
        modelRepeat.taskId = taskId
        modelRepeat.repeatType = repeatType
        modelRepeat.repeatNext = repeatNext
        modelRepeat.numberOfRepeat = repeatNumberOfTime
        modelRepeat.programCount = 0
        modelRepeat.createDate = createDate
        modelRepeat.updateDate = updateDate

        if(functionRepeat.insert(modelRepeat)){
            Log.i("ghhhhhhh", "repeat insert success")
        }
    }

    private fun insertReminder(taskId: Long){
        //functionReminder.deleteByTaskId(taskId)

        for(m in reminderList){
            m.taskId = taskId

            m.setNotifyTime(calDueDate, currentHour, currentMinute)
            m.status = "active"
            m.createDate = System.currentTimeMillis()
            m.updateDate = System.currentTimeMillis()

            functionReminder.insert(m)
        }
    }

    private fun updateTask(){

        if(nameEDT.text.isEmpty()){
            Toast.makeText(this, getString(R.string.please_enter_a_job), Toast.LENGTH_SHORT).show()
            return
        }

        when(state){
            1L -> completeDate = System.currentTimeMillis()
            0L -> completeDate = null
        }

        updateDate = System.currentTimeMillis()

        val modelTask = ModelTask()
        modelTask.id = taskId
        modelTask.categoryId = categoryId.toLong()
        modelTask.name = nameEDT.text.toString()
        modelTask.createDate = createDate
        modelTask.updateDate = updateDate
        modelTask.dueDate = calDueDate.timeInMillis
        modelTask.hour = currentHour
        modelTask.minute = currentMinute
        modelTask.favorite = favorite
        modelTask.state = state
        modelTask.completeDate = completeDate
        modelTask.status = KEY_ACTIVE
        modelTask.latitude = latitude
        modelTask.longitude = longitude
        modelTask.place = place


        if(functionTask.update(modelTask) != 0){

            updateSubTask(taskId!!)
            updateAttach(taskId!!)
            updateRepeat(taskId!!)
            updateReminder(taskId!!)

            Toast.makeText(this, getString(R.string.fixed_it), Toast.LENGTH_SHORT).show()
            notifyUpdate()
            finish()

        }else{
            Toast.makeText(this, getString(R.string.fail), Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateReminder(taskId: Long) {

        val reminder = functionReminder.getReminderByTaskId(taskId)
        reminder.forEach { m->
            val searchList = reminderList.filter { it.id == m.id }
             if(searchList.isEmpty()){
                 functionReminder.delete(m)
             }
        }

        for(m in reminderList){

            if(m.id != null){
                m.setNotifyTime(calDueDate, currentHour, currentMinute)
                m.updateDate = System.currentTimeMillis()
                functionReminder.update(m)
            }else{
                m.taskId = taskId
                m.setNotifyTime(calDueDate, currentHour, currentMinute)
                m.updateDate = System.currentTimeMillis()
                m.createDate = System.currentTimeMillis()
                m.status = "active"

                functionReminder.insert(m)
            }
        }
    }

    private fun updateAttach(taskId: Long){
        //save attach image
        for(m in attachImageList){

            if(m.model.id != null){
                m.model.categoryId = categoryId.toLong()
                m.model.updateDate = System.currentTimeMillis()
                functionAttach.update(m.model)
                continue
            }

            m.model.taskId = taskId
            m.model.categoryId = categoryId.toLong()
            m.model.createDate = System.currentTimeMillis()
            m.model.updateDate = System.currentTimeMillis()
            m.model.name = System.currentTimeMillis().toString()+".jpg"

            val imageByte = contentResolver.openInputStream(m.uri!!)?.readBytes()
            val imageDir = File(filesDir, FOLDER_IMAGE)
            val filename = File(imageDir, m.model.name)

            try {

                val out = FileOutputStream(filename)
                out.write(imageByte)
                out.close()

                m.model.path = filename.path
                Log.i("fewfw", "Saved to ${filename.path}")


            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("fewfw", "Exception: " + e)
            }

            functionAttach.insert(m.model)

        } //end save attach image

        //save attach video
        for(m in attachVideoList){

            Log.i("safefwq", "attachVideoList: " + m.model.id)
            if(m.model.id != null){
                m.model.categoryId = categoryId.toLong()
                m.model.updateDate = System.currentTimeMillis()
                functionAttach.update(m.model)
                continue
            }
            m.model.taskId = taskId
            m.model.categoryId = categoryId.toLong()
            m.model.createDate = System.currentTimeMillis()
            m.model.updateDate = System.currentTimeMillis()
            m.model.name = System.currentTimeMillis().toString()+".mp4"

            val byte = contentResolver.openInputStream(m.uri!!)?.readBytes()
            val imageDir = File(filesDir, FOLDER_VIDEO)
            val filename = File(imageDir, m.model.name)

            try {

                val out = FileOutputStream(filename)
                out.write(byte)
                out.close()

                m.model.path = filename.path
                Log.i("fewfw", "Saved to ${filename.path}")


            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("fewfw", "Exception: " + e)
            }

            functionAttach.insert(m.model)

        } //end save attach video

        //save attach audio
        for(m in attachAudioList){

            if(m.model.id != null){
                m.model.categoryId = categoryId.toLong()
                m.model.updateDate = System.currentTimeMillis()
                functionAttach.update(m.model)
                continue
            }

            m.model.taskId = taskId
            m.model.categoryId = categoryId.toLong()
            m.model.createDate = System.currentTimeMillis()
            m.model.updateDate = System.currentTimeMillis()
            m.model.name = "${System.currentTimeMillis()}.mp3"

            val audioDir = File(filesDir, FOLDER_AUDIO)
            val filename = File(audioDir, m.model.name)

            val draftPath = m.model.path!!.toString()
            val byteArray = contentResolver.openInputStream(convertUri(draftPath))?.readBytes()

            try {

                val out = FileOutputStream(filename)
                out.write(byteArray)
                out.close()

                File(draftPath).delete()
                m.model.path = filename.path
                Log.i("safefwq", "Saved to ${filename.path}")


            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("safefwq", "Exception: " + e)
            }

            functionAttach.insert(m.model)

        }//end save attach audio
    }

    private fun updateSubTask(taskID: Long){

        for(m in subTaskList){
            Log.i("hhhhhghgd", "subTaskList: " + m.todo)
        }

        val dataSubTask = subTaskList.filter { it.todo!!.isNotEmpty() }
        val subtask = functionTaskSub.getDataByTaskId(taskID)

        for (m in subtask){
            val data = dataSubTask.filter { it.id == m.id }
            if(data.isEmpty()) functionTaskSub.delete(m.id)
        }

        //save subTask
        for(i in dataSubTask.indices){
            dataSubTask[i].categoryId = categoryId.toLong()
            dataSubTask[i].priority = i.toLong()
            dataSubTask[i].taskId = taskID
            dataSubTask[i].updateDate = updateDate

            Log.i("hhhhhghgd", "dataSubTask: " + dataSubTask[i].todo)

            if(dataSubTask[i].id != null){
                functionTaskSub.update(dataSubTask[i])
            }else{
                dataSubTask[i].createDate = createDate
                functionTaskSub.insert(dataSubTask[i])
            }
        }
        //end save subTask
    }

    private fun updateRepeat(taskID: Long){

        when(repeatId){
            null->{ //update not set repeat
                if(isSetRepeat()){
                    val m = ModelRepeat()
                    m.taskId = taskID
                    m.repeatType = repeatType
                    m.repeatNext = repeatNext
                    m.numberOfRepeat = repeatNumberOfTime
                    m.programCount = programCount
                    m.createDate = createDate
                    m.updateDate = updateDate

                    functionRepeat.insert(m)
                }
            }
            else->{ // update has set repeat
                when(repeatType){
                    null->{
                        functionRepeat.delete(repeatId)
                    }
                    else->{
                        val m = ModelRepeat()
                        m.id = repeatId
                        m.taskId = taskId
                        m.repeatType = repeatType
                        m.repeatNext = repeatNext
                        m.numberOfRepeat = repeatNumberOfTime
                        m.programCount = programCount
                        m.updateDate = updateDate

                        functionRepeat.update(m)
                    }
                }
            }
        }

    }

    private fun notifyUpdate(){
        prefs!!.boolUpdateTask = true
    }

    private fun setRepeatDialog(){

        val modelTask = ModelTask()
        modelTask.dueDate = calDueDate.timeInMillis
        modelTask.hour = currentHour
        modelTask.minute = currentMinute

        val modelRepeat = ModelRepeat()
        modelRepeat.repeatType = repeatType
        modelRepeat.repeatNext = repeatNext
        modelRepeat.numberOfRepeat = repeatNumberOfTime

        val d = SetRepeatDialog(this)
        d.setInit(modelRepeat, modelTask)
        d.setChangeDataListener(object : SetRepeatDialog.OnChangeDataListener{
            override fun OnChangeDataListener(m: ModelRepeat) {
                repeatType = m.repeatType
                repeatNext = m.repeatNext
                repeatNumberOfTime = m.numberOfRepeat
                programCount = 0

                Log.i("fhhhhh", "repeatNumberOfTime: " + repeatNumberOfTime)

                updateUI()
            }
        })
    }

    private fun setReminderDialog(){
        val d = SetReminderDialog(this)
        d.setDataList(reminderList)
        d.setOnMyEvent(object : SetReminderDialog.OnMyEvent{
            override fun OnClickPositive(selectItemList: ArrayList<ModelTaskReminder>) {
                reminderList = selectItemList

                reminderList.forEach {
                    d("hhjjjjjhhhhh", "output id: " + it.id + " ,taskId: " + it.taskId + " , optionId: " + it.optionId)
                }
                updateUI()
            }
        })

    }

    private fun setTimeDialog(){
        val d = SetTimeDialog(this)
        d.setInitValue(currentHour, currentMinute)
        d.setOnPositiveListener(object : SetTimeDialog.OnPositiveListener{
            override fun OnPositiveListener(hour: Int, minute: Int) {
                currentHour = hour
                currentMinute = minute

                if(isSetTime()){
                    if(reminderList.size == 0){
                        reminderList.add(ModelTaskReminder(null, null, "op2", 5, Calendar.MINUTE,null, null))
                    }
                }else{
                    reminderList.clear()
                }

                updateUI()
            }
        })
    }

    private fun setDueDateDialog(){
        val d = SetDueDateDialog(this)
        d.setInitValue(calDueDate, currentHour, currentMinute, reminderList)
        d.setOnPositiveListener(object : SetDueDateDialog.OnPositiveListener{
            override fun OnPositiveListener(
                calDue: Calendar,
                hour: Int,
                minute: Int,
                selectReminderList: ArrayList<ModelTaskReminder>,
            ) {
                reminderList.clear()

                calDueDate.timeInMillis = calDue.timeInMillis
                currentHour = hour
                currentMinute = minute
                reminderList.addAll(selectReminderList)

                updateUI()
            }
        })
    }

    private fun setLocationDialog(){

        var dialog: SetLocationDialog? = null
        if(latitude != null && longitude != null){
            dialog = SetLocationDialog(this, LatLng(latitude!!, longitude!!))
        }else{
            val lat = prefs!!.floatLastLat.toDouble()
            val lng = prefs!!.floatLastLng.toDouble()
            dialog = SetLocationDialog(this, LatLng(lat, lng))
        }

        dialog.setMyEvent(object : SetLocationDialog.MyEvent{
            override fun onMySelect(latLng: LatLng) {
                latitude = latLng.latitude
                longitude = latLng.longitude

                var geocoder = Geocoder(this@InputTasksActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1) as List<Address>
                var address = ""
                address = if (addresses.isNotEmpty()) {
                    var city = addresses[0].locality
                    var area = addresses[0].subAdminArea
                    var state = addresses[0].adminArea
                    var postalCode = addresses[0].postalCode
                    var address = addresses[0].getAddressLine(0)


                    if (city != null && area != null) "$city $area \n$state $postalCode" else "$address"

                } else {
                    latitude.toString() +", " + longitude.toString()
                }

                place = address
                updateUI()
            }

        })
        dialog.show()



    }

    private var dataNameCategoty = ""
    private var dataImageInByte: ByteArray? = null
    private lateinit var addCategoryDialog: AddCategoryDialog
    private fun createCategoryDialog(){
        addCategoryDialog = AddCategoryDialog(this)
        addCategoryDialog.show()

        addCategoryDialog.setOnAddCategoryListener(object: AddCategoryDialog.OnAddCategoryListener{
            override fun OnAddCategoryListener(name: String, image: ByteArray) {
                dataNameCategoty = name
                dataImageInByte = image
                addCategory()

                Log.i("fewfr", "name: " + name)
                Log.i("fewfr", "image: " + image)
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

            addChipCategoryView()
            notifyUpdate()
        }else{
            Toast.makeText(this, getString(R.string.add_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSubTask(){
        val m = ModelTaskSub()
        m.todo = ""
        m.state = 0L
        m.createDate = System.currentTimeMillis()
        m.updateDate = System.currentTimeMillis()

        subTaskList.add(m)
        subtasksRCV.adapter!!.notifyItemChanged(subTaskList.lastIndex)
    }

    private var helper: ItemTouchHelper? = null

    private fun setAdap(){
        setAdapSubTask()
        setAdapAttachImage()
        setAdapAttachVideo()
        setAdapAttachAudio()
    }

    private fun setAdapSubTask(){

        val adapter = AdapSubTask(this, subTaskList)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        subtasksRCV.adapter = adapter
        subtasksRCV.layoutManager = layoutManager

        helper?.attachToRecyclerView(null)

        helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                Log.i("ItemTouchHelper", "onMove: ")
                val positionDragged = viewHolder.adapterPosition
                val positionTarget = target.adapterPosition

                Collections.swap(subTaskList, positionDragged, positionTarget)
                adapter.notifyItemMoved(positionDragged, positionTarget)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.i("ItemTouchHelper", "onSwiped: ")
                adapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    //drop
                    Log.i("ItemTouchHelper", "onSelectedChanged: ")
                    adapter.notifyDataSetChanged()


                }
            }
        })

        helper!!.attachToRecyclerView(subtasksRCV)
    }

    private fun setAdapAttachImage(){
        val adapter = AdapAttach(this, attachImageList)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        imageRCV.adapter = adapter
        imageRCV.layoutManager = layoutManager
    }

    private fun setAdapAttachVideo(){
        val adapter = AdapAttach(this, attachVideoList)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        videoRCV.adapter = adapter
        videoRCV.layoutManager = layoutManager
    }

    private val ON_RESUME = 1
    private val ON_PAUSE = 2
    private val ON_DESTROY = 0

    private var lifecycle = 0
    private fun setAdapAttachAudio(){
        val adapter = AdapAttach(this, attachAudioList)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        audioRCV.adapter = adapter
        audioRCV.layoutManager = layoutManager
    }

    private val PICTURE = 0
    private val VIDEO = 1
    private val RECORD_SOUND = 2
    private fun attachBottomSheetMenu(){

        menuAttachList.clear()

        menuAttachList.add(BottomSheetMenu.ModelMenuBottomSheet(getString(R.string.picture),"", R.drawable.ic_image))
        menuAttachList.add(BottomSheetMenu.ModelMenuBottomSheet(getString(R.string.video),"",R.drawable.ic_video))
        menuAttachList.add(BottomSheetMenu.ModelMenuBottomSheet(getString(R.string.record_sound),"",R.drawable.ic_microphone))

        BottomSheetMenu(this, menuAttachList, object: SelectListener {
            override fun onMyClick(m: BottomSheetMenu.ModelMenuBottomSheet, position: Int) {
                when(position){
                    PICTURE -> {
                        ActivityCompat.requestPermissions(this@InputTasksActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_IMAGE_PERMISSION)
                    }
                    VIDEO -> {
                        ActivityCompat.requestPermissions(this@InputTasksActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_VIDEO_PERMISSION)
                    }
                    RECORD_SOUND -> {
                        ActivityCompat.requestPermissions(this@InputTasksActivity, arrayOf(Manifest.permission.RECORD_AUDIO),
                            REQUEST_AUDIO_PERMISSION)
                    }
                }
            }
        })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,true)
        startActivityForResult(intent, SELECT_IMAGE)

    }

    private fun openVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,true)
        startActivityForResult(intent, SELECT_VEDIO)

    }

    private var mr: MediaRecord? = null
    private lateinit var audioRecordBsd: BottomSheetDialog
    //private lateinit var timer: Timer
    private fun showAudioRecordBottomSheet(){
        audioRecordBsd = BottomSheetDialog(this, R.style.SheetDialog)
        audioRecordBsd.setContentView(layoutInflater.inflate(R.layout.bottom_sheet_audio_record, null))
        audioRecordBsd.setCancelable(true)
        audioRecordBsd.show()

        val bgStartRecordLL = audioRecordBsd.findViewById<LinearLayout>(R.id.bgStartRecordLL)!!
        val bgRecordLL = audioRecordBsd.findViewById<LinearLayout>(R.id.bgRecordLL)!!
        val stateRecordIV = audioRecordBsd.findViewById<ImageView>(R.id.stateRecordIV)!!
        val timerTV = audioRecordBsd.findViewById<TextView>(R.id.timerTV)!!
        val waveformSeekBar = audioRecordBsd.findViewById<WaveformSeekBar>(R.id.waveformSeekBar)!!

        //bottom
        val microphoneRecordRL = audioRecordBsd.findViewById<RelativeLayout>(R.id.microphoneRecordRL)!!
        val recordCancelRL = audioRecordBsd.findViewById<RelativeLayout>(R.id.recordCancelRL)!!
        val recordStartRL = audioRecordBsd.findViewById<RelativeLayout>(R.id.recordStartRL)!!
        val recordSaveRL = audioRecordBsd.findViewById<RelativeLayout>(R.id.recordSaveRL)!!

        //init
        bgRecordLL.visibility = View.VISIBLE
        bgStartRecordLL.visibility = View.GONE

        val amplitudes = ArrayList<Int>()

        for(i in 1..100){
            amplitudes.add(0)
        }

        val maxSpikes = waveformSeekBar.maxProgress.toInt()

        //waveformSeekBar.stopNestedScroll()

        microphoneRecordRL!!.setOnClickListener {

            bgRecordLL.visibility = View.GONE
            bgStartRecordLL.visibility = View.VISIBLE

            stateRecordIV.setImageResource(R.drawable.ic_pause_filled)
            stateRecordIV.tag = 1

            mr = MediaRecord(this)
            mr!!.setTimer(object : Timer.OnTimerTickListener{
                override fun onTimerTick(duration: Long, timerStr: String) {

                    timerTV.text = timerStr
                    amplitudes.add(mr!!.getMaxAmplitude())
                    val amps = amplitudes.takeLast(maxSpikes).toIntArray()

                    waveformSeekBar.setSampleFrom(amps)
                    println(mr!!.getMaxAmplitude())
                    waveformSeekBar.invalidate()

                }
            })
            mr!!.onRecord("start")

        }

        recordSaveRL.setOnClickListener {
            addAudio()
            audioRecordBsd.dismiss()

        }

        recordStartRL.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                when(stateRecordIV.tag){
                    0->{
                        stateRecordIV.setImageResource(R.drawable.ic_pause_filled)
                        stateRecordIV.tag = 1
                        mr!!.onRecord("resume")
                    }
                    1->{
                        stateRecordIV.setImageResource(R.drawable.ic_play_filled)
                        stateRecordIV.tag = 0
                        mr!!.onRecord("pause")
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
                mr!!.onRecord("pause")
            }

            showConfirmDeleteAudioDialog()

        }

        audioRecordBsd.setOnDismissListener {
            mr?.onRecord("stop")
        }

    }

    private fun addAudio(){
        mr?.onRecord("stop")
        val model = ModelInputAttach()

        val filename = "${System.currentTimeMillis()}.mp3"
        val draftFolder = File(filesDir, FOLDER_DRAFT)
        val audioFile = File(draftFolder, filename)

        val byte = contentResolver.openInputStream(convertUri(mr!!.getDataFilePath()))?.readBytes()

        try {

            val out = FileOutputStream(audioFile)
            out.write(byte)
            out.close()
            model.model.path = audioFile.path
            Log.i("fewfw", "Saved to ${model.model.path}")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("fewfw", "Exception: " + e)
        }


        model.model.type = "audio"

        attachAudioList.add(model)
        audioRCV.adapter!!.notifyDataSetChanged()

        Log.i("fewf", "  audioRCV.adapter!!.notifyDataSetChanged()")
    }

    private fun formatTimer(duration: Long): String{
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
    }

    private fun showConfirmDeleteAudioDialog(){
        val d = Dialog(this)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.dialog_confirm)
        d.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        d.window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        d.setCancelable(false)
        d.show()

        val contentTV = d.findViewById<TextView>(R.id.contentTV)
        val cancelTV = d.findViewById<TextView>(R.id.negativeTV)
        val selectTV = d.findViewById<TextView>(R.id.positiveTV)

        contentTV.text = getString(R.string.are_you_sure_you_want_to_delete_the_recording)

        selectTV.setOnClickListener {
            mr!!.onRecord("stop")
            audioRecordBsd.dismiss()
            d.dismiss()

            showAudioRecordBottomSheet()
        }

        cancelTV.setOnClickListener {
            d.dismiss()
        }
    }

    private fun showConfirmSaveAudioDialog(){
        val d = Dialog(this)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.dialog_confirm)
        d.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        d.window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        d.setCancelable(false)
        d.show()

        val contentTV = d.findViewById<TextView>(R.id.contentTV)
        val cancelTV = d.findViewById<TextView>(R.id.negativeTV)
        val selectTV = d.findViewById<TextView>(R.id.positiveTV)

        contentTV.text = getString(R.string.do_you_want_to_end_this_recording)
        selectTV.text = getString(R.string.save)

        selectTV.setOnClickListener {
            mr!!.onRecord("stop")
            addAudio()

            d.dismiss()
            audioRecordBsd.dismiss()

        }

        cancelTV.setOnClickListener {
            d.dismiss()
        }
    }

    private fun isSetDueDate(): Boolean{
        return calDueDate.timeInMillis != 0L
    }
    private fun isSetTime(): Boolean{
        return currentHour != -1 && currentMinute != -1
    }

    private fun isSetRepeat(): Boolean{
        return repeatType != null
    }

    private fun isSetMap(): Boolean{
        return latitude != null && longitude != null
    }

    private fun isSetReminder(): Boolean{
        return reminderList.size != 0
    }

    private fun bitmapDescriptorFromDrawable(context: Context, resId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, resId)
        vectorDrawable!!.setBounds(0, 0, 100, 100)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        // below line is use to generate a drawable.
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)

        // below line is use to set bounds to our vector drawable.
        vectorDrawable!!.setBounds(0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight)

        // below line is use to create a bitmap for our
        // drawable which we have added.
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /*private fun TextView.setTextDate(date: Date?) {

        val format_date = FORMAT_DATE_CALENDAR
        val format = try {
            SimpleDateFormat(format_date, Locale.getDefault())
        } catch (e: Exception) {
            null
        }

        if (format != null) {
            val dateStr = format!!.format(date)
            this.text = dateStr

            val monthCur = Date().month
            val monthSel = date!!.month

            if (monthCur == monthSel) {
                this.setTextColor(resources.getColor(R.color.colorBlack))
            } else {
                this.setTextColor(resources.getColor(R.color.colorWhiteDarkDark))
            }
        }
    }*/

    private fun createFolder(){
        val imageFolder = File(filesDir, FOLDER_IMAGE)
        val videoFolder = File(filesDir, FOLDER_VIDEO)
        val audioFolder = File(filesDir, FOLDER_AUDIO)
        val draftFolder = File(filesDir, FOLDER_DRAFT)

        if (!imageFolder.exists()) imageFolder.mkdir()
        if (!videoFolder.exists()) videoFolder.mkdir()
        if (!audioFolder.exists()) audioFolder.mkdir()
        if (!draftFolder.exists()) draftFolder.mkdir()
    }

    inner class AdapSubTask(private val activity: Activity, private var taskSubList: ArrayList<ModelTaskSub>)
        : RecyclerView.Adapter<AdapSubTask.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_subtask, parent, false)
            return ViewHolder(view)
        }

        private val function = intent.getStringExtra(KEY_FUNCTION)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val m =  taskSubList[position]
            holder.todoEDT.tag = position

            Log.i("hhhhhhjy", "onBindViewHolder: " + position + " todo: " + m.todo)

            when(m.state){
                1L->{
                    updateStateTodo(position, holder, "done")
                }
                0L->{
                    updateStateTodo(position, holder, "don't")
                }
            }

            holder.cancelIV.setOnClickListener {
                taskSubList.removeAt(position)
                notifyDataSetChanged()
            }

            holder.stateIV.setOnClickListener {
                taskSubList[position].state = if(taskSubList[position].state == 0L){ 1 }else{ 0 }

                when(taskSubList[position].state){
                    1L->{
                        updateStateTodo(position, holder, "done")
                    }
                    0L->{
                        updateStateTodo(position, holder, "don't")
                    }
                }
            }

            holder.itemMoveIV.setOnTouchListener { view, motionEvent ->
                if (motionEvent.metaState == MotionEvent.ACTION_DOWN){
                    helper!!.startDrag(holder)
                }
                false
            }


            if(position == taskSubList.lastIndex && function != KEY_UPDATE) holder.todoEDT.requestFocus()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val todoEDT = itemView.findViewById<EditText>(R.id.todoEDT)
            val stateIV = itemView.findViewById<ImageView>(R.id.stateIV)
            val cancelIV = itemView.findViewById<ImageView>(R.id.cancelIV)
            val itemMoveIV = itemView.findViewById<ImageView>(R.id.itemMoveIV)

            init {

                cancelIV.visibility = View.GONE
                itemMoveIV.visibility = View.VISIBLE
                //Log.i("fwf", "init")

                todoEDT.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        val position =  todoEDT.tag as Int

                        taskSubList[position].todo = p0.toString()
                        Log.i("hhhhhhjy", "onTextChanged: " + position + " todo: " + p0)

                        todoEDT.removeTextChangedListener(this)

                        when(taskSubList[position].state){
                            1L ->{
                                val ss = strikethroughSpan(p0.toString())
                                todoEDT.setText(ss)
                                todoEDT.setSelection(ss.length)
                            }
                            0L->{
                                todoEDT.setText(p0.toString())
                                todoEDT.setSelection(p0!!.length)
                            }
                        }

                        todoEDT.addTextChangedListener(this)

                    }

                    override fun afterTextChanged(p0: Editable?) {}
                })

                todoEDT.onFocusChangeListener = View.OnFocusChangeListener { _, focus ->
                    if (focus){
                        cancelIV.visibility = View.VISIBLE
                    }else{
                        cancelIV.visibility = View.GONE
                    }
                }
            }
        }

        private fun strikethroughSpan(text: String): SpannableString{
            val ss = SpannableString(text)
            ss.setSpan(StrikethroughSpan(), 0, text.length, 0)
            return ss
        }

        private fun updateStateTodo(position: Int, holder: ViewHolder, state: String){

            when(state){
                "done"->{
                    holder.stateIV.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_ok_filled))
                    //holder.stateIV.background.setTint(activity.resources.getColor(R.color.colorGreen))
                    holder.todoEDT.setTextColor(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))
                    holder.todoEDT.hint = strikethroughSpan(getString(R.string.enter_subtasks))

                    holder.stateIV.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreen))

                    if(taskSubList[position].todo != ""){
                        val ss = strikethroughSpan(taskSubList[position].todo!!)
                        holder.todoEDT.setText(ss)
                    }

                }
                "don't"->{
                    holder.stateIV.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_round))
                    //holder.stateIV.background.setTint(activity.resources.getColor(R.color.colorWhiteDarkDark))
                    holder.todoEDT.setTextColor(ContextCompat.getColor(activity, R.color.colorBlack))
                    holder.stateIV.setColorFilter(ContextCompat.getColor(activity, R.color.colorWhiteDarkDark))
                    holder.todoEDT.setText(taskSubList[position].todo)
                    holder.todoEDT.hint = getString(R.string.enter_subtasks)


                }
            }
        }

        override fun getItemCount(): Int {
            return taskSubList.size
        }
    }

    inner class AdapAttach(private val activity: Activity, private var listInputAttaches: ArrayList<ModelInputAttach>)
        : RecyclerView.Adapter<AdapAttach.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listview_attach, parent, false)
            return ViewHolder(view)
        }

        val handler = Handler()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val uri = listInputAttaches[position].uri
            val type = listInputAttaches[position].model.type

            when(type){

                "image"->{

                    holder.itemImageCV.visibility = View.VISIBLE

                    Glide.with(activity)
                        .asBitmap()
                        .apply(RequestOptions.centerCropTransform())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .load(uri).centerCrop()
                        .into(holder.imageIV)

                    holder.removeImageRL.setOnClickListener {

                        if(listInputAttaches[position].model.id != null){
                            val file = File(listInputAttaches[position].model.path)
                            file.delete()
                            functionAttach.delete(listInputAttaches[position].model.id!!)
                        }

                        listInputAttaches.removeAt(position)
                        this.notifyDataSetChanged()
                    }

                }

                "video"->{
                    holder.itemVideoCV.visibility = View.VISIBLE

                    val mediaController = MediaController(activity)
                    mediaController.setAnchorView(holder.videoVV)

                    holder.videoVV.setVideoURI(uri)
                    holder.videoVV.setMediaController(mediaController)

                    holder.removeVideoRL.setOnClickListener {
                        if(listInputAttaches[position].model.id != null){

                            File(listInputAttaches[position].model.path).delete()
                            functionAttach.delete(listInputAttaches[position].model.id!!)
                        }

                        listInputAttaches.removeAt(position)
                        this.notifyDataSetChanged()
                    }
                }

                "audio"->{

                    holder.itemAudioCV.visibility = View.VISIBLE

                    val path = listInputAttaches[position].model.path
                    val attId = listInputAttaches[position].model.id

                    var mediaPlayer:MediaPlayer? = MediaPlayer()

                    try {
                        mediaPlayer!!.setDataSource(path)
                        mediaPlayer!!.prepare()
                        holder.playerDurationTV.text = formatTimer(mediaPlayer.duration.toLong())

                    } catch (e: IOException) {
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }

                    holder.seekBar.max = mediaPlayer!!.duration

                    val runnable = object: Runnable {
                        override fun run() {
                            when(lifecycle){

                                ON_RESUME->{
                                    holder.seekBar.progress = mediaPlayer!!.currentPosition
                                    handler.postDelayed(this , 500)
                                }

                                ON_PAUSE ->{
                                    handler.removeCallbacks(this)
                                    mediaPlayer?.pause()
                                }
                                ON_DESTROY ->{
                                    handler.removeCallbacks(this)
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                }
                            }

                        }
                    }

                    holder.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                        override fun onProgressChanged(p0: SeekBar?, process: Int, fromUser: Boolean) {
                            if(fromUser){
                                mediaPlayer?.seekTo(process)
                            }
                            holder.playerPointTV.text = formatTimer(process.toLong())
                        }

                        override fun onStartTrackingTouch(p0: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(p0: SeekBar?) {

                        }

                    })

                    mediaPlayer!!.setOnCompletionListener {
                        Log.i("fewfvczx", "setOnCompletionListener")

                        mediaPlayer!!.seekTo(0)
                        holder.seekBar.progress = 0
                        holder.playerPointTV.text = formatTimer(0)
                        handler.removeCallbacks(runnable)

                    }

                    holder.removeAudioRL.setOnClickListener {
                        if(listInputAttaches[position].model.id != null){
                            functionAttach.delete(attId)
                        }

                        mediaPlayer?.release()
                        mediaPlayer = null
                        handler.removeCallbacks(runnable)

                        File(path).delete()
                        listInputAttaches.removeAt(position)
                        this.notifyDataSetChanged()
                    }

                    holder.playerIV.setOnClickListener {
                        if(!mediaPlayer!!.isPlaying){
                            mediaPlayer!!.start()
                            handler.postDelayed(runnable, 0)
                        }else{
                            mediaPlayer!!.pause()
                            handler.removeCallbacks(runnable)
                        }
                    }
                }
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val itemImageCV = itemView.findViewById<CardView>(R.id.itemImageCV)
            val imageIV = itemView.findViewById<ImageView>(R.id.imageIV)
            val removeImageRL = itemView.findViewById<RelativeLayout>(R.id.removeImageRL)

            val itemVideoCV = itemView.findViewById<CardView>(R.id.itemVideoCV)
            val videoVV = itemView.findViewById<VideoView>(R.id.videoVV)
            val removeVideoRL = itemView.findViewById<RelativeLayout>(R.id.removeVideoRL)

            val itemAudioCV = itemView.findViewById<CardView>(R.id.itemAudioCV)
            val playerIV = itemView.findViewById<ImageView>(R.id.playerIV)
            val playerPointTV = itemView.findViewById<TextView>(R.id.playerPointTV)
            val playerDurationTV = itemView.findViewById<TextView>(R.id.playerDurationTV)
            val seekBar = itemView.findViewById<SeekBar>(R.id.seekBar)
            val removeAudioRL = itemView.findViewById<RelativeLayout>(R.id.removeAudioRL)

            init {
                itemImageCV.visibility = View.GONE
                itemVideoCV.visibility = View.GONE
                itemAudioCV.visibility = View.GONE
            }
        }
        override fun getItemCount(): Int {
            return listInputAttaches.size
        }
    }

    data class ModelInputAttach(
        var model: ModelTaskAttach = ModelTaskAttach(),
        var uri: Uri? = null,
    )
}