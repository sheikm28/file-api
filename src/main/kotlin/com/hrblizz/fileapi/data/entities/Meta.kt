package com.hrblizz.fileapi.data.entities

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "metas")
data class Meta(var creatorEmployeeId: String?)
