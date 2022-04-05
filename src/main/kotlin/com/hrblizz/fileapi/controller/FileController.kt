package com.hrblizz.fileapi.controller

import com.hrblizz.fileapi.library.AppConstants
import com.hrblizz.fileapi.library.FileUploadRequestValidator
import com.hrblizz.fileapi.library.JsonUtil
import com.hrblizz.fileapi.library.log.LogItem
import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.rest.FileInfo
import com.hrblizz.fileapi.rest.FileMeta
import com.hrblizz.fileapi.rest.FileMetaRequest
import com.hrblizz.fileapi.rest.FileUploadRequest
import com.hrblizz.fileapi.rest.ResponseEntity
import com.hrblizz.fileapi.service.IFileStorageService
import io.swagger.annotations.ApiOperation
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@RestController
class FileController(
    private val fileStorageService: IFileStorageService,
    private val fileUploadRequestValidator: FileUploadRequestValidator,
    private val log: Logger
) {

    @ApiOperation(
        value = "File Upload",
        response = ResponseEntity::class,
        notes = "This API saves the file metadata to DB and uploads the file to the disk and returns the token(identifier) for the file "
    )
    @PostMapping(
        value = [AppConstants.UPLOAD_FILE_URI_PATH],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun uploadFile(@Valid fileUploadRequest: FileUploadRequest): ResponseEntity<Map<String, String?>> {
        log.info(LogItem("File Upload Request Received"))
        val fileMeta: FileMeta = JsonUtil.toObject(fileUploadRequest.meta, false, FileMeta::class.java) as FileMeta
        fileUploadRequestValidator.validate(fileUploadRequest, fileMeta)
        val token = fileStorageService.saveFile(fileUploadRequest, fileMeta)
        log.info(LogItem("File Upload Request Completed"))
        return ResponseEntity(
            mapOf(
                "token" to token
            ),
            HttpStatus.OK.value()
        )
    }

    @ApiOperation(
        value = "File MetaData",
        response = ResponseEntity::class,
        notes = "This API returns the file metadata for the input tokens"
    )
    @PostMapping(
        value = [AppConstants.FILE_METADATA_URI_PATH],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getFileDetails(@Valid @RequestBody fileMetaRequest: FileMetaRequest): ResponseEntity<List<FileInfo>> {
        log.info(LogItem("File MetaData Request Received for tokens ${fileMetaRequest.tokens}"))
        return ResponseEntity(
            fileStorageService.loadFileMeta(fileMetaRequest.tokens), HttpStatus.OK.value()
        )
    }

    @ApiOperation(
        value = "File Download",
        response = org.springframework.http.ResponseEntity::class,
        notes = "This API loads and returns the file for the given token"
    )
    @GetMapping(value = [AppConstants.FILE_URI_PATH])
    fun getFile(@PathVariable @NotBlank token: String): org.springframework.http.ResponseEntity<Resource> {
        log.info(LogItem("File Download Request Received for token $token"))
        return fileStorageService.loadFile(token)
    }

    @ApiOperation(
        value = "Delete File",
        response = ResponseEntity::class,
        notes = "This API deletes the file for given token"
    )
    @DeleteMapping(value = [AppConstants.FILE_URI_PATH])
    fun deleteFile(@PathVariable @NotBlank token: String): ResponseEntity<Boolean> {
        log.info(LogItem("File Delete Request Received for token $token"))
        val fileDeleted: Boolean = fileStorageService.deleteFile(token)
        return ResponseEntity(
            fileDeleted, HttpStatus.OK.value()
        )
    }
}
