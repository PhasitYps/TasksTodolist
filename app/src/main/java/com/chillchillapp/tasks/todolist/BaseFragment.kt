package com.chillchillapp.tasks.todolist

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chillchillapp.tasks.todolist.master.Prefs
import java.text.SimpleDateFormat
import java.util.*

open class BaseFragment(@LayoutRes contentLayoutId: Int): Fragment(contentLayoutId) {

    var prefs: Prefs? = null

    /*var sp: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null*/

    val KEY_FUNCTION = "function"
    val KEY_UPDATE = "Update"
    val KEY_INSERT = "Insert"


    fun initBase(){

        prefs = Prefs(requireContext())

        /*sp = requireActivity().getSharedPreferences("Setting", Context.MODE_PRIVATE)
        editor = sp!!.edit()*/
    }


    fun themeColor(context: Context?, @AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute (attrRes, typedValue, true)
        return typedValue.data
    }

    fun formatDate(formatStr: String, day: Date): String{
        val format = SimpleDateFormat(formatStr)
        val dateStr = format.format(day)
        return dateStr
    }

    fun getDrawable(context: Context, drawable: Int): Drawable? {
        return ContextCompat.getDrawable(context, drawable)
    }
}