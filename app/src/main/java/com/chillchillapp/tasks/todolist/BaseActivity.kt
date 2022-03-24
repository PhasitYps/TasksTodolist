package com.chillchillapp.tasks.todolist

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


open class BaseActivity: AppCompatActivity() {

    var sp: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    private val themeColorDefault = R.style.BlueTheme
    var styple: Int? = 0

    fun initBase(){
        sp = getSharedPreferences("Setting", Context.MODE_PRIVATE)
        editor = sp!!.edit()
    }

    fun setTheme(){
        styple = sp?.getInt(KEY_CURRENT_THEME_COLOR, themeColorDefault)
        setTheme(styple!!)
    }

    fun initLanguage(){
        val language = sp!!.getString(KEY_CURRENT_LANGUAGE, "")
        if(language != ""){
            setLocale(language)
        }
    }

    private fun setLocale(lang: String?) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val dm: DisplayMetrics = resources.displayMetrics
        val conf: Configuration = resources.configuration
        conf.locale = locale
        resources.updateConfiguration(conf, dm)
    }

    fun getCurrentLanguage(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0].language
        } else {
            Locale.getDefault().language
        }
    }

    fun Context.getColor(color: Int):Int{
        return ContextCompat.getColor(this, color)
    }

    fun Context.themeColor(@AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute (attrRes, typedValue, true)
        return typedValue.data
    }

    fun formatDate(formatStr: String, day: Date): String{
        val format = SimpleDateFormat(formatStr)
        val dateStr = format.format(day)
        return dateStr
    }


    fun convertUri(s: String): Uri {
        val file = File(s)
        return Uri.fromFile(file)
    }

    fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }



}