package com.hrblizz.fileapi.library

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.hrblizz.fileapi.controller.exception.BadRequestException
import java.text.SimpleDateFormat

object JsonUtil {
    /**
     * Safely writes the input object into a JSON string
     */
    fun toJson(obj: Any, usePrettyWriter: Boolean = false, formatDates: Boolean = false): String? {
        try {
            val mapper = ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

            if (formatDates) {
                mapper.dateFormat = SimpleDateFormat(AppConstants.DATE_PATTERN)
            }

            var writer = mapper.writer()

            if (usePrettyWriter) {
                writer = writer.withDefaultPrettyPrinter()
            }
            return writer.writeValueAsString(obj)
        } catch (e: JsonProcessingException) {
            // Do nothing
        }

        return null
    }

    /**
     * Safely creates input JSON string into the object
     */
    fun toObject(value: String?, formatDates: Boolean = false, cls: Class<*>): Any? {
        try {
            val mapper = ObjectMapper()
            if (formatDates) {
                mapper.dateFormat = SimpleDateFormat(AppConstants.DATE_PATTERN)
            }
            val reader = mapper.reader()
            return reader.readValue(value, cls)
        } catch (e: JsonProcessingException) {
            throw BadRequestException("Json Invalid Format Received")
        }
        return null
    }
}
