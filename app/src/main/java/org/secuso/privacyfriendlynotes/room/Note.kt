package org.secuso.privacyfriendlynotes.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_table")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;
    private int category;
    private int type;

    public Note(String title, String content, int type, int category) {
        this.title = title;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public int getCategory() {
        return category;
    }

    public int getType() {
        return type;
    }



}
