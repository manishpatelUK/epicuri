package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriEvent;
import uk.co.epicuri.waiter.model.EpicuriEvent.HubNotification;
import uk.co.epicuri.waiter.model.EpicuriEvent.Notification;
import uk.co.epicuri.waiter.model.EpicuriEvent.Type;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;

public class EventAdapter extends BaseAdapter {
	private enum ViewType {
		ACKNOWLEDGED(0),
		DUE(1),
		HIDDEN(2);
		
		private final int id;
		public int getId() { return id; }
		public static ViewType fromId(int id){
			for(ViewType v: values()){
				if(v.id == id) return v;
			}
			throw new IllegalArgumentException("Id not recognised " + id);
		}
		ViewType(int id){ this.id = id; }
	}
	
	private List<? extends EpicuriEvent.Notification> notifications;
	private LayoutInflater inflater;
	
	private int[] colours;
	
	private final Set<EpicuriEvent.Notification> hiddenRows = new HashSet<EpicuriEvent.Notification>();
	
	public void hide(int position){
		Notification n = notifications.get(position);
		hiddenRows.add(n);
		notifyDataSetChanged();
	}
	public void show(int position){
		Notification n = notifications.get(position);
		hiddenRows.remove(n);
		notifyDataSetChanged();
	}
	
	public EventAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		colours = new int[3];
		colours[0] = context.getResources().getColor(R.color.table_idle);
		colours[1] = context.getResources().getColor(R.color.table_soon);
		colours[2] = context.getResources().getColor(R.color.table_attention);
	}

	public void setState(List<? extends Notification> notifications){
		this.notifications = notifications;
		notifyDataSetChanged();
	}
	
	private Map<String, EpicuriSessionDetail> sessionsById = new HashMap<>();
	public void setSessionState(List<EpicuriSessionDetail> sessions){
		sessionsById.clear();
		for(EpicuriSessionDetail sess: sessions){
			sessionsById.put(sess.getId(), sess);
		}
		notifyDataSetChanged();
	}
	
	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		Notification n = notifications.get(position);
		if(hiddenRows.contains(n)){
			return ViewType.HIDDEN.getId();
		}
		if(n.getType() == Type.TYPE_RECURRING) return ViewType.DUE.getId();
		return n.getAcknowledgements().size() == 0 ? ViewType.DUE.getId() : ViewType.ACKNOWLEDGED.getId();
	}

	@Override
	public int getCount() {
		if(null == notifications) return 0;
		return notifications.size();
	}

	@Override
	public Object getItem(int position) {
		return notifications.get(position);
	}

	@Override
	public long getItemId(int position) {
//		return ((EpicuriEvent)getItem(position)).id;
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		ViewType viewType = ViewType.fromId(getItemViewType(position));
		if(viewType == ViewType.HIDDEN){
			View v = new View(parent.getContext());
			return v;
		}
		if(null == convertView){
			convertView = inflater.inflate( (viewType == ViewType.DUE ? R.layout.row_event : R.layout.row_event_done), parent, false);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}
		EpicuriEvent.Notification notification = notifications.get(position);
 
		StringBuffer sb = new StringBuffer();
		
		boolean futureEvent = false;
		if(notification instanceof HubNotification){
			HubNotification hubNot = (HubNotification)notification;
			EpicuriSessionDetail session = sessionsById.get(hubNot.getSessionId());
			if(null != session){
//				if(null != session.getName()){
//					sb.append(session.getName()).append(" - ");
//				}
				sb.append("Party of ").append(session.getNumberInParty());
				if(session.getTables().length > 1){
					sb.append(", seated on tables: ");
				} else {
					sb.append(", seated on table: ");
				}
				sb.append(session.getTablesString());
			}
		} else if(notification instanceof EpicuriEvent.ScheduledEventNotification){
			futureEvent = ((EpicuriEvent.ScheduledEventNotification)notification).isFutureAction();
		}
		
		Date now = new Date();
		if(futureEvent){
			// future event is a scheduled event beyond the "next" one
			vh.type.setBackgroundColor(0x0000);
			if(null != vh.overdue){
				vh.overdue.setVisibility(View.GONE);
			}
		} else if(notification.getDue().getTime() - now.getTime() < - GlobalSettings.ON_TIME_THRESHOLD){
			// overdue by more than "ON TIME THRESHOLD"
			vh.type.setBackgroundColor(colours[2]);
			if(null != vh.overdue){
				vh.overdue.setVisibility(View.VISIBLE);
				long millisOverdue = (now.getTime() - notification.getDue().getTime()); 
				vh.overdue.setText(GlobalSettings.minsLate(millisOverdue));
			}
		} else if(notification.getDue().getTime() - now.getTime() < EpicuriSessionDetail.A_FEW_MINUTES){
			// due within next 5 mins
			if(null != vh.overdue){
				vh.overdue.setText("Due in a few minutes");
				vh.overdue.setVisibility(View.VISIBLE);
			}
			vh.type.setBackgroundColor(colours[1]);
		} else {
			// more than 5 mins away
			if(null != vh.overdue){
				vh.overdue.setVisibility(View.GONE);
			}
			vh.type.setBackgroundColor(colours[0]);
		}
		vh.actionDue.setText("Due " + LocalSettings.niceFormat(notification.getDue()));
		
		vh.title.setText(notification.getText());
		vh.detail.setText(sb);

		if(viewType == ViewType.ACKNOWLEDGED){
			Date ack = notification.getAcknowledgements().get(0);
			Date due = notification.getDue();
			long millisecondsLate = ack.getTime() - due.getTime();
			vh.actionDue.setText("Done: " + LocalSettings.niceFormat(ack) + "\n" + GlobalSettings.minsLate(millisecondsLate));
			
			vh.type.setBackgroundColor(0x0000);
		}
		switch(notification.getType()){
		case TYPE_SCHEDULED:
			vh.type.setImageResource(R.drawable.scheduled);
			break;
		case TYPE_ADHOC:
			vh.type.setImageResource(R.drawable.adhoc);
			break;
		case TYPE_RECURRING:
			vh.type.setImageResource(R.drawable.recurring);
			break;
		}
		return convertView;
	}
	
	static class ViewHolder{
		@InjectView(R.id.title) TextView title;
		@InjectView(R.id.detail) TextView detail;
		@InjectView(R.id.actionDue) TextView actionDue;
		@Optional @InjectView(R.id.actionOverDue) TextView overdue;
		@InjectView(R.id.type) ImageView type;
//		@InjectView(R.id.quickStatus) View quickStatus;

		public ViewHolder(View view){
			ButterKnife.inject(this, view);
		}
	}
}
