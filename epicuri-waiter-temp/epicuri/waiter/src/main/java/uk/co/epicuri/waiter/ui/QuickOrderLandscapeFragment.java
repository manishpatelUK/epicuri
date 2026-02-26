package uk.co.epicuri.waiter.ui;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wefika.flowlayout.FlowLayout;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.CategoriesLandscapeAdapter;
import uk.co.epicuri.waiter.adapters.GroupsLandscapeAdapter;
import uk.co.epicuri.waiter.adapters.ItemsLandscapeAdapter;
import uk.co.epicuri.waiter.adapters.OrderLandscapeAdapter;
import uk.co.epicuri.waiter.adapters.PaymentsLandscapeAdapter;
import uk.co.epicuri.waiter.interfaces.OnEpicuriMenuItemsSelectedListener;
import uk.co.epicuri.waiter.interfaces.OnItemQueuedListener;
import uk.co.epicuri.waiter.interfaces.OnSessionCreationListener;
import uk.co.epicuri.waiter.interfaces.ValidWebServiceCall;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.MenuLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.MenuSummaryLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenuSummary;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.NewAdjustmentRequest;
import uk.co.epicuri.waiter.model.NumericalAdjustmentType;
import uk.co.epicuri.waiter.model.QuickOrderLandscapeState;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.printing.FakeReceiptFragment;
import uk.co.epicuri.waiter.printing.PrintUtil;
import uk.co.epicuri.waiter.printing.SendEmailHandlerImpl;
import uk.co.epicuri.waiter.service.CalculationService;
import uk.co.epicuri.waiter.service.MoneyService;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.MoneyWatcher;
import uk.co.epicuri.waiter.webservice.GetPrintersWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.NewAdjustmentWebServiceCall;
import uk.co.epicuri.waiter.webservice.PayBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.QuickOrderUnifiedWebServiceCall;
import uk.co.epicuri.waiter.webservice.QuickOrderWebServiceCall;
import uk.co.epicuri.waiter.webservice.VoidSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

import static uk.co.epicuri.waiter.ui.PendingOrderFragment.FRAGMENT_ROW_AMEND;

public class QuickOrderLandscapeFragment extends Fragment implements
        Spinner.OnItemSelectedListener, CategoriesLandscapeAdapter.ICategoriesClickListener,
        GroupsLandscapeAdapter.IGroupsClickListener, OnEpicuriMenuItemsSelectedListener,
        OnItemQueuedListener, SearchView.OnQueryTextListener,
        QuickOrderActivity.OnBackPressedListener, OrderLandscapeAdapter.SwipeItemsListener {
    private static final String LOGGER = "QOLandscapeFragment";

    private static final String NO_MENU_SELECTED = "-1";
    private static final int LOADER_MENU_SUMMARIES = 2;
    private static final int LOADER_MENU = 1;
    private static final int REQUEST_REMOVE_ITEM = 1;
    private static final String FRAGMENT_MENU_ITEM = "menuItem";

    EpicuriMenu.Course quickOrderCourse = EpicuriMenu.Course.getDummyCourse("Quick Order");
    @InjectView(R.id.menu_spinner)
    Spinner menuSpinner;
    @InjectView(R.id.category_recycler_view)
    RecyclerView categoryRecyclerView;
    @InjectView(R.id.group_recycler_view)
    RecyclerView groupRecyclerView;
    @InjectView(R.id.menu_items_recycler)
    RecyclerView menuItemsRecycler;
    @InjectView(R.id.selected_items_list_view)
    ListView ordersListView;
    @InjectView(R.id.flowLayout)
    FlowLayout flowLayout;
    @InjectView(R.id.recent_item)
    View recentItem;
    @InjectView(R.id.recent_price)
    TextView recentPrice;
    @InjectView(R.id.recent_title)
    TextView recentTitle;
    @InjectView(R.id.bill_total)
    TextView billTotal;
    @InjectView(R.id.payments)
    TextView paymentsDiscounts;
    @InjectView(R.id.bill_remaining)
    TextView billRemaining;
    @InjectView(R.id.search_view)
    SearchView search;
    @InjectView(R.id.btn_new_tab)
    Button newTabButton;
    @InjectView(R.id.btn_add)
    Button addToSessionButton;
    @InjectView(R.id.btn_void)
    Button voidButton;
    @InjectView(R.id.btn_refund)
    Button refundButton;
    @InjectView(R.id.tableLocationLabel)
    TextView tableLocationLabel;
    @InjectView(R.id.btn_print_bill)
    Button printBillButton;
    @InjectView(R.id.expand)
    CheckableImageButton expand;
    @InjectView(R.id.bill_vat)
    TextView billVat;
    @InjectView(R.id.btn_clear)
    Button clearButton;
    @InjectView(R.id.bill_prints)
    Switch billPrintsSwitch;
    @InjectView(R.id.order_prints)
    Switch orderPrintsSwitch;
    @InjectView(R.id.btn_payments)
    Button paymentsBtn;
    @InjectView(R.id.btn_discounts)
    Button discountBtn;
    @InjectView(R.id.payments_disc_container)
    View paymentsDiscContainer;
    @InjectView(R.id.quick_payments_disc_container1)
    View dynamicPaymentsContainer1;
    @InjectView(R.id.quick_payments_disc_container2)
    View dynamicPaymentsContainer2;
    @InjectView(R.id.btn_dynamic_payments1)
    Button dynamicPaymentButton1;
    @InjectView(R.id.btn_dynamic_payments2)
    Button dynamicPaymentButton2;
    @InjectView(R.id.btn_dynamic_payments3)
    Button dynamicPaymentButton3;
    @InjectView(R.id.btn_dynamic_payments4)
    Button dynamicPaymentButton4;
    @InjectView(R.id.alphabetical)
    ImageView alphabeticSortingButton;


    private String selectedMenuId;
    private SharedPreferences preferences;
    private CategoriesLandscapeAdapter categoryAdapter;
    private GroupsLandscapeAdapter groupAdapter;
    private ItemsLandscapeAdapter itemsAdapter;
    private OrderLandscapeAdapter ordersAdapter;
    private EpicuriSessionDetail currentSession;
    private ArrayList<EpicuriOrderItem> pendingOrders = new ArrayList<>(1);
    private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups;
    private EpicuriOrderItem recentOrder;
    private Handler recentHandler = new Handler();
    private CurrencyUnit currency;
    private CalculationService calculator = new CalculationService();
    private List<NewAdjustmentRequest> unsyncedDiscounts = new ArrayList<>(1);
    private boolean billPrintingOnCloseRequired = true;
    private boolean orderPrint = true;
    private boolean billPrint = true;
    private String deliveryLocation = "";
    private TableLocationSelectionDialogFragment tableLocationSelectionDialogFragment;
    private long lastItemsRefresh = System.currentTimeMillis();
    private final long itemRefreshFrequency = 1000 * 10 * 60;
    private boolean stockCountdownEnabled = false;
    private ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    private boolean changeDialogShowing = false;

    private LoaderManager.LoaderCallbacks<EpicuriMenu> menuLoaderCallback =
            new LoaderManager.LoaderCallbacks<EpicuriMenu>() {
                @Override
                public Loader<EpicuriMenu> onCreateLoader(int id, Bundle args) {
                    String menuId = args.getString(GlobalSettings.EXTRA_MENU_ID);
                    if (menuId == null || menuId.equals("-1")) {
                        throw new IllegalArgumentException("Menu not found");
                    }
                    return new OneOffLoader<>(getActivity(), new MenuLoaderTemplate(menuId));
                }

                @Override
                public void onLoadFinished(Loader<EpicuriMenu> loader, EpicuriMenu data) {
                    if (null == data) { // nothing returned, ignore
                        return;
                    }
                    final FragmentActivity activity = getActivity();
                    if(activity != null) {
                        activity.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }

                    categoryAdapter.changeData(data.getCategories());
                    deselectCategory(data.getCategories());
                    if(itemsOutOfDate() && recentOrder != null && activity != null) {
                        EpicuriMenu.Item item = findItem(data, recentOrder);
                        if(item != null && item.isUnavailable()) {
                            pendingOrders.remove(recentOrder);
                            ordersAdapter.changeData(pendingOrders);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String itemName = recentOrder.getItem().getName();
                                    Toast.makeText(activity, itemName + " is currently unavailable for ordering (out of stock)", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    lastItemsRefresh = System.currentTimeMillis();
                }

                @Override
                public void onLoaderReset(Loader<EpicuriMenu> loader) {
                    categoryAdapter.changeData(null);
                    groupAdapter.changeData(null);
                }
            };

    private static EpicuriMenu.Item findItem(EpicuriMenu data, EpicuriOrderItem recentOrder) {
        for(EpicuriMenu.Category category : data.getCategories()) {
            for(EpicuriMenu.Group group : category.getGroups()) {
                for(EpicuriMenu.Item item : group.getItems()) {
                    if(recentOrder.getItem() != null && recentOrder.getItem().getId() != null && recentOrder.getItem().getId().equals(item.getId())) {
                        return item;
                    }
                }
            }
        }

        return null;
    }

    private LoaderManager.LoaderCallbacks<? extends Object> menuSummaryLoaderCallback =
            new LoaderManager.LoaderCallbacks<LoaderWrapper<List<EpicuriMenuSummary>>>() {

                @Override
                public Loader<LoaderWrapper<List<EpicuriMenuSummary>>> onCreateLoader(int id,
                                                                                      Bundle args) {
                    return new EpicuriLoader<>(getActivity(),
                            new MenuSummaryLoaderTemplate(false));
                }

                @Override
                public void onLoadFinished(Loader<LoaderWrapper<List<EpicuriMenuSummary>>> loader,
                                           LoaderWrapper<List<EpicuriMenuSummary>> data) {
                    if (null == data || data.isError()) {
                        return;
                    }

                    String qoMenuId = LocalSettings.getInstance(getActivity()).getQuickOrderMenuId();
                    int qoMenuPosition = -1;
                    List<EpicuriMenuSummary> menus = new ArrayList<EpicuriMenuSummary>();
                    int i = 0;
                    for (EpicuriMenuSummary m : data.getPayload()) {
                        if (m.isActive()) {
                            menus.add(m);
                            if (qoMenuId != null && m.getId().equals(qoMenuId)) {
                                qoMenuPosition = i;
                            }
                        }
                        i++;
                    }

                    ArrayAdapter<EpicuriMenuSummary> menuAdapter
                            = new ArrayAdapter<EpicuriMenuSummary>(getActivity(),
                            R.layout.spinner_menutitle_white, menus);
                    menuAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    menuSpinner.setAdapter(menuAdapter);
                    if (qoMenuPosition > -1) {
                        menuSpinner.setSelection(qoMenuPosition);
                    }
                    menuSpinner.setOnItemSelectedListener(QuickOrderLandscapeFragment.this);

                    // set starting selection from passed in argument
                    for (int pos = 0; pos < menus.size(); pos++) {
                        if (menus.get(pos).getId().equals(selectedMenuId)) {
                            menuSpinner.setSelection(pos);

                            Bundle args = new Bundle();
                            args.putString(GlobalSettings.EXTRA_MENU_ID, selectedMenuId);
                            getLoaderManager().restartLoader(LOADER_MENU, args, menuLoaderCallback);

                            break;
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<LoaderWrapper<List<EpicuriMenuSummary>>> loader) {
                }

            };
    private PaymentsLandscapeAdapter.IAdjustmentListener adjustmentListener = new
            PaymentsLandscapeAdapter.IAdjustmentListener() {

                @Override
                public void onPaymentTypeSelected(final EpicuriAdjustmentType paymentType) {
                    if(isDoubleTap()) return;
                    if (getOrderedItems().size() > 0) {
                        triggerPaymentDialog(paymentType, false);
                    } else {
                        Toast.makeText(getActivity(), R.string.add_items_first, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDiscountTypeSelected(EpicuriAdjustmentType adjustmentType) {
                    if (isDoubleTap()) return;
                    if (getOrderedItems().size() > 0) {
                        triggerDiscountDialog(adjustmentType);
                    } else {
                        Toast.makeText(getActivity(), R.string.add_items_first, Toast.LENGTH_SHORT).show();
                    }
                }
            };

    private long lastTap = 0;
    private boolean isDoubleTap() {
        if(lastTap != 0 && System.currentTimeMillis() - lastTap < 500){
            return true;
        } else {
            lastTap = System.currentTimeMillis();
            return false;
        }
    }

    private Runnable hideItem = new Runnable() {
        @Override
        public void run() {
            if (recentItem != null) recentItem.setVisibility(View.GONE);
            recentOrder = null;
        }
    };

    public static QuickOrderLandscapeState quickOrderLandscapeState;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_quickorder_landscape, container);
        ButterKnife.inject(this, view);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        selectedMenuId = preferences.getString(GlobalSettings.PRED_KEY_SELECTED_MENU,
                NO_MENU_SELECTED);

        categoryAdapter = new CategoriesLandscapeAdapter(getActivity(), null);
        groupAdapter = new GroupsLandscapeAdapter(getActivity(), null);
        itemsAdapter = new ItemsLandscapeAdapter(getActivity(), null);
        categoryAdapter.setClickListener(this);
        groupAdapter.setClickListener(this);
        itemsAdapter.setListener(this);

        setupRecyclerView();

        ordersAdapter = new OrderLandscapeAdapter(getActivity());
        ordersAdapter.setListener(this);
        ordersAdapter.setSwipeItemsListener(this);
        ordersListView.setAdapter(ordersAdapter);
        search.setOnQueryTextListener(this);

        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (search.isIconified()){
                    categoryRecyclerView.setVisibility(View.VISIBLE);
                    groupRecyclerView.setVisibility(View.VISIBLE);
                }else {
                    categoryRecyclerView.setVisibility(View.GONE);
                    groupRecyclerView.setVisibility(View.GONE);
                }
            }
        });

        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean b) {
                if (!search.isIconified()){
                    categoryRecyclerView.setVisibility(View.VISIBLE);
                    groupRecyclerView.setVisibility(View.VISIBLE);
                }else {
                    categoryRecyclerView.setVisibility(View.GONE);
                    groupRecyclerView.setVisibility(View.GONE);
                }
            }
        });

        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override public boolean onClose() {

                    categoryRecyclerView.setVisibility(View.VISIBLE);
                    groupRecyclerView.setVisibility(View.VISIBLE);

                return false;
            }
        });

        billPrintsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                billPrint = isChecked;
                if(isChecked){
                    Toast.makeText(getContext(), R.string.receipt_prints_on, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.receipt_prints_off, Toast.LENGTH_SHORT).show();
                }
                LocalSettings.getInstance(getContext()).cacheBillPrint(isChecked);
            }
        });

        orderPrintsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                orderPrint = isChecked;
                if(isChecked){
                    Toast.makeText(getContext(), R.string.order_prints_on, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.order_prints_off, Toast.LENGTH_SHORT).show();
                }
                LocalSettings.getInstance(getContext()).cacheOrderPrint(isChecked);
            }
        });

        LocalSettings localSettings = LocalSettings.getInstance(getContext());
        billPrintsSwitch.setChecked(localSettings.isBillPrint());
        orderPrintsSwitch.setChecked(localSettings.isOrderPrint());
        if(localSettings.getCachedRestaurant() != null) {
            stockCountdownEnabled = localSettings.getCachedRestaurant().stockCountdownEnabled();
        }

        if(itemsAdapter != null) {
            boolean sortAlphabetically = localSettings.isOrderItemsAlphabeticallyInQO();
            if(sortAlphabetically && !itemsAdapter.isSortAlphabetically()) {
                itemsAdapter.switchSorting();
            }
            if(sortAlphabetically) {
                alphabeticSortingButton.setColorFilter(Color.GREEN);
            }
        }

        currency = LocalSettings.getInstance(getContext())
                .getCachedRestaurant().getCurrency();

        refreshButtons();
        refreshCalculations();
        triggerTableNumberDialog();

        getLoaderManager().initLoader(LOADER_MENU_SUMMARIES, null, menuSummaryLoaderCallback);

        if (getActivity() != null)
            ((QuickOrderActivity) getActivity()).setOnBackPressedListener(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GlobalSettings.EXTRA_MENU_ID, selectedMenuId);
    }

    @Override
    public void onDestroyView() {
        categoryAdapter.setClickListener(null);
        groupAdapter.setClickListener(null);
        itemsAdapter.setListener(null);
        ordersAdapter.setListener(null);
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        quickOrderLandscapeState = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        quickOrderLandscapeState = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        quickOrderLandscapeState = new QuickOrderLandscapeState(
                currentSession,
                calculator,
                pendingOrders,
                unsyncedDiscounts
        );
    }

    public void setOrders(ArrayList<EpicuriOrderItem> orders){
        pendingOrders = orders;
        ordersAdapter.changeData(pendingOrders);
    }

    public void setModifierGroups(
            ArrayList<EpicuriMenu.ModifierGroup> modifierGroups) {
        this.modifierGroups = modifierGroups;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String newMenuId = ((EpicuriMenuSummary) adapterView.getItemAtPosition(i)).getId();

        if (newMenuId == null || newMenuId.equals(selectedMenuId)) return;

        preferences.edit().putString(GlobalSettings.PRED_KEY_SELECTED_MENU, newMenuId).apply();
        selectedMenuId = newMenuId;

        Bundle args = new Bundle();
        args.putString(GlobalSettings.EXTRA_MENU_ID, selectedMenuId);
        getLoaderManager().restartLoader(LOADER_MENU, args, menuLoaderCallback);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        selectedMenuId = NO_MENU_SELECTED;
        preferences.edit().putString(GlobalSettings.PRED_KEY_SELECTED_MENU, selectedMenuId).apply();
    }

    @Override
    public void onItemSelected(EpicuriMenu.Category category) {
        if (groupAdapter != null) groupAdapter.changeData(category.getGroups());
        if (itemsAdapter != null) itemsAdapter.changeData(category.getItems());
    }

    @Override
    public void onItemSelected(EpicuriMenu.Group group) {
        if (itemsAdapter != null) itemsAdapter.changeData(group.getItems());
    }

    @Override
    public void onClearGroups(List<EpicuriMenu.Group> groups) {
        deselectGroup(groups);
    }

    @Override
    public void onClearCategories(List<EpicuriMenu.Category> categories) {
        deselectCategory(categories);
    }

    @Override
    public void onEpicuriMenuItemSelected(EpicuriMenu.Item item) {
        menuItemClicked(item);
        if(itemsOutOfDate()) {
            Bundle args = new Bundle();
            args.putString(GlobalSettings.EXTRA_MENU_ID, selectedMenuId);
            getLoaderManager().restartLoader(LOADER_MENU, args, menuLoaderCallback);
        }
    }

    private boolean itemsOutOfDate() {
        return stockCountdownEnabled && (System.currentTimeMillis() - lastItemsRefresh) > itemRefreshFrequency;
    }

    private void menuItemClicked(EpicuriMenu.Item item) {
        EpicuriOrderItem orderItem = new EpicuriOrderItem(item, quickOrderCourse);
        search.setIconified(true);
        search.clearFocus();
        boolean autoAdd = true;
        for (String modifierId : item.getModifierGroupIds()) {
            EpicuriMenu.ModifierGroup group = null;
            for (EpicuriMenu.ModifierGroup g : modifierGroups) {
                if (g.getId().equals(modifierId)) {
                    group = g;
                    break;
                }
            }

            if (null == group) {
                continue;
            }

            if (group.getLowerLimit() > 0) {
                autoAdd = false;
                break;
            }
        }

        if (autoAdd) {
            queueItem(orderItem);
        } else {
            if (null == modifierGroups) return;

            MenuItemFragment frag = MenuItemFragment.newInstance(null, orderItem, null,
                    modifierGroups);
            frag.setTargetFragment(QuickOrderLandscapeFragment.this, REQUEST_REMOVE_ITEM);
            frag.show(getActivity().getSupportFragmentManager(), FRAGMENT_MENU_ITEM);
        }
    }

    @Override
    public void queueItem(EpicuriOrderItem orderItem, EpicuriSessionDetail.Diner diner) {
        queueItem(orderItem);
    }

    @Override
    public void unQueueItem(EpicuriOrderItem orderItem, EpicuriSessionDetail.Diner diner) {
        unQueueItem(orderItem);
    }

    @Override
    public void onItemEdit(EpicuriOrderItem.GroupedOrderItem item) {
        if(isDoubleTap()) return;
        showMenuItemFragment(item);
    }

    public void onLocation(String location) {
        if(location != null && location.length() > 0) {
            this.deliveryLocation = location;
            this.tableLocationLabel.setText(getString(R.string.table_location) + " " + location);
            if(tableLocationSelectionDialogFragment != null) {
                tableLocationSelectionDialogFragment.dismiss();
                tableLocationSelectionDialogFragment = null;
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @OnClick({R.id.back, R.id.session_history, R.id.lock_screen, R.id.btn_clear, R.id.recent_item, R.id.expand, R.id.btn_void, R.id.alphabetical, R.id.takeaways})
    public void onViewClicked(View view) {
        if(isDoubleTap()) return;
        switch (view.getId()) {
            case R.id.back:
                doBack();
                break;
            case R.id.session_history:
                Intent intent = new Intent(getActivity(), SessionHistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_void:
            case R.id.btn_clear:
                if (pendingOrders.isEmpty()) {
                    this.tableLocationLabel.setText(getString(R.string.table_location));
                    this.deliveryLocation = "";
                    triggerTableNumberDialog();

                    return;
                }

                showDialogForClear(null);
                break;
            case R.id.recent_item:
                if (recentOrder == null) return;

                MenuItemFragment frag = MenuItemFragment.newInstance(
                        null,
                        recentOrder,
                        null,
                        modifierGroups);
                frag.setTargetFragment(QuickOrderLandscapeFragment.this, REQUEST_REMOVE_ITEM);
                frag.show(getFragmentManager(), FRAGMENT_ROW_AMEND);

                break;
            case R.id.expand:
                expand.toggle();
                itemsAdapter.changeExpanded();
                itemsAdapter.notifyDataSetChanged();
                break;
            case R.id.lock_screen:
                Intent lockScreenIntent = new Intent(getContext(), LockActivity.class);
                lockScreenIntent.putExtra(LockActivity.EXTRA_POPUP_SWITCH, true);
                startActivity(lockScreenIntent);
                break;
            case R.id.alphabetical:
                itemsAdapter.switchSorting();
                try {
                    LocalSettings localSettings = LocalSettings.getInstance(getContext());
                    localSettings.cacheOrderItemsAlphabeticallyInQO(itemsAdapter.isSortAlphabetically());
                } catch (Exception ex){}
                if(itemsAdapter.isSortAlphabetically()) alphabeticSortingButton.setColorFilter(Color.GREEN);
                else alphabeticSortingButton.setColorFilter(Color.WHITE);
                break;
            case R.id.takeaways:
                Intent takeawayManagerIntent = new Intent(getContext(), TakeawaysActivity.class);
                startActivity(takeawayManagerIntent);
                break;
        }
    }

    @OnClick({R.id.btn_refund})
    public void onRefundClicked(View view) {
        EpicuriRestaurant restaurant = LocalSettings.getInstance(getActivity())
                .getCachedRestaurant();
        ArrayList<EpicuriAdjustmentType> paymentTypes = restaurant.getPaymentTypes();
        if(restaurant == null || paymentTypes == null || paymentTypes.size() == 0) {
            return;
        }
        EpicuriAdjustmentType cash = null;
        for(EpicuriAdjustmentType type : paymentTypes) {
            if(type.getName() != null && type.getName().equalsIgnoreCase("cash")) {
                cash = type;
                break;
            }
        }

        triggerPaymentDialog(cash == null ? paymentTypes.get(0) : cash, true);
    }

    private void showDialogForClear(final DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Clear orders")
                .setMessage(currentSession == null ? "Are you sure?" : "Are you sure? This session will be voided.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (currentSession != null) {
                            WebServiceTask task = new WebServiceTask(getActivity(), new VoidSessionWebServiceCall(currentSession.getId(), "VOIDED BY USER", true));
                            task.execute();
                        }
                        clear();

                        if (onClickListener != null) {
                            onClickListener.onClick(dialogInterface, i);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @OnClick({R.id.btn_new_tab, R.id.btn_add, R.id.btn_print_bill})
    public void onActivityNeededViewClicked(View view) {
        if (!(getActivity() instanceof QuickOrderActivity)) return;
        if (isDoubleTap()) return;

        if (pendingOrders.isEmpty()) {
            Toast.makeText(getActivity(), R.string.add_items_first, Toast.LENGTH_SHORT).show();
            return;
        }

        final QuickOrderActivity activity = (QuickOrderActivity) getActivity();
        switch (view.getId()) {
            case R.id.btn_new_tab:
                if (currentSession == null) {
                    if (unsyncedDiscounts.size() > 0) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Pending discounts")
                                .setMessage("Discounts cannot be sent to Tab / Tables from Quick Order. Continue?")
                                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        activity.addToTab(false);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    } else {
                        activity.addToTab(false);
                    }

                } else {
                    Toast.makeText(getContext(), "Session is already locked, cannot send to tab", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_add:
                if (currentSession == null) {
                    if (unsyncedDiscounts.size() > 0) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Pending discounts")
                                .setMessage("Discounts cannot be sent to Tab / Tables from Quick Order. Continue?")
                                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        activity.addToSession();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    } else {
                        activity.addToSession();
                    }
                } else {
                    Toast.makeText(getContext(), "Session is already locked, cannot send to tab", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_print_bill:
                onBillPrintPressed();
                break;
        }
    }

    public ArrayList<EpicuriOrderItem> getOrderedItems() {
        return pendingOrders;
    }

    @OnClick(R.id.tableLocationLabel)
    public void onTableLocationClick() {
        triggerTableNumberDialog();
    }

    public void clear() {
        currentSession = null;
        pendingOrders.clear();
        ordersAdapter.changeData(pendingOrders);
        calculator.resetItems();
        calculator.resetAdjustments();
        calculator.recalculate();
        unsyncedDiscounts.clear();

        refreshCalculations();
        refreshButtons();

        if (getActivity() instanceof QuickOrderActivity) {
            ((QuickOrderActivity) getActivity()).unlockScreen();
        }
        billPrintingOnCloseRequired = true;

        this.tableLocationLabel.setText(getString(R.string.table_location));
        this.deliveryLocation = "";
        triggerTableNumberDialog();
    }

    private void triggerTableNumberDialog() {
        if(changeDialogShowing) {
            return;
        }
        // check if restaurant needs dialog, if not, return
        EpicuriRestaurant cachedRestaurant = LocalSettings.getInstance(getContext()).getCachedRestaurant();
        if(!cachedRestaurant.isForceLocationSelectionOnQO()) {
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();

        // if dialog is already up, return
        if(tableLocationSelectionDialogFragment != null) {
            tableLocationSelectionDialogFragment.dismiss();
            tableLocationSelectionDialogFragment = null;
        }

        // pop up the dialog
        tableLocationSelectionDialogFragment = TableLocationSelectionDialogFragment.newInstance(deliveryLocation);
        tableLocationSelectionDialogFragment.show(fragmentManager, "locationSelectionDialog");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        categoryRecyclerView.setLayoutManager(manager);
        categoryRecyclerView.setItemAnimator(new DefaultItemAnimator());
        categoryRecyclerView.setAdapter(categoryAdapter);

        LinearLayoutManager groupManager = new LinearLayoutManager(getActivity());
        groupManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        groupRecyclerView.setLayoutManager(groupManager);
        groupRecyclerView.setAdapter(groupAdapter);

        GridLayoutManager contentManager = new GridLayoutManager(getActivity(), 5);
        menuItemsRecycler.setLayoutManager(contentManager);
        menuItemsRecycler.setAdapter(itemsAdapter);
        menuItemsRecycler.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHOLD = 50;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isAClick(startX, endX, startY, endY)) {
                            categoryAdapter.deselect();
                        }
                        break;
                }
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }

            private boolean isAClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD);
            }
        });

        setupFlowLayout();
    }

    private void setupFlowLayout() {
        EpicuriRestaurant restaurant = LocalSettings.getInstance(getActivity())
                .getCachedRestaurant();
        final List<EpicuriAdjustmentType> types = new LinkedList<>();

        Button[] buttonArray = new Button[]{dynamicPaymentButton1, dynamicPaymentButton2, dynamicPaymentButton3, dynamicPaymentButton4};
        int quickPaymentsPopulated = 0;
        for (int i = 0; i < restaurant.getPaymentTypes().size(); i++) {
            final EpicuriAdjustmentType type = restaurant.getPaymentTypes().get(i);
            if (!type.isVisible()) {
                continue;
            }
            types.add(type);

            if(i < buttonArray.length) {
                if(type.getShortCode() != null && type.getShortCode().length()>0) {
                    buttonArray[i].setText(type.getShortCode());
                } else {
                    buttonArray[i].setText(type.getName());
                }
                buttonArray[i].setEnabled(true);
                buttonArray[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adjustmentListener.onPaymentTypeSelected(type);
                    }
                });
                quickPaymentsPopulated++;
            }
        }
        // for any quick payment buttons that are not populated, just hide them
        for (int i = quickPaymentsPopulated; i < 4; i++) {
            buttonArray[i].setVisibility(View.GONE);
        }


        if (currentSession != null && currentSession.isBillRequested()) {
            paymentsDiscContainer.setVisibility(View.GONE);
            flowLayout.setVisibility(View.VISIBLE);
            flowLayout.removeAllViews();
        } else {
            paymentsDiscContainer.setVisibility(View.VISIBLE);
            flowLayout.setVisibility(View.GONE);
        }

        if (!types.isEmpty()) {
            Button button;
            final EpicuriAdjustmentType type = restaurant.getPaymentTypes().get(0);
            if (currentSession != null && currentSession.isBillRequested()) {
                paymentsDiscContainer.setVisibility(View.GONE);
                flowLayout.setVisibility(View.VISIBLE);
                flowLayout.addView(button = getAdjustmentView(type, currentSession != null));
            } else {
                button = paymentsBtn;
            }

            if (types.size() > 1) {
                button.setText("Payments");
                addPopupMenu(button, types);
            } else button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustmentListener.onPaymentTypeSelected(type);
                }
            });
        }

        if (!restaurant.getDiscountTypes().isEmpty()) {
            final Button button;
            final EpicuriAdjustmentType type = restaurant.getDiscountTypes().get(0);
            if (currentSession != null && currentSession.isBillRequested()) {
                flowLayout.addView(button = getAdjustmentView(type, currentSession != null));
            } else {
                button = discountBtn;
            }

            if (restaurant.getDiscountTypes().size() > 1) {
                button.setText("Discounts");
                addPopupMenu(button, restaurant.getDiscountTypes());
            } else button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustmentListener.onDiscountTypeSelected(type);
                }
            });
        }
    }

    private void addPopupMenu(View button, final List<EpicuriAdjustmentType> types) {
        final PopupMenu popupMenu = new PopupMenu(button.getContext(), button);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                for (EpicuriAdjustmentType type : types) {
                    if (type.getId().hashCode() == item.getItemId()) {
                        if (type.getType() == EpicuriAdjustmentType.TYPE_PAYMENT) {
                            adjustmentListener.onPaymentTypeSelected(type);
                        } else if (type.getType() == EpicuriAdjustmentType.TYPE_DISCOUNT) {
                            adjustmentListener.onDiscountTypeSelected(type);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        for (EpicuriAdjustmentType type : types)
            popupMenu.getMenu().add(0, type.getId().hashCode(), 0,
                    type.getShortCode() != null ? type.getShortCode() : type.getName());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private AppCompatButton getAdjustmentView(EpicuriAdjustmentType adjustment, boolean fullWidth) {
        AppCompatButton text = (AppCompatButton) getLayoutInflater().inflate(R.layout.adjustment_item, flowLayout, false);
        text.setText(adjustment.getShortCode() != null ? adjustment.getShortCode() : adjustment.getName());
        int colorId = adjustment.getType() == EpicuriAdjustmentType.TYPE_PAYMENT ? R.color.qo_green : R.color.qo_red;
        ViewGroup.LayoutParams params = text.getLayoutParams();
        params.width = fullWidth ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;

        text.setLayoutParams(params);
        text.setSupportBackgroundTintList(ContextCompat.getColorStateList(getActivity(), colorId));
        return text;
    }

    private void triggerPaymentDialog(final EpicuriAdjustmentType paymentType, boolean isRefund) {
        final EpicuriSessionDetail session = currentSession;
        if (session == null && unsyncedDiscounts.size() == 0) {
            createSessionNoAdjustments(new OnSessionCreationListener() {
                @Override
                public void onSessionCreated(EpicuriSessionDetail epicuriSessionDetail) {
                    PaymentDialogFragment.newInstance(epicuriSessionDetail, paymentType.getId())
                            .show(getFragmentManager(), "Fragment Payment");
                }
            }, false, isRefund);
        } else if (session == null && unsyncedDiscounts.size() > 0) {
            createSessionWithAdjustments(new OnSessionCreationListener() {
                @Override
                public void onSessionCreated(EpicuriSessionDetail epicuriSessionDetail) {
                    PaymentDialogFragment.newInstance(epicuriSessionDetail, paymentType.getId())
                            .show(getFragmentManager(), "Fragment Payment");
                }
            }, false, isRefund);
        } else if (session != null && unsyncedDiscounts.size() == 0) {
            PaymentDialogFragment.newInstance(currentSession, paymentType.getId())
                    .show(getFragmentManager(), "Fragment Payment");
        } else if (session != null && unsyncedDiscounts.size() > 0) {
            NewAdjustmentWebServiceCall adjustmentWebServiceCall = new NewAdjustmentWebServiceCall(unsyncedDiscounts);
            WebServiceTask webServiceTask = new WebServiceTask(getContext(), adjustmentWebServiceCall);
            webServiceTask.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                @Override
                public void onSuccess(int code, String response) {
                    refreshSessionFromServer(session, new WebServiceTask.OnSuccessListener() {
                        @Override
                        public void onSuccess(int code, String response) {
                            try {
                                currentSession = new EpicuriSessionDetail(new JSONObject(response));
                                unsyncedDiscounts.clear();

                                refreshCalculations();
                                refreshButtons();

                                if (currentSession.getRemainingTotal().isZero()) {
                                    markAsPaid();
                                    clear();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            webServiceTask.execute();
        }
    }

    private void refreshSessionFromServer(EpicuriSessionDetail session, WebServiceTask.OnSuccessListener listener) {
        WebServiceTask task = new WebServiceTask(getContext(), new GetSessionWebServiceCall(session.getId()), true);
        task.setOnCompleteListener(listener);
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Toast.makeText(getContext(), "Could not get data from server", Toast.LENGTH_SHORT).show();
            }
        });
        task.execute();
    }

    private void deselectCategory(List<EpicuriMenu.Category> categories) {
        ArrayList<EpicuriMenu.Group> groups = new ArrayList<>();
        for (EpicuriMenu.Category category : categories) {
            if (category.getGroups() != null) {
                groups.addAll(category.getGroups());
            }
        }
        groupAdapter.changeData(groups);

        deselectGroup(groups);
    }

    private void deselectGroup(List<EpicuriMenu.Group> groups) {
        ArrayList<EpicuriMenu.Item> items = new ArrayList<>();
        for (EpicuriMenu.Group group : groups) {
            if (group.getItems() != null) {
                items.addAll(group.getItems());
            }
        }
        itemsAdapter.changeData(items);
    }

    private void showMenuItemFragment(EpicuriOrderItem.GroupedOrderItem item) {
        MenuItemFragment frag = MenuItemFragment.newInstance(
                null,
                item.getOrderItem(),
                null,
                modifierGroups);
        frag.setTargetFragment(QuickOrderLandscapeFragment.this, REQUEST_REMOVE_ITEM);
        frag.show(getFragmentManager(), FRAGMENT_ROW_AMEND);
    }

    public void queueItem(EpicuriOrderItem item) {
        if (currentSession != null) {
            Toast.makeText(getActivity(), "Session is locked, cannot add items", Toast.LENGTH_SHORT).show();
            return;
        }

        int position;

        if (item.getId() != null && !item.getId().equals(EpicuriOrderItem.DEFAULT_ID_VALUE)) {
            position = Integer.parseInt(item.getId());
            pendingOrders.remove(position);
            pendingOrders.add(position, item);
        } else {
            item.setId(pendingOrders.size() + "");
            pendingOrders.add(item);
            position = pendingOrders.size();
        }

        ordersAdapter.changeData(pendingOrders);
        ordersListView.smoothScrollToPosition(position);
        ordersAdapter.closeAllItems();

        showRecent(item);

        if (pendingOrders.size() == 1 && getActivity() instanceof QuickOrderActivity) {
            ((QuickOrderActivity) getActivity()).lockScreen();
        }

        refreshCalculations();
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP,150);
    }

    public void unQueueItem(EpicuriOrderItem orderItem) {
        if (currentSession != null) {
            Toast.makeText(getActivity(), "Session is locked, cannot remove items", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.parseInt(orderItem.getId()) < 0) return;

        int position = Integer.parseInt(orderItem.getId());
        pendingOrders.remove(position);

        for (int i = position; i < pendingOrders.size(); i++) {
            pendingOrders.get(i).setId(i + "");
        }

        ordersAdapter.changeData(pendingOrders);

        if (pendingOrders.size() == 0 && getActivity() instanceof QuickOrderActivity) {
            ((QuickOrderActivity) getActivity()).unlockScreen();
        }

        refreshCalculations();
    }

    private void showRecent(EpicuriOrderItem item) {
        recentOrder = item;
        recentPrice.setText(LocalSettings.formatMoneyAmount(item.getCalculatedPriceIncludingQuantity(), true));
        recentTitle.setText(item.getItem().getName() + (item.getQuantity() == 1 ? "" : (" x" + item.getQuantity())));

        recentItem.setVisibility(View.VISIBLE);
        recentHandler.removeCallbacks(hideItem);
        recentHandler.postDelayed(hideItem, 2000);
    }

    @SuppressLint("SetTextI18n")
    private void refreshCalculations() {
        calculator.resetItems();

        for (EpicuriOrderItem order : pendingOrders) {
            Money itemValue = order.getCalculatedPriceIncludingQuantity().multipliedBy(100);
            EpicuriMenu.Item.ItemType type = null;
            if (order.getItem() != null) {
                type = EpicuriMenu.Item.ItemType.fromId(order.getItem().getItemTypeId());
            }
            calculator.addItem(MoneyService.toPenniesRoundNearest(itemValue.getAmount()), type);
        }

        calculator.recalculate();

        int calculatedTotal = calculator.getCalculator().getTotal();
        int calculatedPayments = calculator.getCalculator().getPaymentsTotal();
        int calculatedRemaining = Math.max(0, calculatedTotal - calculatedPayments);
        int calculatedDiscount = calculator.getCalculator().getDiscountTotal();

        billTotal.setText(String.format(Locale.ENGLISH, "%s%.2f", currency.getSymbol(), MoneyService.toMoneyRoundNearest(calculatedTotal)));
        billRemaining.setText(String.format(Locale.ENGLISH, "%s%.2f", currency.getSymbol(), MoneyService.toMoneyRoundNearest(calculatedRemaining)));
        final String pdStr = getString(R.string.payments_discounts_pattern,
                String.format(Locale.ENGLISH, "%s%.2f", currency.getSymbol(), MoneyService.toMoneyRoundNearest(calculatedPayments)),
                String.format(Locale.ENGLISH, "%s%.2f", currency.getSymbol(), MoneyService.toMoneyRoundNearest(calculatedDiscount)));
        paymentsDiscounts.setText(Html.fromHtml(pdStr), TextView.BufferType.SPANNABLE);
        if (currentSession != null) {
            billVat.setText(LocalSettings.formatMoneyAmount(currentSession.getVatTotal(), true));
        } else {
            billVat.setText(LocalSettings.formatMoneyAmount(Money.of(LocalSettings.getCurrencyUnit(), 0D), true));
        }
    }

    private void refreshButtons() {
        newTabButton.setVisibility(currentSession == null ? View.VISIBLE : View.GONE);
        addToSessionButton.setVisibility(currentSession == null ? View.VISIBLE : View.GONE);
        voidButton.setVisibility(currentSession == null ? View.GONE : View.VISIBLE);
        clearButton.setVisibility(currentSession == null ? View.VISIBLE : View.GONE);
        EpicuriRestaurant cachedRestaurant = LocalSettings.getInstance(getContext()).getCachedRestaurant();
        if(!cachedRestaurant.isTableOnQOShown()) {
            addToSessionButton.setVisibility(View.GONE);
        }
        if(!cachedRestaurant.isTabOnQOShown()) {
            newTabButton.setVisibility(View.GONE);
        }
        if(cachedRestaurant.isForceLocationSelectionOnQO()) {
            tableLocationLabel.setVisibility(View.VISIBLE);
        }
        if((!cachedRestaurant.isRefundOnQOShown()) || !cachedRestaurant.getPermission(WaiterAppFeature.GENERIC_REFUND, false)) {
            refundButton.setVisibility(View.GONE);
        }

        setupFlowLayout();
    }

    public void addPayment(double amount, EpicuriAdjustmentType type, final boolean clear, final EpicuriSessionDetail session, final String reference) {
        final NewAdjustmentWebServiceCall adjustment = new NewAdjustmentWebServiceCall(session.getId(), type, NumericalAdjustmentType.MONETARY, amount, reference, SessionActivity.TYPE_ALL);
        WebServiceTask task = new WebServiceTask(getContext(), adjustment, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));

        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                if (clear) {
                    Log.d("PAYMENTSENSE_QO", "Fragment clear all");
                    refreshSessionFromServer(session, new WebServiceTask.OnSuccessListener() {
                        @Override
                        public void onSuccess(int code, String response) {
                            EpicuriSessionDetail refreshedSession;
                            try {
                                refreshedSession = new EpicuriSessionDetail(new JSONObject(response));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return;
                            }

                            FragmentActivity activity = getActivity();
                            if (activity == null) {
                                //should never happen
                                return;
                            }

                            if (activity.getSupportFragmentManager().findFragmentByTag("RECEIPT_TAG") == null) {
                                showBillReceiptConditionally(session, new SendEmailHandlerImpl(getContext(), session));
                            }

                            if (activity instanceof QuickOrderActivity) {
//                                ((QuickOrderActivity) activity).kickDrawer(null);//todo kickDrawer should be called in bill or preselected printer
                            }
                            if (!refreshedSession.getChange().isZero()) {
                                showChangeDialog(refreshedSession.getChange());
                            }

                            markAsPaid();
                            clear();
                        }
                    });
                } else {
                    EpicuriAdjustment epicuriAdjustment = null;
                    try {
                        epicuriAdjustment = new EpicuriAdjustment(new JSONObject(response));

                        Log.d("PAYMENTSENSE_QO", "Fragment don't clear type: " + epicuriAdjustment.getTypeId());
                        addPaymentAndRefresh(epicuriAdjustment);
                    } catch (JSONException e) {
                        Log.e(LOGGER, e.getMessage());
                        Toast.makeText(getContext(), "Cannot process payments at this time", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        task.execute();
    }

    private void showChangeDialog(Money change) {
        String amount = LocalSettings.formatMoneyAmount(change, true);

        changeDialogShowing = true;
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setMessage("Change Due: " + amount)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeDialogClosed();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        changeDialogClosed();
                    }
                })
                .show();
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTextSize(40);
    }

    private void changeDialogClosed() {
        changeDialogShowing = false;
        triggerTableNumberDialog();
    }

    private void showChangeNotification(String amount) {
        Toast toast = Toast.makeText(getActivity(), "Change Due: " + amount, Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup)toast.getView();
        TextView textView = (TextView)group.getChildAt(0);
        textView.setTextSize(40);
        toast.show();
    }

    private void addPaymentAndRefresh(EpicuriAdjustment epicuriAdjustment) {
        calculator.addPayment(MoneyService.toPenniesRoundNearest(epicuriAdjustment.getAmount().getAmount().doubleValue()));
        refreshCalculations();

        refreshSessionFromServer(currentSession, new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try {
                    currentSession = new EpicuriSessionDetail(new JSONObject(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void markAsPaid() {
        PayBillWebServiceCall payBillWebServiceCall = new PayBillWebServiceCall(currentSession.getId());
        new WebServiceTask(getContext(), payBillWebServiceCall).execute();
    }

    private void triggerDiscountDialog(final EpicuriAdjustmentType type) {
        ArrayList<EpicuriAdjustmentType> discountTypes = LocalSettings.getInstance(getActivity())
                .getCachedRestaurant().getDiscountTypes();
        final ArrayAdapter<EpicuriAdjustmentType> adjustmentTypeAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item, discountTypes);
        adjustmentTypeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        int preselected = -1;
        for (int i = 0; i < discountTypes.size(); ++i) {
            if (type.getId().equals(discountTypes.get(i))) {
                preselected = i;
                break;
            }
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_addadjustment, (ViewGroup) getView(), false);
        final EditText amountText = (EditText) view.findViewById(R.id.amount);

        final RadioGroup percentageRadio = (RadioGroup) view.findViewById(R.id.percentage);
        percentageRadio.check(R.id.percentage_yes);
        final Spinner adjustmentTypeSpinner = (Spinner) view.findViewById(R.id.adjustmentType);
        adjustmentTypeSpinner.setAdapter(adjustmentTypeAdapter);
        if (preselected != -1) adjustmentTypeSpinner.setSelection(preselected);

        final TextWatcher normalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        final MoneyWatcher moneyTextWatcher = new MoneyWatcher(amountText, "#.00", null);

        final RadioGroup foodTypeRadio = (RadioGroup) view.findViewById(R.id.food_type);
        percentageRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                boolean percentage = checkedId == R.id.percentage_yes;
                if (percentage) {
                    amountText.removeTextChangedListener(moneyTextWatcher);
                    amountText.addTextChangedListener(normalTextWatcher);
                    for (int i = 0; i < foodTypeRadio.getChildCount(); i++) {
                        foodTypeRadio.getChildAt(i).setEnabled(true);
                    }
                } else {
                    if(amountText.length() > 0) {
                        try {
                            double val = Double.valueOf(amountText.getText().toString());
                            DecimalFormat df = new DecimalFormat("#.##");
                            df.setRoundingMode(RoundingMode.DOWN);
                            amountText.setText(df.format(val));
                        } catch (Exception ex) {
                            amountText.setText("");
                        }
                    }
                    amountText.removeTextChangedListener(normalTextWatcher);
                    amountText.addTextChangedListener(moneyTextWatcher);
                    for (int i = 0; i < foodTypeRadio.getChildCount(); i++) {
                        foodTypeRadio.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        View adjustmentTypeHolder = view.findViewById(R.id.food_type_box);
        adjustmentTypeHolder.setVisibility(View.VISIBLE);
        foodTypeRadio.check(R.id.type_all);

        new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle("Add Adjustment")
                .setView(view)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean percentage =
                                percentageRadio.getCheckedRadioButtonId() == R.id.percentage_yes;

                        final double amount;
                        try {
                            String amountValue = amountText.getText().toString();
                            if (amountValue.length() != 0 && !Character.isDigit(
                                    amountValue.charAt(0))) {
                                amountValue = amountValue.replace(amountValue.charAt(0) + "", "");
                            }

                            amount = Double.parseDouble(amountValue);

                            // ensure this format is valid monetary amount
                            if (!percentage) {
                                Money.of(LocalSettings.getCurrencyUnit(), amount);
                            }
                        } catch (NumberFormatException | ArithmeticException e) {
                            new android.support.v7.app.AlertDialog.Builder(getActivity())
                                    .setTitle("Cannot add adjustment")
                                    .setMessage(R.string.invalid_adjustment_amount)
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            return;
                        }

                        EpicuriAdjustmentType selectedType = adjustmentTypeAdapter.getItem
                                (adjustmentTypeSpinner.getSelectedItemPosition());

                        EpicuriMenu.Item.ItemType itemType;
                        String itemTypeString = null;

                        switch (foodTypeRadio.getCheckedRadioButtonId()) {
                            case R.id.type_all:
                                itemType = null;
                                itemTypeString = SessionActivity.TYPE_ALL;
                                break;
                            case R.id.type_food:
                                itemType = EpicuriMenu.Item.ItemType.FOOD;
                                itemTypeString = SessionActivity.TYPE_FOOD;
                                break;
                            case R.id.type_drink:
                                itemType = EpicuriMenu.Item.ItemType.DRINK;
                                itemTypeString = SessionActivity.TYPE_DRINK;
                                break;
                            case R.id.type_other:
                                itemType = EpicuriMenu.Item.ItemType.OTHER;
                                itemTypeString = SessionActivity.TYPE_OTHER;
                                break;
                            default:
                                itemType = null;
                                itemTypeString = SessionActivity.TYPE_ALL;
                        }

                        unsyncedDiscounts.add(new NewAdjustmentRequest(currentSession == null ? null : currentSession.getId(), selectedType,
                                percentage ? NumericalAdjustmentType.PERCENTAGE :
                                        NumericalAdjustmentType.MONETARY, amount, null,
                                itemTypeString));
                        billPrintingOnCloseRequired = true;
                        calculator.addDiscount(amount, percentage, itemType);
                        calculator.recalculate();

                        refreshCalculations();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void onBillPrintPressed() {
        if(!(billPrint || orderPrint)) {
            Toast.makeText(getContext(), "Printing of Orderslip and Customer Receipt is OFF", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentSession == null) {
            OnSessionCreationListener onSessionCreationListener = new OnSessionCreationListener() {
                @Override
                public void onSessionCreated(EpicuriSessionDetail epicuriSessionDetail) {
                    if (getActivity().getSupportFragmentManager().findFragmentByTag("RECEIPT_TAG") == null) {
                        showBillReceipt(epicuriSessionDetail, new SendEmailHandlerImpl(getContext(), epicuriSessionDetail), false, billPrint, true);
                        billPrintingOnCloseRequired = false;
                    }
                }
            };
            if (unsyncedDiscounts.size() > 0) {
                createSessionWithAdjustments(onSessionCreationListener, true, false);
            } else {
                createSessionNoAdjustments(onSessionCreationListener, true, false);
            }
        } else {
            if (unsyncedDiscounts.size() > 0) {
                WebServiceTask task = new WebServiceTask(getContext(), new NewAdjustmentWebServiceCall(unsyncedDiscounts));
                task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                    @Override
                    public void onSuccess(int code, String response) {
                        refreshSessionFromServer(currentSession, new WebServiceTask.OnSuccessListener() {
                            @Override
                            public void onSuccess(int code, String response) {
                                try {
                                    currentSession = new EpicuriSessionDetail(new JSONObject(response));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                refreshCalculations();
                                showBillReceipt(currentSession, new SendEmailHandlerImpl(getContext(), currentSession), false, billPrint, true);
                                unsyncedDiscounts.clear();

                                if (currentSession.getRemainingTotal().isZero()) {
                                    markAsPaid();
                                    clear();
                                }
                            }
                        });
                    }
                });
            } else {
                showBillReceipt(currentSession, new SendEmailHandlerImpl(getContext(), currentSession), false, billPrint, true);
            }
        }
    }

    private void showBillReceiptConditionally(EpicuriSessionDetail currentSession, SendEmailHandlerImpl emailSendHandler) {
        if (!billPrintingOnCloseRequired) {
            if(getActivity() == null) return;
            String previousPrinterId = getActivity().getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE).getString(GlobalSettings.PREF_KEY_RECEIPT_PRINTER, "");
            LocalSettings settings = LocalSettings.getInstance(getContext());
            List<EpicuriMenu.Printer> cachedPrinters = settings.getCachedPrinters();
            if (cachedPrinters == null || cachedPrinters.size() == 0) {
                getPrintersAndKickDrawer(settings, previousPrinterId);
            } else {
                for (EpicuriMenu.Printer printer : cachedPrinters) {
                    if (printer.getId().equals(previousPrinterId)) {
                        PrintUtil.kickDrawer(getContext(), printer);
                    }
                }
            }
            return;
        }
        showBillReceipt(currentSession, emailSendHandler, true, billPrint, false);
        billPrintingOnCloseRequired = false;
    }

    private void getPrintersAndKickDrawer(final LocalSettings settings, final String previousPrinterId) {
        WebServiceTask task = new WebServiceTask(getContext(), new GetPrintersWebServiceCall());
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try {
                    JSONArray responseJson = new JSONArray(response);
                    List<EpicuriMenu.Printer> allPrinters = new ArrayList<>();
                    for (int i = 0; i < responseJson.length(); i++) {
                        EpicuriMenu.Printer p = new EpicuriMenu.Printer(responseJson.getJSONObject(i));
                        allPrinters.add(p);
                    }
                    settings.cachePrinters(allPrinters);

                    for (EpicuriMenu.Printer printer : allPrinters) {
                        if (printer.getId().equals(previousPrinterId)) {
                            PrintUtil.kickDrawer(getContext(), printer);
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "Cannot get printers - offline", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {

            }
        });
        task.execute();
    }

    private void showBillReceipt(EpicuriSessionDetail currentSession, SendEmailHandlerImpl emailSendHandler, boolean shouldKickDrawer, boolean shouldPrint, boolean disableAutoPrint) {
        EpicuriBaseActivity activity = (EpicuriBaseActivity) getActivity();
        if (activity != null) {
            FakeReceiptFragment f = FakeReceiptFragment.newInstance(activity.getLoggedInUser().getName(), currentSession);
            f.setSendEmailHandler(emailSendHandler);
            f.setShouldKickDrawer(shouldKickDrawer);
            f.setShouldPrint(shouldPrint);
            f.disableAutoPrint(disableAutoPrint);
            f.show(getActivity().getSupportFragmentManager(), "RECEIPT_TAG");
        }
    }

    private void createSessionNoAdjustments(final OnSessionCreationListener listener, boolean showConfirmDialog, boolean isRefund) {
        final QuickOrderWebServiceCall call = new QuickOrderWebServiceCall
                (isRefund ? "Refund" : "QuickOrder", 0, null, true, new String[0], getOrderedItems(), orderPrint, deliveryLocation, isRefund);
        if(showConfirmDialog){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Finished ordering?")
                    .setMessage("You will not be able to add any more orders after this action.")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            createSession(call, listener);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            createSession(call, listener);
        }
    }

    private void createSessionWithAdjustments(final OnSessionCreationListener listener, boolean showConfirmDialog, boolean isRefund) {
        final QuickOrderUnifiedWebServiceCall call = new QuickOrderUnifiedWebServiceCall(getOrderedItems(), unsyncedDiscounts, deliveryLocation, isRefund);
        if (showConfirmDialog) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Finished ordering?")
                    .setMessage("You will not be able to add any more orders after this action.")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            createSession(call, listener);
                            unsyncedDiscounts.clear();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            createSession(call, listener);
            unsyncedDiscounts.clear();
        }
    }

    public void setSession(final QuickOrderLandscapeState state) {
        if (state == null || state.getCurrentSession() == null) return;
        refreshSessionFromServer(state.getCurrentSession(), new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try {
                    currentSession = new EpicuriSessionDetail(new JSONObject(response));
                    calculator = state.getCalculator();
                    unsyncedDiscounts = state.getUnsyncedDiscounts();
                    refreshCalculations();
                    refreshButtons();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createSession(WebServiceCall call, final OnSessionCreationListener listener) {
        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText("Creating orders...");
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    currentSession = new EpicuriSessionDetail(jsonObject.getJSONObject("hostSessionView"));
                    PrintUtil.printFromJsonResponse(jsonObject, getContext(), QuickOrderLandscapeFragment.this.getActivity());
                    if (listener != null) {
                        listener.onSessionCreated(currentSession);
                    }
                    refreshButtons();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
            }
        });

        if(call instanceof ValidWebServiceCall &&
                ((ValidWebServiceCall) call).isValid()) {
            task.execute();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (itemsAdapter != null) itemsAdapter.setFilter(query);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (itemsAdapter != null) itemsAdapter.setFilter(newText);
        return true;
    }

    @Override
    public void doBack() {
        if (currentSession != null || pendingOrders.size() > 0) {
            showDialogForClear(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getActivity() != null) ((QuickOrderActivity) getActivity()).setDoBack();
                    if (getActivity() != null) getActivity().onBackPressed();
                }
            });
        } else {
            if (getActivity() != null) ((QuickOrderActivity) getActivity()).setDoBack();
            if (getActivity() != null) getActivity().onBackPressed();
        }
    }
}
