package com.hrblizz.fileapi.rest

import org.springframework.web.multipart.MultipartFile
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class FileUploadRequest {
    @NotBlank
    var source: String = ""

    @NotBlank
    var meta: String = ""
    var expireTime: String? = null

    @NotNull
    lateinit var content: MultipartFile
}
