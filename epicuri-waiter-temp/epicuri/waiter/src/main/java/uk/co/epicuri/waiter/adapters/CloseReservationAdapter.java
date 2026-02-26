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
import uk.co.epicuri.waiter.model.EpicuriReservation;
import uk.co.epicuri.waiter.model.LocalSettings;

/**
 * Created by Home on 7/20/16.
 */
public class CloseReservationAdapter extends ArrayAdapter<EpicuriReservation> {
    private final LayoutInflater inflater;

    public CloseReservationAdapter(Context context, List<EpicuriReservation> objects, boolean accepted) {
        super(context, android.R.layout.simple_list_item_1, objects);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(null == convertView){
            convertView = inflater.inflate(R.layout.row_endofdaysession, parent, false);
            vh = new ViewHolder(convertView);

            vh.cost.setVisibility(View.GONE);
            vh.sessionType.setImageResource(R.drawable.reservation_light);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder)convertView.getTag();
        }

        EpicuriReservation res = getItem(position);
        vh.title.setText(res.getName());
        vh.arriveDate.setText(LocalSettings.niceFormat(res.getStartDate()));

        vh.id = res.getId().hashCode();

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
