package com.sechat.core.crypto

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeEncoder {

    private const val QR_SIZE = 512

    fun encode(publicKeyRaw: ByteArray, fingerprint: String): Bitmap {
        val content = buildString {
            append("SECHAT1:")
            append(fingerprint.replace(" ", ""))
            append(":")
            append(publicKeyRaw.joinToString("") { "%02x".format(it) })
        }
        return generateQrBitmap(content)
    }

    private fun generateQrBitmap(content: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE)
        val bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565)
        for (x in 0 until QR_SIZE) {
            for (y in 0 until QR_SIZE) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

}
