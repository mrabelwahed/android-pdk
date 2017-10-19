package com.pinterest.android.pdk

import android.util.Log

import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils


import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.experimental.and

object Utils {

    private val TAG = "PDK"
    val dateFormatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    fun <T> isEmpty(c: Collection<T>?): Boolean {
        return c == null || c.size == 0
    }

    fun isEmpty(m: Map<*, *>?): Boolean {
        return m == null || m.size == 0
    }

    fun isEmpty(s: String?): Boolean {
        return s == null || s.length == 0
    }

    /**
     * Log errors

     * @param s base String
     * *
     * @param params Objects to format in
     */
    fun loge(s: String, vararg params: Any) {
        if (PDKClient.isDebugMode)
            Log.e(TAG, String.format(s, *params))
    }

    /**
     * Log info

     * @param s base String
     * *
     * @param params Objects to format in
     */
    fun log(s: String, vararg params: Any) {
        if (PDKClient.isDebugMode)
            Log.i(TAG, String.format(s, *params))
    }

    fun getUrlWithQueryParams(url: String?, params: List<NameValuePair>?): String? {
        var url: String = url ?: return null

        url = url.replace(" ", "%20")

        if (!url.endsWith("?"))
            url += "?"

        if (params != null && params.size > 0) {
            val paramString = URLEncodedUtils.format(params, "utf-8")
            url += paramString
        }
        return url
    }

    fun sha1Hex(byteArray: ByteArray): String {

        val messageDigest: MessageDigest
        try {
            messageDigest = MessageDigest.getInstance("SHA-1")
        } catch (e: NoSuchAlgorithmException) {
            return ""
        }

        val encoded = messageDigest.digest(byteArray)
        val builder = StringBuilder()
        for (b in encoded) {
            builder.append(Integer.toHexString((b.toInt() and 0xf0) shr 4))
            builder.append(Integer.toHexString(b.toInt() and 0x0f))
        }
        return builder.toString()
    }
}
