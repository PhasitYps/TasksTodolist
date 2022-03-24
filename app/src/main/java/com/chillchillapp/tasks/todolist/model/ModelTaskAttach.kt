package com.chillchillapp.tasks.todolist.model

import android.net.Uri
import java.io.File

class ModelTaskAttach (
    var id: Long? = null,
    var taskId: Long? = null,
    var categoryId: Long? = null,
    var name: String? = null,
    var path: String? = null,
    var type: String? = null,
    var createDate: Long? = null,
    var updateDate: Long? = null
)