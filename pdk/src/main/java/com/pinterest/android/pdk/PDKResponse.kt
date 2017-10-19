package com.pinterest.android.pdk

import org.json.JSONException
import org.json.JSONObject


import java.util.HashMap

open class PDKResponse(obj: JSONObject?,
                       val path: String?,
                       val params: HashMap<String, String>?,
                       val status: Int = -1) {


    //Setter & Getters

    var data: Any? = null
    protected var _cursor: String? = null

    init {
        obj?.let {
            if (it.has("data")) {
                try {
                    data = it.get("data")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            if (it.has("page")) {
                try {
                    val pageObj = it.getJSONObject("page")
                    if (pageObj.has("cursor")) {
                        _cursor = pageObj.getString("cursor")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

    }

    val isValid: Boolean
        get() = data != null
    val pin: PDKPin
        get() = PDKPin.makePin(data!!)

    val pinList: List<PDKPin>
        get() = PDKPin.makePinList(data!!)

    val user: PDKUser
        get() = PDKUser.makeUser(data!!)

    val userList: List<PDKUser>
        get() = PDKUser.makeUserList(data!!)

    val board: PDKBoard
        get() = PDKBoard.makeBoard(data!!)

    val boardList: List<PDKBoard>
        get() = PDKBoard.makeBoardList(data!!)

    val interest: PDKInterest
        get() = PDKInterest.makeInterest(data!!)

    val interestList: List<PDKInterest>
        get() = PDKInterest.makeInterestList(data!!)

    fun loadNext(callback: PDKCallback) {
        params!!.put(PDKClient.PDK_QUERY_PARAM_CURSOR, _cursor!!)
        PDKClient.instance.getPath(path!!, params, callback)
    }

    operator fun hasNext(): Boolean {
        return _cursor != null && _cursor!!.isNotEmpty() && !_cursor!!.equals("null", ignoreCase = true)
    }
}
