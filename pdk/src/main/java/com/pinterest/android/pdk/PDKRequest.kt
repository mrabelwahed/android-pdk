package com.pinterest.android.pdk

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest

import org.json.JSONObject


import java.util.Collections
import java.util.HashMap

class PDKRequest : JsonObjectRequest {

    private val _callback: PDKCallback?
    private var _headers: Map<String, String>? = null

    constructor(method: Int, url: String, jsonRequest: JSONObject?,
                listener: Response.Listener<JSONObject>,
                errorListener: Response.ErrorListener) : super(method, url, jsonRequest, listener, errorListener) {
        _callback = null
    }

    constructor(method: Int, url: String, `object`: JSONObject?, callback: PDKCallback, headers: Map<String, String>) : super(method, url, `object`, callback, callback) {
        _callback = callback
        _headers = headers
    }


    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        var headers = _headers
        if (null == headers || headers == emptyMap<Any, Any>()) {
            headers = HashMap<String, String>()
            _headers = headers
        }
        return headers
    }


    override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
        _callback?.setResponseHeaders(response.headers)
        _callback?.setStatusCode(response.statusCode)
        return super.parseNetworkResponse(response)
    }


}
