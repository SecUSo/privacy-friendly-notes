package org.secuso.privacyfriendlynotes.room.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.secuso.privacyfriendlynotes.R;
import org.secuso.privacyfriendlynotes.room.DbContract;
import org.secuso.privacyfriendlynotes.room.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private List<Note> notes = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.textViewTitle.setText(currentNote.getName());
        holder.textViewDescription.setText(currentNote.getContent());
        holder.textViewDescription.setMaxLines(3);


        switch (currentNote.getType()) {
            case DbContract.NoteEntry.TYPE_TEXT:
                holder.imageViewcategory.setImageResource(R.drawable.ic_short_text_black_24dp);
                break;
            case DbContract.NoteEntry.TYPE_AUDIO:
                holder.imageViewcategory.setImageResource(R.drawable.ic_mic_black_24dp);
                break;
            case DbContract.NoteEntry.TYPE_SKETCH:
                holder.imageViewcategory.setImageResource(R.drawable.ic_photo_black_24dp);
                break;
            case DbContract.NoteEntry.TYPE_CHECKLIST:
                holder.imageViewcategory.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp);
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
