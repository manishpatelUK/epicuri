package uk.co.epicuri.waiter.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;

/**
 * Created by pharris on 21/08/15.
 */
public class QuickOrderHeaderFragment extends Fragment implements OnSessionChangeListener {

    private EpicuriSessionDetail session;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quick_order_header, container, false);
        
        if(null != session) updateView(view);
        return view;
    }

    @Override
    public void onSessionChanged(EpicuriSessionDetail session) {
        this.session = session;
        if (null == getView()) return;
        updateView(getView());
    }
    
    private void updateView(View view){
        TextView tv;
        StringBuilder sb;

        tv = (TextView) view.findViewById(R.id.sessionName);
        String sessionId = session.getReadableId() == null ? session.getId() : session.getReadableId();
        sb = new StringBuilder(String.format("Bill %s", sessionId));
        if (session.isVoided()) {
            sb.append(" - VOIDED");
        } else if (session.isClosed()) {
            sb.append(" - CLOSED");
        }
        tv.setText(sb);

        tv = (TextView) view.findViewById(R.id.seated);
        sb = new StringBuilder(LocalSettings.getDateFormatWithDate().format(session.getStartTime()));
        if (session.isClosed()) {
            sb.append(" - ").append(LocalSettings.getDateFormatWithDate().format(session.getClosedTime()));
        }
        tv.setText(sb);
    }
}
