package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import uk.co.epicuri.waiter.R;

public class CustomDialogAdapter extends BaseAdapter {
    private String[] items;
    private LayoutInflater layoutInflater;

    public CustomDialogAdapter(Context context, String[] items){
        this.items = items;
        layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            convertView = layoutInflater.inflate(R.layout.row_custom_dialog, null);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(items[position]);
        return convertView;
    }

    static class ViewHolder{
        TextView name;
    }
}
