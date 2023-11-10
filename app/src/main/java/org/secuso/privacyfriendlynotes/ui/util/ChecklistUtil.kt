package org.secuso.privacyfriendlynotes.ui.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ChecklistUtil {

    companion object {
        fun parse(checklist: String): List<Pair<Boolean, String>> {
            try {
                val content = JSONArray(checklist)
                return (0 until content.length()).map {
                    val obj = content.getJSONObject(it)
                    return@map Pair(obj.getBoolean("checked"), obj.getString("name"))
                }
            } catch (ex: JSONException) {
                return ArrayList()
            }
        }

        fun json(checklist: List<Pair<Boolean, String>>): JSONArray {
            val jsonArray = JSONArray()
            try {
                for ((checked, name) in checklist) {
                    val jsonObject = JSONObject()
                    jsonObject.put("name", name)
                    jsonObject.put("checked", checked)
                    jsonArray.put(jsonObject)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return jsonArray
        }
    }
}