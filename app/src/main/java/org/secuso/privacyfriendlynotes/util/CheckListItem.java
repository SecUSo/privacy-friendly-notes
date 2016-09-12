package org.secuso.privacyfriendlynotes.util;

/**
 * Created by Robin on 12.09.2016.
 */
public class CheckListItem {
    private boolean isChecked = false;
    private String name = "";

    public CheckListItem(boolean isChecked, String name) {
        this.isChecked = isChecked;
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
