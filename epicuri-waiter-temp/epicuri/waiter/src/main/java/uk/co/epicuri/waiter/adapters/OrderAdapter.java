package uk.co.epicuri.waiter.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierValue;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriOrderItem.GroupedOrderItem;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.interfaces.IOrderItem;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.MenuAlertFragment;
import uk.co.epicuri.waiter.ui.SessionOrdersFragment;

public class OrderAdapter extends BaseAdapter implements SessionOrdersFragment.IOrderAdapter {
	private static final String TAG = "EpicuriOrderAdapter";
	
	private EpicuriSessionDetail session = null;
	private ArrayList<EpicuriOrderItem> pendingOrders = null;
	
	private List<GroupedOrderItem> displayedOrders = null;
	
	private LayoutInflater inflater;
	
	private Diner selectedDiner;
	
	public OrderAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}
	
	boolean combineSimilarItems = false;
	@Override
	public void setCombineSimilarItems(boolean group){
		combineSimilarItems = group;
	}

    /**
     * overloaded method to replace the data in this adapter
     * @param orders new list of orders to show
     */
	public void changeData(List<EpicuriOrderItem> orders){
		// copy so we don't change the order of the original list
		this.pendingOrders = new ArrayList<EpicuriOrderItem>(orders.size());
		pendingOrders.addAll(orders);
		Collections.sort(pendingOrders, new Comparator<EpicuriOrderItem>() {

			@Override
			public int compare(EpicuriOrderItem lhs, EpicuriOrderItem rhs) {
				return lhs.getCourse().getOrdering() - rhs.getCourse().getOrdering();
			}
			
		});
		selectDiner(selectedDiner);
		notifyDataSetChanged();
	}

    /**
     * overloaded method to replace the data in this adapter
     * @param newSession session to show
     */
    @Override
	public void changeData(EpicuriSessionDetail newSession){
		session = newSession;
		
		selectDiner(selectedDiner);
		notifyDataSetChanged();
	}
	
	@Override
	public boolean hasStableIds() {
		return true; // not technically true
	}
	
	private ArrayList<GroupedOrderItem> combineOrders(List<EpicuriOrderItem> orders){
		ArrayList<GroupedOrderItem> groupedOrders = new ArrayList<EpicuriOrderItem.GroupedOrderItem>();
		
		for(EpicuriOrderItem o: orders){
			boolean merged = false;
			
			if(combineSimilarItems){
				for(GroupedOrderItem go: groupedOrders){
					if(go.getOrderItem().isSameOrder(o)){
						go.mergeWith(o);

						merged = true;
						break;
					}
				}
			}

			if(!merged){
				groupedOrders.add(new GroupedOrderItem(o));
			}
		}
        // now sort by course
        Collections.sort(groupedOrders, new Comparator<GroupedOrderItem>() {

            @Override
            public int compare(GroupedOrderItem lhs, GroupedOrderItem rhs) {return lhs.getCourse().getOrdering() - rhs.getCourse().getOrdering();
            }

        });
		return groupedOrders;
	}

	@Override
	public void selectDiner(Diner diner){
		this.selectedDiner = diner;
		if(null != pendingOrders){
			if(null == diner){
				displayedOrders = combineOrders(pendingOrders);
			} else {
				ArrayList<EpicuriOrderItem> orders = new ArrayList<EpicuriOrderItem>();
				for(EpicuriOrderItem i: pendingOrders){
					if(i.getDinerId() != null && i.getDinerId().equals(diner.getId())){
						orders.add(i);
					}
				}
				displayedOrders = combineOrders(orders);
			}
		} else if (null == session){
			return;
		} else {
			if(null == selectedDiner){
				displayedOrders = combineOrders(session.getOrders());
				notifyDataSetChanged();
				return;
			}
			List<EpicuriOrderItem> orders = new LinkedList<>();
			Map<String, EpicuriOrderItem> itemLookup = new HashMap<>();
			for(EpicuriOrderItem i: session.getOrders()){
				itemLookup.put(i.getId(), i);
			}
			for(String orderId: diner.getOrders()){
				EpicuriOrderItem item = itemLookup.get(orderId);
				if(null != item && !orders.contains(item)){
					orders.add(item);
				}
			}
			displayedOrders = combineOrders(orders);
		}
		Collections.sort(displayedOrders, new Comparator<IOrderItem>() {

			@Override
			public int compare(IOrderItem lhs, IOrderItem rhs) {
				return lhs.getCourse().getOrdering() - rhs.getCourse().getOrdering();
			}
			
		});
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if(null == displayedOrders) return 0;
		return displayedOrders.size();
	}

	@Override
	public Object getItem(int position) {

		return displayedOrders.get(position);
	}

	@Override
	public long getItemId(int position) {
		if(displayedOrders.size() <= position) return -1;
		return displayedOrders.get(position).getOrderItem().getId().hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MenuItemViewHolder vh;
		if(null == convertView){
			convertView = inflater.inflate(R.layout.row_order_item, parent, false);
			vh = new MenuItemViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (MenuItemViewHolder)convertView.getTag();
		}
		final GroupedOrderItem i = displayedOrders.get(position);
		boolean showCourseHeader = true;
		if(position > 0){
			showCourseHeader = (!displayedOrders.get(position-1).getCourse().getName().equals(i.getCourse().getName()));

		}

		vh.show(i, showCourseHeader);
		return convertView;
	}

    public List<GroupedOrderItem> getDisplayedOrders() {
        return displayedOrders;
    }

    public static class MenuItemViewHolder{
		@InjectView(R.id.title) TextView title;
		@InjectView(R.id.modifiers) TextView modifiers;
		@InjectView(R.id.course) TextView course;
		@InjectView(R.id.price) TextView price;
		@InjectView(R.id.note) TextView note;

		public MenuItemViewHolder(View view){
			ButterKnife.inject(this, view);
		}
		
		public void show(IOrderItem order, boolean showCourseHeader){
			StringBuilder titleSB = new StringBuilder(order.getItem().getName());

			if(order.getQuantity() > 1){
				titleSB.append(" x").append(order.getQuantity());
			}
			course.setVisibility(showCourseHeader ? View.VISIBLE : View.GONE);
			title.setText(titleSB);
	//		description.setText(String.format("Quantity: %d", i.quantity));
            if(order.getCourse() != null){
                course.setText(order.getCourse().getName());
            }

			if(order.getNote() != null && !order.getNote().trim().equals("")){
				note.setText(order.getNote());
				note.setVisibility(View.VISIBLE);
			} else {
				note.setVisibility(View.GONE);
			}
			
			StringBuilder modifiersString = new StringBuilder();

			boolean first = true;
			for(ModifierValue mod: order.getChosenModifiers()){
				if(!first) modifiersString.append(", ");
				first = false;
				modifiersString.append(mod.getName());
				if(mod.getPrice().isPositive()){
					modifiersString.append(" (").append(LocalSettings.formatMoneyAmount(mod.getPrice(), true)).append(")");
				}
			}
			modifiers.setText(modifiersString);
			
			if(order.isPriceOverridden()){
				String reason = order.getDiscountReason();
				if(null == reason) reason = "";
				if(!reason.isEmpty() && order.getPriceOverride().getAmount().doubleValue() == 0.0){
                    price.setText(String.format("* %s %s", reason, order.getPriceOverride()));
                } else {
                    price.setText(String.format("* %s %s", reason, LocalSettings.formatMoneyAmount(order.getCalculatedPriceIncludingQuantity(), true)));
                }
			} else {
				price.setText(LocalSettings.formatMoneyAmount(order.getCalculatedPriceIncludingQuantity(), true));
			}
		}
	}
}
