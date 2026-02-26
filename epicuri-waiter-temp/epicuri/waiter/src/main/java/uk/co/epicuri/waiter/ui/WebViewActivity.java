package uk.co.epicuri.waiter.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Printer;
import uk.co.epicuri.waiter.webservice.TokenManager;
import uk.co.epicuri.waiter.webservice.TokenManager.NotLoggedInException;

public class WebViewActivity extends EpicuriBaseActivity {
	
	public static final String EXTRA_URL = "uk.co.epicuri.waiter.URL";
	public static final String EXTRA_TYPE= "uk.co.epicuri.waiter.WEBVIEW_TYPE";
	
	public static final int LOADER_PRINTERS = 1;
	private String mWebViewPrefix;

	public enum Type {
		KITCHEN, BI, HELP, NEWS
	}

	private static final String[] biOptions = {"Week","Month","Three Months","Year","Forever"};

    @InjectView(R.id.webview)
	WebView webView;
    @InjectView(R.id.printer_spinner)
    Spinner webViewSpinner;

	private String token;
	private Type type = null;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayUseLogoEnabled(false);
		actionbar.setDisplayHomeAsUpEnabled(true);

		mWebViewPrefix = EpicuriPreferenceActivity.getWebPrefix(this);

        setContentView(R.layout.activity_webview);
        ButterKnife.inject(this);

		// ensure links are loaded within the webview and not launched externally
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		try {
			token = TokenManager.getTokenKey(this);
		} catch (NotLoggedInException e) {
			// not logged in
			TokenManager.newTokenAction(this);
			return;
		}
		
		if(getIntent().hasExtra(EXTRA_TYPE)){
			type = (Type)getIntent().getSerializableExtra(EXTRA_TYPE);
			switch(type){
			case KITCHEN:{
				setTitle(R.string.menu_kitchen);
				startPrinterLoader();
                webViewSpinner.setVisibility(View.VISIBLE);
				break;
			}
			case BI:{
				setTitle(R.string.menu_businessintelligence);

				ArrayAdapter<String> aa = new ArrayAdapter<String>(WebViewActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1, biOptions);
				aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                webViewSpinner.setAdapter(aa);
                webViewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        webView.loadUrl("about:blank");
                        webView.loadUrl(String.format(mWebViewPrefix + "/BusinessIntelligence?Auth=%s&Period=%d", token, position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        webView.loadUrl("about:blank");
                    }
                });
                webViewSpinner.setVisibility(View.VISIBLE);

				webView.loadUrl(String.format(mWebViewPrefix + "/BusinessIntelligence?Auth=%s", token));
				break;
			}
			case NEWS:
			case HELP:{
				actionbar.setDisplayShowTitleEnabled(false);
				webView.loadUrl(getIntent().getStringExtra(EXTRA_URL));
				webView.getSettings().setJavaScriptEnabled(true);
				break;
			}
			default:
				throw new IllegalArgumentException("Unrecognised type: " + type);
			}
		} else if(getIntent().hasExtra(EXTRA_URL)){
			actionbar.setDisplayShowTitleEnabled(false);
			webView.loadUrl(getIntent().getStringExtra(EXTRA_URL));
			webView.getSettings().setJavaScriptEnabled(true);
		} else {
			throw new IllegalArgumentException("Not enough input provided");
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.findItem(R.id.menu_help).setVisible(Type.HELP != type);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home: {
				if(type == Type.HELP){
					finish();
					return true;
				}
				Intent intent = new Intent(this, HubActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				return true;
			}
			case R.id.menu_help: {
				Intent helpIntent = new Intent(this, WebViewActivity.class);
				helpIntent.putExtra(WebViewActivity.EXTRA_TYPE, WebViewActivity.Type.HELP);
				if(type!=null){
					switch(type){
						case BI:{
							helpIntent.putExtra(WebViewActivity.EXTRA_URL, "http://epicuri.co.uk/help/BI");
							break;
						}
						case KITCHEN:{
							helpIntent.putExtra(WebViewActivity.EXTRA_URL, "http://epicuri.co.uk/help/KitchenSchedule");
							break;
						}
						case NEWS:{
							helpIntent.putExtra(WebViewActivity.EXTRA_URL, "http://epicuri.co.uk/help/News");
							break;
						}
					}
				}
				startActivity(helpIntent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	ArrayList<EpicuriMenu.Printer> printers;
	private void startPrinterLoader(){
		getSupportLoaderManager().restartLoader(LOADER_PRINTERS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Printer>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<Printer>>> onCreateLoader(
					int arg0, Bundle arg1) {
				return new EpicuriLoader<ArrayList<Printer>>(WebViewActivity.this, new PrinterLoaderTemplate());
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<Printer>>> loader,
					LoaderWrapper<ArrayList<Printer>> data) {
				if(null == data) return;
				else if(data.isError()){
					Toast.makeText(WebViewActivity.this, "WebViewActivity Error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				printers = data.getPayload();
				ArrayAdapter<EpicuriMenu.Printer> aa = new ArrayAdapter<EpicuriMenu.Printer>(WebViewActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1, printers);
				aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                webViewSpinner.setAdapter(aa);
                webViewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        webView.loadUrl("about:blank");
                        webView.loadUrl(String.format(mWebViewPrefix + "/Kitchen?Auth=%s&Printer=%s", token, printers.get(position).getId()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        webView.loadUrl("about:blank");
                    }
                });
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<Printer>>> arg0) {
			}
			
		});
	}
}
