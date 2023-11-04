package org.secuso.privacyfriendlynotes.model

import android.content.Context
import android.preference.PreferenceManager
import org.secuso.privacyfriendlynotes.preference.PreferenceKeys

class SortingOrder(context: Context) {

    val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
    var ordering = Options.values().filter {
        it.name == prefManager.getString(PreferenceKeys.SP_NOTES_ORDERING, Options.Creation.name)
    }.getOrElse(0) { _ -> Options.Creation }
        set(value) {
            prefManager.edit()
                .putString(PreferenceKeys.SP_NOTES_ORDERING, value.name)
                .apply()
            field = value
        }

    enum class Options {
        AlphabeticalAscending,
        TypeAscending,
        Creation,
        LastModified
    }
}