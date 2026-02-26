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


public class GroupsLandscapeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface IGroupsClickListener {
        void onItemSelected(EpicuriMenu.Group group);

        void onClearGroups(List<EpicuriMenu.Group> groups);
    }

    private List<EpicuriMenu.Group> groups;
    private LayoutInflater inflater;
    private IGroupsClickListener clickListener;
    private int selectedPosition = -1;

    public GroupsLandscapeAdapter(Context context, List<EpicuriMenu.Group> groups) {
        inflater = LayoutInflater.from(context);
        changeData(groups);
    }

    public void changeData(List<EpicuriMenu.Group> groups) {
        selectedPosition = -1;
        this.groups = groups == null ? new ArrayList<EpicuriMenu.Group>(0) : groups;
        notifyDataSetChanged();
    }

    public void setClickListener(IGroupsClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupViewHolder(inflater.inflate(R.layout.qo_clasification_item_red, parent,
                false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((GroupViewHolder) holder).render(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    private class GroupViewHolder extends RecyclerView.ViewHolder {

        GroupViewHolder(View view) {
            super(view);
        }

        public void render(final EpicuriMenu.Group group) {
            TextView name = (TextView) itemView.findViewById(R.id.name);
            View left = itemView.findViewById(R.id.leftView);
            if (getAdapterPosition() == 0){
                left.setVisibility(View.GONE);
            } else {
                left.setVisibility(View.VISIBLE);
            }
            name.setText(group.getName() == null ? "" : group.getName().toUpperCase());
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
                        clickListener.onClearGroups(groups);
                    } else {
                        clickListener.onItemSelected(group);
                    }

                    notifyDataSetChanged();
                }
            });
        }
    }
}
