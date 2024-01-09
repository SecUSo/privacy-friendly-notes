package org.secuso.privacyfriendlynotes.model

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import org.secuso.privacyfriendlycore.ui.settings.ISettings
import org.secuso.privacyfriendlycore.ui.settings.MultiStageSettings
import org.secuso.privacyfriendlycore.ui.settings.Settings
import org.secuso.privacyfriendlycore.ui.stage.MultiStage
import org.secuso.privacyfriendlycore.ui.stage.builder.SimpleMultiStage
import org.secuso.privacyfriendlynotes.R

class PFNoteSettings private constructor(context: Context) {

    companion object {
        private var instance: PFNoteSettings? = null
        fun instance(context: Context): PFNoteSettings {
            if (instance == null) {
                instance = PFNoteSettings(context)
            }
            return instance!!
        }
    }
    val settings: ISettings
    val lightMode: Boolean
        get() {
            return settings.all.find { it.key === "settings_day_night_theme" }!!.state.value.toString().toInt() == AppCompatDelegate.MODE_NIGHT_NO
        }

    init {
        this.settings = MultiStageSettings.build(SimpleMultiStage(), context) {
            stage(context.getString(R.string.settings_general_heading)) {
                category(R.string.settings_general_heading) {
                    switch {
                        key = "settings_del_notes"
                        default = false
                        title { resource(R.string.settings_del_notes_title) }
                        summary { resource(R.string.settings_del_notes_sum) }
                    }
                    switch {
                        key = "settings_dialog_on_trashing"
                        default = true
                        title { resource(R.string.settings_note_trashing_dialog_title) }
                        summary { resource(R.string.settings_note_trashing_dialog_desc) }
                    }
                }
            }
            stage(context.getString(R.string.settings_appearance_heading)) {
                category(R.string.settings_appearance_heading) {
                    radioString {
                        key = "settings_day_night_theme"
                        default = "-1"
                        onUpdate = { AppCompatDelegate.setDefaultNightMode(it.toInt()) }
                        title { resource(R.string.select_day_night_theme) }
                        summary {transform { state, value -> state.entries!!.find { it.value == value}!!.entry }}
                        entries {
                            entries(R.array.array_day_night_theme)
                            values(context.resources.getStringArray(R.array.array_day_night_theme_values).toList())
                        }
                    }
                    switch {
                        key = "settings_color_category"
                        title {resource(R.string.settings_color_category_title)}
                        summary {resource(R.string.settings_color_category_summary)}
                        default = true
                    }
                    switch {
                        key = "settings_use_custom_font_size"
                        default = false
                        title {resource(R.string.settings_use_custom_font_size_title)}
                        summary {resource(R.string.settings_use_custom_font_size_sum)}
                    }
                    radioString {
                        key = "settings_font_size"
                        default = "15"
                        depends = "settings_use_custom_font_size"
                        title {resource(R.string.settings_font_size_title)}
                        summary {resource(R.string.settings_font_size_sum)}
                        entries {
                            entries(R.array.font_size_entries)
                            values(context.resources.getStringArray(R.array.font_size_values).toList())
                        }
                    }
                    switch {
                        key = "settings_show_preview"
                        default = true
                        title {resource(R.string.settings_settings_show_preview_title)}
                        summary {resource(R.string.settings_settings_settings_show_preview_sum)}
                    }
                }
            }

        }
    }
}