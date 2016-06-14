package org.secuso.privacyfriendlynotes;

import android.provider.BaseColumns;

/**
 * Created by Robin on 11.06.2016.
 */
public class NotesContract {
    public NotesContract(){}

    public static abstract class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_CONTENT = "content";
    }
}
