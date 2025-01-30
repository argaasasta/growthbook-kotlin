package com.sdk.growthbook.sandbox

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Interface for Caching Layer
 */
internal interface CachingLayer {
    fun saveContent(fileName: String, content: JsonElement) {
    }

    fun getContent(fileName: String): JsonElement? {
        return null
    }
}

/**
 * Default Implementation for Caching Layer Interface methods
 */
internal fun <T> CachingLayer.getData(fileName: String, serializer: KSerializer<T>): T? {
    val content = getContent(fileName)
        ?: return null
    return Json.decodeFromJsonElement(serializer, content)
}

/**
 * Default Implementation for Caching Layer Interface methods
 */
internal fun <T> CachingLayer.putData(
    fileName: String,
    content: T,
    serializer: KSerializer<T>
) {
    val jsonContent = Json.encodeToJsonElement(serializer, content)
    saveContent(fileName, jsonContent)
}

/**
 * Expectation of Implementation of Caching Layer in respective Library - Android, iOS, JS
 */
internal expect object CachingImpl {
    fun getLayer(localEncryptionKey: String?): CachingLayer
}