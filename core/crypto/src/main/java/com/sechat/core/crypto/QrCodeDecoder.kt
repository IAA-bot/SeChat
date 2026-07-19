package com.sechat.core.crypto

object QrCodeDecoder {

    data class DecodedData(
        val version: String,
        val fingerprint: String,
        val publicKeyHex: String
    )

    fun decode(rawText: String): DecodedData? {
        val parts = rawText.split(":")
        if (parts.size < 3) return null
        if (parts[0] != "SECHAT1") return null
        return DecodedData(
            version = parts[0],
            fingerprint = parts[1],
            publicKeyHex = parts.subList(2, parts.size).joinToString(":")
        )
    }
}
