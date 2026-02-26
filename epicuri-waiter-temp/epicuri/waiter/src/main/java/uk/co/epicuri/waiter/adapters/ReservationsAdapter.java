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
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriReservation;
import uk.co.epicuri.waiter.model.LocalSettings;

public class ReservationsAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<EpicuriReservation> reservations = null;
		
	public ReservationsAdapter(Context context){
		inflater = LayoutInflater.from(context);
	}

	public void setState(List<EpicuriReservation> reservations){
		this.reservations = reservations;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if(null == reservations) return 0;
		return reservations.size();
	}

	@Override
	public Object getItem(int position) {
		return reservations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return reservations.get(position).getId().hashCode();
	}
	/*
	 *         "SessionId": 21,
       "Notes": "",
       "Telephone": "1234",
       "ReservationTime": 1365296400,
       "LeadCustomer": null,
       "Accepted": true,
       "Id": 27,
       "NumberOfPeople": 1,
       "Name": "Pqilo",
       "Created": 1365783244(non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(null == convertView){
			convertView = inflater.inflate(R.layout.row_reservation, parent, false);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}
		
		EpicuriReservation reservation = reservations.get(position);
		vh.title.setText(reservation.getName());
		vh.phone.setText(reservation.getPhoneNumber());
		vh.numberInParty.setText(String.format(Locale.UK, "Party of %d", reservation.getNumberInParty()));
		vh.startDate.setText(LocalSettings.getDateFormat().format(reservation.getStartDate()));
		
		vh.rejectedReason.setVisibility(View.GONE);

//		boolean hasTimedOut = reservation.getSessionId() <= 0
//			&& reservation.isTimedOut();

		boolean hasTimedOut = (reservation.getSessionId() == null || reservation.getId().equals("-1") || reservation.getId().equals("0"))
				&& reservation.isTimedOut();

		if(reservation.isDeleted()){
			vh.status.setText("Cancelled");
			convertView.setBackgroundResource(R.drawable.hub_row);
		} else if(hasTimedOut){
			if(reservation.isAccepted()){
				vh.status.setText("Closed (Timed out)");
			} else {
				vh.status.setText("Rejected (Timed out)");
			}
		} else if(!reservation.isAccepted()) {
			if(reservation.getArrivedTime() != null){
				vh.status.setText("Pending (arrived)");
			} else {
				vh.status.setText("Pending");
			}
			convertView.setBackgroundResource(R.drawable.hub_row);
			if(null != reservation.getRejectedReason()){
				vh.rejectedReason.setText(reservation.getRejectedReason());
				vh.rejectedReason.setVisibility(View.VISIBLE);	
			}
			convertView.setBackgroundResource(R.drawable.hub_row_red);
		} else {
			vh.status.setText("Accepted");
			convertView.setBackgroundResource(R.drawable.hub_row);	
		}

		
		if(null == reservation.getNotes() ||  reservation.getNotes().equals("")){
			vh.notes.setVisibility(View.GONE);
		} else {
			vh.notes.setVisibility(View.VISIBLE);
			vh.notes.setText(reservation.getNotes());
		}

		vh.epicuriCustomer.setVisibility(reservation.getEpicuriUser() != null ? View.VISIBLE: View.GONE);
		vh.birthday.setVisibility(reservation.isBirthday() ? View.VISIBLE: View.GONE);
		
	//	boolean inSession = (reservation.getSessionId().length() > 0 || null != reservation.getArrivedTime()) && reservation.isAccepted(); // session, or marked as arrived
		boolean inSession = (reservation.getSessionId() != null && !reservation.getSessionId().equals("0") && !reservation.getSessionId().equals("-1"))
				|| (null != reservation.getArrivedTime()) && reservation.isAccepted();
		boolean isDeleted = reservation.isDeleted();
		for(TextView v: new TextView[]{vh.title, vh.notes, vh.phone, vh.startDate, vh.numberInParty, vh.status, vh.at}){
			v.setTextColor(inSession || isDeleted || hasTimedOut ? Color.GRAY: Color.BLACK);
		}
		
		
		return convertView;
	}

	static class ViewHolder{
		@InjectView(R.id.name) TextView title;
		@InjectView(R.id.notes) TextView notes;
		@InjectView(R.id.phone) TextView phone;
		@InjectView(R.id.time) TextView startDate;
		@InjectView(R.id.numberInParty) TextView numberInParty;
		@InjectView(R.id.status) TextView status;
		@InjectView(R.id.notaccepted) TextView rejectedReason;
		@InjectView(R.id.customer_image) ImageView epicuriCustomer;
		@InjectView(R.id.birthday_image) ImageView birthday;
		@InjectView(R.id.text_at) TextView at;
		public ViewHolder(View view){
			ButterKnife.inject(this, view);
		}
	}
}
