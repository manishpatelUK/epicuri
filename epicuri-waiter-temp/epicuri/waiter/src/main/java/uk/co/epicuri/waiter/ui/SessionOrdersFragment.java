package uk.co.epicuri.waiter.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.money.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.BillSplitOrderAdapter;
import uk.co.epicuri.waiter.adapters.OrderAdapter;
import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.interfaces.SessionContainer;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.CourseLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.ModifierGroupLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.EditDinerOrderWebServiceCall;
import uk.co.epicuri.waiter.webservice.SplitSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class SessionOrdersFragment extends Fragment implements OnSessionChangeListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    static final String FRAGMENT_ROW_AMEND = "RowAmend";
    private static final int LOADER_COURSES = 8;
    private static final int REQUEST_REVIEWORDERS = 1;
    private static final int LOADER_MODIFIERS = 2;
    public interface IOrderAdapter {

        void setCombineSimilarItems(boolean flag);
        void changeData(EpicuriSessionDetail sessionDetail);
        void selectDiner(EpicuriSessionDetail.Diner diner);
        Object getItem(int position);
    }

    @InjectView(android.R.id.list)
    ListView ordersListView;
    @InjectView(android.R.id.empty)
    LoaderEmptyView ev;
    @InjectView(R.id.orderQuantity)
    TextView orderQuantityText;
    @InjectView(R.id.orderTotal)
    TextView orderTotalText;
    @InjectView(R.id.subtotal)
    View subtotalRow;

    private IOrderAdapter orderAdapter;

    private EpicuriSessionDetail session;
    private ArrayList<EpicuriMenu.Course> courses = new ArrayList<>();
    private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups = new ArrayList<>();
    ArrayList<EpicuriOrderItem> pendingItems = new ArrayList<>();
    public SessionOrdersFragment() {
        // Required empty public constructor
    }

    public static SessionOrdersFragment newInstance(){
        return new SessionOrdersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sessiondetail_orders, container, false);

        ButterKnife.inject(this, view);
        if(session != null && session.isBillSplitMode() && getActivity() instanceof SeatedSessionActivity){
            orderAdapter = new BillSplitOrderAdapter(getActivity(), ((SeatedSessionActivity) getActivity()).getSelectedDiner());
        }else {
            orderAdapter = new OrderAdapter(getActivity());
        }

        orderAdapter.setCombineSimilarItems(session == null || (session!= null && !(getActivity()
                instanceof SeatedSessionActivity) || !session.isBillSplitMode()));
        ordersListView.setAdapter((BaseAdapter) orderAdapter);

        ev.setText("No Orders Found");
        ordersListView.setEmptyView(ev);
        return view;
    }

    private void loadCourses() {
        getActivity().getSupportLoaderManager().initLoader(LOADER_COURSES, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Course>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> onCreateLoader(int id,
                                                                                       Bundle args) {
                return new EpicuriLoader<ArrayList<EpicuriMenu.Course>>(getContext(), new CourseLoaderTemplate(session.getServiceId()));
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> loader,
                                       LoaderWrapper<ArrayList<EpicuriMenu.Course>> data) {
                if(null == data){ // nothing returned, ignore
                    return;
                }else if(data.isError()){
                    Toast.makeText(getContext(), "Error loading courses", Toast.LENGTH_SHORT).show();
                    return;
                }
                courses = new ArrayList<EpicuriMenu.Course>();
                for(EpicuriMenu.Course c: data.getPayload()){
                    if(c.getServiceId().equals(session.getServiceId())){
                        courses.add(c);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> loader) {
            }

        });
    }

    private void loadModifiers() {
        getActivity().getSupportLoaderManager().restartLoader(LOADER_MODIFIERS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> onCreateLoader(
                    int id, Bundle args) {
                return new EpicuriLoader<ArrayList<EpicuriMenu.ModifierGroup>>(getContext(), new ModifierGroupLoaderTemplate());
            }

            @Override
            public void onLoadFinished(
                    Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> loader,
                    LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>> data) {
                if(null == data){ // nothing returned, ignore
                    return;
                }else if(data.isError()){
                    Toast.makeText(getContext(), "Error loading modifiers", Toast.LENGTH_SHORT).show();
                    return;
                }
                modifierGroups = data.getPayload();
            }

            @Override
            public void onLoaderReset(
                    Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> loader) {

            }
        });
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

    public void selectDiner(EpicuriSessionDetail.Diner diner) {
        orderAdapter.selectDiner(diner);

        String dishesOrdered, orderTotal;
        if (session.isBillSplitMode() && getActivity() instanceof SeatedSessionActivity && diner !=
                null) {
            dishesOrdered = getString(R.string.orderQuantityBillSplit, diner.getOrders().length);
            orderTotal = "Subtotal: " + LocalSettings.formatMoneyAmount(
                    getDinerSubtotal(diner), true);
        } else {
            String deliveryCostString = null;

            if (session.getType() == EpicuriSessionDetail.SessionType.DELIVERY) {
                Money deliveryCost = session.getDeliveryCost();
                if (deliveryCost == null) {
                    deliveryCostString = "Unknown delivery cost";
                } else if (deliveryCost.isZero()) {
                    deliveryCostString = "Free Delivery";
                } else {
                    deliveryCostString = "Delivery cost: " + LocalSettings.formatMoneyAmount(
                            deliveryCost, true);
                }
            }

            dishesOrdered = getString(R.string.orderQuantity, session.getNumberOfDishes());
            if (null != deliveryCostString) {
                dishesOrdered += "\n" + deliveryCostString;
            }
            orderTotal = "Subtotal: " + LocalSettings.formatMoneyAmount(
                    session.getSubtotal(), true);
        }

        orderQuantityText.setText(dishesOrdered);
        orderTotalText.setText(orderTotal);
    }

    public void confirmAssignChanges() {
        EpicuriSessionDetail.Diner diner = ((SeatedSessionActivity) getActivity())
                .getSelectedDiner();

        if (!(orderAdapter instanceof BillSplitOrderAdapter) || diner == null) return;

        Set<String> selectedIds = ((BillSplitOrderAdapter)orderAdapter).getItemLookup().keySet();
        List<String> allOrders = Arrays.asList(diner.getOrders());

        if (selectedIds.isEmpty() && allOrders.isEmpty()) {
            Toast.makeText(getActivity(), "No changes to confirm.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> orderIds = new ArrayList<>(selectedIds);
        ArrayList<String> unassignedOrderIds = new ArrayList<>(allOrders);

        for (String id : selectedIds) {
            for (String existingId : allOrders) {
                if (existingId.equals(id)) {
                    orderIds.remove(id);
                    unassignedOrderIds.remove(existingId);
                    break;
                }
            }
        }

        if (orderIds.isEmpty() && unassignedOrderIds.isEmpty()) {
            Toast.makeText(getActivity(), "No changes to confirm.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditDinerOrderWebServiceCall call = new EditDinerOrderWebServiceCall(session.getId(),
                diner.getId(), orderIds, unassignedOrderIds);
        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    public void deselectAll() {
        EpicuriSessionDetail.Diner diner = ((SeatedSessionActivity) getActivity())
                .getSelectedDiner();

        if (!(orderAdapter instanceof BillSplitOrderAdapter) || diner == null) return;

        List<String> allOrders = Arrays.asList(diner.getOrders());

        if (allOrders.isEmpty()) {
            Toast.makeText(getActivity(), "No items to deselect.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditDinerOrderWebServiceCall call = new EditDinerOrderWebServiceCall(session.getId(),
               diner.getId(),  new ArrayList<String>(0), new ArrayList<>(allOrders));
        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override public void onSuccess(int code, String response) {
                ((BillSplitOrderAdapter) orderAdapter).getItemLookup().clear();
            }
        });
        task.execute();
    }

    public void ordersToTab() {
        EpicuriSessionDetail.Diner diner = ((SeatedSessionActivity) getActivity())
                .getSelectedDiner();

        if (!(orderAdapter instanceof BillSplitOrderAdapter) || diner == null) return;

        Set<String> selectedIds = ((BillSplitOrderAdapter)orderAdapter).getItemLookup().keySet();

        if (selectedIds.isEmpty()) {
            Toast.makeText(getActivity(), "No items to push.", Toast.LENGTH_SHORT).show();
            return;
        }

        SplitSessionWebServiceCall call = new SplitSessionWebServiceCall(session.getId(),
                new ArrayList<>(selectedIds));
        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override public void onSuccess(int code, String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String name = jsonObject.getString("name");
                    Toast.makeText(getActivity(), "Selected items pushed onto new tab: " + name, Toast
                            .LENGTH_LONG).show();
                } catch (JSONException ex) {
                    Toast.makeText(getActivity(), "Selected items pushed onto new tab", Toast
                            .LENGTH_SHORT).show();
                }
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
            }
        });
        task.execute();
    }

    @Override
    public void onSessionChanged(EpicuriSessionDetail session) {
        this.session = session;
        if(session!= null && courses.isEmpty() && modifierGroups.isEmpty()) {
            loadCourses();
            loadModifiers();
        }

        if (orderAdapter instanceof OrderAdapter && session.isBillSplitMode() && getActivity()
        instanceof SeatedSessionActivity) {
            orderAdapter = new BillSplitOrderAdapter(getActivity(), ((SeatedSessionActivity) getActivity()).getSelectedDiner());
            ordersListView.setAdapter((BaseAdapter)orderAdapter);
        }

        if (orderAdapter instanceof BillSplitOrderAdapter && !session.isBillSplitMode()) {
            orderAdapter = new OrderAdapter(getActivity());
            orderAdapter.setCombineSimilarItems(true);
        }

        ((LoaderEmptyView) ordersListView.getEmptyView()).setDataLoaded();

        String deliveryCostString = null;
        if (session.getType() == EpicuriSessionDetail.SessionType.DELIVERY) {
            Money deliveryCost = session.getDeliveryCost();
            if (deliveryCost == null) {
                deliveryCostString = "Unknown delivery cost";
            } else if (deliveryCost.isZero()) {
                deliveryCostString = "Free Delivery";
            } else {
                deliveryCostString = "Delivery cost: " + LocalSettings.formatMoneyAmount(
                        deliveryCost, true);
            }
        }

        String dishesOrdered, orderTotal;
        if (session.isBillSplitMode() && getActivity() instanceof SeatedSessionActivity && (
                (SeatedSessionActivity) getActivity()).getSelectedDiner() != null) {
            EpicuriSessionDetail.Diner diner = ((SeatedSessionActivity) getActivity())
                    .getSelectedDiner();
            dishesOrdered = getString(R.string.orderQuantityBillSplit, diner.getOrders().length);
            orderTotal = "Subtotal: " + LocalSettings.formatMoneyAmount(
                    getDinerSubtotal(diner), true);

            orderQuantityText.setText(dishesOrdered);
            orderTotalText.setText(orderTotal);
        } else {
            dishesOrdered = getString(R.string.orderQuantity, session.getNumberOfDishes());
            if (null != deliveryCostString) {
                dishesOrdered += "\n" + deliveryCostString;
            }
            orderTotal = "Subtotal: " + LocalSettings.formatMoneyAmount(
                    session.getSubtotal(), true);
        }
        orderQuantityText.setText(dishesOrdered);
        orderTotalText.setText(orderTotal);

        // seated sessions can remove items from the order or adjust tip
        if (session.getType() == EpicuriSessionDetail.SessionType.DINE)
        {
            ordersListView.setOnItemClickListener(this);
            ordersListView.setOnItemLongClickListener(this);
        }

        orderAdapter.changeData(session);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!session.isBillSplitMode() || (getActivity() instanceof SeatedSessionActivity && (
                (SeatedSessionActivity) getActivity()).getSelectedDiner() == null)) {
            EpicuriOrderItem.GroupedOrderItem item =
                    (EpicuriOrderItem.GroupedOrderItem) orderAdapter.getItem(position);
            if (!session.isClosed() && !session.isPaid() && item.getCalculatedPrice().isPositive()) {
                if(session.isBillSplitMode()) {
                    Toast.makeText(getActivity(), "Choose a guest then choose items to assign", Toast.LENGTH_LONG).show();
                } else {
                    RemoveOrderItemFragment frag = RemoveOrderItemFragment.newInstance(item.getGroupedItems());
                    frag.show(getFragmentManager(), FRAGMENT_ROW_AMEND);
                }
            }
            return;
        }

        if (session.isBillSplitMode() && orderAdapter instanceof BillSplitOrderAdapter) {
            ((BillSplitOrderAdapter)orderAdapter).onItemSelected(position);
        }
    }

    @Override public boolean onItemLongClick(AdapterView<?> adapterView, View view, int longClickPosition, long l) {
        if ((!session.isBillSplitMode() || !(orderAdapter instanceof BillSplitOrderAdapter)) && !session.isBillRequested()) {
            ordersListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            ordersListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int p, long id, boolean checked) {
                    final EpicuriOrderItem selectedItem = ((OrderAdapter) orderAdapter).getDisplayedOrders().get(p).getOrderItem();
                    if (checked) {
                        pendingItems.add(selectedItem);
                    } else {
                        pendingItems.remove(selectedItem);
                    }
                }

                @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getActivity().getMenuInflater().inflate(R.menu.action_reorder, menu);
                    return true;
                }

                @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    //To make id's compatible with pending order id's
                    for (int i = 0; i < pendingItems.size(); i++){
                        pendingItems.get(i).setId(i+"");
                    }

                    Intent reviewOrdersIntent = new Intent(getContext(), SubmitOrderActivity.class);
                    reviewOrdersIntent.putExtra(SubmitOrderActivity.EXTRA_PENDING_ORDERS, pendingItems);
                    reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, session.getId());
                    reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_MODIFIER_GROUPS, modifierGroups);
                    reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_COURSES, courses);
                    reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_ENFORCE_LIMITS, getActivity().getIntent().getBooleanArrayExtra(GlobalSettings.EXTRA_ENFORCE_LIMITS));
                    startActivityForResult(reviewOrdersIntent, REQUEST_REVIEWORDERS);
                    pendingItems.clear();
                    return false;
                }

                @Override public void onDestroyActionMode(ActionMode mode) {
                    ordersListView.clearChoices();
                }
            });
            ordersListView.setItemChecked(longClickPosition, true);
            return true;
        }


        EpicuriOrderItem.GroupedOrderItem item = (EpicuriOrderItem.GroupedOrderItem) orderAdapter.getItem(longClickPosition);
        if (!session.isClosed() && !session.isPaid() && item.getCalculatedPrice().isPositive()) {
            if(session.isBillSplitMode()) {
                Toast.makeText(getActivity(), "Choose a guest then choose items to assign", Toast.LENGTH_LONG).show();
            } else {
                RemoveOrderItemFragment frag = RemoveOrderItemFragment.newInstance(
                        item.getGroupedItems());
                frag.show(getFragmentManager(), FRAGMENT_ROW_AMEND);
            }
        }
        return true;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(getActivity() != null && ordersListView != null){
            ordersListView.clearChoices();
            ordersListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        }
    }

    private Money getDinerSubtotal(EpicuriSessionDetail.Diner diner) {

        EpicuriRestaurant restaurant = LocalSettings.getInstance(getActivity())
                .getCachedRestaurant();
        Money subtotal = Money.of(restaurant.getCurrency(), 0.0);

        if (session == null) return subtotal;

        for (String dinerId : diner.getOrders()) {
            for (EpicuriOrderItem item : session.getOrders()) {
                if (item.getId().equals(dinerId)) {
                    subtotal = subtotal.plus(item.getCalculatedPriceIncludingQuantity());
                    break;
                }
            }
        }

        return subtotal;
    }
}
