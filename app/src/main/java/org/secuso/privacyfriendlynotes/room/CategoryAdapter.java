package org.secuso.privacyfriendlynotes.room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.secuso.privacyfriendlynotes.R;

import java.util.ArrayList;
import java.util.List;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder>{

    private List<Category> categories = new ArrayList<>();
    private CategoryAdapter.OnItemClickListener listener;

    @NonNull
    @Override
    public CategoryAdapter.CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryAdapter.CategoryHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
        Category currentCategory = categories.get(position);
        holder.textViewCategoryName.setText(currentCategory.getName());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }


    public Category getCategoryAt(int position){
        return categories.get(position);
    }


    class CategoryHolder extends RecyclerView.ViewHolder {
        private TextView textViewCategoryName;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.item_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(categories.get(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    public void setOnItemClickListener(CategoryAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
