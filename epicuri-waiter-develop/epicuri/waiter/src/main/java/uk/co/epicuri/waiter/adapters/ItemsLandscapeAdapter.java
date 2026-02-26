package uk.co.epicuri.waiter.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.OnEpicuriMenuItemsSelectedListener;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.MenuAlertFragment;


public class ItemsLandscapeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<EpicuriMenu.Item> originalItems;
    private List<EpicuriMenu.Item> filteredItems;
    private LayoutInflater inflater;
    private OnEpicuriMenuItemsSelectedListener listener;
    private long lastClickTime = 0;
    private boolean expandedName = false;
    private boolean sortAlphabetically = false;
    private static Comparator<? super EpicuriMenu.Item> itemComparator = getItemComparator();
    private float menuItemFontSize = 1f;

    private static Comparator<? super EpicuriMenu.Item> getItemComparator() {
        return new Comparator<EpicuriMenu.Item>() {
            @Override
            public int compare(EpicuriMenu.Item o1, EpicuriMenu.Item o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
    }

    public ItemsLandscapeAdapter(Context context, List<EpicuriMenu.Item> items) {
        inflater = LayoutInflater.from(context);
        try {
            LocalSettings localSettings = LocalSettings.getInstance(context);
            sortAlphabetically = localSettings.isOrderItemsAlphabeticallyInQO();
        } catch (Exception ex){}
        changeData(items);
        menuItemFontSize = determineFontSize(context);
    }

    public void changeData(List<EpicuriMenu.Item> items) {
        this.originalItems = items == null ? new ArrayList<EpicuriMenu.Item>(0) : distinct(items);
        onChangeData();
    }

    private void onChangeData() {
        if(sortAlphabetically) {
            this.filteredItems = new ArrayList<>(originalItems);
            sortAlphabetically(this.filteredItems);
        } else {
            this.filteredItems = new ArrayList<>(originalItems);
        }
        notifyDataSetChanged();
    }

    public void setFilter(String filter) {
        if(originalItems == null) {
            return;
        }

        filteredItems.clear();
        filter = filter.toLowerCase();

        for (EpicuriMenu.Item item : originalItems) {
            if (item.getName().toLowerCase().contains(filter) || item.getShortCode().toLowerCase().contains(filter)) {
                filteredItems.add(item);
            }
        }

        notifyDataSetChanged();
    }

    public void switchSorting() {
        sortAlphabetically = !sortAlphabetically;
        onChangeData();
    }

    public boolean isSortAlphabetically() {
        return sortAlphabetically;
    }

    private void sortAlphabetically(List<EpicuriMenu.Item> items) {
        Collections.sort(items, itemComparator);
    }

    private List<EpicuriMenu.Item> distinct(List<EpicuriMenu.Item> items) {
        Set<String> ids = new HashSet<>();
        List<EpicuriMenu.Item> copy = new ArrayList<>();
        for(EpicuriMenu.Item item: items) {
            if(!ids.contains(item.getId())) {
                copy.add(item);
                ids.add(item.getId());
            }
        }
        return copy;
    }

    public void setListener(OnEpicuriMenuItemsSelectedListener listener) {
        this.listener = listener;
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(inflater.inflate(R.layout.item_landscape_quickorder, parent,
                false));
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ItemViewHolder)holder).render(filteredItems.get(position));
    }

    @Override public int getItemCount() {
        return filteredItems.size();
    }

    public void changeExpanded(){
        this.expandedName = !this.expandedName;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private static final float ITEM_STROKE_WIDTH_DP = 4;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public void render(final EpicuriMenu.Item item) {
            ViewGroup container = itemView.findViewById(R.id.container);
            TextView title = itemView.findViewById(R.id.title);
            TextView price = itemView.findViewById(R.id.price);
            GradientDrawable background = (GradientDrawable) container.getBackground();
            Resources r = itemView.getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ITEM_STROKE_WIDTH_DP, r.getDisplayMetrics());
            background.setStroke((int) px, Color.parseColor(item.getColourHex()));
            String text = item.getShortCode() != null &&
                    !item.getShortCode().isEmpty() && !expandedName ?
                    item.getShortCode().toUpperCase() : item.getName().toUpperCase();
            SpannableString styledText = new SpannableString(text);
            styledText.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
            styledText.setSpan(new RelativeSizeSpan(menuItemFontSize), 0,text.length(), 0);
            title.setText(styledText);
            title.setTextColor(Color.parseColor(item.getColourHex()));

            price.setText(LocalSettings.formatMoneyAmount(item.getPrice(), true));
            price.setTextColor(Color.parseColor(item.getColourHex()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
                        return;
                    }
                    lastClickTime = SystemClock.elapsedRealtime();

                    if (item.isUnavailable()) {
                        new AlertDialog.Builder(itemView.getContext())
                                .setTitle("Item Unavailable").setMessage(
                                String.format("%s is unavailable", item.getName())).show();
                        return;
                    }

                    if (listener != null) listener.onEpicuriMenuItemSelected(item);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View view) {
                    MenuAlertFragment fragment = MenuAlertFragment.newInstance(item);
                    fragment.show(((Activity) view.getContext()).getFragmentManager(), "dialog");

                    return true;
                }
            });
        }
    }

    private float determineFontSize(Context context) {
        try {
            LocalSettings settings = LocalSettings.getInstance(context);
            return settings.getQOFontSize();
        } catch (Exception ex) {
            return 1f;
        }
    }
}
