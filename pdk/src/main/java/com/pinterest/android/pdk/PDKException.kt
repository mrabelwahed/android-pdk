package com.pinterest.android.pdk

import com.android.volley.VolleyError

import org.json.JSONException
import org.json.JSONObject

open class PDKException : Exception {
    var stausCode = -1
    var detailMessage = ""
    protected var _baseUrl: String? = null
    protected var _method: String? = null

    constructor() : super() {}

    constructor(message: String) : super(message) {
        detailMessage = message
    }

    constructor(message: String, throwable: Throwable) : super(message, throwable) {}

    constructor(error: VolleyError?) : super() {
        var message = ""
        if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
            message = String(error.networkResponse.data)
            detailMessage = message
        }

        if (message.isNotEmpty() && message.startsWith("{")) {
            try {
                val errObj = JSONObject(message)

                if (errObj.has("status")) {
                    stausCode = errObj.getInt("status")
                }
                if (errObj.has("messsage")) {
                    detailMessage = errObj.getString("message")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    companion object {

        internal val serialVersionUID: Long = 1
    }

}