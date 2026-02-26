package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.model.EpicuriFloor;

public class FloorNameAdapter extends BaseAdapter {
	LayoutInflater inflater;
	List<EpicuriFloor> floors = null;
	
	public FloorNameAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}
	
	public void setFloors(List<EpicuriFloor> floors){
		this.floors = floors;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if(null == floors) return 0;
		return floors.size();
	}

	@Override
	public Object getItem(int position) {
		return floors.get(position).getName();
	}

	@Override
	public long getItemId(int position) {
		return floors.get(position).getId().hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(null == convertView){
			convertView = inflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}
		vh.text.setText((String)getItem(position));
		
		return convertView;
	}

	static class ViewHolder {
		@InjectView(android.R.id.text1) TextView text;
		public ViewHolder(View view){
			ButterKnife.inject(this, view);
		}
	}
	
	
}
