package org.secuso.privacyfriendlynotes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplication(), TextNoteActivity.class));
            }
        });

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
                text.setText(name);

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
                text.setText(name);

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
                        break;
                    case DbContract.NoteEntry.TYPE_SKETCH:
                        break;
                    case DbContract.NoteEntry.TYPE_CHECKLIST:
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView notesList = (ListView) findViewById(R.id.notes_list);
        CursorAdapter adapter = (CursorAdapter) notesList.getAdapter();
        adapter.changeCursor(DbAccess.getCursorAllNotes(getBaseContext()));
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_private) {

        } else if (id == R.id.nav_work) {

        } else if (id == R.id.nav_university) {

        } else if (id == R.id.nav_all) {

        }  else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
