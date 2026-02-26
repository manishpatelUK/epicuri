package uk.co.epicuri.waiter.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class CheckableWrapper extends LinearLayout implements Checkable, OnCheckedChangeListener {
	
	public CheckableWrapper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupCheckListener();
	}

	public CheckableWrapper(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupCheckListener();
	}

	public CheckableWrapper(Context context) {
		super(context);
		setupCheckListener();
	}

	private boolean checked = false;
	
	@Override
	public boolean isChecked() {
		return this.checked;
	}

	@Override
	public void setChecked(boolean checked) {
		this.checked = checked;
		if(null != findViewById(android.R.id.checkbox)) ((Checkable)findViewById(android.R.id.checkbox)).setChecked(this.checked);
	}

	@Override
	public void toggle() {
		this.checked = !this.checked;
		if(null != findViewById(android.R.id.checkbox)) ((Checkable)findViewById(android.R.id.checkbox)).setChecked(this.checked);
	}
	
	private void setupCheckListener(){
		if(null != findViewById(android.R.id.checkbox)){
			((CheckBox)findViewById(android.R.id.checkbox)).setOnCheckedChangeListener(this);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		this.checked = isChecked;		
	}
}
