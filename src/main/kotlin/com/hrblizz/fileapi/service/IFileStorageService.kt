package com.hrblizz.fileapi.service

import com.hrblizz.fileapi.data.entities.File
import com.hrblizz.fileapi.rest.FileInfo
import com.hrblizz.fileapi.rest.FileMeta
import com.hrblizz.fileapi.rest.FileUploadRequest
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
interface IFileStorageService {

    fun init()
    fun saveFile(fileReq: FileUploadRequest, fileMeta: FileMeta): String?
    fun loadFile(token: String): ResponseEntity<Resource>
    fun deleteFile(token: String): Boolean
    fun loadFileMeta(tokens: List<String>): List<FileInfo>?
    fun deleteExpiredFiles()
    fun getFile(token: String): File
}
