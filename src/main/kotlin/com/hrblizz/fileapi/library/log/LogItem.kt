package com.hrblizz.fileapi.library.log

import com.hrblizz.fileapi.library.AppConstants
import com.hrblizz.fileapi.library.AppContext
import java.time.LocalDateTime

open class LogItem constructor(
    val message: String
) {
    val dateTime: LocalDateTime = LocalDateTime.now()
    var correlationId: String? = AppContext.get(AppConstants.CORRELATION_ID)
    var type: String? = null

    override fun toString(): String {
        return "[$dateTime] [$correlationId] $message"
    }
}
