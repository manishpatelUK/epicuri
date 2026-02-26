package uk.co.epicuri.waiter.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.OrderAdapter;
import uk.co.epicuri.waiter.interfaces.OnItemQueuedListener;
import uk.co.epicuri.waiter.interfaces.PandingOrderListener;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.utils.GlobalSettings;

/**
 * A simple {@link Fragment} subclass.
 */
public class PendingOrderFragment extends Fragment implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, OnItemQueuedListener {
    public static ArrayList<EpicuriOrderItem> orders = new ArrayList<>();

    static final String FRAGMENT_ROW_AMEND = "RowAmend";
    private static final int REQUEST_REMOVE_ITEM = 1;

    public static final String EXTRA_PENDING_ORDERS = "uk.co.epicuri.waiter.PendingOrders";

    @InjectView(R.id.list)
    ListView ordersListView;

    private OrderAdapter orderAdapter;

    @InjectView(android.R.id.empty)
    TextView ev;
    // TODO make this a flag somehow to indicate when this is not editable
    private boolean editable = true;

    private ArrayList<EpicuriOrderItem> pendingOrders;
    private PandingOrderListener listener;
    private ArrayList<EpicuriMenu.Course> courses;
    private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups;
    private EpicuriSessionDetail session;

    public static PendingOrderFragment newInstance(ArrayList<EpicuriMenu.Course> courses, ArrayList<EpicuriMenu.ModifierGroup> modifierGroups) {
        return newInstance(null, courses, modifierGroups);
    }

    public static PendingOrderFragment newInstance(ArrayList<EpicuriOrderItem> pendingItems,
                                                   ArrayList<EpicuriMenu.Course> courses, ArrayList<EpicuriMenu.ModifierGroup> modifierGroups) {
        PendingOrderFragment frag = new PendingOrderFragment();
        Bundle args = new Bundle(3);
        args.putParcelableArrayList(GlobalSettings.EXTRA_COURSES, courses);
        args.putParcelableArrayList(GlobalSettings.EXTRA_MODIFIER_GROUPS, modifierGroups);
        if (null != pendingItems) args.putParcelableArrayList(EXTRA_PENDING_ORDERS, pendingItems);
        frag.setArguments(args);
        return frag;
    }

    public PendingOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState) {
            pendingOrders = savedInstanceState.getParcelableArrayList(EXTRA_PENDING_ORDERS);
        } else if (getArguments() != null && getArguments().containsKey(EXTRA_PENDING_ORDERS)) {
            pendingOrders = getArguments().getParcelableArrayList(EXTRA_PENDING_ORDERS);
        } else {
            pendingOrders = new ArrayList<>(0);
        }

        courses = getArguments().getParcelableArrayList(GlobalSettings.EXTRA_COURSES);
        modifierGroups = getArguments().getParcelableArrayList(GlobalSettings.EXTRA_MODIFIER_GROUPS);
    }

    public void allOrdersASAP(boolean asap) {
        if (asap) {
            if (courses != null) {
                for (EpicuriMenu.Course course : courses) {
                    if (course.getName().equals("ASAP")) {
                        for (EpicuriOrderItem order : pendingOrders) {
                            order.setASAP(course);
                        }
                        break;
                    }
                }
            }

            Toast.makeText(getContext(), R.string.asap_orders_toast, Toast.LENGTH_SHORT).show();
        } else {
            for (EpicuriOrderItem order : pendingOrders) {
                order.resetToDefault();
            }
            Toast.makeText(getContext(), R.string.default_orders_toast, Toast.LENGTH_SHORT).show();
        }
        orderAdapter.changeData(pendingOrders);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (null != getTargetFragment()) {
            if (context instanceof PandingOrderListener)
                listener = (PandingOrderListener) context;
        } else {
            if (getTargetFragment() instanceof PandingOrderListener)
                listener = (PandingOrderListener) getTargetFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        orders = pendingOrders;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        orders = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) outState.putParcelableArrayList(EXTRA_PENDING_ORDERS, pendingOrders);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_in_progress, container, false);

        ButterKnife.inject(this, view);

        orderAdapter = new OrderAdapter(getActivity());
        orderAdapter.setCombineSimilarItems(false);
        orderAdapter.changeData(pendingOrders);
        ordersListView.setAdapter(orderAdapter);
        ordersListView.setOnItemClickListener(this);
        ordersListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        ordersListView.setOnItemLongClickListener(this);
        ev.setText("No Orders Found");
        ordersListView.setEmptyView(ev);

        return view;
    }

    @Override
    public void queueItem(EpicuriOrderItem item, EpicuriSessionDetail.Diner diner) {
        if (null != diner) item.setDinerId(diner.getId());
        int position;
        try {
            if (item.getId() != null && !item.getId().equals(EpicuriOrderItem.DEFAULT_ID_VALUE)) {
                position = Integer.parseInt(item.getId());
                pendingOrders.remove(position);
                pendingOrders.add(position, item);
            } else {
                item.setId(pendingOrders.size() + "");
                pendingOrders.add(item);
                position = pendingOrders.size();
            }
        }catch (NumberFormatException e){
            item.setId(pendingOrders.size() + "");
            pendingOrders.add(item);
            position = pendingOrders.size();
        }
        orderAdapter.changeData(pendingOrders);
        ordersListView.smoothScrollToPosition(position);

        if(pendingOrders.size() == 1) {
            if (!(getActivity() instanceof QuickOrderActivity)) return;

            ((QuickOrderActivity)getActivity()).lockScreen();
        }
    }

    @Override
    public void unQueueItem(EpicuriOrderItem orderItem, EpicuriSessionDetail.Diner diner) {
        if (Integer.parseInt(orderItem.getId()) >= 0) {
            int position = Integer.parseInt(orderItem.getId());
            if(pendingOrders.size() > position)
                pendingOrders.remove(position);

            // now renumber the remaining items
            for (int i = position; i < pendingOrders.size(); i++) {
                pendingOrders.get(i).setId(i + "");
            }
            if (pendingOrders.size() == 0 && null != listener) {
                listener.OnAllItemsRemoved();
            }
            orderAdapter.changeData(pendingOrders);

            if(pendingOrders.size() == 0) {
                if (!(getActivity() instanceof QuickOrderActivity)) return;

                ((QuickOrderActivity)getActivity()).unlockScreen();
            }
        } else {
            throw new RuntimeException("cannot remove item");
        }
    }

    public void clearOrders() {
        pendingOrders.clear();
        orderAdapter.changeData(pendingOrders);

        if (!(getActivity() instanceof QuickOrderActivity)) return;

        ((QuickOrderActivity)getActivity()).unlockScreen();
    }

    public ArrayList<EpicuriOrderItem> getOrders() {
        return pendingOrders;
    }

    public void setPendingItems(ArrayList<EpicuriOrderItem> pendingOrders) {
        this.pendingOrders = pendingOrders;
        if (null != orderAdapter) orderAdapter.changeData(pendingOrders);
    }

    public void setModifierGroups(ArrayList<EpicuriMenu.ModifierGroup> modifierGroups) {
        this.modifierGroups = modifierGroups;
    }

    public void selectDiner(EpicuriSessionDetail.Diner diner) {
        orderAdapter.selectDiner(diner);
    }

    final long[] lastClickTime = {0};
    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if (SystemClock.elapsedRealtime() - lastClickTime[0] < 250){
            return;
        }
        lastClickTime[0] = SystemClock.elapsedRealtime();
        EpicuriOrderItem.GroupedOrderItem item = (EpicuriOrderItem.GroupedOrderItem) adapter.getItemAtPosition(position);
        MenuItemFragment frag = MenuItemFragment.newInstance(
                session != null ? session.getDinerFromId(item.getDinerId()) : null,
                item.getOrderItem(),
                courses,
                modifierGroups);
        frag.setTargetFragment(this, REQUEST_REMOVE_ITEM);
        frag.show(getFragmentManager(), FRAGMENT_ROW_AMEND);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final EpicuriOrderItem.GroupedOrderItem i = orderAdapter.getDisplayedOrders().get(position);
        MenuAlertFragment fragment = MenuAlertFragment.newInstance(i.getItem());
        fragment.show(((Activity) getContext()).getFragmentManager(), "dialog");
        return true;
    }
}
