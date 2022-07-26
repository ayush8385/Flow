package com.ayush.flow.Services

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESEncryption {
    private val SECRET_KEY = "6378512345630968"
    private val INIT_VECTOR = IvParameterSpec(ByteArray(16))

    fun encrypt(value: String): String? {
        try {
            val skeySpec = SecretKeySpec(SECRET_KEY.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, INIT_VECTOR)
            val encrypted = cipher.doFinal(value.toByteArray())
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun decrypt(value: String?): String? {
        try {
            val skeySpec = SecretKeySpec(SECRET_KEY.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, INIT_VECTOR)
            val original = cipher.doFinal(Base64.decode(value, Base64.DEFAULT))
            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}