package com.chillchillapp.tasks.todolist.model

open class ModelRepeat(
    var id: Long? = null,
    var taskId: Long? = null,
    var repeatType: Int? = null, //ชนิดวัน เดือน ปี
    var repeatNext: Int? = null, //ทำทุกกี่วัน เดือน ปี
    var numberOfTime: Long? = null,
    var programCount: Long? = null,// ทำไปเเล้วกี่ครั้ง
    var createDate: Long? = null,
    var updateDate: Long? = null
//8
)