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
package org.secuso.privacyfriendlynotes.room.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.DbContract;
import org.secuso.privacyfriendlynotes.room.model.Note;
import org.secuso.privacyfriendlynotes.ui.util.CheckListItem;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that provides a binding for notes
 * @see org.secuso.privacyfriendlynotes.ui.main.MainActivity
 * @see org.secuso.privacyfriendlynotes.ui.RecycleActivity
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private List<Note> notes = new ArrayList<>();
    private List<Note> notesFilteredList = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteHolder(itemView);
    }


    /**
     * Defines how notes are presented in the RecyclerView.
     * @see org.secuso.privacyfriendlynotes.ui.main.MainActivity
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.textViewTitle.setText(currentNote.getName());
        holder.textViewDescription.setText("");

        switch (currentNote.getType()) {
            case DbContract.NoteEntry.TYPE_TEXT:
                holder.imageViewcategory.setImageResource(R.drawable.ic_short_text_black_24dp);
                holder.textViewDescription.setText(Html.fromHtml(currentNote.getContent()));
                holder.textViewDescription.setMaxLines(3);
                break;
            case DbContract.NoteEntry.TYPE_AUDIO:
                holder.imageViewcategory.setImageResource(R.drawable.ic_mic_black_24dp);
                break;
            case DbContract.NoteEntry.TYPE_SKETCH:
                holder.imageViewcategory.setImageResource(R.drawable.ic_photo_black_24dp);
                break;
            case DbContract.NoteEntry.TYPE_CHECKLIST:
                holder.imageViewcategory.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
                String preview = "";
                try {
                    JSONArray content = new JSONArray(currentNote.getContent());
                    for (int i=0; i < content.length(); i++) {
                        JSONObject o = content.getJSONObject(i);
                        if(o.getBoolean("checked")){
                            preview = preview + "(\u2713)";
                        } else {
                            preview = preview + "(X)";
                        }
                        preview = preview + "     " + o.getString("name");
                        if(i != content.length()-1){
                            preview = preview + "\n";
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.textViewDescription.setText(preview);
                holder.textViewDescription.setMaxLines(3);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDescription;
        private ImageView imageViewcategory;


        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            imageViewcategory = itemView.findViewById(R.id.imageView_category);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(notes.get(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


}
