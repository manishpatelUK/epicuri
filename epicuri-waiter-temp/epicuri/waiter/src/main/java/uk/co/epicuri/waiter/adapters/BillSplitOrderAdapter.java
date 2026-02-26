package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.IOrderItem;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierValue;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriOrderItem.GroupedOrderItem;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.SessionOrdersFragment;

public class BillSplitOrderAdapter extends BaseAdapter implements
		SessionOrdersFragment.IOrderAdapter {
	static EpicuriSessionDetail session;
	ArrayList<EpicuriOrderItem> pendingOrders;
	List<GroupedOrderItem> displayedOrders;
	static Map<String, String> itemLookup = new HashMap<>(1);

	private LayoutInflater inflater;

	static Diner selectedDiner;

	public BillSplitOrderAdapter(Context context, Diner diner) {
		session = null;
		inflater = LayoutInflater.from(context);
		selectDiner(diner);
	}

	boolean combineSimilarItems = false;
	@Override
	public void setCombineSimilarItems(boolean group){
		combineSimilarItems = group;
	}

    /**
     * overloaded method to replace the data in this adapter
     * @param newSession session to show
     */
    @Override
	public void changeData(EpicuriSessionDetail newSession){
		session = newSession;
		if (selectedDiner != null)
			selectedDiner = newSession.getDinerFromId(selectedDiner.getId());

		selectDiner(selectedDiner);
		notifyDataSetChanged();
	}

	public void onItemSelected(int position) {
		if (selectedDiner == null) return;

		String itemId = ((EpicuriOrderItem.GroupedOrderItem)getItem(position)).getId();

		if (!itemLookup.containsKey(itemId))
			itemLookup.put(itemId, itemId);
		else
			itemLookup.remove(itemId);

		notifyDataSetChanged();
	}

	public Map<String, String> getItemLookup() {
		return itemLookup;
	}

	@Override
	public boolean hasStableIds() {
		return true; // not technically true
	}

	private ArrayList<GroupedOrderItem> combineOrders(List<EpicuriOrderItem> orders){
		ArrayList<GroupedOrderItem> groupedOrders = new ArrayList<GroupedOrderItem>();

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

		final TreeMap<String, Integer> dinerMap = new TreeMap<>();
		for (Diner diner : session.getDiners())
			if (!dinerMap.containsKey(diner.getId()))
				dinerMap.put(diner.getId(), dinerMap.size());

		TreeMap<Integer, List<GroupedOrderItem>> map = new TreeMap<>();
		for (GroupedOrderItem item : groupedOrders) {
			if (!map.containsKey(item.getCourse().getOrdering()))
				map.put(item.getCourse().getOrdering(), new ArrayList<GroupedOrderItem>(1));

			map.get(item.getCourse().getOrdering()).add(item);
		}

		groupedOrders.clear();
		for (Integer key : map.keySet()) {
			Collections.sort(map.get(key), new Comparator<GroupedOrderItem>() {
				@Override
				public int compare(GroupedOrderItem lhs, GroupedOrderItem rhs) {
					return dinerMap.get(lhs.getDinerId()) - dinerMap.get(rhs.getDinerId());
				}

			});
			groupedOrders.addAll(map.get(key));
		}

		return groupedOrders;
	}

	@Override
	public void selectDiner(Diner diner){
		if (selectedDiner == null || diner == null || !selectedDiner.getId().equals(diner.getId())) {
			itemLookup.clear();

			if (diner != null)
				for(String i: diner.getOrders()){
					itemLookup.put(i, i);
				}
		}

		selectedDiner = diner;

		if(null != pendingOrders){
			displayedOrders = combineOrders(pendingOrders);
		} else if (null == session){
			notifyDataSetChanged();
			return;
		} else {
			if(null == selectedDiner){
				displayedOrders = combineOrders(session.getOrders());
				notifyDataSetChanged();
				return;
			}

			displayedOrders = combineOrders(session.getOrders());
		}

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
		GroupedOrderItem i = displayedOrders.get(position);
		boolean showCourseHeader = true;
		if(position > 0){
			showCourseHeader = (!displayedOrders.get(position-1).getCourse().getName().equals(i.getCourse().getName()));

		}
		vh.show(i, showCourseHeader);
		return convertView;
	}

	private static Diner getItemDiner(String dinerId) {
		return session == null ? null : session.getDinerFromId(dinerId);
	}

	public static class MenuItemViewHolder{
		@InjectView(R.id.title) TextView title;
		@InjectView(R.id.modifiers) TextView modifiers;
		@InjectView(R.id.course) TextView course;
		@InjectView(R.id.price) TextView price;
		@InjectView(R.id.note) TextView note;
		@InjectView(R.id.diner) TextView diner;
		@InjectView(R.id.row) View row;

		public MenuItemViewHolder(View view){
			ButterKnife.inject(this, view);
		}

		private String getDiner(EpicuriSessionDetail.Diner diner) {
			/*if (!diners.containsKey(dinerId))
				diners.put(dinerId, "Guest" + counter++);

			return diners.get(dinerId);*/
			if(TextUtils.isEmpty(diner.getName())) {
				return "Guest";
			}

			return diner.getName();
		}

		public void show(IOrderItem order, boolean showCourseHeader){
			StringBuilder titleSB = new StringBuilder(order.getItem().getName());

			if(order.getQuantity() > 1){
				titleSB.append(" x").append(order.getQuantity());
			}
			course.setVisibility(showCourseHeader ? View.VISIBLE : View.GONE);
			title.setText(titleSB);

			course.setText(order.getCourse().getName());

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
				price.setText(String.format("* %s %s", reason, LocalSettings.formatMoneyAmount(order.getCalculatedPriceIncludingQuantity(), true)));
			} else {
				price.setText(LocalSettings.formatMoneyAmount(order.getCalculatedPriceIncludingQuantity(), true));
			}

			row.setActivated(selectedDiner != null && itemLookup.containsKey(order.getId()));

			Diner itemDiner = getItemDiner(order.getDinerId());
			if (itemDiner == null) return;

			diner.setVisibility(View.VISIBLE);
			if (itemDiner.isTable()) {
				diner.setText("Table");
				diner.setTextColor(Color.RED);
				return;
			}

			diner.setTextColor(Color.BLACK);
			diner.setText(itemDiner.getEpicuriCustomer() == null ? getDiner(itemDiner) :
					itemDiner.getEpicuriCustomer().getName());
		}
	}
}
