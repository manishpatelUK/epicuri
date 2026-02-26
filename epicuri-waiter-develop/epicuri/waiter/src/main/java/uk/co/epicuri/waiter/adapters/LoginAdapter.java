package uk.co.epicuri.waiter.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriLogin;
import uk.co.epicuri.waiter.model.StaffRole;

/**
 * Created by Home on 7/26/16.
 */
public class LoginAdapter extends ArrayAdapter<EpicuriLogin> {

    Context context;
    int layoutResourceId;
    ArrayList<EpicuriLogin> data = null;

    public LoginAdapter(Context context, int layoutResourceId, ArrayList<EpicuriLogin> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LoginHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new LoginHolder();
            holder.name = (TextView)row.findViewById(R.id.name_text);
            holder.username = (TextView)row.findViewById(R.id.username_text);
            holder.staffRole = row.findViewById(R.id.staff_role);

            row.setTag(holder);
        }
        else
        {
            holder = (LoginHolder)row.getTag();
        }

        EpicuriLogin login = data.get(position);
        holder.name.setText(login.getName());
        holder.username.setText(login.getUsername());
        holder.staffRole.setText(StaffRole.valueOf(login.getRole()).getReadableName());

        return row;
    }

    private static class LoginHolder
    {
        TextView username;
        TextView name;
        TextView staffRole;
    }

}
