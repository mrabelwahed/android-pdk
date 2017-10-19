package com.pinterest.android.pdk

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


import java.text.ParseException
import java.util.ArrayList
import java.util.Date

class PDKBoard : PDKModel() {

    override var uid: String? = null
    var name: String? = null
    var description: String? = null
    var creator: PDKUser? = null
    var createdAt: Date? = null
    var pinsCount: Int? = null
    var collaboratorsCount: Int? = null
    var followersCount: Int? = null
    var imageUrl: String? = null

    companion object {

        fun makeBoard(obj: Any): PDKBoard {
            val board = PDKBoard()
            try {
                if (obj is JSONObject) {
                    val dataObj = obj
                    if (dataObj.has("id")) {
                        board.uid = dataObj.getString("id")
                    }
                    if (dataObj.has("name")) {
                        board.name = dataObj.getString("name")
                    }
                    if (dataObj.has("description")) {
                        board.description = dataObj.getString("description")
                    }
                    if (dataObj.has("creator")) {
                        board.creator = PDKUser.makeUser(dataObj.getJSONObject("creator"))
                    }
                    if (dataObj.has("created_at")) {
                        board.createdAt = Utils.dateFormatter.parse(dataObj.getString("created_at"))
                    }
                    if (dataObj.has("counts")) {
                        val countsObj = dataObj.getJSONObject("counts")
                        if (countsObj.has("pins")) {
                            board.pinsCount = countsObj.getInt("pins")
                        }
                        if (countsObj.has("collaborators")) {
                            board.collaboratorsCount = countsObj.getInt("collaborators")
                        }
                        if (countsObj.has("followers")) {
                            board.followersCount = countsObj.getInt("followers")
                        }
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
                                    board.imageUrl = iObj.getString("url")
                                }
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                Utils.loge("PDK: PDKBoard parse JSON error %s", e.localizedMessage)
            } catch (e: ParseException) {
                Utils.loge("PDK: PDKBoard parse error %s", e.localizedMessage)
            }

            return board
        }

        fun makeBoardList(obj: Any): List<PDKBoard> {
            val boardList = ArrayList<PDKBoard>()
            try {
                if (obj is JSONArray) {

                    val jAarray = obj
                    val size = jAarray.length()
                    for (i in 0..size - 1) {
                        val dataObj = jAarray.getJSONObject(i)
                        boardList.add(makeBoard(dataObj))
                    }
                }
            } catch (e: JSONException) {
                Utils.loge("PDK: PDKBoard parse JSON error %s", e.localizedMessage)
            }

            return boardList
        }
    }
}
