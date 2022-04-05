package com.hrblizz.fileapi

import com.hrblizz.fileapi.controller.exception.NotFoundException
import com.hrblizz.fileapi.library.AppConstants
import com.hrblizz.fileapi.library.AppUtil
import com.hrblizz.fileapi.library.FileUploadRequestValidator
import com.hrblizz.fileapi.library.JsonUtil
import com.hrblizz.fileapi.library.log.Logger
import com.hrblizz.fileapi.rest.FileInfo
import com.hrblizz.fileapi.rest.FileMeta
import com.hrblizz.fileapi.rest.FileMetaRequest
import com.hrblizz.fileapi.service.IFileStorageService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Repository
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.Base64Utils
import java.util.Date

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class FileControllerTest(@Autowired val mockMvc: MockMvc) {
    @MockkBean
    lateinit var fileStorageService: IFileStorageService

    @RelaxedMockK
    lateinit var fileUploadRequestValidator: FileUploadRequestValidator

    @MockkBean
    lateinit var fileRepository: Repository

    @RelaxedMockK
    lateinit var logger: Logger

    @Test
    fun getFileMetaDataWithStatus200() {

        val fileMetaRequest = FileMetaRequest()
        fileMetaRequest.tokens = listOf("454c36e2-2bb1-49b9-84eb-0a1de6d5a5")

        val fileInfo = FileInfo(
            "454c36e2-2bb1-49b9-84eb-0a1de6d5a5",
            84750,
            "HRB",
            FileMeta("sheik"),
            null,
            "test.pdf",
            "application/pdf",
            AppUtil.convertStringToDate("2022-05-29 15:00:35")!!
        )

        every { fileStorageService.loadFileMeta(fileMetaRequest.tokens) } returns listOf(fileInfo)

        val postReq: MockHttpServletRequestBuilder = post(
            AppConstants.FILE_METADATA_URI_PATH
        ).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(fileMetaRequest)).header(
            HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("admin:hunter2".toByteArray())
        )

        mockMvc.perform(postReq).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.data[0].token", containsString("454c36e2-2bb1-49b9-84eb-0a1de6d5a5")))
            .andExpect(jsonPath("$.data[0].source", containsString("HRB")))
            .andExpect(jsonPath("$.data[0].fileName", containsString("test.pdf")))
    }

    @Test
    fun getFileMetaDataWithStatus400() {

        val fileMetaRequest = FileMetaRequest()
        fileMetaRequest.tokens = emptyList()

        val fileInfo = FileInfo(
            "454c36e2-2bb1-49b9-84eb-0a1de6d5a5",
            84750,
            "HRB",
            FileMeta("sheik"),
            null,
            "test.pdf",
            "application/pdf",
            AppUtil.convertStringToDate("2022-05-29 15:00:35")!!
        )

        every { fileStorageService.loadFileMeta(fileMetaRequest.tokens) } returns listOf(fileInfo)

        val postReq: MockHttpServletRequestBuilder = post(
            AppConstants.FILE_METADATA_URI_PATH
        ).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJson(fileMetaRequest)).header(
            HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("admin:hunter2".toByteArray())
        )

        mockMvc.perform(postReq).andExpect(status().isBadRequest)
    }

    @Test
    fun getFileWithStatus200() {
        val fileName = "test.txt"
        val file = MockMultipartFile(
            "test-file", fileName, "text/plain", "This is the file content".toByteArray()
        )
        val responseEntity = ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.resource.filename + "\"")
            .header("X-Filename", file.resource.filename).header("X-Filesize", file.size.toString())
            .header("X-CreateTime", Date().toString()).body(file.resource)

        every { fileStorageService.loadFile("687c36e2-2bb1-49b9-84eb-0a1de6dad4") } returns responseEntity

        val getReq: MockHttpServletRequestBuilder = get("/file/687c36e2-2bb1-49b9-84eb-0a1de6dad4").header(
            HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("admin:hunter2".toByteArray())
        )

        mockMvc.perform(getReq).andExpect(status().isOk).andExpect(header().string("X-Filename", equalTo("test.txt")))
            .andExpect(header().string("Content-Type", equalTo("text/plain")))
    }

    @Test
    fun getFileWithStatus404() {
        every { fileStorageService.loadFile("687c36e2-2bb1-49b9-84eb-0a1de6dad4") } throws NotFoundException("File Not Found")
        val getReq: MockHttpServletRequestBuilder = get("/file/687c36e2-2bb1-49b9-84eb-0a1de6dad4").header(
            HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("admin:hunter2".toByteArray())
        )
        mockMvc.perform(getReq).andExpect(status().isNotFound)
    }

    @Test
    fun uploadFileWithStatus200() {
        val file = MockMultipartFile(
            "content", "test.txt", "text/plain", "This is the file content".toByteArray()
        )
        val multipartReq =
            multipart(AppConstants.UPLOAD_FILE_URI_PATH).file(file).param("source", "HRB")
                .param("meta", "{\"creatorEmployeeId\":\"test\"}").header(
                    HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("admin:hunter2".toByteArray())
                )

        every { fileStorageService.saveFile(any(), any()) } returns "687c32-2bb1-49b9-84eb-0a6dad4"

        mockMvc.perform(multipartReq).andExpect(status().isOk)
    }

    @Test
    fun uploadFileWithStatus400() {
        val fileName = "test.txt"
        val file = MockMultipartFile(
            "test-file", fileName, "text/plain", "This is the file content".toByteArray()
        )
        val multipartReq =
            multipart(AppConstants.UPLOAD_FILE_URI_PATH).file("content", file.bytes).param("source", "ABC")
                .param("meta", "{\"creatorEmployeeId\":\"test\"}").header(
                    HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("admin:hunter2".toByteArray())
                )

        every { fileStorageService.saveFile(any(), any()) } returns "687c32-2bb1-49b9-84eb-0a6dad4"

        mockMvc.perform(multipartReq).andExpect(status().isBadRequest)
    }
}
