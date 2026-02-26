package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Locale;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.MenuCategoryAdapter.MenuItemViewHolder;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item;

public class MenuItemAdapter extends BaseAdapter implements Filterable {

    private final int layoutId;
    public ArrayList<EpicuriMenu.Item> filteredItems;
    private ArrayList<EpicuriMenu.Item> allItems;
    private ArrayList<String> selectedItemIds = new ArrayList<String>();
    private LayoutInflater inflater;
    private Filter f;
    private CharSequence latestConstraint;
    private boolean searchDetails;
    private boolean searchSKU;
    private boolean showNotAvailable;

    public MenuItemAdapter(Context context) {
        this(context, false);
    }

    public MenuItemAdapter(Context context, boolean withCheckBox) {
        super();
        inflater = LayoutInflater.from(context);
        if (withCheckBox) {
            layoutId = R.layout.row_menu_item_chooser;
        } else {
            layoutId = R.layout.row_menu_item;
        }
    }

    public void swapData(ArrayList<EpicuriMenu.Item> items, ArrayList<String> selectedItemIds) {
        this.allItems = items;
        this.selectedItemIds = selectedItemIds;

        filterItems();
        notifyDataSetChanged();
    }

    private void filterItems() {
        if (null != f && null != latestConstraint) {
            f.filter(latestConstraint);
        } else {
            filteredItems = allItems;
        }
    }

    @Override
    public int getCount() {
        if (null == filteredItems) return 0;
        return filteredItems.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public ArrayList<String> getCheckedItemIds() {
        return selectedItemIds;
    }

    public boolean isItemChecked(long id) {
        return selectedItemIds.contains(String.valueOf(id));
    }

    public void setItemChecked(String id, boolean checked) {
        if (selectedItemIds.contains(id)) {
            selectedItemIds.remove(id);
        } else {
            selectedItemIds.add(id);
        }

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
//		if(position >= filteredItems.size()) return -1;
        return filteredItems.get(position).getId().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuCategoryAdapter.MenuItemViewHolder vh;
        if (null == convertView) {
            convertView = inflater.inflate(layoutId, parent, false);
            vh = new MenuItemViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (MenuItemViewHolder) convertView.getTag();
        }
        EpicuriMenu.Item item = filteredItems.get(position);
        vh.showData(item);
        if (layoutId == R.layout.row_menu_item_chooser) {
            ((Checkable) convertView).setChecked(selectedItemIds.contains(filteredItems.get(position).getId()));
        }
        return convertView;
    }

    public void setSearchDetails(boolean searchDetails) {
        this.searchDetails = searchDetails;
    }

    public void setSearchSKU(boolean searchSKU) {
        this.searchSKU = searchSKU;
    }

    public void showNotAvailable(boolean isChecked) {
        this.showNotAvailable = isChecked;
    }

    @Override
    public Filter getFilter() {
        return f = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                latestConstraint = constraint;
                ArrayList<Item> results;

                if (null == constraint) {
                    results = new ArrayList<Item>(allItems);
                } else {
                    results = new ArrayList<EpicuriMenu.Item>();
                    String lowerCaseConstraint = constraint.toString().toLowerCase(Locale.UK);
                    if (allItems != null) {
                        for (Item i : allItems) {
                            if (showNotAvailable) {
                                if (i.isUnavailable()) results.add(i);
                                continue;
                            }
                            if (i.getName().toLowerCase(Locale.UK).contains(lowerCaseConstraint)
                                    || i.getShortCode().toLowerCase(Locale.UK).contains(lowerCaseConstraint)
                                    || (searchDetails && i.getDescription().toLowerCase(Locale.UK).contains(lowerCaseConstraint))
                                    || (searchSKU && i.getPlu() != null && i.getPlu().toLowerCase(Locale.UK).contains(lowerCaseConstraint))
                                    ) {
                                results.add(i);
                            }
                        }
                    }
                }

                FilterResults r = new FilterResults();
                r.count = results.size();
                r.values = results;
                return r;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredItems = (ArrayList<Item>) (results.values);
                notifyDataSetChanged();
            }
        };
    }
}