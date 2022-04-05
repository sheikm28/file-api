package com.hrblizz.fileapi.scheduler

import com.hrblizz.fileapi.controller.exception.ApplicationException
import com.hrblizz.fileapi.library.AppContext
import com.hrblizz.fileapi.library.log.LogItem
import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.service.IFileStorageService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.util.UUID

@Component
class FileScheduler(private val fileStorageService: IFileStorageService, private val log: Logger) {

    /**Scheduled FileDeletion
     *
     * Runs every minute and deletes the files that are expired
     */
    @Scheduled(cron = "0 * * * * * ")
    fun scheduledFileDelete() {
        val startTime = Timestamp(System.currentTimeMillis())
        createThreadContext()
        log.info(LogItem("Scheduler - FileDeletion Started | Entry"))
        try {
            fileStorageService.deleteExpiredFiles()
        } catch (e: Exception) {
            throw ApplicationException("Exception occurred while scheduled file deletion", e)
        } finally {
            val endTime = Timestamp(System.currentTimeMillis())
            log.info(
                LogItem("Scheduler - FileDeletion Completed TotalExecutionTime=${endTime.time - startTime.time} milli seconds | Exit")
            )
            AppContext.clearAll()
        }
    }

    private fun createThreadContext() {
        AppContext.put("CorrelationId", UUID.randomUUID().toString())
    }
}
