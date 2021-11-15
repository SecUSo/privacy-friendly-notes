package org.secuso.privacyfriendlynotes.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.secuso.privacyfriendlynotes.database.DbAccess;
import org.secuso.privacyfriendlynotes.database.DbContract;
import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.Category;
import org.secuso.privacyfriendlynotes.room.CategoryAdapter;
import org.secuso.privacyfriendlynotes.room.CategoryViewModel;
import org.secuso.privacyfriendlynotes.room.Note;
import org.secuso.privacyfriendlynotes.room.NoteAdapter;
import org.secuso.privacyfriendlynotes.room.NoteViewModel;

import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity implements View.OnClickListener {

    ListView list;
    RecyclerView recycler_list;
    NoteViewModel noteViewModel;
    CategoryViewModel categoryViewModel;


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

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.getAllCategories().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> categoryNames) {
                adapter.setCategoryNames(categoryNames);
            }

        });

        adapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String category) {

            }

        });


        list = (ListView) findViewById(R.id.category_list);
        String[] from = {DbContract.CategoryEntry.COLUMN_NAME};
        int[] to = {R.id.item_name};
        list.setAdapter(new SimpleCursorAdapter(getBaseContext(), R.layout.item_category, DbAccess.getCategoriesWithoutDefault(getBaseContext()), from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                //do nothing
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.manage_cab, menu);
                return true;

            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteSelectedItems();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                updateList();
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                CursorAdapter adapter = (CursorAdapter) list.getAdapter();
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_NAME));
                new AlertDialog.Builder(ManageCategoriesActivity.this)
                        .setTitle(String.format(getString(R.string.dialog_delete_title), name))
                        .setMessage(String.format(getString(R.string.dialog_delete_message), name))
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        })
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteItem(position);
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
                    categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
                    categoryViewModel.insert(category);
                }
                break;
        }
    }

    private void deleteItem(int position) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean delNotes = sp.getBoolean(SettingsActivity.PREF_DEL_NOTES, false);
        CursorAdapter adapter = (CursorAdapter) list.getAdapter();
        if (delNotes) {
            DbAccess.trashNotesByCategoryId(getBaseContext(), (int) (long) adapter.getItemId(position));
        }
        DbAccess.deleteCategory(getBaseContext(), (int) (long) adapter.getItemId(position));
        updateList();
    }

    private void deleteSelectedItems(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean delNotes = sp.getBoolean(SettingsActivity.PREF_DEL_NOTES, false);
        CursorAdapter adapter = (CursorAdapter) list.getAdapter();
        SparseBooleanArray checkedItemPositions = list.getCheckedItemPositions();
        for (int i=0; i < checkedItemPositions.size(); i++) {
            if(checkedItemPositions.valueAt(i)) {
                if (delNotes) {
                    DbAccess.trashNotesByCategoryId(getBaseContext(), (int) (long) adapter.getItemId(checkedItemPositions.keyAt(i)));
                }
                DbAccess.deleteCategory(getBaseContext(), (int) (long) adapter.getItemId(checkedItemPositions.keyAt(i)));
            }
        }
    }

    private void updateList(){
        CursorAdapter adapter = (CursorAdapter) list.getAdapter();
        adapter.changeCursor(DbAccess.getCategoriesWithoutDefault(getBaseContext()));
    }
}
