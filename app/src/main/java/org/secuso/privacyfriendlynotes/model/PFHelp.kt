package org.secuso.privacyfriendlynotes.model

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import org.secuso.privacyfriendlycore.ui.help.Help
import org.secuso.privacyfriendlycore.ui.settings.ISettings
import org.secuso.privacyfriendlycore.ui.settings.MultiStageSettings
import org.secuso.privacyfriendlycore.ui.settings.Settings
import org.secuso.privacyfriendlycore.ui.stage.MultiStage
import org.secuso.privacyfriendlycore.ui.stage.builder.SimpleMultiStage
import org.secuso.privacyfriendlynotes.R

class PFHelp private constructor(context: Context) {

    companion object {
        private var instance: PFHelp? = null
        fun instance(context: Context): PFHelp {
            if (instance == null) {
                instance = PFHelp(context)
            }
            return instance!!
        }
    }
    val help: Help

    init {
        this.help = Help.build(context) {
            item {
                title { resource(R.string.help_perm_receive_boot_title) }
                description { resource(R.string.help_perm_receive_boot_sum) }
            }
            item {
                title { resource(R.string.help_perm_record_audio_title) }
                description { resource(R.string.help_perm_record_audio_sum) }
            }
            item {
                title { resource(R.string.help_perm_write_external_title) }
                description { resource(R.string.help_perm_write_external_sum) }
            }
        }
    }
}