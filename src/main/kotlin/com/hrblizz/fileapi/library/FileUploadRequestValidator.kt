package com.hrblizz.fileapi.library

import com.hrblizz.fileapi.controller.exception.BadRequestException
import com.hrblizz.fileapi.rest.FileMeta
import com.hrblizz.fileapi.rest.FileUploadRequest
import org.springframework.stereotype.Component
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

@Component
class FileUploadRequestValidator {

    fun validate(fileUploadRequest: FileUploadRequest, fileMeta: FileMeta) {
        validateFile(fileUploadRequest)
        validateFileType(fileUploadRequest)
        validateSource(fileUploadRequest)
        validateExpireTime(fileUploadRequest)
        validateFileMeta(fileMeta)
    }

    private fun validateFileType(request: FileUploadRequest) {
        if (!AppUtil.fileTypes.contains(request.content.contentType)) {
            throw BadRequestException("FileType is invalid")
        }
    }

    private fun validateSource(request: FileUploadRequest) {
        if (!AppUtil.sources.contains(request.source.uppercase())) {
            throw BadRequestException("Source is invalid")
        }
    }

    private fun validateExpireTime(request: FileUploadRequest) {
        if (!request.expireTime.isNullOrBlank()) {
            try {
                val formatter = SimpleDateFormat(AppConstants.DATE_PATTERN)
                val date = formatter.parse(request.expireTime)
                if (date.before(Date())) throw BadRequestException("Expire Time should be a future time")
            } catch (ex: ParseException) {
                throw BadRequestException("Expire Time Format Invalid: Valid Format is $AppConstants.DATE_PATTERN")
            }
        }
    }

    private fun validateFile(request: FileUploadRequest) {
        if (request.content.isEmpty) {
            throw BadRequestException("File can not be empty")
        }
    }

    private fun validateFileMeta(fileMeta: FileMeta) {
        if (fileMeta.creatorEmployeeId.isNullOrBlank()) {
            throw BadRequestException("Creator Employee Id must be null or blank")
        }
    }
}
