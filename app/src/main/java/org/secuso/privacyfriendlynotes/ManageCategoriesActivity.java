package org.secuso.privacyfriendlynotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ManageCategoriesActivity extends AppCompatActivity implements View.OnClickListener {

    ListView list;
    ArrayList selectedIds = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        findViewById(R.id.btn_add).setOnClickListener(this);
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
                selectedIds.clear();
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                EditText name = (EditText) findViewById(R.id.etName);
                if (!name.getText().toString().isEmpty()){
                    if (!DbAccess.addCategory(getBaseContext(), name.getText().toString())){
                        Snackbar.make(name,R.string.toast_category_exists, Snackbar.LENGTH_SHORT).show();
                    }
                    name.setText("");
                }
                updateList();
                break;
        }
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
