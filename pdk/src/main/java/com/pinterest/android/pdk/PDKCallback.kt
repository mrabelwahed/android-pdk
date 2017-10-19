package com.pinterest.android.pdk

import com.android.volley.Response
import com.android.volley.VolleyError

import org.json.JSONObject


import java.util.HashMap

open class PDKCallback : Response.Listener<JSONObject>, Response.ErrorListener {

    private var statusCode: Int = 0
    private var responseHeaders: Map<String, String>? = null
    private var path: String? = null
    private var params: HashMap<String, String>? = null

    override fun onResponse(response: JSONObject) {
        try {
            onSuccess(response)
        } catch (e: Exception) {
        }

    }

    override fun onErrorResponse(error: VolleyError) {
        onFailure(PDKException(error))
    }

    fun onSuccess(response: JSONObject) {
        val apiResponse = PDKResponse(response, path, params, statusCode)
        onSuccess(apiResponse)
    }

    open fun onSuccess(response: PDKResponse) {}

    open fun onFailure(exception: PDKException) {}

    fun setResponseHeaders(map: Map<String, String>) {
        responseHeaders = map
    }

    fun setStatusCode(code: Int) {
        statusCode = code
    }

    fun setPath(path: String) {
        this.path = path
    }

    fun setParams(params: HashMap<String, String>) {
        this.params = params
    }
}
