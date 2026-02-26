package uk.co.epicuri.waiter.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import uk.co.epicuri.waiter.ui.CustomViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.interfaces.SessionContainer;
import uk.co.epicuri.waiter.interfaces.TakeawayDetailsListener;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.SessionType;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.printing.FakeReceiptFragment;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.AcceptTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.CancelTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.CloseSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.PayBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.RejectTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.RequestBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.UnrequestBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class TakeawayFragment extends Fragment implements OnSessionChangeListener {

    private static final int REQUEST_ORDER = 1;

    private static final String KEY_RESHOW_TAB = "reshowTab";

    private static final String TAB_ORDERS = "ORDERS";
    private static final String TAB_PAYMENTS = "PAYMENTS";
    @InjectView(R.id.progress) View pleaseWait;
    @InjectView(R.id.tabs) TabLayout tabLayout;
    @InjectView(R.id.content) View content;
    @InjectView(R.id.takeaway_view_pager) CustomViewPager pager;
    boolean iCancelledIt = false;
    private TakeawayDetailsListener listener;
    private EpicuriSessionDetail session;
    private String requestTab;
    private int reshowTab;
    private boolean ordersTabVisible = false;
    private boolean paymentsTabVisible = false;
    private AlertDialog rejected;
    TabsPagerAdapter adapter;
    public static TakeawayFragment newInstance(String sessionId) {
        TakeawayFragment frag = new TakeawayFragment();
        Bundle args = new Bundle();
        args.putString(GlobalSettings.EXTRA_SESSION_ID, sessionId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (null != savedInstanceState) {
            reshowTab = savedInstanceState.getInt(KEY_RESHOW_TAB);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (TakeawayDetailsListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int id = pager.getCurrentItem();
        outState.putInt(KEY_RESHOW_TAB, id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_takeaway, container, false);
        ButterKnife.inject(this, view);
        adapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
        content.setVisibility(View.GONE);
        pleaseWait.setVisibility(View.VISIBLE);
        refreshTabs();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SessionContainer) getActivity()).registerSessionListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((SessionContainer) getActivity()).deRegisterSessionListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ordersTabVisible = paymentsTabVisible = false;
    }

    private void refreshTabs() {

        int currentTab = pager.getCurrentItem();
        if (reshowTab >= 0) {
            currentTab = reshowTab;
        }

        boolean showPaymentsTab = (null != session && (session.getAdjustments().size() > 0 || session.isBillRequested()));

        if (reshowTab >= 0 || !ordersTabVisible || (showPaymentsTab != paymentsTabVisible)) {
            adapter.resetTabs();
            adapter.notifyDataSetChanged();
            adapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());
            pager.removeAllViews();
            pager.setAdapter(adapter);
            ordersTabVisible = paymentsTabVisible = false;
            adapter.addPage(TAB_ORDERS, SessionOrdersFragment.newInstance());
            ordersTabVisible = true;
            if (showPaymentsTab) {
                adapter.addPage(TAB_PAYMENTS, SessionPaymentsFragment.newInstance());

                if (paymentsTabVisible != showPaymentsTab) {
                    requestTab = TAB_PAYMENTS;
                }
                paymentsTabVisible = true;
            }

            if (null != requestTab) {
                pager.setCurrentItem(adapter.getPositionOfFragmentByTag(requestTab));
                requestTab = null;
            } else {
                pager.setCurrentItem(currentTab);
            }

            if (session != null) {
                // only ignore once session is loaded
                reshowTab = -1;
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (null == session || session.isRejected() || session.isDeleted()) return;

        boolean closed = session.isClosed() || session.isDeleted() || session.isRejected();

		if(closed) {
			return;
		} else if(!session.isAccepted()){
			inflater.inflate(R.menu.activity_takeaway_pending, menu);
		} else if(!session.isBillRequested()) {
			inflater.inflate(R.menu.activity_takeaway_accepted, menu);
			if (menu.findItem(R.id.menu_forceCloseSession) != null)
				menu.findItem(R.id.menu_forceCloseSession).setVisible(LocalSettings.getInstance(getActivity()).isAllowed(WaiterAppFeature.FORCE_CLOSE));

		} else if(session.getRemainingTotal().isPositive()){
			inflater.inflate(R.menu.activity_takeaway_requestbill_notpaid, menu);
			if (menu.findItem(R.id.menu_forceCloseSession) != null)
				menu.findItem(R.id.menu_forceCloseSession).setVisible(LocalSettings.getInstance(getActivity()).isAllowed(WaiterAppFeature.FORCE_CLOSE));

		} else if(!session.isPaid()){
			inflater.inflate(R.menu.activity_takeaway_requestbill_paid, menu);
		} else if(session.isPaid() && !session.isClosed()) {
			inflater.inflate(R.menu.activity_paid_takeaway, menu);
		} else {
			Log.e("Takeaway menu", "Unknown state");
		}

		if(menu.findItem(R.id.menu_addPayment) != null){
			menu.findItem(R.id.menu_addPayment)
					.setVisible(LocalSettings.getInstance(getActivity())
							.isAllowed(WaiterAppFeature.ADD_DELETE_PAYMENT));
		}

		if(menu.findItem(R.id.menu_addDiscount) != null){
			menu.findItem(R.id.menu_addDiscount)
					.setVisible(LocalSettings.getInstance(getActivity())
							.isAllowed(WaiterAppFeature.ADD_DELETE_DISCOUNT));
		}

		MenuItem cancel = menu.findItem(R.id.menu_cancel);
		if(null != cancel){
			// only allow cancel if the takeaway is in the future
			cancel.setVisible(session.getExpectedTime().after(Calendar.getInstance().getTime()));
		}

		/*
		MenuItem requestBill = menu.findItem(R.id.menu_requestbill);
		MenuItem printBill = menu.findItem(R.id.menu_print);
		MenuItem payBill = menu.findItem(R.id.menu_paybill);
		MenuItem forceCloseSesson = menu.findItem(R.id.menu_forceCloseSession);
		MenuItem closeSession = menu.findItem(R.id.menu_closeSession);
		MenuItem editTakeaway = menu.findItem(R.id.menu_edit);
		MenuItem cancelTakeaway = menu.findItem(R.id.menu_cancel);
		MenuItem addPayment = menu.findItem(R.id.menu_addPayment);
		MenuItem addDiscount = menu.findItem(R.id.menu_addDiscount);

		if(pending){
			menu.setGroupVisible(R.id.menugroup_notaccepted, true);
			for(MenuItem mi: new MenuItem[]{requestBill, printBill, payBill, forceCloseSesson, closeSession, editTakeaway, addDiscount, addPayment}){
				mi.setVisible(false);
			}
			return;
		} */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_paybill:
                payBill();
                return true;
            case R.id.menu_requestbill:
                requestBill();
                return true;
            case R.id.menu_print: {
//			new ReceiptPrintTask(this, "TCP:192.168.102.250", "").execute(session);
                FakeReceiptFragment f = FakeReceiptFragment.newInstance(((EpicuriBaseActivity) getActivity()).getLoggedInUser().getName(), session);
                f.show(getFragmentManager(), null);
                return true;
            }
            case R.id.menu_edit: {
                if (session.isBillRequested()) {
                    new AlertDialog.Builder(getActivity()).setTitle("Unlock session?")
                            .setMessage("This will reopen the session for editing")
                            .setPositiveButton("Unlock session", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    unlockSession(true);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    listener.editTakeawayDetails(session);
                }
                return true;
            }
            case R.id.menu_cancel: {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.cancelTakeaway_title)
                        .setMessage(R.string.cancelTakeaway_message)
                        .setPositiveButton("Cancel Takeaway",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        cancelTakeaway();
                                    }
                                })
                        .setNegativeButton("Do nothing", null)
                        .show();
                return true;
            }
            case R.id.menu_closeSession: {
                closeSession(false);
                return true;
            }
            case R.id.menu_forceCloseSession: {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.forceCloseTakeaway_title)
                        .setMessage(R.string.forceCloseTakeaway_message)
                        .setPositiveButton(R.string.forceCloseTakeaway_close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeSession(false);
                            }
                        })
                        .setNeutralButton(R.string.forceCloseTakeaway_closeUnfulfilled, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                closeSession(true);
                            }
                        })
                        .setNegativeButton(R.string.forceCloseTakeaway_leave, null)
                        .show();
                return true;
            }
            case R.id.menu_accept: {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.acceptTakeaway_title)
                        .setMessage(getString(R.string.acceptTakeaway_message, session.getName(), LocalSettings.getDateFormatWithDate().format(session.getExpectedTime())))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                acceptTakeaway(session.getId());
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
            case R.id.menu_reject: {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_takeawayreject, null, false);
                final EditText rejectMessage = (EditText) view.findViewById(R.id.rejectmessage);
                rejectMessage.setSelectAllOnFocus(true);
                rejectMessage.setText(session.getRejectedReason());
                new AlertDialog.Builder(getActivity()).setTitle("Reject takeaway with message")
                        .setView(view)
                        .setTitle("Reject this takeaway")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Reject", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rejectTakeaway(session.getId(), rejectMessage.getText());
                            }
                        }).show();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSessionChanged(EpicuriSessionDetail session) {
        this.session = session;
        if (session.isDeleted()) {
            if (null == rejected && !iCancelledIt) {
                rejected = new AlertDialog.Builder(getActivity()).setTitle("Takeaway has been cancelled").setMessage("This takeaway has been cancelled so cannot be edited further").show();
            }
            getActivity().invalidateOptionsMenu();
        } else if (session.isRejected()) {
            if (null == rejected)
                rejected = new AlertDialog.Builder(getActivity()).setTitle("Takeaway has been rejected").setMessage("This takeaway has been rejected so cannot be edited further").show();
            getActivity().invalidateOptionsMenu();
        }


        TextView tv;
        View view = getView();

        tv = (TextView) view.findViewById(R.id.name_text);
        tv.setText(session.getName());

        view.findViewById(R.id.epicuriCustomer_image).setVisibility(session.getTakeawayDiner().getEpicuriCustomer() != null ? View.VISIBLE : View.GONE);

        tv = ((TextView) view.findViewById(R.id.notes_text));
        if (TextUtils.isEmpty(session.getMessage())) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(session.getMessage());
            tv.setVisibility(View.VISIBLE);
        }

        tv = (TextView) view.findViewById(R.id.time_text);
        tv.setText("Due: " + LocalSettings.getDateFormatWithDate().format(session.getExpectedTime()));

        tv = (TextView) view.findViewById(R.id.address_text);
        if (null == session.getDeliveryAddress()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(session.getDeliveryAddress().toString());
        }

        tv = (TextView) view.findViewById(R.id.phoneNumber_text);
        if (null == session.getDeliveryPhoneNumber()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(session.getDeliveryPhoneNumber());
        }

        String message = session.getRejectedReason();
        if (null == message) message = "Unknown reason";
        if (session.isRejected()) {
            view.findViewById(R.id.notAccepted_layout).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.status_text)).setText("Rejected");
            ((TextView) view.findViewById(R.id.notAcceptedReason_text)).setText(message);
        } else if (!session.isAccepted()) {
            view.findViewById(R.id.notAccepted_layout).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.status_text)).setText("Pending");
            ((TextView) view.findViewById(R.id.notAcceptedReason_text)).setText(message);
        } else {
            view.findViewById(R.id.notAccepted_layout).setVisibility(View.GONE);
            if (session.isDeleted()) {
                ((TextView) view.findViewById(R.id.status_text)).setText("Cancelled");
            } else if (session.isClosed()) {
                ((TextView) view.findViewById(R.id.status_text)).setText("Completed: " + LocalSettings.getDateFormatWithDate().format(session.getClosedTime()));
            } else {
                ((TextView) view.findViewById(R.id.status_text)).setText("Accepted");
            }
        }

//		Money totalCost = session.getTotal();
//		if(null != session.getDeliveryCost()){
//			totalCost = totalCost.plus(session.getDeliveryCost());
//		}
//		((TextView)view.findViewById(R.id.orderTotal)).setText("Total: " + LocalSettings.formatMoneyAmount(totalCost, true));

        tv = (TextView) view.findViewById(R.id.takeawayType_text);
        tv.setText(session.getType() == SessionType.COLLECTION ? R.string.takeaway_collection : R.string.takeaway_delivery);

        pleaseWait.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);

        refreshTabs();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ORDER) {
            if (resultCode == Activity.RESULT_OK) {
                // work out whether this is within the takeaway min time
                EpicuriRestaurant r = LocalSettings.getInstance(getActivity()).getCachedRestaurant();
                int takeawayMinTime = Integer.parseInt(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_TAKEAWAYMINIMUMTIME));
                Calendar now = Calendar.getInstance();
                now.add(Calendar.MINUTE, takeawayMinTime);
                Date expectedTime = session.getExpectedTime();
                boolean takeawayTooSoon = now.getTimeInMillis() > expectedTime.getTime();
                if (takeawayTooSoon) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Takeaway modified")
                            .setMessage(getString(R.string.takeaway_orderChanged, takeawayMinTime))
                            .setPositiveButton("Dismiss", null)
                            .show();
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void closeSession(boolean blackMark) {
        WebServiceTask task = new WebServiceTask(getActivity(), new CloseSessionWebServiceCall(session.getId(), blackMark), true);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if (null != listener) {
                    listener.closeTakeaway();
                }
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    private void cancelTakeaway() {
        WebServiceTask task = new WebServiceTask(getActivity(), new CancelTakeawayWebServiceCall(session.getId()), true);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                iCancelledIt = true;

                // work out whether this is within the takeaway min time
                EpicuriRestaurant r = LocalSettings.getInstance(getActivity()).getCachedRestaurant();
                int takeawayLockWindow = Integer.parseInt(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_TAKEAWAYLOCKWINDOW));
                Calendar now = Calendar.getInstance();
                now.add(Calendar.MINUTE, takeawayLockWindow);
                Date expectedTime = session.getExpectedTime();
                boolean takeawayTooSoon = now.getTimeInMillis() > expectedTime.getTime();

                if (takeawayTooSoon) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.takeawaylockcancel_title)
                            .setMessage(getString(R.string.takeawaylockcancel_message, takeawayLockWindow))
                            .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((TakeawayDetailsListener) getActivity()).closeTakeaway();
                                }
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    listener.closeTakeaway();
                }
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    private void requestBill() {
        if (session.isBillRequested()) {
            throw new IllegalStateException("Bill already requested");
        }
        WebServiceTask task = new WebServiceTask(getActivity(), new RequestBillWebServiceCall(session.getId()));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                Toast.makeText(getActivity(), getString(R.string.toast_billRequested), Toast.LENGTH_SHORT).show();
                getActivity().invalidateOptionsMenu();
            }
        });
        task.execute();
    }

    private void payBill() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Mark takeaway as paid")
                .setMessage("This will mark as paid and close the takeaway")
                .setPositiveButton("Mark as paid", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        WebServiceTask task = new WebServiceTask(getActivity(), new PayBillWebServiceCall(session.getId()), true);
                        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
                        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                            @Override
                            public void onSuccess(int code, String response) {
                                Toast.makeText(getActivity(), getString(R.string.toast_markAsPaid), Toast.LENGTH_SHORT).show();

                                if (null != listener) {
                                    listener.closeTakeaway();
                                }

                            }
                        });
                        task.execute();
                    }
                })
                .setNegativeButton("Do nothing", null)
                .show();


    }

    private void unlockSession(boolean showAddOrders) {
        WebServiceTask task = new WebServiceTask(getActivity(), new UnrequestBillWebServiceCall(session.getId()));
        if (showAddOrders) {
            task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

                @Override
                public void onSuccess(int code, String response) {
                    if (null != listener) {
                        listener.editTakeawayDetails(session);
                    }
                }
            });
        }
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    private void acceptTakeaway(String takeawayId) {
        AcceptTakeawayWebServiceCall call = new AcceptTakeawayWebServiceCall(takeawayId);
        WebServiceTask task = new WebServiceTask(getActivity(), call);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    private void rejectTakeaway(String takeawayId, CharSequence message) {
        RejectTakeawayWebServiceCall call = new RejectTakeawayWebServiceCall(takeawayId, message);
        WebServiceTask task = new WebServiceTask(getActivity(), call);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if (null != listener) {
                    listener.closeTakeaway();
                }
            }
        });
        task.execute();
    }

}
