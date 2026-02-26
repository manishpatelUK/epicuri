package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMenu;


public class CategoriesLandscapeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface ICategoriesClickListener {
        void onItemSelected(EpicuriMenu.Category category);

        void onClearCategories(List<EpicuriMenu.Category> categories);
    }

    private List<EpicuriMenu.Category> categories;
    private LayoutInflater inflater;
    private ICategoriesClickListener clickListener;
    private int selectedPosition = -1;

    public CategoriesLandscapeAdapter(Context context, List<EpicuriMenu.Category> categories) {
        inflater = LayoutInflater.from(context);
        changeData(categories);
    }

    public void changeData(List<EpicuriMenu.Category> categories) {
        selectedPosition = -1;
        this.categories = categories == null ? new ArrayList<EpicuriMenu.Category>(0) : categories;
        notifyDataSetChanged();
    }

    public void deselect() {
        selectedPosition = -1;
        if (clickListener != null)
            clickListener.onClearCategories(categories);
        notifyDataSetChanged();
    }

    public void setClickListener(ICategoriesClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(inflater.inflate(R.layout.qo_clasification_item_red, parent,
                false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CategoryViewHolder) holder).render(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private class CategoryViewHolder extends RecyclerView.ViewHolder {
        CategoryViewHolder(View view) {
            super(view);
        }

        public void render(final EpicuriMenu.Category category) {
            TextView name = itemView.findViewById(R.id.name);
            View left = itemView.findViewById(R.id.leftView);
            if (getAdapterPosition() == 0){
                left.setVisibility(View.GONE);
            }else {
                left.setVisibility(View.VISIBLE);
            }
            name.setText(category.getName() == null ? "" : category.getName().toUpperCase());
            name.setTextColor(selectedPosition == -1 || (selectedPosition == getAdapterPosition()
            ) ? ContextCompat.getColor(name.getContext(), R.color.white) : ContextCompat.getColor
                    (name.getContext(), R.color.lightgray));
            if (getAdapterPosition() == selectedPosition)
                name.setBackgroundResource(R.drawable.tab_selected_holo);
            else name.setBackground(null);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener == null) return;

                    int previous = selectedPosition;
                    selectedPosition = getAdapterPosition();

                    if (previous == selectedPosition) {
                        selectedPosition = -1;
                        clickListener.onClearCategories(categories);
                    } else {
                        clickListener.onItemSelected(category);
                    }

                    notifyDataSetChanged();
                }
            });
        }
    }
}
