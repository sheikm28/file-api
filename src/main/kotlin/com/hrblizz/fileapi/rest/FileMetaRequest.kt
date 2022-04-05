package com.hrblizz.fileapi.rest

import javax.validation.constraints.NotEmpty

class FileMetaRequest {
    @NotEmpty
    lateinit var tokens: List<String>
}
