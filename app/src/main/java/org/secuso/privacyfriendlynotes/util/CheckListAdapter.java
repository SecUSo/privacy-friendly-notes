package org.secuso.privacyfriendlynotes.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.SettingsActivity;

import java.util.List;

/**
 * Created by Robin on 12.09.2016.
 */
public class CheckListAdapter extends ArrayAdapter <CheckListItem> {
    public CheckListAdapter(Context context, int resource) {
        super(context, resource);
    }

    public CheckListAdapter(Context context, int resource, List<CheckListItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.item_checklist, null);
        }
        CheckListItem item = getItem(position);

        if (item != null) {
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.item_checkbox);
            TextView textView = (TextView) v.findViewById(R.id.item_name);

            checkBox.setChecked(item.isChecked());
            textView.setText(item.getName());
            // Should we set a custom font size?
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (sp.getBoolean(SettingsActivity.PREF_CUSTOM_FONT, false)) {
                textView.setTextSize(Float.parseFloat(sp.getString(SettingsActivity.PREF_CUTSOM_FONT_SIZE, "15")));
            }
        }

        return v;
    }
}
