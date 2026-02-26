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
import uk.co.epicuri.waiter.model.EpicuriMenuSummary;

/**
 * Created by Home on 7/18/16.
 */
public class MenuAdapter extends BaseAdapter {

    private Context context;
    private List<EpicuriMenuSummary> menus;
    private String takeawayMenuId;

    public MenuAdapter(Context context, List<EpicuriMenuSummary> menus, String takeawayMenuId) {
        this.context = context;
        this.menus = menus;
        this.takeawayMenuId = takeawayMenuId;
    }

    @Override
    public int getCount() {
        if(null == menus) return 0;
        return menus.size();
    }

    @Override
    public Object getItem(int position) {
        return menus.get(position);
    }

    @Override
    public long getItemId(int position) {
        return menus.get(position).getId().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        if(null == convertView){
            convertView = inflater.inflate(R.layout.listitem_row_draggable, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder)convertView.getTag();
        }

        EpicuriMenuSummary menu = menus.get(position);
        vh.textview.setText(menu.getName());
        int colour = menu.isActive() ? Color.BLACK : Color.GRAY;
        vh.textview.setTextColor(colour);

        vh.takeawayIcon.setVisibility(menu.getId().equals(takeawayMenuId) ? View.VISIBLE : View.GONE);

        return convertView;
    }

    public void setTakeaway(String id){
        this.takeawayMenuId = id;
    }

    static class ViewHolder {
        @InjectView(R.id.text1)
        TextView textview;
        @InjectView(R.id.takeaway_icon)
        ImageView takeawayIcon;
        public ViewHolder(View view){
            ButterKnife.inject(this, view);
        }
    }

    public List<EpicuriMenuSummary> getMenus() {
        return menus;
    }

    public void drop(int from, int to){
        if (from != to){
            EpicuriMenuSummary menu = menus.get(from);
            menus.remove(from);
            menus.add(to, menu);
            notifyDataSetChanged();
        }
    }
}
