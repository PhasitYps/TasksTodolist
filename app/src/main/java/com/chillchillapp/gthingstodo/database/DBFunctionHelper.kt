package com.chillchillapp.gthingstodo.database

import android.content.Context

class DBFunctionHelper (private var context: Context, private var sqliteName: String){

    val functionCategory = FunctionCategory(context, sqliteName)
    val functionRepeat = FunctionRepeat(context, sqliteName)
    val functionTask = FunctionTask(context, sqliteName)
    val functionTaskAttach = FunctionTaskAttach(context, sqliteName)
    val functionReminder = FunctionTaskReminder(context, sqliteName)
    val functionTaskSub = FunctionTaskSub(context, sqliteName)

}