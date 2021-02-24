package kr.co.petdoc.petdoc.utils

import android.util.Base64
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.log.Logger
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Petdoc
 * Class: AES256Chiper
 * Created by kimjoonsung on 2020/04/24.
 *
 * Description : 쇼핑 탭에서 고도몰로 이동할 때 암호화 사용
 */
object AES256Cipher {

    private var iv: String = "asdfqwerasdfqwer"


    fun encryptURL(str: String) = URLEncoder.encode(String(AES_Encode(str, AppConstants.GODO_KEY, iv)), "UTF-8")

    /**
     * AES256 암호화
     */
    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    private fun AES_Encode(data: String, strKey:String, strlv:String): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = SecretKeySpec(strKey.toByteArray(Charset.defaultCharset()), "AES")
        val iv = IvParameterSpec(strlv.toByteArray(Charset.defaultCharset()), 0, cipher.blockSize)

        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val encrypted = cipher.doFinal(data.toByteArray(Charset.defaultCharset()))

        val result = ByteArray(strlv.toByteArray(Charset.defaultCharset()).size + encrypted.size)
        System.arraycopy(
            strlv.toByteArray(Charset.defaultCharset()),
            0,
            result,
            0,
            strlv.toByteArray(Charset.defaultCharset()).size
        )
        System.arraycopy(
            encrypted,
            0,
            result,
            strlv.toByteArray(Charset.defaultCharset()).size,
            encrypted.size
        )

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            java.util.Base64.getEncoder().encode(result)
        } else {
            Base64.encode(result, Base64.DEFAULT)
        }
    }
}