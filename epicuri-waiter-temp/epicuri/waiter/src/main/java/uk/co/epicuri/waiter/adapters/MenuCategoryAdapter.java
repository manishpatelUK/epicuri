package uk.co.epicuri.waiter.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Category;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.MenuAlertFragment;

public class MenuCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        Filterable {

    private EpicuriMenu menu;
    private String categoryName;

    @Override public Filter getFilter() {
        return null;
    }

    public interface IMenuCategoryItemClicked {
        void onItemClicked(Item item);
    }

    private ArrayList<TitleOrItem> items;
    private ArrayList<TitleOrItem> originalItems = new ArrayList<>();
    private ArrayList<TitleOrItem> allItems = new ArrayList<>();
    LayoutInflater inflater;
    static IMenuCategoryItemClicked listener;
    boolean flag;
    static boolean expandedNames = false;

    public MenuCategoryAdapter(Context context, EpicuriMenu menu, String categoryName,
            IMenuCategoryItemClicked listener) {
        this.listener = listener;
        this.categoryName = categoryName;
        this.menu = menu;
        inflater = LayoutInflater.from(context);
        sortAndInitMenuItems();
    }

    public void sortAndInitMenuItems(){
        EpicuriMenu.Category category = null;
        if (null != menu && menu.getCategories().size() > 0) {
            allItems.clear();

            for (Category c : menu.getCategories()) {
                if (c.getName().equals(categoryName)) {
                    category = c;
                }

                allItems.addAll(populateItems(c));
            }
        }

        items = populateItems(category);
        originalItems.clear();
        originalItems.addAll(items);
    }
    public MenuCategoryAdapter(Context context, Category category) {
        inflater = LayoutInflater.from(context);
        items = populateItems(category);
        originalItems.clear();
        originalItems.addAll(items);
    }

    private ArrayList<TitleOrItem> populateItems(Category category) {
        if (null != category) {
            ArrayList<TitleOrItem> items = new ArrayList<TitleOrItem>();
            for (EpicuriMenu.Group g : category.getGroups()) {
                items.add(new TitleOrItem(g));
                for (Item i : g.getItems()) {
                    items.add(new TitleOrItem(i));
                }
            }
            return items;
        } else {
            return new ArrayList<TitleOrItem>();
        }
    }

    public void changeExpanded(){
        expandedNames = !expandedNames;
        sortAndInitMenuItems();
    }

    public int getSpanCount(int position) {
        if (items == null || items.size() == 0) return 2;

        return items.get(position).type == TitleOrItem.ITEM ? 1 : 2;
    }

    public static class TitleOrItem{
        static final int TITLE = 0;
        static final int ITEM = 1;
        public final int type;
        public Item item;
        public EpicuriMenu.Group group;

        public TitleOrItem(EpicuriMenu.Group group) {
            type = TITLE;
            this.group = group;
        }

        public TitleOrItem(Item item) {
            type = ITEM;
            this.item = item;
        }

        String getId() {
            if (type == TITLE) {
                return group.getId();
            } else {
                return item.getId();
            }
        }
    }

    @Override public int getItemCount() {
        return items.size();
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == TitleOrItem.TITLE) {
            GroupViewHolder vh;
            view = inflater.inflate(R.layout.row_menugroup_title, parent, false);
            vh = new GroupViewHolder(view);

            return vh;
        }

        MenuItemViewHolder vh;
        view = inflater.inflate(R.layout.row_menu_item_divided, parent, false);
        vh = new MenuItemViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TitleOrItem item = items.get(position);

        if (holder.getItemViewType() == TitleOrItem.TITLE) {
            ((GroupViewHolder) holder).showData(item.group);
            return;
        }

        ((MenuItemViewHolder) holder).showData(item.item);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.title) TextView text;

        public GroupViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        public void showData(EpicuriMenu.Group group) {
            text.setText(group.getName());
        }
    }

    public boolean orderItem(String query) {
        if (query.isEmpty()) return false;

        for (TitleOrItem titleOrItem : allItems) {
            if (titleOrItem.type != TitleOrItem.ITEM) continue;

            if (titleOrItem.item.getShortCode().equals(query) || titleOrItem.item.getName()
                    .equals(query)) {
                if (listener != null && titleOrItem.item != null) {
                    listener.onItemClicked(titleOrItem.item);
                }

                return true;
            }
        }

        return false;
    }

    public void filterItems(String query) {
        items.clear();

        if (query == null || query.isEmpty()) {
            items.addAll(originalItems);
            notifyDataSetChanged();
            return;
        }

        for (TitleOrItem titleOrItem : allItems) {
            if (titleOrItem.type == TitleOrItem.TITLE) {
                items.add(titleOrItem);
                continue;
            }

            if (titleOrItem.item.getShortCode().toLowerCase().contains(query.toLowerCase())
                    || titleOrItem.item.getName().toLowerCase().contains(query.toLowerCase())) {
                items.add(titleOrItem);
            }
        }

        List<TitleOrItem> filtered = new ArrayList<>(1);

        for (int i = 0; i < items.size() - 1; ++i) {
            TitleOrItem item = items.get(i);

            if (item.type == TitleOrItem.ITEM) {
                filtered.add(item);
                continue;
            }

            if (item.type == TitleOrItem.TITLE && items.get(i + 1).type == TitleOrItem.ITEM)
                filtered.add(item);
        }

        if (items.size() > 0 && items.get(items.size()-1).type == TitleOrItem.ITEM) {
            filtered.add(items.get(items.size()-1));
        }

        items.clear();
        items.addAll(filtered);

        notifyDataSetChanged();
    }


    public static class MenuItemViewHolder extends RecyclerView.ViewHolder implements
            View.OnLongClickListener {
        @InjectView(R.id.title) TextView title;
        @Optional @InjectView(R.id.detail) TextView detail;
        @InjectView(R.id.price) TextView price;
        @Optional @InjectView(R.id.divider) View divider;
        @Optional @InjectView(R.id.parent) View parent;
        Item item;

        public MenuItemViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        public void showData(Item item) {
            this.item = item;

            int colour = 0xff000000;
            if (item.isUnavailable()) {
                colour = 0xff888888;
            }
            for (TextView tv : new TextView[]{title, detail, price}) {
                if (tv != null) tv.setTextColor(colour);
            }

            //if divider is null, we're probably in menu management in which case always show the full menu item name, otherwise show short code (if available)
            if(divider == null || expandedNames) {
                title.setText(item.getName());
            } else {
                title.setText(item.getShortCode() != null && !item.getShortCode().isEmpty() ? item.getShortCode()
                        : item.getName());
            }
            price.setText(String.format("%s", LocalSettings.formatMoneyAmount(item.getPrice(), true)));

            if(detail != null) {
                detail.setVisibility(View.GONE);
                detail.setText(item.getDescription());
            }

            if (divider != null) divider.setVisibility(View.VISIBLE);
            if (parent != null) parent.setOnLongClickListener(this);
        }

        @Optional @OnClick(R.id.parent) public void onItemClick() {
            if (listener != null && item != null) listener.onItemClicked(item);
        }


        @Override public boolean onLongClick(View view) {
            MenuAlertFragment fragment = MenuAlertFragment.newInstance(item);
            fragment.show(((Activity) view.getContext()).getFragmentManager(), "dialog");

            return true;
        }
    }
}
