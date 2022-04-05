package com.hrblizz.fileapi.data.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.Date

@Document(collection = "files")
data class File(
    @Id val token: String,
    var size: Long,
    var source: String,
    var meta: Meta?,
    var expireTime: Date?,
    var fileName: String,
    var contentType: String,
    var createTime: Date
)
