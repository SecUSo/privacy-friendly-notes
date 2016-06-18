package org.secuso.privacyfriendlynotes;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ManageCategoriesActivity extends AppCompatActivity implements View.OnClickListener {

    ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        findViewById(R.id.btn_add).setOnClickListener(this);
        list = (ListView) findViewById(R.id.category_list);
        list.setAdapter(new CursorAdapter(getApplicationContext(), DbAccess.getCategories(getBaseContext()), CursorAdapter.FLAG_AUTO_REQUERY) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.item_category, null);

                TextView text = (TextView) rowView.findViewById(R.id.item_name);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_NAME));
                text.setText(name);
                ImageView iv = (ImageView) rowView.findViewById(R.id.item_icon);
                iv.setTag(R.id.tag_id, cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_ID))); //The tag is later used to delete a category.
                iv.setTag(R.id.tag_name, name);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int id = (int) v.getTag(R.id.tag_id);
                        String name = (String) v.getTag(R.id.tag_name);
                        new AlertDialog.Builder(ManageCategoriesActivity.this)
                                .setTitle(String.format(getString(R.string.dialog_delete_title), name))
                                .setMessage(String.format(getString(R.string.dialog_delete_message), name))
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //do nothing
                                    }
                                })
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DbAccess.deleteCategory(getBaseContext(), id);
                                        updateList();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
                return rowView;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text = (TextView) view.findViewById(R.id.item_name);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_NAME));
                text.setText(name);
                ImageView iv = (ImageView) view.findViewById(R.id.item_icon);
                iv.setTag(R.id.tag_id, cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_ID))); //The tag is later used to delete a category.
                iv.setTag(R.id.tag_name, name);
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
                }
                updateList();
                break;
        }
    }

    private void updateList(){
        CursorAdapter adapter = (CursorAdapter) list.getAdapter();
        adapter.changeCursor(DbAccess.getCategories(getBaseContext()));
    }
}
