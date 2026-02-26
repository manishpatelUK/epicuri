package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriParty;
import uk.co.epicuri.waiter.model.LocalSettings;

public class PartyAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<EpicuriParty> parties = null;
	private List<EpicuriCustomer.Checkin> checkins = null;
	
	private List<Object> partyThings = new ArrayList<Object>(0);
	
	public PartyAdapter(Context context){
		inflater = LayoutInflater.from(context);
	}

	public void setParties(List<EpicuriParty> parties){
		this.parties = parties;
		recreateList();
		notifyDataSetChanged();
	}
	public void setCheckins(List<EpicuriCustomer.Checkin> checkins){
		this.checkins = checkins;
		recreateList();
		notifyDataSetChanged();
	}
	private void recreateList(){
		partyThings = new ArrayList<Object>( (null == checkins ? 0 : checkins.size()) + (null == parties ? 0 : parties.size()) );
		if(null != parties){
			partyThings.addAll(parties);
		}
		if(null != checkins){
			partyThings.addAll(checkins);
		}
		Collections.sort(partyThings, new Comparator<Object>(){
			@Override
			public int compare(Object lhs, Object rhs) {
				if(lhs.equals(rhs)) return 0;
				
				int lMasterOrder;
				int rMasterOrder;
				Date lDate;
				Date rDate;
				
				if(lhs instanceof EpicuriCustomer.Checkin){
					lMasterOrder = 1;
					lDate = ((EpicuriCustomer.Checkin)lhs).getDate();
				} else {
					EpicuriParty p = (EpicuriParty)lhs;
					//if(p.getSessionId() > 0){
					if(p.getSessionId() != null && !(p.getSessionId().equals("0")||p.getSessionId().equals("-1"))){
						lMasterOrder = 3;
					} else if(p.getReservationTime() == null || p.isAccepted()){
						// either not a reservation, or it's a reservation which has been accepted
						lMasterOrder = 1;
					} else {
						lMasterOrder = 2;
					}
					lDate = p.getArrivedTime();
					if(null == lDate) lDate = p.getCreateTime();
				}
				
				if(rhs instanceof EpicuriCustomer.Checkin){
					rMasterOrder = 1;
					rDate = ((EpicuriCustomer.Checkin)rhs).getDate();
				} else {
					EpicuriParty p = (EpicuriParty)rhs;
					//if(p.getSessionId() > 0){
					if(p.getSessionId() != null && !(p.getSessionId().equals("0")||p.getSessionId().equals("-1"))){
						rMasterOrder = 3;
					} else if(p.isAccepted()){
						rMasterOrder = 1;
					} else {
						rMasterOrder = 2;
					}
					rDate = p.getArrivedTime();
					if(null == rDate) rDate = p.getCreateTime();
				}
				
				if(lMasterOrder != rMasterOrder){
					return lMasterOrder - rMasterOrder;
				} else {
					return rDate.compareTo(lDate);
				}
			}
		});
	}
	
	@Override
	public int getCount() {
		return partyThings.size();
	}

	@Override
	public Object getItem(int position) {
		return partyThings.get(position);
	}
	
	public enum Type {
		PARTY, CHECKIN
	}
	public Type getItemType(int position){
		if(partyThings.get(position) instanceof EpicuriParty){
			return Type.PARTY;
		} else if(partyThings.get(position) instanceof EpicuriCustomer.Checkin) {
			return Type.CHECKIN;
		}
		throw new IllegalStateException();
	}
	
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(null == convertView){
			convertView = inflater.inflate(R.layout.row_party, parent, false);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}
		

		if(partyThings.get(position) instanceof EpicuriParty){
			EpicuriParty party= (EpicuriParty)partyThings.get(position);
			
			vh.title.setText(party.getPartyName());
			vh.detail.setText(String.format(Locale.UK, "Party of %d", party.getNumberInParty()));
			
			if(null != party.getReservationTime()){
				// reservation
				vh.reservationTime.setText(String.format("%s for %s", party.isAccepted() ? "Reservation" : "Requested reservation", LocalSettings.niceFormat(party.getReservationTime())));
			} else {
				// walk in
				vh.arriveTime.setText(String.format("Arrived at %s", LocalSettings.niceFormat(party.getCreateTime())));
			}
			vh.customer.setVisibility(party.getLeadCustomer() == null ? View.GONE : View.VISIBLE);
			if(party.getSessionId() == null || party.getSessionId().equals("-1") || party.getSessionId().equals("0")){
			//if(null != party.getSessionId()){
				if(null != party.getArrivedTime()){
					vh.arriveTime.setText(String.format("Arrived at %s", LocalSettings.niceFormat(party.getArrivedTime())));
				} else {
					vh.reservationTime.setText("");
				}
				if(null != party.getReservationTime()){
					if(party.isAccepted()){
						vh.icon.setImageResource(R.drawable.reservation_light);
					} else {
						vh.icon.setImageResource(R.drawable.walkin_light);
					}
				} else {
					vh.icon.setImageResource(R.drawable.walkin_light);
				}
			} else {
				vh.reservationTime.setText(String.format("Tab Started"));// at %s", dateFormat.format(party.getArriveTime())));
				vh.icon.setImageResource(R.drawable.inbar_light);
			}
		} else if(partyThings.get(position) instanceof EpicuriCustomer.Checkin){
			EpicuriCustomer.Checkin checkin = (EpicuriCustomer.Checkin)partyThings.get(position);
			
			vh.title.setText(checkin.getCustomer().getName());
			vh.customer.setVisibility(View.VISIBLE);
			vh.detail.setText("");
	
			vh.arriveTime.setText(String.format("Arrived at %s", LocalSettings.niceFormat(checkin.getDate())));
			vh.reservationTime.setText("Checked in");
			vh.icon.setImageResource(R.drawable.checkin);
		}
		return convertView;
	}

	static class ViewHolder{
		@InjectView(R.id.title) TextView title;
		@InjectView(R.id.detail) TextView detail;
		@InjectView(R.id.arriveTime) TextView arriveTime;
		@InjectView(R.id.reservationTime) TextView reservationTime;
		@InjectView(R.id.icon) ImageView icon;
		@InjectView(R.id.customer) ImageView customer;
		public ViewHolder(View view){
			ButterKnife.inject(this, view);
		}
	}
	
}
