package com.hrblizz.fileapi.data.repository

import com.hrblizz.fileapi.data.entities.File
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.Optional

@Repository
interface FileRepository : MongoRepository<File, String> {

    fun findByFileNameAndSourceAndMeta_creatorEmployeeId(
        filename: String,
        source: String,
        empId: String
    ): Optional<File>

    fun deleteAllByExpireTimeBefore(date: Date): List<File>?
}
