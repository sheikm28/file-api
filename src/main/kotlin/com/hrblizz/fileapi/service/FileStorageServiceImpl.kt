package com.hrblizz.fileapi.service

import com.hrblizz.fileapi.controller.exception.ApplicationException
import com.hrblizz.fileapi.controller.exception.BadRequestException
import com.hrblizz.fileapi.controller.exception.NotFoundException
import com.hrblizz.fileapi.data.entities.File
import com.hrblizz.fileapi.data.entities.Meta
import com.hrblizz.fileapi.data.repository.FileRepository
import com.hrblizz.fileapi.library.AppUtil
import com.hrblizz.fileapi.library.log.LogItem
import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.rest.FileInfo
import com.hrblizz.fileapi.rest.FileMeta
import com.hrblizz.fileapi.rest.FileUploadRequest
import org.modelmapper.ModelMapper
import org.modelmapper.TypeToken
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Date
import java.util.UUID
import javax.annotation.PostConstruct

@Service
@Transactional
class FileStorageServiceImpl(
    private val fileRepository: FileRepository,
    env: Environment,
    private val modelMapper: ModelMapper,
    private val log: Logger
) : IFileStorageService {

    private val listType = object : TypeToken<List<FileInfo>>() {}.type
    private val rootPath: Path = Paths.get(env["upload-dir"]).toAbsolutePath().normalize()

    /**
     * Init
     *
     * Creates the root directories for the files
     *
     * @throws ApplicationException
     */
    @PostConstruct
    override fun init() {
        try {
            log.info(LogItem("Creating root directories for uploading files $rootPath "))
            AppUtil.sources.forEach {
                AppUtil.createFolders(rootPath, it)
            }
        } catch (ex: IOException) {
            throw ApplicationException("IO Exception occurred while creating base directories", ex)
        }
    }

    /**
     * Save file
     *
     * Checks if the file already exists based on the filename, creatorEmployeeId and source
     * and if not then saves the file to DB and disk
     *
     * @returns token: String
     * @throws BadRequestException
     * @throws ApplicationException
     */
    override fun saveFile(fileReq: FileUploadRequest, fileMeta: FileMeta): String? {
        val file = fileReq.content
        var expireTime: Date? = null
        val fileName = fileMeta.creatorEmployeeId + "_" + file.originalFilename
        try {
            val fileEntity = fileRepository.findByFileNameAndSourceAndMeta_creatorEmployeeId(
                fileName, fileReq.source, fileMeta.creatorEmployeeId
            )
            return if (fileEntity.isPresent) {
                throw BadRequestException("File ${file.originalFilename} already exists for the Employee Id ${fileMeta.creatorEmployeeId} ar Source ${fileReq.source}")
            } else {
                if (!fileReq.expireTime.isNullOrEmpty()) expireTime = AppUtil.convertStringToDate(fileReq.expireTime)
                val fileModel = fileRepository.save(
                    File(
                        UUID.randomUUID().toString(),
                        file.size,
                        fileReq.source,
                        Meta(fileMeta.creatorEmployeeId),
                        expireTime,
                        fileName,
                        file.contentType,
                        Date()
                    )
                )
                Files.copy(file.inputStream, rootPath.resolve(fileReq.source.uppercase()).resolve(fileName))
                log.info(LogItem("File $fileName with token ${fileModel.token} successfully uploaded to directory $rootPath"))
                fileModel.token
            }
        } catch (ex: DataAccessException) {
            throw ApplicationException("DataAccessException occurred while saving file details to DB", ex)
        } catch (ex: IOException) {
            throw ApplicationException(
                "IO Exception occurred while uploading file $fileName to the directory $rootPath", ex
            )
        }
    }

    /**
     * Load file
     *
     * Checks if the file exists based on the filename, creatorEmployeeId and source
     * and then loads the file from disk
     *
     * @returns file: ResponseEntity<Resource>
     * @throws NotFoundException
     * @throws ApplicationException
     */
    override fun loadFile(token: String): ResponseEntity<Resource> {
        val file = getFile(token)
        log.info(LogItem("File ${file.fileName} found in DB for the token $token"))
        try {
            val filePath: Path = rootPath.resolve(file.source).resolve(file.fileName)
            val resource: Resource = UrlResource(filePath.toUri())
            return if (resource.exists()) {
                log.info(LogItem("File ${file.fileName} found in the directory $rootPath"))
                ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.filename + "\"")
                    .header("X-Filename", resource.filename).header("X-Filesize", file.size.toString())
                    .header("X-CreateTime", file.createTime.toString()).body(resource)
            } else {
                throw NotFoundException("File not found in the directory $rootPath for the token $token")
            }
        } catch (ex: IOException) {
            throw ApplicationException(
                "IO Exception occurred while loading file ${file.fileName} with token $token from the directory $rootPath",
                ex
            )
        }
    }

    /**
     * Load FileMetaData
     *
     * Retrieves the file metadata from DB for the given tokens
     *
     * @returns fileMetas: List<FileInfo>
     * @throws ApplicationException
     */
    override fun loadFileMeta(tokens: List<String>): List<FileInfo>? {
        try {
            val fileMetas = fileRepository.findAllById(tokens).toList()
            return modelMapper.map(fileMetas, this.listType)
        } catch (ex: DataAccessException) {
            throw ApplicationException("DataAccessException occurred while loading file details from DB", ex)
        }
    }

    /**
     * FIle Delete
     *
     * Retrieves the file metadata from DB for the given token
     * and deletes the file from DB and disk
     *
     * @returns deleted: Boolean
     * @throws NotFoundException
     * @throws ApplicationException
     */
    override fun deleteFile(token: String): Boolean {
        val file = getFile(token)
        fileRepository.deleteById(token)
        try {
            val filePath: Path = rootPath.resolve(file.source).resolve(file.fileName)
            log.info(LogItem("Deleting the File ${file.fileName} with token ${file.token}"))
            return Files.deleteIfExists(filePath)
        } catch (ex: IOException) {
            throw ApplicationException(
                "IO Exception occurred while deleting file ${file.fileName} with token $token from the directory $rootPath",
                ex
            )
        }
    }

    /**
     * Get Delete
     *
     * Retrieves the file metadata from DB for the given token
     *
     * @returns file: File
     * @throws NotFoundException
     * @throws ApplicationException
     */
    override fun getFile(token: String): File {
        try {
            return fileRepository.findById(token)
                .orElseThrow { NotFoundException("File not found for the token $token") }
        } catch (ex: DataAccessException) {
            throw ApplicationException(
                "DataAccessException occurred while loading file details from DB for the token $token", ex
            )
        }
    }

    /**
     * Delete Expired Files
     *
     * Retrieves the file details from DB from DB which are expired
     * and deletes the respective file from DB and dish
     *
     * @throws ApplicationException
     */
    override fun deleteExpiredFiles() {
        try {
            val files = fileRepository.deleteAllByExpireTimeBefore(Date())
            files?.forEach {
                val filePath: Path = rootPath.resolve(it.source).resolve(it.fileName)
                log.info(LogItem("Deleting the expired File ${it.fileName} with token ${it.token}"))
                Files.deleteIfExists(filePath)
            }
        } catch (ex: DataAccessException) {
            throw ApplicationException("DataAccessException occurred while deleting the expired files from DB", ex)
        } catch (ex: IOException) {
            throw ApplicationException("IO Exception occurred while deleting the expired files from Directory", ex)
        }
    }
}
