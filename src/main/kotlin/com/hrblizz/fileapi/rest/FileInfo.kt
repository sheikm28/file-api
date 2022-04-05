package com.hrblizz.fileapi.rest

import java.util.Date

class FileInfo() {
    var token: String = ""
    var size: Long = 0
    var source: String = ""
    lateinit var meta: FileMeta
    var expireTime: Date? = null
    var fileName: String = ""
    var contentType: String = ""
    lateinit var createTime: Date

    constructor(
        token: String,
        size: Long,
        source: String,
        meta: FileMeta,
        expireTime: Date?,
        fileName: String,
        contentType: String,
        createTime: Date
    ) : this() {
        this.token = token
        this.size = size
        this.source = source
        this.meta = meta
        this.expireTime = expireTime
        this.fileName = fileName
        this.contentType = contentType
        this.createTime = createTime
    }
}
