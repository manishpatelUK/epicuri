package uk.co.epicuri.waiter.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.ModifierGroupLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierGroup;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.interfaces.OnEpicuriMenuItemsSelectedListener;
import uk.co.epicuri.waiter.interfaces.OnItemQueuedListener;
import uk.co.epicuri.waiter.interfaces.TakeawayOrderListener;
import uk.co.epicuri.waiter.adapters.OrderAdapter;


public class TakeawayOrderFragment extends Fragment implements
        OnItemQueuedListener, OnEpicuriMenuItemsSelectedListener {

    private static final int LOADER_MODIFIERS = 4;

    private static final int REQUEST_REVIEWORDERS = 1;
    private static final int REQUEST_ADDITEM = 2;
    private static final int REQUEST_MENU_CHOOSER = 3;

    private static final String FRAGMENT_MENU = "menu";

    private EpicuriOrderItem pendingItem = null;
    private ViewGroup pendingItemView;
    private OrderAdapter.MenuItemViewHolder pendingItemViewHolder;

    private DinerChooserFragment dinerChooserFragment;

    private ArrayList<EpicuriMenu.Course> courses;

    private ArrayList<EpicuriOrderItem> pendingItems;
    private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups;

    private long clickOneTime = 0;

//	public static TakeawayOrderFragment newInstance(EpicuriSessionDetail session){
//		TakeawayOrderFragment frag = new TakeawayOrderFragment();
//		Bundle args = new Bundle();
//		args.putParcelable(GlobalSettings.EXTRA_SESSION, session);
//		frag.setArguments(args);
//		return frag;
//	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        courses = new ArrayList<EpicuriMenu.Course>();
        courses.add(EpicuriMenu.Course.getDummyCourse("Takeaway"));

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        pendingItems = ((TakeawayOrderListener) getActivity()).getOrder();
    }

    @Override
    public void onPause() {
        ((TakeawayOrderListener) getActivity()).setOrder(pendingItems);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_ordering, container, false);

        v.findViewById(R.id.dinerChooserPlaceholder).setVisibility(View.GONE);

        pendingItemView = (ViewGroup) v.findViewById(R.id.orderItem);
        pendingItemViewHolder = new OrderAdapter.MenuItemViewHolder(pendingItemView);

        pendingItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == modifierGroups) return;
                MenuItemFragment f = MenuItemFragment.newInstance(EpicuriSessionDetail.Diner.getDummyDiner(), pendingItem, courses, modifierGroups);
                f.setTargetFragment(TakeawayOrderFragment.this, REQUEST_ADDITEM);
                f.show(getFragmentManager(), "menuitem");
            }
        });

        String takeawayMenuId = LocalSettings.getInstance(getActivity()).getCachedRestaurant().getTakeawayMenuId();

        MenuFragment f = MenuFragment.newInstance(takeawayMenuId, false);
        getFragmentManager().beginTransaction()
                .replace(R.id.menuFrame, f, FRAGMENT_MENU)
                .commit();
        f.setTargetFragment(this, REQUEST_MENU_CHOOSER);


        getLoaderManager().initLoader(LOADER_MODIFIERS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<ModifierGroup>>> onCreateLoader(
                    int id, Bundle args) {
                return new EpicuriLoader<ArrayList<ModifierGroup>>(getActivity(), new ModifierGroupLoaderTemplate());
            }

            @Override
            public void onLoadFinished(
                    Loader<LoaderWrapper<ArrayList<ModifierGroup>>> loader,
                    LoaderWrapper<ArrayList<ModifierGroup>> data) {
                if (null == data) { // nothing returned, ignore
                    return;
                } else if (data.isError()) {
                    Toast.makeText(getActivity(), "TakeawayOrderFragment error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                modifierGroups = data.getPayload();
            }

            @Override
            public void onLoaderReset(
                    Loader<LoaderWrapper<ArrayList<ModifierGroup>>> loader) {

            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO: store these in parent activity
        outState.putParcelableArrayList(SubmitOrderActivity.EXTRA_PENDING_ORDERS, pendingItems);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_ordering, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem reviewSendMenuItem = menu.findItem(R.id.menu_reviewOrders);
        reviewSendMenuItem.setEnabled(pendingItems.size() > 0);
        reviewSendMenuItem.setTitle(String.format(Locale.UK, "Done Adding (%d items)", pendingItems.size()));

        MenuItem dinersMenuItem = menu.findItem(R.id.menu_showDiners);
        if (null == dinerChooserFragment) {
            dinersMenuItem.setVisible(false);
            menu.findItem(R.id.menu_dinerDetail).setVisible(false);
        } else {
            View v = getView().findViewById(R.id.dinerChooserPlaceholder);
            dinersMenuItem.setTitle(v.getVisibility() == View.GONE ? R.string.menu_perGuest : R.string.menu_allItems);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_showDiners: {
                View v = getView().findViewById(R.id.dinerChooserPlaceholder);
                v.setVisibility(v.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                getActivity().invalidateOptionsMenu();
                return true;
            }
            case R.id.menu_reviewOrders: {
                pendingItemView.setVisibility(View.GONE);
                checkMoneyThenValidateOrders();
                return true;
            }
            case R.id.menu_dinerDetail: {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Diner Details")
                        .setMessage("Some information about the Diner")
                        .show();
                return true;
            }
            case android.R.id.home: {
                validateExit();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkMoneyThenValidateOrders() {
        EpicuriRestaurant r = LocalSettings.getInstance(getActivity()).getCachedRestaurant();
        double maxTakeaway = Double.parseDouble(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_MAXTAKEAWAYVALUE));
        double minTakeaway = Double.parseDouble(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_MINTAKEAWAYVALUE));
        ((TakeawayOrderListener) getActivity()).finishAdding();
    }

    @Override
    public void onEpicuriMenuItemSelected(Item item) {
        Diner diner = Diner.getDummyDiner();

        EpicuriOrderItem newItem = new EpicuriOrderItem(item, null);
        if (null == newItem.getCourse() && courses.size() == 1) {
            newItem.setCourse(courses.get(0));
        }
        boolean autoAdd = true;

        if (null == newItem.getCourse()) {
            // have not been able to automatically determine course for this item in this service
            autoAdd = false;
        }
        if (autoAdd) {
            for (String modifierId : item.getModifierGroupIds()) {
                EpicuriMenu.ModifierGroup group = null;
                for (EpicuriMenu.ModifierGroup g : modifierGroups) {
                    if (g.getId().equals(modifierId)) {
                        group = g;
                        break;
                    }
                }
                if (null == group) throw new RuntimeException("Modifier not found");
                if (group.getLowerLimit() > 0) {
                    autoAdd = false;
                    break;
                }
            }
        }

        if (!autoAdd) {
            if (null == modifierGroups) return;
            DialogFragment f = MenuItemFragment.newInstance(diner, newItem, courses, modifierGroups);
            f.setTargetFragment(this, REQUEST_ADDITEM);
            f.show(getFragmentManager(), "menuitem");
        } else {
            queueItem(newItem, diner);
        }
    }

    public void setPendingItem(EpicuriOrderItem item) {
        pendingItem = item;
        if (null == item) {
            pendingItemView.setVisibility(View.GONE);
            return;
        }

        pendingItemView.setVisibility(View.VISIBLE);
        pendingItemViewHolder.show(item, false);
    }


    @Override
    public void queueItem(EpicuriOrderItem item, Diner diner) {
        item.setDinerId(diner.getId());
        if(item.getId() != null && !item.getId().equals(EpicuriOrderItem.DEFAULT_ID_VALUE)){
            int position = Integer.parseInt(item.getId());
            pendingItems.remove(position);
            pendingItems.add(position, item);
            Toast.makeText(getActivity(), "Pending item updated ", Toast.LENGTH_SHORT).show();
        } else {
            item.setId(pendingItems.size()+"");
            pendingItems.add(item);
            Toast.makeText(getActivity(), "Added to pending items", Toast.LENGTH_SHORT).show();
        }
        setPendingItem(item);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void unQueueItem(EpicuriOrderItem orderItem, Diner diner) {
        if(Integer.parseInt(orderItem.getId()) >= 0){
            int position = Integer.parseInt(orderItem.getId());
            pendingItems.remove(position);
            // now renumber the remaining items
            for(int i=position; i<pendingItems.size(); i++){
                pendingItems.get(i).setId(i+"");
            }
            Toast.makeText(getActivity(), "Removed from pending items", Toast.LENGTH_SHORT).show();
            setPendingItem(null);
        } else {
            Toast.makeText(getActivity(), "Could not remove item", Toast.LENGTH_SHORT).show();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void validateExit() {
        long now = new Date().getTime();
        if (0 == clickOneTime || (now - clickOneTime) > 4000) {
            Toast.makeText(getActivity(), "Abandon this order? Click again to exit", Toast.LENGTH_SHORT).show();
            clickOneTime = now;
        } else {
            // TODO: cope with exit
            getActivity().finish();
        }
    }
}

