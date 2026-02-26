package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.content.res.Resources;
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
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.SessionType;
import uk.co.epicuri.waiter.model.LocalSettings;

public class SessionAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<EpicuriSessionDetail> sessions = null;
	private Resources res;
	
	EpicuriSessionDetail limitToSession = null;
		
	public SessionAdapter(Context context){
		inflater = LayoutInflater.from(context);
		res = context.getResources();
	}

	public void setState(List<EpicuriSessionDetail> sessions){
		this.sessions = sessions;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if(null == sessions) return 0;
		if(limitToSession != null) return 1;
		return sessions.size();
	}

	@Override
	public EpicuriSessionDetail getItem(int position) {
		if(limitToSession != null) return limitToSession;
		return sessions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId().hashCode();
	}
	
	public void showSession(EpicuriSessionDetail session){
		this.limitToSession = session;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(null == convertView){
			convertView = inflater.inflate(R.layout.row_session, parent, false);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}
		
		EpicuriSessionDetail session = getItem(position);
		if(session.isClosed()) {
			String sessionId = session.getReadableId() == null || session.getReadableId().isEmpty()? session.getId() : session.getReadableId();
			String titleText = String.format("%s (%s)%s", session.getName(), sessionId, session.isVoided() ? " - VOIDED": "");
			if (!session.isPaid()){
				titleText += " Force closed";
			}
			vh.title.setText(titleText);
		} else {
			vh.title.setText(session.getName());
		}
		if (session.isClosed() && !session.isPaid()){
			vh.status.setText("Force closed");
		}else {
			vh.status.setText(session.getStatusString());
		}

		if(session.getType() == SessionType.DINE){
			
			final StringBuilder tablesString = new StringBuilder("Party of ")
				.append(session.getNumberInParty());
			final StringBuilder dateString = new StringBuilder();

			if(session.isTab()){
				// This shouldn't actually happen - tabs aren't shown using this adapter
				vh.sessionType.setImageResource(R.drawable.inbar_light);
				tablesString.append(", in bar");
				if(!session.isClosed()) dateString.append("Arrived at ");
			} else {
				if(!session.isClosed()) dateString.append("Seated at ");
				vh.sessionType.setImageResource(R.drawable.diner_light);
				if(session.getTables().length > 1){
					tablesString.append(", seated on tables: ")
							.append(session.getTablesString());
				} else {
					tablesString.append(", seated on table: ")
							.append(session.getTablesString());
				}
			}
			if(session.isClosed()){
				tablesString.append(" " + LocalSettings.formatMoneyAmount(session.getTotal(), true));
			}

			vh.detail.setText(tablesString);

			if(session.isClosed()){
				dateString.append("Closed at ").append(LocalSettings.niceFormat(session.getClosedTime()));
			} else {
				dateString.append(LocalSettings.niceFormat(session.getStartTime()));
			}
			vh.startDate.setText(dateString);

//			int minsOverdue = (int)((new Date().getTime() - session.getStartTime().getTime()) / 60000);
//							,minsOverdue
		} else {
			vh.sessionType.setImageResource(R.drawable.takeaway_light);
			String appendage = "";
			if(session.isClosed()) appendage = " " + LocalSettings.formatMoneyAmount(session.getTotal(), true);
			if (session.getType() == SessionType.COLLECTION) {
				vh.detail.setText("For collection" + appendage);
			} else {
				vh.detail.setText("For delivery" + appendage);
			}


			vh.startDate.setText("Due " + LocalSettings.niceFormat(session.getExpectedTime()));
		}


		switch(session.getState()){
		case CLOSED:
			vh.sessionType.setBackgroundColor(res.getColor(R.color.lightgray));
			break;
		case EMPTY:
			vh.sessionType.setBackgroundColor(res.getColor(R.color.table_empty));
			break;
		case IDLE:
			vh.sessionType.setBackgroundColor(res.getColor(R.color.table_idle));
			break;
		case SOON:
			vh.sessionType.setBackgroundColor(res.getColor(R.color.table_soon));
			break;
		case ATTENTION:
			vh.sessionType.setBackgroundColor(res.getColor(R.color.table_attention));
			break;
		}
		
		return convertView;
	}

	static class ViewHolder{
		@InjectView(R.id.title) TextView title;
		@InjectView(R.id.detail) TextView detail;
		@InjectView(R.id.status) TextView status;
		@InjectView(R.id.time) TextView startDate;
		@InjectView(R.id.sessionType) ImageView sessionType;
		public ViewHolder(View view){
			ButterKnife.inject(this, view);
		}
	}
}
