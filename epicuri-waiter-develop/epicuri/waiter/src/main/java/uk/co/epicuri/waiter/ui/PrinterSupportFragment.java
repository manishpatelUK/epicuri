package uk.co.epicuri.waiter.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarPrinterStatus;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.printing.DiscoverStarPrinterTask;
import uk.co.epicuri.waiter.printing.GetStarPrinterStatusTask;
import uk.co.epicuri.waiter.printing.IStarPrinterStatusListener;
import uk.co.epicuri.waiter.printing.IStarPrintersDiscoveredListener;
import uk.co.epicuri.waiter.printing.PrintUtil;
import uk.co.epicuri.waiter.ui.dialog.PrinterSelectDialog;
import uk.co.epicuri.waiter.webservice.EditPrinterWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetPrintersWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class PrinterSupportFragment extends Fragment implements PrinterSelectDialog.OnPrinterSelectedListener {

    @InjectView(R.id.printerRowData)
    LinearLayout printerRowData;

    @InjectView(R.id.discoveredPrinterView)
    LinearLayout discoveredPrinterView;

    @InjectView(R.id.configuredPrintersProgressBar)
    ProgressBar configuredPrintersProgressBar;

    @InjectView(R.id.discoveredPrintersProgressBar)
    ProgressBar discoveredPrintersProgressBar;

    @InjectView(R.id.printerSearchButton)
    Button printerSearchButton;

    private long lastCall;
    private List<EpicuriMenu.Printer> printers = new ArrayList<>();
    Map<String,String> printerStatuses = new HashMap<>();

    private WebServiceTask configuredWebServiceTask;

    private int screenWidth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.support_printer_diagnostic, container, false);
        ButterKnife.inject(this, view);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;

        return view;
    }

    public void trigger() {
        if(System.currentTimeMillis() - lastCall < 5000) {
            return;
        }

        lastCall = System.currentTimeMillis();
        startPrinterInfoCollection(true);
    }

    private void startPrinterInfoCollection(final boolean triggerSearch) {
        if(configuredWebServiceTask != null && configuredWebServiceTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }

        printerRowData.removeAllViews();
        synchronized (PrinterSupportFragment.this) {
            printers.clear();
            printerStatuses.clear();
        }

        configuredPrintersProgressBar.setVisibility(View.VISIBLE);
        configuredWebServiceTask = new WebServiceTask(getContext(), new GetPrintersWebServiceCall(), false);
        configuredWebServiceTask.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                redrawConfiguredPrintersSection();
                configuredPrintersProgressBar.setVisibility(View.INVISIBLE);
            }
        });
        configuredWebServiceTask.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                if(response == null) {
                    configuredPrintersProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                try{
                    synchronized (PrinterSupportFragment.this) {
                        JSONArray responseJson = new JSONArray(response);
                        for (int i = 0; i < responseJson.length(); i++) {
                            printers.add(new EpicuriMenu.Printer(responseJson.getJSONObject(i)));
                        }
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                } finally {
                    redrawConfiguredPrintersSection();
                }

                if(triggerSearch) {
                    startPrinterDiscovery();
                }
            }
        });
        configuredWebServiceTask.execute();
    }

    @OnClick(R.id.printerSearchButton)
    public void startPrinterDiscovery() {
        discoveredPrintersProgressBar.setVisibility(View.VISIBLE);
        printerSearchButton.setEnabled(false);
        discoveredPrinterView.removeAllViews();

        DiscoverStarPrinterTask discoverStarPrinterTask = new DiscoverStarPrinterTask(new IStarPrintersDiscoveredListener() {
            @Override
            public void onPrintersDiscovered(final List<PortInfo> discoveredPrinters) {
                FragmentActivity activity = getActivity();
                if(activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(discoveredPrinters == null || discoveredPrinters.size() == 0) {
                            TextView empty = new TextView(getContext());
                            empty.setText("There are no printers on this network (TCP)");
                            discoveredPrinterView.addView(empty);
                        } else {
                            for (final PortInfo portInfo : discoveredPrinters) {
                                LinearLayout linearLayout = createPrinterInfoLayout(portInfo.getModelName(), portInfo.getPortName());
                                if (linearLayout == null) return;
                                linearLayout.addView(createTestPrintButton(portInfo.getPortName()));

                                Button linkButton = new Button(getContext());
                                linkButton.setText("Link");
                                linkButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onConfigureClicked(portInfo);
                                    }
                                });
                                linearLayout.addView(linkButton);

                                discoveredPrinterView.addView(linearLayout);
                            }
                        }
                        discoveredPrintersProgressBar.setVisibility(View.INVISIBLE);
                        printerSearchButton.setEnabled(true);
                    }
                });
            }
        });
        discoverStarPrinterTask.execute();
    }

    private void onConfigureClicked(PortInfo portInfo) {
        if(printers.size() == 0) {
            Toast.makeText(getContext(),"Printers not yet loaded or have not been configured", Toast.LENGTH_SHORT).show();
        } else {
            PrinterSelectDialog.newInstance().show(getContext(), this, printers, portInfo);
        }
    }

    @Override
    public void onPrinterSelected(EpicuriMenu.Printer printer, PortInfo currentInfo) {
        String portName = currentInfo.getPortName();
        if(portName.startsWith("TCP:")) {
            portName = portName.substring(4);
        }
        WebServiceTask task = new WebServiceTask(getContext(), new EditPrinterWebServiceCall(printer.getId(), portName, currentInfo.getMacAddress()));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                Toast.makeText(getContext(), "Printer configuration has been updated", Toast.LENGTH_LONG).show();
                startPrinterInfoCollection(false);
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Toast.makeText(getContext(), "Could not update printer information: " + response, Toast.LENGTH_SHORT).show();
            }
        });
        task.execute();
    }

    private synchronized void redrawConfiguredPrintersSection() {
        for(EpicuriMenu.Printer printer : printers) {
            addConfiguredPrinterView(printer);
        }
    }

    private void addConfiguredPrinterView(final EpicuriMenu.Printer printer) {
        LinearLayout linearLayout = createPrinterInfoLayout(printer.getName(), printer.getIpAddress());

        //the buttons and statuses to add
        if (getContext() == null) return;
        final TextView status = new TextView(getContext());
        status.setWidth(screenWidth/4);
        status.setText("?");
        if(printer.getIpAddress() == null || printer.getIpAddress().trim().equals("")) {
            status.setText("LOGICAL");
        } else {
            GetStarPrinterStatusTask task = new GetStarPrinterStatusTask(getContext(), new IStarPrinterStatusListener() {
                @Override
                public void onStatusRetrieved(final StarPrinterStatus starPrinterStatus) {
                    if(getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(getContext() == null) return;
                                String statusString = null;
                                if (starPrinterStatus == null) {
                                    statusString = "OFFLINE";
                                    status.setTextColor(getResources().getColor(R.color.red));
                                } else {
                                    statusString = "ONLINE";
                                    status.setTextColor(getResources().getColor(R.color.green));
                                }
                                status.setText(statusString);
                                printerStatuses.put(printer.getId(), statusString);

                                if (printerStatuses.size() == printers.size()) {
                                    configuredPrintersProgressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });
            task.execute(printer.getIpAddress());
        }
        linearLayout.addView(status);
        linearLayout.addView(createTestPrintButton(printer.getIpAddress()));

        printerRowData.addView(linearLayout);
    }

    @Nullable
    private LinearLayout createPrinterInfoLayout(String name, String address){
        if(address != null && address.startsWith("TCP:")) {
            address = address.substring(4);
        } else {
            address = "";
        }
        if(getContext() == null) return null;
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView nameView = new TextView(getContext());
        nameView.setWidth(screenWidth/4);
        nameView.setText(name);
        linearLayout.addView(nameView);
        TextView ipAddress = new TextView(getContext());
        ipAddress.setWidth(screenWidth/4);
        ipAddress.setText(address);
        linearLayout.addView(ipAddress);

        return linearLayout;
    }

    private Button createTestPrintButton(final String ipAddress) {
        Button button = new Button(getContext());
        button.setText("TEST");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintUtil.testPrint(getContext(), ipAddress);
            }
        });
        if(ipAddress == null || ipAddress.trim().equals("TCP:") || ipAddress.trim().equals("")) {
            button.setEnabled(false);
        }

        return button;
    }
}
