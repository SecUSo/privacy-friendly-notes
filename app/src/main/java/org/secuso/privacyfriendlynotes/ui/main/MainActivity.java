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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;

import android.widget.ArrayAdapter;
import android.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONObject;
import org.secuso.privacyfriendlynotes.room.DbContract;
import org.secuso.privacyfriendlynotes.room.model.Category;
import org.secuso.privacyfriendlynotes.room.adapter.NoteAdapter;
import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.ui.AboutActivity;
import org.secuso.privacyfriendlynotes.ui.TutorialActivity;
import org.secuso.privacyfriendlynotes.ui.notes.AudioNoteActivity;
import org.secuso.privacyfriendlynotes.ui.notes.ChecklistNoteActivity;
import org.secuso.privacyfriendlynotes.ui.HelpActivity;
import org.secuso.privacyfriendlynotes.ui.manageCategories.ManageCategoriesActivity;
import org.secuso.privacyfriendlynotes.ui.RecycleActivity;
import org.secuso.privacyfriendlynotes.ui.SettingsActivity;
import org.secuso.privacyfriendlynotes.ui.notes.SketchActivity;
import org.secuso.privacyfriendlynotes.ui.notes.TextNoteActivity;
import org.secuso.privacyfriendlynotes.ui.util.CheckListItem;

import java.util.ArrayList;
import java.util.List;

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

        //Fill from Room database

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        mainActivityViewModel.getActiveNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                adapter.setNotes(notes);
            }
        });

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


        /**
         * Handels when a note is clicked.
         */
        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {

                switch (note.getType()) {
                    case DbContract.NoteEntry.TYPE_TEXT:
                        Intent i = new Intent(getApplication(), TextNoteActivity.class);
                        i.putExtra(TextNoteActivity.EXTRA_ID, note.get_id());
                        i.putExtra(TextNoteActivity.EXTRA_TITLE, note.getName());
                        i.putExtra(TextNoteActivity.EXTRA_CONTENT, note.getContent());
                        i.putExtra(TextNoteActivity.EXTRA_CATEGORY, note.getCategory());
                        i.putExtra(TextNoteActivity.EXTRA_ISTRASH, note.getIn_trash());

                        startActivity(i);
                        break;
                    case DbContract.NoteEntry.TYPE_AUDIO:
                        Intent i2 = new Intent(getApplication(), AudioNoteActivity.class);
                        i2.putExtra(AudioNoteActivity.EXTRA_ID, note.get_id());
                        i2.putExtra(AudioNoteActivity.EXTRA_TITLE, note.getName());
                        i2.putExtra(AudioNoteActivity.EXTRA_CONTENT, note.getContent());
                        i2.putExtra(AudioNoteActivity.EXTRA_CATEGORY, note.getCategory());
                        i2.putExtra(TextNoteActivity.EXTRA_ISTRASH, note.getIn_trash());

                        startActivity(i2);
                        break;
                    case DbContract.NoteEntry.TYPE_SKETCH:
                        Intent i3 = new Intent(getApplication(), SketchActivity.class);
                        i3.putExtra(SketchActivity.EXTRA_ID, note.get_id());
                        i3.putExtra(SketchActivity.EXTRA_TITLE, note.getName());
                        i3.putExtra(SketchActivity.EXTRA_CONTENT, note.getContent());
                        i3.putExtra(SketchActivity.EXTRA_CATEGORY, note.getCategory());
                        i3.putExtra(TextNoteActivity.EXTRA_ISTRASH, note.getIn_trash());

                        startActivity(i3);
                        break;
                    case DbContract.NoteEntry.TYPE_CHECKLIST:
                        Intent i4 = new Intent(getApplication(), ChecklistNoteActivity.class);
                        i4.putExtra(ChecklistNoteActivity.EXTRA_ID, note.get_id());
                        i4.putExtra(ChecklistNoteActivity.EXTRA_TITLE, note.getName());
                        i4.putExtra(ChecklistNoteActivity.EXTRA_CONTENT, note.getContent());
                        i4.putExtra(ChecklistNoteActivity.EXTRA_CATEGORY, note.getCategory());
                        i4.putExtra(TextNoteActivity.EXTRA_ISTRASH, note.getIn_trash());

                        startActivity(i4);
                        break;
                }
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
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
        switch (v.getId()) {
            case R.id.fab_text:
                startActivity(new Intent(getApplication(), TextNoteActivity.class));
                fabMenu.collapseImmediately();
                break;
            case R.id.fab_checklist:
                startActivity(new Intent(getApplication(), ChecklistNoteActivity.class));
                fabMenu.collapseImmediately();
                break;
            case R.id.fab_audio:
                startActivity(new Intent(getApplication(), AudioNoteActivity.class));
                fabMenu.collapseImmediately();
                break;
            case R.id.fab_sketch:
                startActivity(new Intent(getApplication(), SketchActivity.class));
                fabMenu.collapseImmediately();
                break;
        }
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
        if(!alphabeticalAsc){
            mainActivityViewModel.getNotesFilteredAlphabetical(filter).observe(this, new Observer<List<Note>>() {
                @Override
                public void onChanged(@Nullable List<Note> notes) {
                    // Filter checklist notes
                    List<Note> filteredNotes = new ArrayList<>();
                    for(Note note: notes){
                        Boolean add = false;
                        if(note.getType() == 3){
                            try {
                                JSONArray content = new JSONArray(note.getContent());
                                for (int i=0; i < content.length(); i++) {
                                    JSONObject o = content.getJSONObject(i);
                                    if (o.getString("name").contains(filter) || note.getName().contains(filter)){
                                        add = true;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else{
                            add = true;
                        }
                        if(add){
                            filteredNotes.add(note);
                        }
                    }
                    adapter.setNotes(filteredNotes);
                }
            });
            alphabeticalAsc = true;
        } else {
            mainActivityViewModel.getActiveNotesFiltered(filter).observe(this, new Observer<List<Note>>() {
                @Override
                public void onChanged(@Nullable List<Note> notes) {
                    // Filter checklist notes
                    List<Note> filteredNotes = new ArrayList<>();
                    for(Note note: notes){
                        Boolean add = false;
                        if(note.getType() == 3){
                            try {
                                JSONArray content = new JSONArray(note.getContent());
                                for (int i=0; i < content.length(); i++) {
                                    JSONObject o = content.getJSONObject(i);
                                    if (o.getString("name").contains(filter) || note.getName().contains(filter)){
                                        add = true;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else{
                            add = true;
                        }
                        if(add){
                            filteredNotes.add(note);
                        }
                    }
                    adapter.setNotes(filteredNotes);
                }
            });
            alphabeticalAsc = false;
        }

    }

    /**
     * Filters active notes.
     * @param filter
     */
    private void applyFilter(String filter){
        mainActivityViewModel.getActiveNotesFiltered(filter).observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                // Filter checklist notes
                List<Note> filteredNotes = new ArrayList<>();
                for(Note note: notes){
                    Boolean add = false;
                    if(note.getType() == 3){
                        try {
                            JSONArray content = new JSONArray(note.getContent());
                            for (int i=0; i < content.length(); i++) {
                                JSONObject o = content.getJSONObject(i);
                                if (o.getString("name").contains(filter) || note.getName().contains(filter)){
                                    add = true;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else{
                        add = true;
                    }
                    if(add){
                        filteredNotes.add(note);
                    }
                }
                adapter.setNotes(filteredNotes);
            }
        });
    }

    /**
     * Filters active notes from category.
     * @param filter
     * @param category
     */
    private void applyFilterCategory(String filter, Integer category){
        mainActivityViewModel.getActiveNotesFilteredFromCategory(filter,category).observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                // Filter checklist notes
                List<Note> filteredNotes = new ArrayList<>();
                for(Note note: notes){
                    Boolean add = false;
                    if(note.getType() == 3){
                        try {
                            JSONArray content = new JSONArray(note.getContent());
                            for (int i=0; i < content.length(); i++) {
                                JSONObject o = content.getJSONObject(i);
                                if (o.getString("name").contains(filter) || note.getName().contains(filter)){
                                    add = true;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else{
                        add = true;
                    }
                    if(add){
                        filteredNotes.add(note);
                    }
                }
                adapter.setNotes(filteredNotes);
            }
        });
    }

}
