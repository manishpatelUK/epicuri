package uk.co.epicuri.waiter.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Furniture;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.interfaces.OnDinerClickListener;
import uk.co.epicuri.waiter.interfaces.OnRotationGestureListener;

public class TableSeatingView extends View {
	private Paint paint;
	private Paint paintBorder;
	private Paint background;
	private Paint editBorder;
	private Bitmap dinerBitmap;
	private Bitmap dinerSelectedBitmap;
	private Bitmap customerBitmap;
	private Bitmap customerSelectedBitmap;
	private Rect dinerRect = new Rect(-80,-80,80,80);
	private EpicuriTable.Shape tableShape;
	
	/** if true, there must be a diner selected */
	private boolean forceSelection = false;
	private String selectedDinerId = "-1";

	boolean editMode = false;
	
	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		
		// deselect items
		if(!editMode) selectedFurniture = null;
		invalidate();
	}

	float scaleFactor = 1;

	private HashMap<String, Diner> dinerLookup;
	private List<Furniture> furniture = new LinkedList<Furniture>();
	Furniture selectedFurniture = null;
	
	int selectedColour;
	int unselectedColour;
	
	private RotationGestureDetector rotationDetector;
	
	public TableSeatingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintBorder.setColor(0xff000000);
		paintBorder.setStyle(Style.STROKE);
		paintBorder.setStrokeWidth(10);
		
		selectedColour = context.getResources().getColor(R.color.blue);
		unselectedColour = context.getResources().getColor(R.color.lightgray);
		dinerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.diner);
		dinerSelectedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.diner_selected);
		customerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.diner_epicuri);
		customerSelectedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.diner_epicuri_selected);
		
		background = new Paint();
		background.setColor(context.getResources().getColor(R.color.darkgray));
		
		editBorder = new Paint();
		editBorder.setColor(context.getResources().getColor(R.color.midgray));
		editBorder.setStyle(Style.FILL);
//		editBorder.setStrokeWidth(100);
		
		rotationDetector = new RotationGestureDetector(new OnRotationGestureListener() {
			
			@Override
			public boolean OnRotation(RotationGestureDetector rotationDetector) {
				return false;
			}
		});
	}
	
	public TableSeatingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TableSeatingView(Context context) {
		this(context, null);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float scaledX = event.getX() / scaleFactor;
		float scaledY = event.getY() / scaleFactor;
		
		rotationDetector.onTouchEvent(event);
		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			Furniture prevSelected = selectedFurniture;
			if(null != selectedFurniture && !forceSelection){
				// do something gestury
				selectedFurniture = null;
			} 
			double distance = -1;
			for(Furniture f: furniture){
				double thisDistance = Math.sqrt( ( (scaledX - f.getX()) * (scaledX - f.getX())) + ((scaledY - f.getY()) * (scaledY - f.getY())));
				if(thisDistance < 150 && (distance == -1 || thisDistance < distance)){
					selectedFurniture = f;
					distance = thisDistance;
				}
			}
			if(!editMode && prevSelected == selectedFurniture && !forceSelection) selectedFurniture = null; // click twice to deselect
			invalidate();
			return true;
		} else if(editMode && null != selectedFurniture){
			selectedFurniture.setRotation(selectedFurniture.getRotation() - rotationDetector.getAngle());
			if(event.getAction() == MotionEvent.ACTION_MOVE){
				// prevent diner from being dragged outside image bounds
				if(scaledX < 0) scaledX = 0;
				else scaledX = Math.min(scaledX, getWidth() / scaleFactor);

				if(scaledY < 0) scaledY = 0;
				else scaledY = Math.min(scaledY, getHeight() / scaleFactor);

				selectedFurniture.setPosition(scaledX, scaledY);
			}
			invalidate();
			return true;
		} else if(event.getAction() == MotionEvent.ACTION_UP){
			if(editMode){
				return false;
			} else {
				if(null != dinerClickListener){
					// TODO: select diner
					if(null == selectedFurniture){
						dinerClickListener.onDinerClick(null);
					} else {
						dinerClickListener.onDinerClick(dinerLookup.get(selectedFurniture.getDinerId()));
					}
				}
				invalidate();
				return true;
			}
		}

		return super.onTouchEvent(event);
	}
	
	/**
	 * select the diner with the matching id.  does not fire the changeListener
	 * @param diner
	 */
	public void selectDiner(Diner diner){
		if(null == diner){
			selectedFurniture = null;
		} else {
			for(Furniture f: furniture){
				if(f.getDinerId() != null && f.getDinerId().equals(diner.getId())){
					selectedFurniture = f;
					break;
				}
			}
		}
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		if(parentHeight > parentWidth){
			this.setMeasuredDimension(parentWidth, parentWidth);
		} else {
			this.setMeasuredDimension(parentHeight, parentHeight);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if(w>h){
			scaleFactor = h / 1000f;
		} else {
			scaleFactor = w / 1000f;
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	private Rect textBounds = new Rect();

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.scale(scaleFactor, scaleFactor);
		canvas.drawRect(0, 0, 1000, 1000, background);
		if(editMode){
			canvas.drawRect(0, 0, 1000, 1000, editBorder);
		}
		for(Furniture f: furniture){
			paint.setColor((f == selectedFurniture) ? selectedColour : unselectedColour);
			canvas.save();
			canvas.translate(f.getX(), f.getY());
			Diner d = dinerLookup.get(f.getDinerId());
			if(d == null){
				// do nothing
			}else if(d.isTable()){
				canvas.rotate(f.getRotation());
				RectF ovalShape = new RectF(-f.getWidth(), -f.getBreadth(), f.getWidth(), f.getBreadth());
				if(null == tableShape || tableShape == EpicuriTable.Shape.SQUARE){
					canvas.drawRoundRect(ovalShape, 20, 20, paint);
					canvas.drawRoundRect(ovalShape, 20, 20, paintBorder);
				} else {
					canvas.drawOval(ovalShape, paint);
					canvas.drawOval(ovalShape, paintBorder);
				}
			} else {
				Bitmap b;
				if(null == d.getEpicuriCustomer()){
					b = (f == selectedFurniture) ? dinerSelectedBitmap : dinerBitmap;
				} else {
					b = (f == selectedFurniture) ? customerSelectedBitmap : customerBitmap;
				}
				canvas.drawBitmap(b, null, dinerRect, paint);
			}
			canvas.restore();
		}
	}
	
	private OnDinerClickListener dinerClickListener = null;
	public void setOnTableChangeListener(OnDinerClickListener dinerChangeListener){
		this.dinerClickListener = dinerChangeListener;
	}
	
	public void setLayout(List<Furniture> furniture, List<Diner> diners, EpicuriTable.Shape tableShape) {
		this.tableShape = tableShape;
		if(null != furniture){
			this.furniture = furniture;
		} else {
			this.furniture = new ArrayList<EpicuriSessionDetail.Furniture>(diners.size());
			
			Point[] customerPoints;
			
			final int numberOfDiners = diners.size() - 1; // one is the table
			switch(numberOfDiners){
			case 1:
			case 2:
				customerPoints = new Point[2];
				customerPoints[0] = new Point(500, 220);
				customerPoints[1] = new Point(500, 780);
				break;
			case 3:
			case 4:
				customerPoints = new Point[4];
				customerPoints[0] = new Point(280, 220);
				customerPoints[1] = new Point(720, 220);
				customerPoints[2] = new Point(280, 780);
				customerPoints[3] = new Point(720, 780);
				break;
			case 5:
			case 6:
				customerPoints = new Point[6];
				customerPoints[0] = new Point(280, 220);
				customerPoints[1] = new Point(720, 220);
				customerPoints[2] = new Point(280, 780);
				customerPoints[3] = new Point(720, 780);
				customerPoints[4] = new Point(100, 500);
				customerPoints[5] = new Point(900, 500);
				break;
			default:
				customerPoints = new Point[numberOfDiners];
				double increment = Math.PI * 2 / numberOfDiners;
				for(int i=0; i<numberOfDiners; i++){
					int x = (int)(Math.floor(500 + 300 * Math.sin(i * increment)));
					int y = (int)(Math.floor(500 + 300 * Math.cos(i * increment)));
					customerPoints[i] = new Point(x,y);
				}
			}
			
			int dinerId = 0;
			for(Diner d: diners){
				int x;
				int y;
				
				if(d.isTable()){
					x = 500;
					y = 500;
				} else {
					x = customerPoints[dinerId].x;
					y = customerPoints[dinerId].y;
					dinerId++;
				}
				String jsonString = String.format(Locale.UK, "{\"x\":%d,\"y\":%d,\"width\":150,\"breadth\":200,\"rotation\":90, \"dinerId\":%s}", x, y, d.getId());
				try{
					Furniture f = new Furniture(new JSONObject(jsonString));
					this.furniture.add(f);
				} catch (JSONException e){
					e.printStackTrace();
				}
			}
		}
		dinerLookup = new HashMap<>(diners.size());
		for(EpicuriSessionDetail.Diner diner: diners){
			if(forceSelection && diner.isTable()){
				selectedDinerId = diner.getId();
			}
			dinerLookup.put(diner.getId(), diner);
		}
		
		// persist selected furniture when redrawing
		if(null != selectedFurniture){
			for(Furniture f: this.furniture){
				if(f.getDinerId().equals(selectedFurniture.getDinerId())){
					selectedFurniture = f;
					break;
				}
			}
		}
		invalidate();
	}
	
	public String getTableDefinition(){
		JSONArray furnitureJson = new JSONArray();
		for(Furniture furn: furniture){
			furnitureJson.put(furn.toJson());
		}
		return furnitureJson.toString();
	}

	public void forceSelect(boolean forceSelection) {
		this.forceSelection = forceSelection;
//		if(null == selectedFurniture){
//			for(Furniture f: furniture){
//				Diner d = dinerLookup.get(f.getDinerId()); 
//				if(d.isTable()){
//					selectDiner(d);
//					selectedFurniture = f;
//					invalidate();
//					return;
//				}
//			}
//		}
	}

	
}
