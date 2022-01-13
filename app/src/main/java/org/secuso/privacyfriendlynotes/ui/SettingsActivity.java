package org.secuso.privacyfriendlynotes.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import org.secuso.privacyfriendlynotes.R;

/**
 * Activity that allows to register some settings like a custom font size.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_DEL_NOTES = "settings_del_notes";
    public static final String PREF_CUSTOM_FONT = "settings_use_custom_font_size";
    public static final String PREF_CUSTOM_FONT_SIZE = "settings_font_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
