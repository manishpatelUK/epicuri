package uk.co.epicuri.waiter.model;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class EpicuriTable implements Parcelable, Cloneable, Serializable {
	private static final String TAG_TABLE_ID = "Id";
	private static final String TAG_NAME = "Name";
	
	private static final String TAG_SHAPE = "Shape";
	private static final String TAG_POSITION = "Position";
	
	private static final String TAG_POSITION_X = "X";
	private static final String TAG_POSITION_Y = "Y";
	private static final String TAG_POSITION_ROTATION = "Rotation";
	private static final String TAG_POSITION_WIDTH = "ScaleX";
	private static final String TAG_POSITION_HEIGHT = "ScaleY";
	
	private static final int MIN_DIMENSION = 50;
	
	public EpicuriTable cloneTable() {
		try {
			return (EpicuriTable)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public enum Shape {
		SQUARE("Square", 0x0),
		CIRCLE("Circle", 0x1);
		
		private final String stringDef;
		private final int id;
		Shape(String s, int id){
			stringDef = s;
			this.id = id;
		}

		@Override
		public String toString() {
			return stringDef;
		}
		
		public int getId() {
			return id;
		}

		public static Shape fromInt(int shape){
			for(Shape s: values()){
				if(s.id == shape){
					return s;
				}
			}
			throw new IllegalArgumentException("Unrecognised shape " + shape);
		}
	}

	public String getId() { return id; }
	private final String id;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private String name;
	

	public void setShape(Shape s) {
		this.shape = s;
	}
	
	/**
	 * get X coordinate
	 * @return
	 */
	public double getX(){return x;}
	private double  x;

	/**
	 * get Y coordinate
	 * @return
	 */
	public double getY(){return y;}
	private double y;

	public float getWidth(){return dimensions.width(); }

	public float getHeight(){return dimensions.height(); }

	
	private PointF[] handles = new PointF[3];
	
	/**
	 *  handles are top left (rotate) bottom right(scale) and middle(move)
	 */
	public PointF[] getHandles(){ return handles; }
	
	private double rotation = 0;
	/**
	 * rotation in degrees
	 * @return
	 */
	public double getRotation(){ return rotation; }
	
	private double sinAngle = 0;
	public double getSinAngle(){return sinAngle;}
	private double cosAngle = 0;
	public double getCosAngle(){return cosAngle;}
	
	private Shape shape;
	public Shape getShape(){return shape;}
	
	private RectF dimensions = new RectF();
	public RectF getDimensions(){ return dimensions; }
	
	public EpicuriTable (JSONObject tableJson) throws JSONException {
		this.id = tableJson.getString(TAG_TABLE_ID);
		this.shape = Shape.fromInt(tableJson.getInt(TAG_SHAPE));
		this.name = tableJson.getString(TAG_NAME);
		
		if(!tableJson.has(TAG_POSITION) || tableJson.isNull(TAG_POSITION)){
			dimensions.left = -10;
			dimensions.right = 10;
			dimensions.top = -10;
			dimensions.bottom = 10;
			x = 10;
			y = 10;
		} else {
			JSONObject positionJson = tableJson.getJSONObject(TAG_POSITION); 
			this.x = (float)positionJson.getDouble(TAG_POSITION_X);
			this.y = (float)positionJson.getDouble(TAG_POSITION_Y);
			this.rotation = (float)positionJson.getDouble(TAG_POSITION_ROTATION);

			this.dimensions.right = (float)positionJson.getDouble(TAG_POSITION_WIDTH) / 2;
			if(dimensions.right < 10) dimensions.right = 10;
			this.dimensions.left = -this.dimensions.right;
			this.dimensions.bottom= (float)positionJson.getDouble(TAG_POSITION_HEIGHT) / 2;
			if(dimensions.bottom < 10) dimensions.bottom= 10;
			this.dimensions.top = -this.dimensions.bottom;
		}		
		for(int i=0;i<handles.length;i++){
			handles[i] = new PointF();
		}
		recalculateHandles();
	}
	
	public void rotateTo(double rotation){
		while(rotation < 0){
			rotation += 360;
		}
		rotation %= 360;
		// round to nearest 15 degrees
		rotation = 15 * Math.round(rotation / 15);
		this.rotation = rotation;
		recalculateHandles();
	}
	public void translateTo(double newX, double newY){
		x = newX; y = newY;
	}
	public void scaleHeight(double newHeight){
		if(newHeight < MIN_DIMENSION) newHeight = MIN_DIMENSION;
		// round to nearest 10 units
		newHeight = 10 * Math.round(newHeight / 10);
		
		dimensions.bottom= (float) Math.abs(newHeight);
		dimensions.top = - dimensions.bottom;
		recalculateHandles();
	}
	public void scaleWidth(double newWidth){
		if(newWidth < MIN_DIMENSION) newWidth = MIN_DIMENSION;
		newWidth = 10 * Math.round(newWidth / 10);
		
		dimensions.right = (float) Math.abs(newWidth);
		dimensions.left = - dimensions.right;
		recalculateHandles();
	}
	
	private void recalculateHandles(){
		cosAngle = Math.cos(-rotation * Math.PI / 180);
		sinAngle = Math.sin(-rotation * Math.PI / 180);
		
		handles[0].x = (float) (dimensions.left * cosAngle + dimensions.top * sinAngle);
		handles[1].x = -handles[0].x;
		handles[2].x = 0;
		
		handles[0].y = (float) (dimensions.right * sinAngle + dimensions.top * cosAngle);
		handles[1].y = - handles[0].y;
		handles[2].y = 0;
	}
	@Override
	public String toString() {
		return getName();
	}
	
	
	@Override
	public int describeContents() {
		return 0;
	}
	public static final Parcelable.Creator<EpicuriTable> CREATOR = new Parcelable.Creator<EpicuriTable>() {

		@Override
		public EpicuriTable createFromParcel(Parcel source) {
			return new EpicuriTable(source);
		}

		@Override
		public EpicuriTable[] newArray(int size) {
			return new EpicuriTable[size];
		}
		
	};
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeSerializable(shape);
		dest.writeString(name);
	}
	private EpicuriTable(Parcel in){
		id = in.readString();
		shape = (Shape)in.readSerializable();
		name = in.readString();
	}
	
}
