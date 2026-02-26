package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.EndOfDayListFragment;

/**
 * Created by Home on 7/20/16.
 */
public class CloseSessionAdapter extends ArrayAdapter<EpicuriSessionDetail> {
    private final LayoutInflater inflater;

    public CloseSessionAdapter(Context context, List<EpicuriSessionDetail> objects, EndOfDayListFragment.Type activeTab) {
        super(context, android.R.layout.simple_list_item_1, objects);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(null == convertView){
            convertView = inflater.inflate(R.layout.row_endofdaysession, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder)convertView.getTag();
        }

        EpicuriSessionDetail session = getItem(position);
        vh.title.setText(session.getName());

        boolean pendingTakeaway = false;
        switch(session.getType()){
            case DINE:{
                if(session.isTab()){
                    vh.sessionType.setImageResource(R.drawable.inbar_light);
                } else {
                    vh.sessionType.setImageResource(R.drawable.diner_light);
                }
                vh.arriveDate.setText(LocalSettings.niceFormat(session.getStartTime()));
                break;
            }
            case COLLECTION: {
                vh.sessionType.setImageResource(R.drawable.forcollection_light);
                vh.arriveDate.setText(LocalSettings.niceFormat(session.getExpectedTime()));
                break;
            }
            case DELIVERY: {
                vh.sessionType.setImageResource(R.drawable.forcollection_light);
                vh.arriveDate.setText(LocalSettings.niceFormat(session.getExpectedTime()));
                if(!session.isAccepted()){
                    pendingTakeaway = true;
                }
                break;
            }
        }
        String paidString = session.isPaid() ? " (Paid)" : "";
        vh.cost.setText(LocalSettings.formatMoneyAmount(session.getTotal(), true) + paidString);

        vh.id = session.getId().hashCode();

        return convertView;
    }


    static class ViewHolder{
        @InjectView(R.id.title)
        TextView title;
        @InjectView(R.id.cost) TextView cost;
        @InjectView(R.id.arriveTime) TextView arriveDate;
        @InjectView(R.id.sessionType)
        ImageView sessionType;
        int id;
        public ViewHolder(View view){
            ButterKnife.inject(this, view);
        }
    }
}

