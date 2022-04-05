package com.hrblizz.fileapi.library

object AppContext {

    private var contextMap = mutableMapOf<String, String>()

    fun put(key: String, value: String) {
        contextMap[key] = value
    }

    fun get(key: String): String? {
        return contextMap[key]
    }

    fun remove(key: String?) {
        contextMap.remove(key)
    }

    fun clearContext() {
        contextMap.clear()
    }

    fun clearAll() {
        clearContext()
    }
}
