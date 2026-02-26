package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.SendEmailHandler;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.webservice.SendBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

/**
 * Created by manish on 28/02/2018.
 */

public class SendEmailHandlerImpl implements SendEmailHandler {
    private final Context context;
    private final EpicuriSessionDetail session;

    public SendEmailHandlerImpl(Context context, EpicuriSessionDetail session) {
        this.context = context;
        this.session = session;
    }

    @Override
    public void sendEmail() {
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_bill_send_email, null);
        final SendEmailDialogViewHolder holder = new SendEmailDialogViewHolder(dialogView);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(context.getString(R.string.send_via_email), null)
                .setNegativeButton("Cancel", null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        View positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {

            private void sendEmail(WebServiceCall webServiceCall, final AlertDialog dialog) {
                WebServiceTask task = new WebServiceTask(context, webServiceCall);
                task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                    @Override
                    public void onSuccess(int code, String response) {
                        Toast.makeText(context, "Email sent", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
                task.setIndicatorText("Send email");
                task.execute();
            }

            private boolean validateEmailData(SendEmailDialogViewHolder holder) {
                holder.emailEdit.setError(null);
                if (holder.emailEdit.getText().toString().isEmpty() ||
                        !Patterns.EMAIL_ADDRESS.matcher(holder.emailEdit.getText().toString()).matches()) {
                    holder.emailEdit.setError("This doesn't look like an email address");
                    return false;
                }
                return true;

            }

            @Override
            public void onClick(View view) {
                if (validateEmailData(holder) && session != null) {
                    SendBillWebServiceCall emailWebServiceCall = new SendBillWebServiceCall(
                            holder.emailEdit.getText().toString(),
                            session.getId()
                    );
                    sendEmail(emailWebServiceCall, dialog);
                }
            }
        });
    }
}
