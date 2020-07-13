package org.secuso.privacyfriendlynotes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_DEL_NOTES = "settings_del_notes";
    public static final String PREF_CUSTOM_FONT = "settings_use_custom_font_size";
    public static final String PREF_CUTSOM_FONT_SIZE = "settings_font_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
