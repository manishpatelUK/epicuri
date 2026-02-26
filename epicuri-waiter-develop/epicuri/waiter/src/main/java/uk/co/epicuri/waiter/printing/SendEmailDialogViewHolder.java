package uk.co.epicuri.waiter.printing;

import android.view.View;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;

/**
 * Created by manish on 28/02/2018.
 */

public class SendEmailDialogViewHolder {
        @InjectView(R.id.email_edit)
        EditText emailEdit;

        SendEmailDialogViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
}
