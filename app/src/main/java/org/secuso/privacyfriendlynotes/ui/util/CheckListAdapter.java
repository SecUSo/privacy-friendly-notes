/*
 This file is part of the application Privacy Friendly Notes.
 Privacy Friendly Notes is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 Privacy Friendly Notes is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Notes. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlynotes.ui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.secuso.privacyfriendlynotes.R;

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
//            CheckBox checkBox = (CheckBox) v.findViewById(R.id.item_checkbox);
            TextView textView = (TextView) v.findViewById(R.id.item_name);

//            checkBox.setChecked(item.isChecked());
            textView.setText(item.getName());
            // Should we set a custom font size?
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (sp.getBoolean("settings_use_custom_font_size", false)) {
                textView.setTextSize(Float.parseFloat(sp.getString("settings_font_size", "15")));
            }
        }

        return v;
    }
}
