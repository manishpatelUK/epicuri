package uk.co.epicuri.waiter.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.CheckableImageButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.MenuCategoryAdapter;
import uk.co.epicuri.waiter.interfaces.OnEpicuriMenuItemsSelectedListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.MenuLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.MenuSummaryLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenuSummary;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class MenuFragment extends Fragment implements MenuCategoryAdapter.IMenuCategoryItemClicked {

    private static final int LOADER_MENU = 1;
    private static final int LOADER_MENU_SUMMARIES = 2;

    private static final String NO_MENU_SELECTED = "-1";

    private static final String EXTRA_HIDE_MENU_CHOOSER = "hideMenuChooser";
    public static final String EXTRA_QUICK_ORDER_MENU = "qomenu";

    @InjectView(R.id.menuChoice)
    Spinner menuSpinner;

    @InjectView(android.R.id.list)
    RecyclerView listView;
    @InjectView(android.R.id.tabhost)
    TabHost tabHost;
    @InjectView(R.id.divider)
    View divider;
    @InjectView(R.id.clicker) View clicker;
    @InjectView(R.id.clicker2) View clicker2;
    @InjectView(R.id.scroll) View scroll;
    @InjectView(R.id.tabs) View tabs;
    @InjectView(R.id.clear) Button clear;
    @InjectView(R.id.short_code_edittext) EditText shortCodeEditText;
    @InjectView(R.id.expand)
    CheckableImageButton expandBtn;
    private MenuCategoryAdapter adapter;
    private EpicuriMenu selectedMenu;
    private OnEpicuriMenuItemsSelectedListener listener;
    final long[] lastClickTime = {0};
    Boolean expanded = false;
    String selectedMenuId = NO_MENU_SELECTED;
    String initialMenu = NO_MENU_SELECTED;
    boolean justClearedFilter;

    public static MenuFragment newInstance(String selectedMenuId, boolean hideMenuChooser) {
        MenuFragment f = new MenuFragment();
        Bundle args = new Bundle(2);
        args.putString(GlobalSettings.EXTRA_MENU_ID, selectedMenuId);
        args.putBoolean(EXTRA_HIDE_MENU_CHOOSER, hideMenuChooser);
        f.setArguments(args);
        return f;
    }

    public static MenuFragment newInstance() {
        MenuFragment f = new MenuFragment();
        Bundle args = new Bundle(0);
        f.setArguments(args);
        return f;
    }

    public static MenuFragment newInstance(boolean isQO) {
        MenuFragment f = new MenuFragment();
        Bundle args = new Bundle(0);
        args.putBoolean(EXTRA_QUICK_ORDER_MENU, isQO);
        f.setArguments(args);
        return f;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState) {
            initialMenu = savedInstanceState.getString(GlobalSettings.EXTRA_MENU_ID,
                    NO_MENU_SELECTED);
        } else if (getArguments() != null) {
            initialMenu = getArguments().getString(GlobalSettings.EXTRA_MENU_ID, NO_MENU_SELECTED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        ButterKnife.inject(this, view);
        tabHost.setOnTabChangedListener(menuSectionChangeListener);
        clicker.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return clearFilter();
            }
        });
        clicker2.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return clearFilter();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                shortCodeEditText.setText("");
                shortCodeEditText.clearFocus();

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(shortCodeEditText.getWindowToken(), 0);
            }
        });

        tabHost.setup();
        adapter = new MenuCategoryAdapter(getActivity(), null);

        expandBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi") @Override public void onClick(View view) {
                expandBtn.toggle();
                adapter.changeExpanded();
                adapter.notifyDataSetChanged();
            }
        });
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int position) {
                return adapter.getSpanCount(position);
            }
        });

        listView.setLayoutManager(manager);
        listView.setAdapter(adapter);
        if(EpicuriApplication.getInstance(getActivity()).getApiVersion() >= GlobalSettings.API_VERSION_6){
            shortCodeEditText.setVisibility(View.VISIBLE);
            clear.setVisibility(View.VISIBLE);
            shortCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
//                        if(!adapter.orderItem(shortCodeEditText.getText().toString())){
//                            Toast.makeText(getActivity(), "No matches for code/item name entered",
//                                    Toast.LENGTH_SHORT).show();
//                        }
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(shortCodeEditText.getWindowToken(), 0);
                        return true;
                    }

                    return false;
                }
            });

            shortCodeEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String query = charSequence.toString();
                    adapter.filterItems(query);
                    if (query == null || query.isEmpty()) {
                        tabs.setVisibility(View.VISIBLE);
                    } else {
                        tabs.setVisibility(View.GONE);
                    }
                }

                @Override public void afterTextChanged(Editable editable) {

                }
            });

            shortCodeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override public void onFocusChange(View view, boolean b) {
                    if(!b){
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(shortCodeEditText.getWindowToken(), 0);
                    }
                }
            });
        } else {
            shortCodeEditText.setVisibility(View.GONE);
            clear.setVisibility(View.GONE);
        }



        menuSpinner.setOnItemSelectedListener(menuChangeListener);

        if (null != getArguments() && getArguments().getBoolean(EXTRA_HIDE_MENU_CHOOSER, false)) {
            Bundle args = new Bundle();
            args.putString(GlobalSettings.EXTRA_MENU_ID,
                    getArguments().getString(GlobalSettings.EXTRA_MENU_ID, NO_MENU_SELECTED));
            getLoaderManager().restartLoader(LOADER_MENU, args, menuLoaderCallback);
            menuSpinner.setVisibility(View.GONE);
        } else {
            getLoaderManager().initLoader(LOADER_MENU_SUMMARIES, null, menuSummaryLoaderCallback);
        }


        return view;
    }

    @Override public void onItemClicked(EpicuriMenu.Item item) {
        if (SystemClock.elapsedRealtime() - lastClickTime[0] < 250) {
            return;
        }
        lastClickTime[0] = SystemClock.elapsedRealtime();

        if (item.isUnavailable()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Item Unavailable").setMessage(
                    String.format("%s is unavailable", item.getName())).show();
            return;
        }
        listener.onEpicuriMenuItemSelected(item);
        if(shortCodeEditText.getText() != null && shortCodeEditText.getText().toString().trim().length() > 0) {
            shortCodeEditText.getText().clear();
            shortCodeEditText.clearFocus();
            scroll.requestFocus();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GlobalSettings.EXTRA_MENU_ID, selectedMenuId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (null != getTargetFragment()) {
            listener = (OnEpicuriMenuItemsSelectedListener) getTargetFragment();
        } else {
            listener = (OnEpicuriMenuItemsSelectedListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private final TabHost.OnTabChangeListener menuSectionChangeListener =
            new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    adapter = new MenuCategoryAdapter(getActivity(), selectedMenu, tabId,
                            MenuFragment.this);
                    listView.setAdapter(adapter);
                    divider.setVisibility(View.VISIBLE);
                }
            };

    private final AdapterView.OnItemSelectedListener menuChangeListener =
            new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapter, View view,
                        int position, long id) {
                    String newMenuId = ((EpicuriMenuSummary) adapter.getItemAtPosition(
                            position)).getId();
                    if (selectedMenuId != null && selectedMenuId.equals(newMenuId)) {
                        // if it hasn't changed, then it doesn't matter
                        return;
                    } else {
                        listView.setAdapter(null);
                        selectedMenuId = newMenuId;
                        Bundle args = new Bundle();
                        args.putString(GlobalSettings.EXTRA_MENU_ID, newMenuId);
                        getLoaderManager().restartLoader(LOADER_MENU, args, menuLoaderCallback);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapter) {
                    listView.setAdapter(null);
                    selectedMenuId = NO_MENU_SELECTED;
                }
            };

    private boolean clearFilter() {
        if (!shortCodeEditText.hasFocus())
            return false;

        shortCodeEditText.setText("");
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(shortCodeEditText.getWindowToken(), 0);
        shortCodeEditText.clearFocus();
        scroll.requestFocus();
        return false;
    }

    private LoaderManager.LoaderCallbacks<? extends Object> menuSummaryLoaderCallback =
            new LoaderManager.LoaderCallbacks<LoaderWrapper<List<EpicuriMenuSummary>>>() {

                @Override
                public Loader<LoaderWrapper<List<EpicuriMenuSummary>>> onCreateLoader(int id,
                        Bundle args) {
                    return new EpicuriLoader<List<EpicuriMenuSummary>>(getActivity(),
                            new MenuSummaryLoaderTemplate(false));
                }

                @Override
                public void onLoadFinished(Loader<LoaderWrapper<List<EpicuriMenuSummary>>> loader,
                        LoaderWrapper<List<EpicuriMenuSummary>> data) {
                    if (null == data) { // nothing returned, ignore
                        return;
                    } else if (data.isError()) {
                        Toast.makeText(getActivity(), "MenuFragment error loading data",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<EpicuriMenuSummary> menus = new ArrayList<EpicuriMenuSummary>();
                    for (EpicuriMenuSummary m : data.getPayload()) {
                        if (m.isActive()) {
                            // only add menus marked as active
                            menus.add(m);
                        }
                    }

                    if(getArguments() != null && getArguments().getBoolean(EXTRA_QUICK_ORDER_MENU)){
                        String qoMenuId = LocalSettings.getInstance(getActivity()).getQuickOrderMenuId();
                        int qoMenuPosition = -1;
                        menus = new ArrayList<EpicuriMenuSummary>();
                        int i = 0;
                        for (EpicuriMenuSummary m : data.getPayload()) {
                            if (m.isActive()) {
                                menus.add(m);
                                if(qoMenuId != null && m.getId().equals(qoMenuId)) {
                                    qoMenuPosition = i;
                                }
                            }
                            i++;
                        }

                        if(qoMenuPosition > -1) {
                            menuSpinner.setSelection(qoMenuPosition);
                        }
                    }

                    ArrayAdapter<EpicuriMenuSummary> menuAdapter
                            = new ArrayAdapter<EpicuriMenuSummary>(getActivity(),
                            R.layout.spinner_menutitle, menus);
                    menuAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    menuSpinner.setAdapter(menuAdapter);

                    if (selectedMenuId != null && !selectedMenuId.equals(NO_MENU_SELECTED)) {
                        initialMenu = selectedMenuId;
                    }

                    // set starting selection from passed in argument
                    for (int pos = 0; pos < menus.size(); pos++) {
                        if (menus.get(pos).getId().equals(initialMenu)) {
                            menuSpinner.setSelection(pos);
                            break;
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<LoaderWrapper<List<EpicuriMenuSummary>>> loader) {
                }

            };

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
                    TabHost.TabContentFactory tcf = new TabHost.TabContentFactory() {
                        @Override
                        public View createTabContent(String tag) {
                            return new View(getActivity());
                        }
                    };

                    selectedMenu = data;
                    tabHost.clearAllTabs();
                    for (EpicuriMenu.Category cat : selectedMenu.getCategories()) {
                        tabHost.addTab(tabHost.newTabSpec(cat.getName()).setIndicator(
                                cat.getName()).setContent(tcf));
                    }
                }

                @Override
                public void onLoaderReset(Loader<EpicuriMenu> loader) {
                    selectedMenu = null;
                    tabHost.clearAllTabs();
                }
            };

}
