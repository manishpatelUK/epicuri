package uk.co.epicuri.waiter.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.OnReservationSelectedListener;
import uk.co.epicuri.waiter.model.EpicuriReservation;
import uk.co.epicuri.waiter.model.LocalSettings;

@TargetApi(11)
public class DayView extends RelativeLayout {
	
	private static int PADDING;
	private static int HEIGHT_PER_HOUR;
	private static int WIDTH;
	private static int TIME_WIDTH;
	
	
	private Context context;
	private RelativeLayout timeLayout;

	private EpicuriReservation newReservation;
	private EpicuriReservation activeReservation = null;
	
	private List<EpicuriReservation> reservations;
	private Date today;
	
	private float startY = 0;

	public DayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setDate(Calendar.getInstance());
		
		this.context = context;
		{
			Resources r = getResources();
			PADDING = r.getDimensionPixelSize(R.dimen.reservations_padding);
			HEIGHT_PER_HOUR = r.getDimensionPixelSize(R.dimen.reservations_height_per_hour);
			WIDTH = r.getDimensionPixelSize(R.dimen.reservations_width);
			TIME_WIDTH = r.getDimensionPixelSize(R.dimen.reservations_time_width);
		}

		HorizontalScrollView hsv;
		{
			hsv = new HorizontalScrollView(context);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			params.addRule(ALIGN_PARENT_RIGHT);
			params.addRule(ALIGN_PARENT_LEFT);
			params.leftMargin = TIME_WIDTH;
			hsv.setFillViewport(true);
			addView(hsv, params);
		}

		{
			timeLayout = new RelativeLayout(context);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			hsv.addView(timeLayout, params);
		}
		
		
		for(int i=0; i<24; i++){
			LayoutParams params;
			
			ImageButton b;
			
			for(float j: new float[]{0,0.5f}){
				b = new ImageButton(context);
				params = new LayoutParams(LayoutParams.MATCH_PARENT, HEIGHT_PER_HOUR / 2);
				params.topMargin = (int)(HEIGHT_PER_HOUR * (i + j));
//				params.leftMargin = TIME_WIDTH;
				b.setBackgroundResource(R.drawable.reservation_row);
				timeLayout.addView(b, params);
				b.setOnTouchListener(backgroundTouchListener);
				b.setTag((int) (60 * (i + j)));
			}
			
			TextView tv = new TextView(context);
			tv.setText(String.format("%s:00", i));
			tv.setId(100 + i);
			tv.setBackgroundColor(0x11000000);
			tv.setTextColor(0x99000000);
			tv.setGravity(Gravity.RIGHT);
			
			params = new LayoutParams(TIME_WIDTH, HEIGHT_PER_HOUR);
			params.topMargin = HEIGHT_PER_HOUR * i;
//			params.addRule(BELOW, 99+i);
			addView(tv, params);
		}
	}

	public DayView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DayView(Context context) {
		this(context, null);
	}
	
	private List<View> reservationViews = new LinkedList<View>();

	
	public void notifyDatasetUpdated(){
		// TODO: this should just update layouts, not delete & recreate views
		redrawReservations();
		requestLayout();
	}
	
	public void setReservations(List<EpicuriReservation> reservations){
		// we are storing a reference to an external list here
		this.reservations = reservations;
		if(null != activeReservation && !reservations.contains(activeReservation)){
			reservations.add(activeReservation);
		}
		notifyDatasetUpdated();
	}
	
	private void redrawReservations(){
		for(View v: reservationViews){
			timeLayout.removeView(v);
		}

		Collections.sort(reservations, new Comparator<EpicuriReservation>() {
			@Override
			public int compare(EpicuriReservation lhs, EpicuriReservation rhs) {
				return lhs.getStartDate().compareTo(rhs.getStartDate());
			}
		});
		
		List<Integer> cols = new LinkedList<Integer>();
		
		LayoutInflater inflater = LayoutInflater.from(context);
		SimpleDateFormat df = LocalSettings.getDateFormat();
		
		List<View> cachedViews = reservationViews;
		reservationViews = new LinkedList<View>();
		
		for(EpicuriReservation res: reservations){
			int difference = (int)((res.getStartDate().getTime() - today.getTime()) / 1000 / 60);
			
			if(difference < 0 || difference > 60*24){
				// skip if reservation is not for 'today'
				continue;
			}
			
			int start = difference * HEIGHT_PER_HOUR / 60;
			int height = res.getDurationInMinutes() * HEIGHT_PER_HOUR / 60 - PADDING;
			
			// try to reuse existing views
			RelativeLayout v;
			if(!cachedViews.isEmpty()){
				v = (RelativeLayout)cachedViews.remove(0);
			} else {
				v = (RelativeLayout)inflater.inflate(R.layout.reservation, this, false);
			}
			reservationViews.add(v);
			v.setTag(res);
			
			// set 'active' if this is being edited
			v.setActivated(res.equals(activeReservation));
			
			v.setOnTouchListener(eventTouchListener);
			
			TextView tv;
			tv = (TextView)v.findViewById(R.id.numberInParty);
			tv.setText(String.format(Locale.UK, "%d guests%s", res.getNumberInParty(), res.isAccepted() ? "" : "\nNOT ACCEPTED"));

			tv = (TextView)v.findViewById(R.id.name);
			tv.setText(res.getName());

			tv = (TextView)v.findViewById(R.id.startTime);
			tv.setText(df.format(res.getStartDate()));
			
			ImageView iv;
			iv = (ImageView)v.findViewById(R.id.epicuri);
			iv.setVisibility(res.getEpicuriUser() == null ? View.GONE : View.VISIBLE);

			iv = (ImageView)v.findViewById(R.id.unsavedChanges);
			iv.setVisibility(res.isModified() ? View.VISIBLE: View.GONE);

			
			LayoutParams params = new LayoutParams(WIDTH, height);
			params.topMargin = start;
			int col=0;
			while(cols.size() > col && cols.get(col) > start){
				col ++;
			}
			if(col < cols.size()){
				cols.set(col, start+height);
			} else {
				cols.add(start+height);
			}
			params.leftMargin = PADDING  + (PADDING + WIDTH) * col;
			timeLayout.addView(v, params);
		}
		timeLayout.requestLayout();
	}
	
	
	
	private void activateRegistration(EpicuriReservation r){
		activeReservation = r;
		notifyDatasetUpdated();
		if(null != listener){
			listener.onReservationSelected(r);
		}
	}

	public void deselect() {
		// delete any unsaved reservation
		if(null != newReservation){
			reservations.remove(newReservation);
			newReservation = null;
		}
		// remove active reservation
		activeReservation = null;
		notifyDatasetUpdated();
	}
	
	View.OnTouchListener backgroundTouchListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getActionMasked() == MotionEvent.ACTION_UP){
				int minutes = (Integer)v.getTag();
				
				Date resTime = new Date(today.getTime() + minutes*60000);

				if(null == activeReservation){
					newReservation = EpicuriReservation.at(resTime);
					newReservation.setStartDate(resTime);
					if(!reservations.contains(newReservation)){
						reservations.add(newReservation);
					}
					activateRegistration(newReservation);
				} else {
					activeReservation.setStartDate(resTime);
					if(null != listener){
						listener.onReservationSelected(activeReservation);
					}
					notifyDatasetUpdated();
				}
				return true;
			}
			return false;
		}
	};
	
	View.OnTouchListener eventTouchListener = new View.OnTouchListener() {
		boolean moving = false;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getActionMasked()){
			case MotionEvent.ACTION_DOWN: {
				EpicuriReservation r = (EpicuriReservation)v.getTag();
				// if already editing another reservation, ignore the touch
				if(r != activeReservation && activeReservation != null && activeReservation.isModified()){
					return false;
				}
				activateRegistration(r);
				startY = event.getY();
				moving = false;
				v.getParent().requestDisallowInterceptTouchEvent(true); 
				return true;
			}
			case MotionEvent.ACTION_MOVE: {
				if(moving){
					LayoutParams params  = ((LayoutParams)v.getLayoutParams());
					// potential rounding error if screen is too small - cannot get 15 minute segments
					params.topMargin += (HEIGHT_PER_HOUR/12) * (Math.round((event.getY() - startY) / (HEIGHT_PER_HOUR/12)));
					v.setLayoutParams(params);
					requestLayout();
				} else if(Math.abs(event.getY() - startY) > 20){ // move threshold
					moving = true;
				} 
				return true;
			}
			case MotionEvent.ACTION_CANCEL: {
//				v.setActivated(false);
//				selectView(null);
				return true;
			}
			case MotionEvent.ACTION_UP:{
				EpicuriReservation r = (EpicuriReservation)v.getTag();
				if(moving){
					long diff = ((LayoutParams)v.getLayoutParams()).topMargin * 60 / HEIGHT_PER_HOUR ;
					r.setStartDate(new Date(60000L*diff + today.getTime()));
					if(null != listener){
						listener.onReservationSelected(activeReservation);
					}
					notifyDatasetUpdated();
				}
				return true;
			}	
			}
			return false;
		}
	};

	public void setDate(Calendar now) {
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		
		today = now.getTime();
		if(null != reservations){
			setReservations(reservations); //TODO: do this in layout
		}
	}
	
	OnReservationSelectedListener listener;
	public void setOnReservationSelectedListener(OnReservationSelectedListener listener){
		this.listener = listener;
	}
}
