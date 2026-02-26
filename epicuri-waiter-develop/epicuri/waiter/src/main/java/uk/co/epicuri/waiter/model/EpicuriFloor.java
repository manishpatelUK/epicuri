package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EpicuriFloor implements Parcelable {
	private static final String TAG_LAYOUT = "Layout";
	private static final String TAG_CAPACITY = "Capacity";
	private static final String TAG_ID = "Id";
	private static final String TAG_NAME = "Name";
	private static final String TAG_IMAGE = "ImageURL";
	private static final String TAG_LAYOUTS = "Layouts";
	
	private final String id;
	private final String name;
	private final String layoutId;
	private final int capacity;
	private final String floorBackgroundImage;
	private final ArrayList<Layout> layouts;
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLayoutId() {
		return layoutId;
	}

	public int getCapacity() {
		return capacity;
	}

	public ArrayList<Layout> getLayouts() {
		return layouts;
	}

	public String getFloorBackgroundImage() {
		return floorBackgroundImage;
	}

	public EpicuriFloor(JSONObject floorJson) throws JSONException{
		floorBackgroundImage = floorJson.getString(TAG_IMAGE);
		id = floorJson.getString(TAG_ID);
		name = floorJson.getString(TAG_NAME);
		capacity = floorJson.getInt(TAG_CAPACITY);
		if(!floorJson.isNull(TAG_LAYOUT)){
			layoutId = floorJson.getString(TAG_LAYOUT);
		} else {
			layoutId = "-1";
		}
		if(floorJson.has(TAG_LAYOUTS) && !floorJson.isNull(TAG_LAYOUTS)){
			JSONArray layoutsArray = floorJson.getJSONArray(TAG_LAYOUTS);
			layouts = new ArrayList<EpicuriFloor.Layout>(layoutsArray.length());
			for(int i=0; i<layoutsArray.length(); i++){
				layouts.add(new Layout(layoutsArray.getJSONObject(i)));
			}
		} else {
			layouts = null;
		}
	}

	public static EpicuriFloor getFloorWithId(List<EpicuriFloor> floors, String floorId) {
		for(EpicuriFloor f: floors){
			if(f.id.equals(floorId)){
				return f;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{Floor - ")
		.append("Id:" + id)
		.append("Name:" + name)
		.append("Capacity:" + capacity);
		
		return sb.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}
/*
 * URL";
	
	public final int id;
	public final String name;
	public final int layoutId;
	public final int capacity;
	public final String floorBackgroundImage;(non-Javadoc)
 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
 */
	
	public static final Parcelable.Creator<EpicuriFloor> CREATOR = new Creator<EpicuriFloor>() {
		
		@Override
		public EpicuriFloor[] newArray(int size) {
			return new EpicuriFloor[size];
		}
		
		@Override
		public EpicuriFloor createFromParcel(Parcel source) {
			return new EpicuriFloor(source);
		}
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(layoutId);
		dest.writeInt(capacity);
		dest.writeString(floorBackgroundImage);
		if(null == layouts){
			dest.writeParcelableArray(null, 0);
		} else {
			Layout[] layoutsArray = new Layout[layouts.size()];
			layouts.toArray(layoutsArray);
			dest.writeParcelableArray(layoutsArray, 0);
		}
	}
	
	private EpicuriFloor(Parcel in){
		id = in.readString();
		name = in.readString();
		layoutId = in.readString();
		capacity = in.readInt();
		floorBackgroundImage = in.readString();
		Parcelable[] tmp = in.readParcelableArray(Layout.class.getClassLoader());
		if(null == tmp){
			layouts = null;
		} else {
			layouts = new ArrayList<EpicuriFloor.Layout>(tmp.length);
			for(Parcelable p: tmp){
				layouts.add((Layout)p);
			}
		}
	}
	
	public static Layout newDummyLayout(String id, String name){
		return new Layout(id, name);
	}

	public static class Layout implements Parcelable {
		private final static String TAG_ID = "Id";
		private final static String TAG_NAME = "Name";
		private final static String TAG_UPDATED = "Updated";
		private final static String TAG_TEMPORARY = "Temporary";
		private final static String TAG_FLOOR = "Floor";
		private final static String TAG_TABLES = "Tables";
		
		private final String id;
		private final String name;
		private final String floorId;
		private final Date updated;
		private final boolean temporary;
		private final ArrayList<EpicuriTable> tables;
		private final boolean current;
		
		public String getId() {
			return id;
		}
		
		public boolean isCurrent() {
			return current;
		}

		private Layout(String id, String name){
			this.id = id;
			this.name = "CURRENT" + (name == null ? "" : " (" + name + ")");
			current = true;
			floorId = "-1";
			updated = null;
			temporary = true;
			tables = null;
		}

		public String getFloorId() {
			return floorId;
		}

		public String getName() {
			return name;
		}

		public Date getUpdated() {
			return updated;
		}

		public boolean isTemporary() {
			return temporary;
		}

		public ArrayList<EpicuriTable> getTables() {
			return tables;
		}
		
		public EpicuriTable getTable(String tableId) {
			for(EpicuriTable t: tables){
				if(t.getId().equals(tableId)) return t;
			}
			return null;
		}

		public Layout(JSONObject layoutJson) throws JSONException {
			id = layoutJson.getString(TAG_ID);
			name = layoutJson.getString(TAG_NAME);
			updated = new Date(1000L * layoutJson.getInt(TAG_UPDATED));
			temporary = layoutJson.getBoolean(TAG_TEMPORARY);
			floorId = layoutJson.getString(TAG_FLOOR);
			current = false;

			if(layoutJson.has(TAG_TABLES) && !layoutJson.isNull(TAG_TABLES)){
				JSONArray tablesJson = layoutJson.getJSONArray(TAG_TABLES);
				tables = new ArrayList<EpicuriTable>(tablesJson.length());
				for(int i=0; i<tablesJson.length(); i++){
					JSONObject tableJson = tablesJson.getJSONObject(i);
					tables.add(new EpicuriTable(tableJson));
				}
			} else {
				tables = null;
			}
		}

		@Override
		public String toString() {
			if(!temporary){
				return String.format("%s (modified %s)", name, LocalSettings.getDateFormatWithDate().format(updated));
			} else {
				return name;
			}
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(id);
			dest.writeString(name);
			dest.writeString(floorId);
			dest.writeLong(updated.getTime());
			dest.writeByte(temporary ? (byte)1: (byte)0);
			dest.writeParcelableArray(tables.toArray(new EpicuriTable[tables.size()]), 0);
			dest.writeByte(current ? (byte)1: (byte)0);
		}
		
		private Layout(Parcel in){
			id = in.readString();
			name = in.readString();
			floorId = in.readString();
			updated = new Date(in.readLong());
			temporary = in.readByte() == 1;
			Parcelable[] tmp = in.readParcelableArray(EpicuriTable.class.getClassLoader());
			tables = new ArrayList<EpicuriTable>(tmp.length);
			for(Parcelable t: tmp){
				tables.add((EpicuriTable)t);
			}
			current = in.readByte() == 1;
		}
		
		public static final Parcelable.Creator<Layout> CREATOR = new Creator<EpicuriFloor.Layout>() {
			
			@Override
			public Layout[] newArray(int size) {
				return new Layout[size];
			}
			
			@Override
			public Layout createFromParcel(Parcel source) {
				return new Layout(source);
			}
		};
	}
}
