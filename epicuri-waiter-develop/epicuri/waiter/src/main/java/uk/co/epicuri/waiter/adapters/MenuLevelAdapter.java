package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.menueditor.MenuLevelFragment;

/**
 * Created by Home on 7/18/16.
 */
public class MenuLevelAdapter extends BaseAdapter {

    private Context context;
    private int layoutId;
    private ArrayList<EpicuriMenu.MenuLevel> rows;
    private ActionMode editRowActionMode;

    public MenuLevelAdapter(Context context, int layoutId, ArrayList<EpicuriMenu.MenuLevel> rows, ActionMode editRowActionMode){
        this.context = context;
        this.layoutId = layoutId;
        this.rows = rows;
        this.editRowActionMode = editRowActionMode;
    }

    static class ViewHolder {
        @InjectView(R.id.text1)
        TextView textview;
        @Optional
        @InjectView(R.id.drag_handle)
        View dragHandle;
        public ViewHolder(View view){
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public int getCount() {
        if(null == rows) return 0;
        return rows.size();
    }

    @Override
    public Object getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rows.get(position).getId().hashCode();
    }

    public void drop(int from, int to) {
        if (from != to) {
            EpicuriMenu.MenuLevel item = rows.get(from);
            rows.remove(item);
            rows.add(to, item);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuLevelAdapter.ViewHolder vh;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        if(null == convertView){
            convertView = inflater.inflate(layoutId, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder)convertView.getTag();
        }

        EpicuriMenu.MenuLevel level = rows.get(position);
        vh.textview.setText(level.getName());
        TextView price = convertView.findViewById(R.id.price);
        if(price != null) {
            if (level instanceof EpicuriMenu.Item) {
                price.setVisibility(View.VISIBLE);
                price.setText(LocalSettings.formatMoneyAmount(((EpicuriMenu.Item) level).getPrice(), true));
            } else {
                price.setVisibility(View.GONE);
            }
        }
        if(level.getType() == MenuLevelFragment.Level.ITEM){
            int colour;
            if( ((EpicuriMenu.Item)level).isUnavailable()){
                colour = 0xff888888;
            } else {
                colour = 0xff000000;
            }
            vh.textview.setTextColor(colour);
        }

        if(null != vh.dragHandle){
            vh.dragHandle.setVisibility(editRowActionMode == null ? View.VISIBLE : View.GONE);
        }
        return convertView;
    }

}
