package org.secuso.privacyfriendlynotes.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_table")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private int category;
    private int type;

    public Note(String title, String description, int type, int category) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCategory() {
        return category;
    }

    public int getType() {
        return type;
    }



}
