package com.pinterest.android.pdk

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


import java.util.ArrayList

class PDKInterest : PDKModel() {

    override var uid: String? = null
    var name: String? = null

    companion object {

        fun makeInterest(obj: Any): PDKInterest {
            val interest = PDKInterest()
            try {
                if (obj is JSONObject) {
                    val dataObj = obj
                    if (dataObj.has("id")) {
                        interest.uid = dataObj.getString("id")
                    }
                    if (dataObj.has("name")) {
                        interest.name = dataObj.getString("name")
                    }
                }
            } catch (e: JSONException) {
                Utils.loge("PDK: PDKInterest parse JSON error %s", e.localizedMessage)
            }

            return interest
        }

        fun makeInterestList(obj: Any): List<PDKInterest> {
            val interestList = ArrayList<PDKInterest>()
            try {
                if (obj is JSONArray) {

                    val jAarray = obj
                    val size = jAarray.length()
                    for (i in 0..size - 1) {
                        val dataObj = jAarray.getJSONObject(i)
                        interestList.add(makeInterest(dataObj))
                    }
                }
            } catch (e: JSONException) {
                Utils.loge("PDK: PDKInterst parse JSON error %s", e.localizedMessage)
            }

            return interestList
        }
    }

}
