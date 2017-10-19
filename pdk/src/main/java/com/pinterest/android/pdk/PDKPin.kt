package com.pinterest.android.pdk

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


import java.text.ParseException
import java.util.ArrayList
import java.util.Date

class PDKPin : PDKModel() {

    override var uid: String? = null
    var board: PDKBoard? = null
    var user: PDKUser? = null
    var link: String? = null
    var note: String? = null
    var color: String? = null
    var metadata: String? = null
    var createdAt: Date? = null
    var likeCount: Int? = null
    var commentCount: Int? = null
    var repinCount: Int? = null
    var imageUrl: String? = null

    companion object {

        fun makePin(obj: Any): PDKPin {
            val pin = PDKPin()
            try {
                if (obj is JSONObject) {
                    val dataObj = obj
                    if (dataObj.has("id")) {
                        pin.uid = dataObj.getString("id")
                    }
                    if (dataObj.has("link")) {
                        pin.link = dataObj.getString("link")
                    }
                    if (dataObj.has("note")) {
                        pin.note = dataObj.getString("note")
                    }
                    if (dataObj.has("color")) {
                        pin.color = dataObj.getString("color")
                    }
                    if (dataObj.has("metadata")) {
                        pin.metadata = dataObj.get("metadata").toString()
                    }
                    if (dataObj.has("counts")) {
                        val countsObj = dataObj.getJSONObject("counts")
                        if (countsObj.has("likes")) {
                            pin.likeCount = countsObj.getInt("likes")
                        }
                        if (countsObj.has("comments")) {
                            pin.commentCount = countsObj.getInt("comments")
                        }
                        if (countsObj.has("repins")) {
                            pin.repinCount = countsObj.getInt("repins")
                        }
                    }
                    if (dataObj.has("metadata")) {
                        pin.metadata = dataObj.getString("metadata")
                    }
                    if (dataObj.has("creator")) {
                        pin.user = PDKUser.makeUser(dataObj.getJSONObject("creator"))
                    }
                    if (dataObj.has("board")) {
                        pin.board = PDKBoard.makeBoard(dataObj.getJSONObject("board"))
                    }
                    if (dataObj.has("created_at")) {
                        pin.createdAt = Utils.dateFormatter.parse(dataObj.getString("created_at"))
                    }
                    if (dataObj.has("image")) {
                        val imageObj = dataObj.getJSONObject("image")
                        val keys = imageObj.keys()

                        //TODO: for now we'll have just one image map. We will change this logic after appathon
                        while (keys.hasNext()) {
                            val key = keys.next()
                            if (imageObj.get(key) is JSONObject) {
                                val iObj = imageObj.getJSONObject(key)
                                if (iObj.has("url")) {
                                    pin.imageUrl = iObj.getString("url")
                                }
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                Utils.loge("PDK: PDKPin JSON parse error %s", e.localizedMessage)
            } catch (e: ParseException) {
                Utils.loge("PDK: PDKPin Text parse error %s", e.localizedMessage)
            }

            return pin
        }

        fun makePinList(obj: Any): List<PDKPin> {
            val pinList = ArrayList<PDKPin>()
            try {
                if (obj is JSONArray) {

                    val jAarray = obj
                    val size = jAarray.length()
                    for (i in 0..size - 1) {
                        val dataObj = jAarray.getJSONObject(i)
                        pinList.add(makePin(dataObj))
                    }
                }
            } catch (e: JSONException) {
                Utils.loge("PDK: PDKPinList parse JSON error %s", e.localizedMessage)
            }

            return pinList
        }
    }
}
