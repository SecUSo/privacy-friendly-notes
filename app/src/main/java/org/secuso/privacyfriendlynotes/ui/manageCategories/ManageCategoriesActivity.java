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
package org.secuso.privacyfriendlynotes.ui.manageCategories;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.model.Category;
import org.secuso.privacyfriendlynotes.room.adapter.CategoryAdapter;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.ui.SettingsActivity;

import java.util.List;

/**
 * Activity provides possibility to add, delete categories.
 * Data is provided by the ManageCategoriesViewModel
 * @see ManageCategoriesViewModel
 */

public class ManageCategoriesActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recycler_list;
    ManageCategoriesViewModel manageCategoriesViewModel;
    List<Category> allCategories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        findViewById(R.id.btn_add).setOnClickListener(this);

        recycler_list = (RecyclerView) findViewById(R.id.recyclerview_category);

        recycler_list.setLayoutManager(new LinearLayoutManager(this));
        recycler_list.setHasFixedSize(true);
        final CategoryAdapter adapter = new CategoryAdapter();
        recycler_list.setAdapter(adapter);

        manageCategoriesViewModel = new ViewModelProvider(this).get(ManageCategoriesViewModel.class);
        manageCategoriesViewModel.getAllCategoriesLive().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                adapter.setCategories(categories);
                allCategories = categories;

            }
        });

        adapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Category currentCategory) {
                new AlertDialog.Builder(ManageCategoriesActivity.this)
                        .setTitle(String.format(getString(R.string.dialog_delete_title), currentCategory.getName()))
                        .setMessage(String.format(getString(R.string.dialog_delete_message), currentCategory.getName()))
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        })
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCategory(currentCategory);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                EditText name = (EditText) findViewById(R.id.etName);
                if (!name.getText().toString().isEmpty()){
                    Category category = new Category(name.getText().toString());
                    boolean duplicate = false;
                    for(Category currentCat: allCategories){
                        if(currentCat.getName().equals(category.getName())){
                            duplicate = true;
                        }
                    }
                    if(!duplicate){
                        manageCategoriesViewModel.insert(category);
                    }
                }
                break;
        }
    }


    private void deleteCategory(Category cat){

        // Delete all notes from category if the option is set
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(SettingsActivity.PREF_DEL_NOTES, false)) {
            manageCategoriesViewModel.getAllNotesLiveData().observe(this, new Observer<List<Note>>() {
                @Override
                public void onChanged(@Nullable List<Note> notes) {
                    for(Note currentNote: notes){
                        if(currentNote.getCategory() == cat.get_id()){
                            manageCategoriesViewModel.delete(currentNote);
                        }
                    }
                }
            });
        }

        manageCategoriesViewModel.delete(cat);
    }
}
