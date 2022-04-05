package com.hrblizz.fileapi.library

import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date

object AppUtil {

    val sources = setOf("TIMESHEET", "MSS", "HRB")
    val fileTypes = setOf(
        "text/plain",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/zip",
        "application/pdf",
        "image/png",
        "image/jpeg",
        "text/csv",
        "application/msword",
        "application/x-7z-compressed",
        "application/x-tar",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    fun createFolders(path: Path, dir: String) {
        Files.createDirectories(path.resolve(dir))
    }

    fun convertStringToDate(value: String?): Date? {
        val formatter = SimpleDateFormat(AppConstants.DATE_PATTERN)
        return formatter.parse(value)
    }
}
