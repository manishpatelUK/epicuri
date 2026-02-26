package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.IOrderItem;
import uk.co.epicuri.waiter.interfaces.OnItemQueuedListener;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.LocalSettings;


public class OrderLandscapeAdapter extends BaseSwipeAdapter {

    public interface SwipeItemsListener {
        void onItemEdit(EpicuriOrderItem.GroupedOrderItem item);
    }

    private List<EpicuriOrderItem> pendingOrders = null;
    private List<EpicuriOrderItem.GroupedOrderItem> displayedOrders = null;
    private OnItemQueuedListener listener;
    private SwipeItemsListener swipeItemsListener;
    private LayoutInflater inflater;


    public OrderLandscapeAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setSwipeItemsListener(SwipeItemsListener swipeItemsListener) {
        this.swipeItemsListener = swipeItemsListener;
    }

    public void setListener(OnItemQueuedListener listener) {
        this.listener = listener;
    }

    public void changeData(List<EpicuriOrderItem> orders) {
        this.pendingOrders = orders;
        combineOrders();
        notifyDataSetChanged();
    }

    private void combineOrders() {
        ArrayList<EpicuriOrderItem.GroupedOrderItem> groupedOrders = new
                ArrayList<EpicuriOrderItem.GroupedOrderItem>();

        for (EpicuriOrderItem o : pendingOrders) {
            boolean merged = false;

            for (EpicuriOrderItem.GroupedOrderItem go : groupedOrders) {
                if (go.getOrderItem().isSameOrder(o)) {
                    go.mergeWith(o);

                    merged = true;
                    break;
                }
            }


            if (!merged) {
                groupedOrders.add(new EpicuriOrderItem.GroupedOrderItem(o));
            }
        }

        displayedOrders = groupedOrders;
    }

    public EpicuriOrderItem.GroupedOrderItem getItemAtPosition(int position) {
        return displayedOrders.get(position);
    }

    @Override
    public int getCount() {
        if (null == displayedOrders) return 0;
        return displayedOrders.size();
    }

    @Override
    public EpicuriOrderItem.GroupedOrderItem getItem(int position) {
        return displayedOrders.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (displayedOrders.size() <= position) return -1;
        return displayedOrders.get(position).getOrderItem().getId().hashCode();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipeRow;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        MenuItemViewHolder vh;
        View convertView;
        convertView = inflater.inflate(R.layout.row_order_item_new, parent, false);
        vh = new MenuItemViewHolder(convertView);
        convertView.setTag(vh);
        return convertView;
    }

    @Override
    public void fillValues(int position, View convertView) {
        MenuItemViewHolder vh = (MenuItemViewHolder) convertView.getTag();
        EpicuriOrderItem.GroupedOrderItem i = displayedOrders.get(position);
        vh.show(i, position);
    }

    class MenuItemViewHolder {
        @InjectView(R.id.title)
        TextView title;
        @InjectView(R.id.modifiers)
        TextView modifiers;
        @InjectView(R.id.price)
        TextView price;
        @InjectView(R.id.count_text)
        TextView count;
        @InjectView(R.id.remove)
        View remove;
        @InjectView(R.id.edit)
        View edit;
        @InjectView(R.id.noteIcon)
        View noteIc;
        @InjectView(R.id.swipeRow)
        SwipeLayout swipeLayout;

        public MenuItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        public void show(final IOrderItem order, final int position) {
            StringBuilder titleSB = new StringBuilder(order.getItem().getName());

            if (order.getQuantity() > 1) {
                count.setText(String.valueOf(order.getQuantity()));
//                titleSB.append(" x").append(order.getQuantity());
            } else count.setText("1");
            title.setText(titleSB);

            if (order.getNote() == null || order.getNote().isEmpty()) {
                noteIc.setVisibility(View.GONE);
            } else {
                noteIc.setVisibility(View.VISIBLE);
            }

            StringBuilder modifiersString = new StringBuilder();

            boolean first = true;
            for (EpicuriMenu.ModifierValue mod : order.getChosenModifiers()) {
                if (!first) modifiersString.append(", ");
                first = false;
                modifiersString.append(mod.getName());
                if (mod.getPrice().isPositive()) {
                    modifiersString.append(" (").append(
                            LocalSettings.formatMoneyAmount(mod.getPrice(), true)).append(")");
                }
            }
            modifiers.setText(modifiersString);
            modifiers.setVisibility(modifiersString.length() == 0 ? View.GONE : View.VISIBLE);

            if (order.isPriceOverridden()) {
                String reason = order.getDiscountReason();
                if (null == reason) reason = "";
                price.setText(String.format("* %s %s", reason, LocalSettings.formatMoneyAmount(order.getCalculatedPriceIncludingQuantity(), true)));
            } else {
                price.setText(LocalSettings.formatMoneyAmount(order.getCalculatedPriceIncludingQuantity(), true));
            }

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EpicuriOrderItem.GroupedOrderItem item = getItemAtPosition(position);
                    if (listener != null && item != null) {
                        if (item.getQuantity() == 1) {
                            swipeLayout.close();
                            displayedOrders.remove(position);
                        }
                        listener.unQueueItem(item.getOrderItem(), null);
                    }
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editItem(position);
                }
            });

            swipeLayout.setOnClickListener(new View.OnClickListener() {
                boolean closed = true;
                @Override public void onClick(View view) {
                    if(swipeLayout.getOpenStatus() == SwipeLayout.Status.Close){
                        if(closed){
                            editItem(position);
                        }else {
                            closed = true;
                        }
                    } else {
                        closed = false;
                    }

                }
            });
        }
        private  void editItem(int position){
            EpicuriOrderItem.GroupedOrderItem item = getItemAtPosition(position);
            if (swipeItemsListener != null && item != null) {
                swipeItemsListener.onItemEdit(item);
            }
        }
    }
}
