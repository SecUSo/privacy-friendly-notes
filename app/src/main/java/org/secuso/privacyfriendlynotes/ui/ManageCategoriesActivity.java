package org.secuso.privacyfriendlynotes.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.Category;
import org.secuso.privacyfriendlynotes.room.CategoryAdapter;
import org.secuso.privacyfriendlynotes.room.CategoryViewModel;

import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity implements View.OnClickListener {

    ListView list;
    RecyclerView recycler_list;
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
        categoryViewModel.getAllCategoriesLive().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categoryNames) {
                adapter.setCategories(categoryNames);
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
                    categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
                    categoryViewModel.insert(category);

                }
                break;
        }
    }


    private void deleteCategory(Category cat){
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.delete(cat);
    }
}
