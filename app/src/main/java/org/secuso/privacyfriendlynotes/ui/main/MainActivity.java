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
package org.secuso.privacyfriendlynotes.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.arch.core.util.Function;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.secuso.privacyfriendlynotes.room.DbContract;
import org.secuso.privacyfriendlynotes.room.model.Category;
import org.secuso.privacyfriendlynotes.ui.adapter.NoteAdapter;
import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.ui.AboutActivity;
import org.secuso.privacyfriendlynotes.ui.TutorialActivity;
import org.secuso.privacyfriendlynotes.ui.notes.AudioNoteActivity;
import org.secuso.privacyfriendlynotes.ui.notes.BaseNoteActivity;
import org.secuso.privacyfriendlynotes.ui.notes.ChecklistNoteActivity;
import org.secuso.privacyfriendlynotes.ui.HelpActivity;
import org.secuso.privacyfriendlynotes.ui.manageCategories.ManageCategoriesActivity;
import org.secuso.privacyfriendlynotes.ui.RecycleActivity;
import org.secuso.privacyfriendlynotes.ui.SettingsActivity;
import org.secuso.privacyfriendlynotes.ui.notes.SketchActivity;
import org.secuso.privacyfriendlynotes.ui.notes.TextNoteActivity;

import java.util.List;

import kotlin.Unit;

/**
 * The MainActivity includes the functionality of the primary screen.
 * It provides the possibility to access existing notes and add new ones.
 * Data is provided by the MainActivityViewModel.
 * @see MainActivityViewModel
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final int CAT_ALL = -1;
    private static final String TAG_WELCOME_DIALOG = "welcome_dialog";
    FloatingActionsMenu fabMenu;
    Boolean alphabeticalAsc = false;
    Boolean categoryActivated = false;

    private int selectedCategory = CAT_ALL; //ID of the currently selected category. Defaults to "all"

    //New Room variables
    private MainActivityViewModel mainActivityViewModel;
    NoteAdapter adapter;
    SearchView searchView;

    // A launcher to receive and react to a NoteActivity returning a category
    // The category is used to set the selectecCategory
    ActivityResultLauncher<Intent> setCategoryResultAfter =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            selectedCategory = result.getData().getIntExtra(BaseNoteActivity.EXTRA_CATEGORY, CAT_ALL);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set the OnClickListeners
        findViewById(R.id.fab_text).setOnClickListener(this);
        findViewById(R.id.fab_checklist).setOnClickListener(this);
        findViewById(R.id.fab_audio).setOnClickListener(this);
        findViewById(R.id.fab_sketch).setOnClickListener(this);

        fabMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        searchView = findViewById(R.id.searchViewFilter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        //Fill from Room database
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new NoteAdapter(mainActivityViewModel);
        recyclerView.setAdapter(adapter);

        mainActivityViewModel.getActiveNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                adapter.setNotes(notes);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Note note = adapter.getNoteAt(viewHolder.getAdapterPosition());
                if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("settings_dialog_on_trashing", false)) {
                    new MaterialAlertDialogBuilder(new ContextThemeWrapper(MainActivity.this, R.style.AppTheme_PopupOverlay_DialogAlert))
                        .setTitle(String.format(getString(R.string.dialog_delete_title),note.getName()))
                        .setMessage(String.format(getString(R.string.dialog_delete_message), note.getName()))
                        .setPositiveButton(R.string.dialog_option_delete, (dialogInterface,i) -> {
                            trashNote(note);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                } else {
                    trashNote(note);
                }
                mainActivityViewModel.update(note);

            }
        }).attachToRecyclerView(recyclerView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if(!categoryActivated){
                    applyFilter(newText);
                } else {
                    applyFilterCategory(newText,selectedCategory);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }
        });



        /*
         * Handels when a note is clicked.
         */
        adapter.setOnItemClickListener(note -> {
            Function<Class<? extends BaseNoteActivity>, Void> launchActivity = activity -> {
                Intent i = new Intent(getApplication(), activity);
                i.putExtra(BaseNoteActivity.EXTRA_ID, note.get_id());
                i.putExtra(BaseNoteActivity.EXTRA_TITLE, note.getName());
                i.putExtra(BaseNoteActivity.EXTRA_CONTENT, note.getContent());
                i.putExtra(BaseNoteActivity.EXTRA_CATEGORY, note.getCategory());
                i.putExtra(BaseNoteActivity.EXTRA_ISTRASH, note.getIn_trash());
                startActivity(i);
                return null;
            };
            switch (note.getType()) {
                case DbContract.NoteEntry.TYPE_TEXT -> launchActivity.apply(TextNoteActivity.class);
                case DbContract.NoteEntry.TYPE_AUDIO -> launchActivity.apply(AudioNoteActivity.class);
                case DbContract.NoteEntry.TYPE_SKETCH -> launchActivity.apply(SketchActivity.class);
                case DbContract.NoteEntry.TYPE_CHECKLIST -> launchActivity.apply(ChecklistNoteActivity.class);
            }
            return Unit.INSTANCE;
        });

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);

        String theme = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_day_night_theme", "-1");
        Log.d("Theme", theme);
        AppCompatDelegate.setDefaultNightMode(Integer.parseInt(theme));
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildDrawerMenu();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_alphabetical) {
            //switch to an alphabetically ascending or descending order
            updateListAlphabetical(searchView.getQuery().toString());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles clicks on navigation items
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        item.setCheckable(true);
        item.setChecked(true);
        int id = item.getItemId();
        if (id == R.id.nav_trash) {
            startActivity(new Intent(getApplication(), RecycleActivity.class));
        } else if (id == R.id.nav_all) {
            mainActivityViewModel.getActiveNotes().observe(this, new Observer<List<Note>>() {
                @Override
                public void onChanged(@Nullable List<Note> notes) {
                    adapter.setNotes(notes);
                }
            });
            categoryActivated = false;
        } else if (id == R.id.nav_manage_categories) {
            startActivity(new Intent(getApplication(), ManageCategoriesActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(getApplication(), SettingsActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(getApplication(), HelpActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(getApplication(), AboutActivity.class));
        } else if (id == R.id.nav_tutorial) {
            startActivity(new Intent(getApplication(), TutorialActivity.class));
        }else {
            selectedCategory = id;
            categoryActivated = true;
            applyFilterCategory(searchView.getQuery().toString(),selectedCategory);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Handles when notes are added.
     * @param v
     */
    @Override
    public void onClick(View v) {
        Function<Class<? extends  BaseNoteActivity>, Intent> intent = activity -> {
            Intent i = new Intent(getApplication(), activity);
            i.putExtra(BaseNoteActivity.EXTRA_CATEGORY, selectedCategory);
            return i;
        };
        Intent i = null;
        switch (v.getId()) {
            case R.id.fab_text:
                i = intent.apply(TextNoteActivity.class);
                break;
            case R.id.fab_checklist:
                i = intent.apply(ChecklistNoteActivity.class);
                break;
            case R.id.fab_audio:
                i = intent.apply(AudioNoteActivity.class);
                break;
            case R.id.fab_sketch:
                i = intent.apply(SketchActivity.class);
                break;
        }
        setCategoryResultAfter.launch(i);
        fabMenu.collapseImmediately();
    }

    private void buildDrawerMenu() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navMenu = navigationView.getMenu();
        //reset the menu
        navMenu.clear();
        //Inflate the standard stuff
        MenuInflater menuInflater = new MenuInflater(getApplicationContext());
        menuInflater.inflate(R.menu.activity_main_drawer, navMenu);

        //Get the rest from the database

        MainActivityViewModel mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        mainActivityViewModel.getAllCategoriesLive().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categories) {
                navMenu.add(R.id.drawer_group2, 0, Menu.NONE, getString(R.string.default_category)).setIcon(R.drawable.ic_label_black_24dp);

                for(Category currentCat : categories){
                    navMenu.add(R.id.drawer_group2, currentCat.get_id(), Menu.NONE, currentCat.getName()).setIcon(R.drawable.ic_label_black_24dp);
                }
            }
        });

    }


    /**
     * Sorts filtered notes alphabetical in descending or ascending order.
     * @param filter
     */
    private void updateListAlphabetical(String filter) {
        LiveData<List<Note>> data = alphabeticalAsc ?
                mainActivityViewModel.getActiveNotesFiltered(filter)
                : mainActivityViewModel.getActiveNotesFilteredAlphabetical(filter);

        data.observe(this, notes -> {
            adapter.setNotes(notes);
            alphabeticalAsc = !alphabeticalAsc;
        });
    }

    /**
     * Filters active notes.
     * @param filter
     */
    private void applyFilter(String filter){
        mainActivityViewModel.getActiveNotesFiltered(filter).observe(this, notes -> {
            adapter.setNotes(notes);
        });
    }

    /**
     * Filters active notes from category.
     * @param filter
     * @param category
     */
    private void applyFilterCategory(String filter, Integer category){
        mainActivityViewModel.getActiveNotesFilteredFromCategory(filter, category).observe(this, notes -> {
            adapter.setNotes(notes);
        });
    }

    private void trashNote(Note note) {
        note.setIn_trash(1);
        Toast.makeText(MainActivity.this,getString(R.string.toast_deleted),Toast.LENGTH_SHORT).show();
        mainActivityViewModel.update(note);
    }

}
