package org.secuso.privacyfriendlynotes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final int CAT_ALL = -1;
    FloatingActionsMenu fabMenu;

    private int selectedCategory = CAT_ALL; //ID of the currently selected category. Defaults to "all"

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Fill the list from database
        ListView notesList = (ListView) findViewById(R.id.notes_list);
        notesList.setAdapter(new CursorAdapter(getApplicationContext(), DbAccess.getCursorAllNotes(getBaseContext()), CursorAdapter.FLAG_AUTO_REQUERY) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View rowView = inflater.inflate(R.layout.item_note, null);

                TextView text = (TextView) rowView.findViewById(R.id.item_name);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
                if (name.length() >= 30) {
                    text.setText(name.substring(0,27) + "...");
                } else {
                    text.setText(name);
                }

                ImageView iv = (ImageView) rowView.findViewById(R.id.item_icon);
                switch (cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
                    case DbContract.NoteEntry.TYPE_SKETCH:
                        iv.setImageResource(R.drawable.ic_photo_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_AUDIO:
                        iv.setImageResource(R.drawable.ic_mic_black_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_TEXT:
                        iv.setImageResource(R.drawable.ic_short_text_black_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_CHECKLIST:
                        iv.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                        break;
                    default:
                }
                return rowView;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text = (TextView) view.findViewById(R.id.item_name);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_NAME));
                if (name.length() >= 30) {
                    text.setText(name.substring(0,27) + "...");
                } else {
                    text.setText(name);
                }

                ImageView iv = (ImageView) view.findViewById(R.id.item_icon);
                switch (cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
                    case DbContract.NoteEntry.TYPE_SKETCH:
                        iv.setImageResource(R.drawable.ic_photo_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_AUDIO:
                        iv.setImageResource(R.drawable.ic_mic_black_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_TEXT:
                        iv.setImageResource(R.drawable.ic_short_text_black_24dp);
                        break;
                    case DbContract.NoteEntry.TYPE_CHECKLIST:
                        iv.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                        break;
                    default:
                }
            }
        });
        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get details about the clicked note
                CursorAdapter ca = (CursorAdapter) parent.getAdapter();
                Cursor c = ca.getCursor();
                c.moveToPosition(position);
                //start the appropriate activity
                switch (c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_TYPE))) {
                    case DbContract.NoteEntry.TYPE_TEXT:
                        Intent i = new Intent(getApplication(), TextNoteActivity.class);
                        i.putExtra(TextNoteActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
                        startActivity(i);
                        break;
                    case DbContract.NoteEntry.TYPE_AUDIO:
                        //TODO click on audio note
                        break;
                    case DbContract.NoteEntry.TYPE_SKETCH:
                        //TODO click on sketch note
                        break;
                    case DbContract.NoteEntry.TYPE_CHECKLIST:
                        Intent i4 = new Intent(getApplication(), ChecklistNoteActivity.class);
                        i4.putExtra(ChecklistNoteActivity.EXTRA_ID, c.getInt(c.getColumnIndexOrThrow(DbContract.NoteEntry.COLUMN_ID)));
                        startActivity(i4);
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
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
            //switch to an alphabetically sorted cursor.
            updateListAlphabetical();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
            selectedCategory = CAT_ALL;
            updateList();
        } else if (id == R.id.nav_manage_categories) {
            startActivity(new Intent(getApplication(), ManageCategoriesActivity.class));
        } else {
            selectedCategory = id;
            updateList();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
                //TODO fab audio
                fabMenu.collapseImmediately();
                break;
            case R.id.fab_sketch:
                //TODO fab sketch
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
        Cursor c = DbAccess.getCategories(getBaseContext());
        while (c.moveToNext()){
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_NAME));
            int id = c.getInt(c.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_ID));
            navMenu.add(R.id.drawer_group2, id, Menu.NONE, name).setIcon(R.drawable.ic_label_black_24dp);
        }
        c.close();
    }

    private void updateList() {
        ListView notesList = (ListView) findViewById(R.id.notes_list);
        CursorAdapter adapter = (CursorAdapter) notesList.getAdapter();
        if (selectedCategory == -1) { //show all
            String selection = DbContract.NoteEntry.COLUMN_TRASH + " = ?";
            String[] selectionArgs = { "0" };
            adapter.changeCursor(DbAccess.getCursorAllNotes(getBaseContext(), selection, selectionArgs));
        } else {
            String selection = DbContract.NoteEntry.COLUMN_CATEGORY + " = ? AND " + DbContract.NoteEntry.COLUMN_TRASH + " = ?";
            String[] selectionArgs = { String.valueOf(selectedCategory), "0" };
            adapter.changeCursor(DbAccess.getCursorAllNotes(getBaseContext(), selection, selectionArgs));
        }
    }

    private void updateListAlphabetical() {
        ListView notesList = (ListView) findViewById(R.id.notes_list);
        CursorAdapter adapter = (CursorAdapter) notesList.getAdapter();
        if (selectedCategory == -1) { //show all
            String selection = DbContract.NoteEntry.COLUMN_TRASH + " = ?";
            String[] selectionArgs = { "0" };
            adapter.changeCursor(DbAccess.getCursorAllNotesAlphabetical(getBaseContext(), selection, selectionArgs));
        } else {
            String selection = DbContract.NoteEntry.COLUMN_CATEGORY + " = ? AND " + DbContract.NoteEntry.COLUMN_TRASH + " = ?";
            String[] selectionArgs = { String.valueOf(selectedCategory), "0" };
            adapter.changeCursor(DbAccess.getCursorAllNotesAlphabetical(getBaseContext(), selection, selectionArgs));
        }
    }
}
