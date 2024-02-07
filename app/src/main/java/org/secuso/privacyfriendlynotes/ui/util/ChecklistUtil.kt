package org.secuso.privacyfriendlynotes.ui.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

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

        fun textToItem(text: String): Pair<Boolean, String> {
            Pattern.compile("-\\s*\\[(.*)]\\s*(.*)").matcher(text).apply {
                if (matches()) {
                    val checked = group(1);
                    val name = group(2);
                    return Pair(checked !== null && checked.isNotEmpty() && checked.isNotBlank(), name!!)
                }
            }
            return Pair(false, text)
        }

//        fun contentString(checklist: List<Pair<Boolean, String>>): String {
//            checklist.map { (checked, text) ->  }
//        }
    }
}