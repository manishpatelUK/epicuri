package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;

public class TakeawayAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<EpicuriSessionDetail> takeaways = null;
		
	public TakeawayAdapter(Context context){
		inflater = LayoutInflater.from(context);
	}

	public void setState(List<EpicuriSessionDetail> sessions){
		this.takeaways = sessions;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if(null == takeaways) return 0;
		return takeaways.size();
	}

	@Override
	public Object getItem(int position) {
		return takeaways.get(position);
	}

	@Override
	public long getItemId(int position) {
		return takeaways.get(position).getId().hashCode();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(null == convertView){
			convertView = inflater.inflate(R.layout.row_takeaway, parent, false);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}
		
		EpicuriSessionDetail session = takeaways.get(position);
		vh.title.setText(session.getName());
		vh.phone.setText("Tel: " + session.getDeliveryPhoneNumber());

		if(session.getDiners().get(0).getEpicuriCustomer() == null){
			vh.epicuriCustomer.setVisibility(View.GONE);
		} else {
			vh.epicuriCustomer.setVisibility(View.VISIBLE);
		}
		
		int background = R.drawable.hub_row;
		vh.rejectReason.setVisibility(View.GONE);
		vh.status.setVisibility(View.VISIBLE);
		if(session.isDeleted()){
			vh.status.setText("Cancelled");
		} else if(session.isRejected()){
			vh.status.setText("Rejected");
		} else if(!session.isAccepted()){
			vh.status.setText("Pending");
			background = R.drawable.hub_row_red;
			if(null != session.getRejectedReason()){
				vh.rejectReason.setText(session.getRejectedReason());
				vh.rejectReason.setVisibility(View.VISIBLE);
			}
		} else if (session.isClosed()){
			if (session.isPaid())
				vh.status.setText("Closed and paid at ");
			else
				vh.status.setText("Closed at ");

			vh.status.append(LocalSettings.getDateFormat().format(session.getClosedTime()));
		} else if (session.isPaid()){
			vh.status.setText("Accepted & Paid");
		} else {
			vh.status.setText("Accepted");
		}
		convertView.setBackgroundResource(background);


		if(null == session.getMessage() ||  session.getMessage().equals("")){
			vh.notes.setVisibility(View.GONE);
		} else {
			vh.notes.setVisibility(View.VISIBLE);
			vh.notes.setText(session.getMessage());
		}

		StringBuilder sb = new StringBuilder();
		if(session.getType() == EpicuriSessionDetail.SessionType.COLLECTION) {
			sb.append("Collection due at ");
		} else if(session.getType() == EpicuriSessionDetail.SessionType.DELIVERY) {
			sb.append("Delivery due at ");
		} else {
			sb.append("Due at ");
		}
		sb.append(LocalSettings.getDateFormat().format(session.getExpectedTime()));
		vh.startDate.setText(sb);
		
		int colour = !session.isClosed() && !session.isDeleted() && !session.isRejected()
				? Color.BLACK : Color.GRAY;
		for(TextView t: new TextView[]{vh.title, vh.startDate, vh.notes, vh.phone, vh.status}){
			t.setTextColor(colour);
		}

		return convertView;
	}

	static class ViewHolder{
		@InjectView(R.id.name) TextView title;
		@InjectView(R.id.customer_image) ImageView epicuriCustomer;
		@InjectView(R.id.phone) TextView phone;
		@InjectView(R.id.time) TextView startDate;
		@InjectView(R.id.status) TextView status;
		@InjectView(R.id.notaccepted) TextView rejectReason;
		@InjectView(R.id.notes) TextView notes;
		public ViewHolder(View view){
			ButterKnife.inject(this, view);
		}
	}
}
