package org.secuso.privacyfriendlynotes.ui.util

import android.content.Context
import android.content.res.Configuration

class DarkModeUtil {
    companion object {
        fun isDarkMode(context: Context): Boolean {
            return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
    }
}