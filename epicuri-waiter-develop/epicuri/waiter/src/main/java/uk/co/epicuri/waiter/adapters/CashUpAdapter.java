package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.model.EpicuriCashUp;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.CashUpActivity;

/**
 * Created by Home on 7/18/16.
 */
public class CashUpAdapter extends BaseAdapter {


    private Context context;
    private ArrayList<EpicuriCashUp> cashUps;

    public CashUpAdapter(Context context, ArrayList<EpicuriCashUp> cashUps) {
        this.context = context;
        this.cashUps = cashUps;
    }

    @Override
    public int getCount() {
        if(cashUps == null) return 0;
        return cashUps.size();
    }

    @Override
    public Object getItem(int position) {
        return cashUps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return cashUps.get(position).getId().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(null == convertView){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            vh = new ViewHolder(convertView);
            ButterKnife.inject(vh, convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        EpicuriCashUp cashUp = cashUps.get(position);
        String dates = String.format("Up to %s %s",
                CashUpActivity.dateFormat.format(cashUp.getEndTime()),
                CashUpActivity.timeFormat.format(cashUp.getEndTime()));

			 /* dateFormat.format(cashUp.getStartTime()), timeFormat.format(cashUp.getStartTime()),  */
        vh.t1.setText(dates);

        double gross = cashUp.getReport().get("GrossValue");

        vh.t2.setText(String.format(Locale.UK, "%.0f Sessions, %.0f Takeaways, Gross: %s",
                cashUp.getReport().get("SeatedSessionsCount"),
                cashUp.getReport().get("TakeawaySessionsCount"),
                LocalSettings.formatMoneyAmount(gross, true)));
        return convertView;
    }

    static class ViewHolder {
        @InjectView(android.R.id.text1)
        TextView t1;
        @InjectView(android.R.id.text2)
        TextView t2;

        public ViewHolder(View view){
            ButterKnife.inject(this, view);
        }
    }
}
