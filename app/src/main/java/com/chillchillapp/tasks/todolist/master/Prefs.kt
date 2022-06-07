package com.chillchillapp.tasks.todolist.master

import android.content.Context
import android.content.SharedPreferences

class Prefs(private var context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("Setting", Context.MODE_PRIVATE)

    private val INT_INSERT_CATEGORY_DEFAULT = "intInsertCategoryDefault"
    private val STR_CURRENT_LANGUAGE = "strCurrentLanguage"
    private val INT_CURRENT_THEMECOLOR = "intCurrentThemeColor"
    private val BOOL_AUTO_SYNC = "boolAutoSync"
    private val LONG_LAST_SYNC_TIME = "longLastSyncTime"
    private val BOOL_UPDATE_TASK = "boolUpdate"
    private val BOOL_DISPLAY_TASKOTHER = "boolDisplayTaskOther"
    private val FLOAT_LAST_LAT = "floatLastLatitude"
    private val FLOAT_LAST_LNG = "floatLastLongitude"

    var intInsertCategoryDefault: Int
        get() = preferences.getInt(INT_INSERT_CATEGORY_DEFAULT, 0)
        set(value) = preferences.edit().putInt(INT_INSERT_CATEGORY_DEFAULT, value).apply()

    var intCurrentThemeColor: Int
        get() = preferences.getInt(INT_CURRENT_THEMECOLOR, 0)
        set(value) = preferences.edit().putInt(INT_CURRENT_THEMECOLOR, value).apply()

    var strCurrentLanguage: String?
        get() = preferences.getString(STR_CURRENT_LANGUAGE, "local")
        set(value) = preferences.edit().putString(STR_CURRENT_LANGUAGE, value).apply()

    var boolAutoSync: Boolean
        get() = preferences.getBoolean(BOOL_AUTO_SYNC, false)
        set(value) = preferences.edit().putBoolean(BOOL_AUTO_SYNC, value).apply()

    var longLastAutoSync: Long
        get() = preferences.getLong(LONG_LAST_SYNC_TIME, 0L)
        set(value) = preferences.edit().putLong(LONG_LAST_SYNC_TIME, value).apply()

    var boolUpdateTask: Boolean
        get() = preferences.getBoolean(BOOL_UPDATE_TASK, false)
        set(value) = preferences.edit().putBoolean(BOOL_UPDATE_TASK, value).apply()

    var boolDisplayTaskOther: Boolean
        get() = preferences.getBoolean(BOOL_DISPLAY_TASKOTHER, true)
        set(value) = preferences.edit().putBoolean(BOOL_DISPLAY_TASKOTHER, value).apply()

    var floatLastLat: Float
        get() = preferences.getFloat(FLOAT_LAST_LAT, 0f)
        set(value) = preferences.edit().putFloat(FLOAT_LAST_LAT, value).apply()

    var floatLastLng: Float
        get() = preferences.getFloat(FLOAT_LAST_LNG, 0f)
        set(value) = preferences.edit().putFloat(FLOAT_LAST_LNG, value).apply()



}
