package com.chillchillapp.tasks.todolist.model

class ModelTaskSub(
    var id: Long? = null,
    var taskId: Long? = null,
    var categoryId: Long? = null,
    var todo: String? = null,
    var state: Long? = null,
    var createDate: Long? = null,
    var updateDate: Long? = null,
    var priority: Long? = null
)