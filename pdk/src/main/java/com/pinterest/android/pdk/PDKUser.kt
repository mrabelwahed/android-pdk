package com.pinterest.android.pdk

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


import java.text.ParseException
import java.util.ArrayList
import java.util.Date

class PDKUser : PDKModel() {

    override var uid: String? = null
    var username: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var bio: String? = null
    var createdAt: Date? = null
    var imageUrl: String? = null
    var imageWidth: Int? = null
    var imageHeight: Int? = null
    var followersCount: Int? = null
    var followingCount: Int? = null
    var pinCount: Int? = null
    var likesCount: Int? = null
    var boardsCount: Int? = null

    companion object {

        fun makeUser(obj: Any): PDKUser {
            val user = PDKUser()
            try {
                if (obj is JSONObject) {

                    val dataObj = obj
                    if (dataObj.has("id")) {
                        user.uid = dataObj.getString("id")
                    }
                    if (dataObj.has("first_name")) {
                        user.firstName = dataObj.getString("first_name")
                    }
                    if (dataObj.has("last_name")) {
                        user.lastName = dataObj.getString("last_name")
                    }
                    if (dataObj.has("username")) {
                        user.username = dataObj.getString("username")
                    }
                    if (dataObj.has("bio")) {
                        user.bio = dataObj.getString("bio")
                    }
                    if (dataObj.has("created_at")) {
                        user.createdAt = Utils.dateFormatter.parse(dataObj.getString("created_at"))
                    }
                    if (dataObj.has("counts")) {
                        val countsObj = dataObj.getJSONObject("counts")
                        if (countsObj.has("pins")) {
                            user.likesCount = countsObj.getInt("pins")
                        }
                        if (countsObj.has("following")) {
                            user.followingCount = countsObj.getInt("following")
                        }
                        if (countsObj.has("followers")) {
                            user.followersCount = countsObj.getInt("followers")
                        }
                        if (countsObj.has("boards")) {
                            user.boardsCount = countsObj.getInt("boards")
                        }
                        if (countsObj.has("likes")) {
                            user.likesCount = countsObj.getInt("likes")
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
                                    user.imageUrl = iObj.getString("url")
                                }
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                Utils.loge("PDK: PDKUser parse JSON error %s", e.localizedMessage)
            } catch (e: ParseException) {
                Utils.loge("PDK: PDKUser parse error %s", e.localizedMessage)
            }

            return user
        }

        fun makeUserList(obj: Any): List<PDKUser> {
            val userList = ArrayList<PDKUser>()
            try {
                if (obj is JSONArray) {

                    val jAarray = obj
                    val size = jAarray.length()
                    for (i in 0..size - 1) {
                        val dataObj = jAarray.getJSONObject(i)
                        userList.add(makeUser(dataObj))
                    }
                }
            } catch (e: JSONException) {
                Utils.loge("PDK: PDKUserList parse JSON error %s", e.localizedMessage)
            }

            return userList
        }
    }
}
