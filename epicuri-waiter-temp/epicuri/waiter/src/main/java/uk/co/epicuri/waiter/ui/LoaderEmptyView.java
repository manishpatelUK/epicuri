/**
 * Copyright The Distance 2013
 */
package uk.co.epicuri.waiter.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import uk.co.epicuri.waiter.R;

/**
 * View which shows a spinner while loading, then shows "No Rows Found" when
 * the data has loader (setDataLoaded())
 * 
 * @author Pete Harris <peteh@thedistance.co.uk>
 */
public class LoaderEmptyView extends FrameLayout {
	private TextView tv ;
	private ProgressBar bar;
	
	public LoaderEmptyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		insertSubViews();
	}

	public LoaderEmptyView(Context context) {
		super(context);
		insertSubViews();
	}

	private void insertSubViews(){
		LayoutParams centred = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		centred.gravity = Gravity.CENTER;
		bar = new ProgressBar(getContext());
		bar.setIndeterminate(true);
		addView(bar, centred);
		
		LayoutParams fullscreen = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

//        style="@style/EmptyText"
		tv = new TextView(getContext());
		tv.setGravity(Gravity.CENTER);
		tv.setTextAppearance(getContext(), R.style.EmptyText);
		tv.setText("No Rows Found");
		addView(tv, fullscreen);
		tv.setVisibility(View.GONE);
	}
	
	public void setText(CharSequence label){
		tv.setText(label);
	}

	public void setDataUnloaded(){
		bar.setVisibility(View.VISIBLE);
		tv.setVisibility(View.GONE);
	}
	public void setDataLoaded(){
		bar.setVisibility(View.GONE);
		tv.setVisibility(View.VISIBLE);
	}
}
