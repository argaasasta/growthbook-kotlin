package com.sdk.growthbook.utils

import com.sdk.growthbook.model.GBFeature
import com.sdk.growthbook.serializable_model.SerializableGBFeature
import com.sdk.growthbook.serializable_model.gbDeserialize
import com.soywiz.krypto.AES
import com.soywiz.krypto.Padding
import com.soywiz.krypto.encoding.Base64
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

interface Crypto {
    fun decrypt(
        cipherText: ByteArray,
        key: ByteArray,
        iv: ByteArray,

        ): ByteArray

    fun encrypt(
        inputText: ByteArray,
        key: ByteArray,
        iv: ByteArray,
    ): ByteArray
}

class DefaultCrypto : Crypto {

    private val padding = Padding.PKCS7Padding

    override fun decrypt(cipherText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        return AES.decryptAesCbc(cipherText, key, iv, padding)
    }

    override fun encrypt(inputText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        return AES.encryptAesCbc(inputText, key, iv, padding)
    }
}

fun decodeBase64(base64: String): ByteArray {
    return Base64.decode(base64)
}

fun encryptToFeaturesDataModel(string: String): GBFeatures? {
    val jsonParser = Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true }

    return try {
        val serializableGBFeatures: Map<String, SerializableGBFeature> = jsonParser.decodeFromString(
            deserializer = MapSerializer(String.serializer(), SerializableGBFeature.serializer()),
            string = string
        )
        serializableGBFeatures.mapValues { it.value.gbDeserialize() }
    } catch (e: Exception) {
        null
    }
}

fun getFeaturesFromEncryptedFeatures(
    encryptedString: String,
    encryptionKey: String,
    subtleCrypto: Crypto? = null,
): GBFeatures? {
    val encryptedArrayData = encryptedString.split(".")

    val iv = decodeBase64(encryptedArrayData[0])
    val key = decodeBase64(encryptionKey)
    val stringToDecrypt = decodeBase64(encryptedArrayData[1])

    val cryptoLocal = subtleCrypto ?: DefaultCrypto()

    val encrypt: ByteArray = cryptoLocal.decrypt(stringToDecrypt, key, iv)
    val encryptString: String =
        encrypt.decodeToString()
    return encryptToFeaturesDataModel(encryptString)
}

fun getSavedGroupFromEncryptedSavedGroup(
    encryptedString: String,
    encryptionKey: String,
    subtleCrypto: Crypto? = null,
): JsonObject? {
    val encryptedArrayData = encryptedString.split(".")

    val iv = decodeBase64(encryptedArrayData[0])
    val key = decodeBase64(encryptionKey)
    val stringToDecrypt = decodeBase64(encryptedArrayData[1])

    val cryptoLocal = subtleCrypto ?: DefaultCrypto()

    val encrypt: ByteArray = cryptoLocal.decrypt(stringToDecrypt, key, iv)
    val encryptString: String =
        encrypt.decodeToString()

    return try {
        Json.decodeFromString(JsonObject.serializer(), encryptString)
    } catch (e : Exception) {
        null
    }
}
