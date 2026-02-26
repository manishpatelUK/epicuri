package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class EpicuriCustomer implements Parcelable {
	private static final String TAG_CUSTOMER = "Customer";
	private static final String TAG_ID = "Id";
	private static final String TAG_NAME = "Name";
	private static final String TAG_EMAIL = "email";
	private static final String TAG_NAME_FIRST = "Firstname";
	private static final String TAG_NAME_SURNAME = "Surname";
	private static final String TAG_ADDRESS = "Address";
	private static final String TAG_PHONENUMBER = "PhoneNumber";
	
	private static final String TAG_FAVOURITE_FOOD = "FavouriteFood";
	private static final String TAG_FAVOURITE_DRINK = "FavouriteDrink";
	private static final String TAG_HATED_FOOD = "HatedFood";
	private static final String TAG_ALLERGIES = "Allergies";
	private static final String TAG_BLACKMARKS = "IsBlackMarked";
	private static final String TAG_DIETARY_REQUIREMENTS = "DietaryRequirements";
	private static final String TAG_FOOD_PREFERENCES = "FoodPreferences";
	
	/*
	 * "FavouriteFood": "Micro Chips",
                    "FavouriteDrink": "Diet Iron Brew",
                    "HatedFood": "Salad",
                    "Allergies": {
                        "3": "Nuts"
                    },
                    "DietaryRequirements": {
                        "2": "Hindu"
                    },
                    "FoodPreferences": {
                        "1": "Butter"
                    }
	 */

	private final String id;
	private final String name;
	private final String phoneNumber;
	private final Address address;
	private final String favouriteFood;
	private final String favouriteDrink;
	private final String hatedFood;
	private final String[] allergies;
	private final String[] dietaryRequirements;
	private final String[] foodPreferences;
	private final boolean blackMarked;
	private final String email;

	public EpicuriCustomer(String name, String phoneNumber) {
		this.id = null;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.address = null;
		this.favouriteFood = null;
		this.favouriteDrink = null;
		this.hatedFood = null;
		this.allergies = new String[0];
		this.dietaryRequirements = new String[0];
		this.foodPreferences = new String[0];
		this.blackMarked = false;
		this.email = "";
	}

	public EpicuriCustomer(JSONObject customer) throws JSONException{
		JSONObject obj = customer.getJSONObject(TAG_NAME);
		if(obj.has(TAG_NAME_FIRST)){
			name = obj.getString(TAG_NAME_FIRST) + " " + obj.getString(TAG_NAME_SURNAME);
		} else {
			name = "ERROR-  no name";
		}
		if(customer.has(TAG_PHONENUMBER)){
			phoneNumber = customer.getString(TAG_PHONENUMBER);
		} else {
			phoneNumber = null;
		}
		if(customer.has(TAG_ADDRESS)){
			address = new Address(customer.getJSONObject(TAG_ADDRESS));
		} else {
			address = null;
		}
		id = customer.getString(TAG_ID);
		blackMarked = !customer.isNull(TAG_BLACKMARKS) && customer.getBoolean(TAG_BLACKMARKS);
		
		if(customer.isNull(TAG_FAVOURITE_FOOD)){
			favouriteFood = "None specified";
		} else {
			favouriteFood = customer.getString(TAG_FAVOURITE_FOOD);
		}
		
		if(customer.isNull(TAG_FAVOURITE_DRINK)){
			favouriteDrink = "None specified";
		} else {
			favouriteDrink = customer.getString(TAG_FAVOURITE_DRINK);
		}
		
		if(customer.isNull(TAG_HATED_FOOD)){
			hatedFood = "None specified";
		} else {
			hatedFood = customer.getString(TAG_HATED_FOOD);
		}
		
		if(customer.isNull(TAG_ALLERGIES)){
			allergies = new String[0];
		} else {
			JSONArray allergiesJson = customer.getJSONArray(TAG_ALLERGIES);
			allergies = new String[allergiesJson.length()];
			for(int i=0; i<allergies.length;i++){
				JSONObject o = allergiesJson.getJSONObject(i);
				allergies[i] = o.getString("Name");
			}
		}
		
		if(customer.isNull(TAG_FOOD_PREFERENCES)){
			foodPreferences = new String[0];
		} else {
			JSONArray preferencesJson = customer.getJSONArray(TAG_FOOD_PREFERENCES);
			foodPreferences = new String[preferencesJson.length()];
			for(int i=0; i<foodPreferences.length;i++){
				JSONObject o = preferencesJson.getJSONObject(i);
				foodPreferences[i] = o.getString("Name");
			}
		}
		
		if(customer.isNull(TAG_DIETARY_REQUIREMENTS)){
			dietaryRequirements = new String[0];
		} else {
			JSONArray dietaryRequirementsJson = customer.getJSONArray(TAG_DIETARY_REQUIREMENTS);
			dietaryRequirements = new String[dietaryRequirementsJson.length()];
			for(int i=0; i<dietaryRequirements.length;i++){
				JSONObject o = dietaryRequirementsJson.getJSONObject(i);
				dietaryRequirements[i] = o.getString("Name");
			}
		}

		if(customer.has(TAG_EMAIL)) {
			this.email = customer.getString(TAG_EMAIL);
		} else {
			this.email = "";
		}
	}
	
	public String getName(){ return name;}
	public String getPhoneNumber() { return phoneNumber; }
	public String getId(){ return id;}
	public Address getAddress() { return address; }

	public String getFavouriteFood() {
		return favouriteFood;
	}

	public String getFavouriteDrink() {
		return favouriteDrink;
	}

	public String getHatedFood() {
		return hatedFood;
	}

	public String[] getAllergies() {
		return allergies;
	}

	public String[] getDietaryRequirements() {
		return dietaryRequirements;
	}

	public String[] getFoodPreferences() {
		return foodPreferences;
	}

	public boolean isBlackMarked() {
		return blackMarked;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EpicuriCustomer that = (EpicuriCustomer) o;

        return id != null ? id.equals(that.id) : that.id == null;

	}

	@Override
	public int hashCode() {
        return id != null ? id.hashCode() : 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(id);
		dest.writeString(phoneNumber);
		dest.writeParcelable(address, 0);
		dest.writeByte(blackMarked ? (byte)1 : (byte)0);
		
		dest.writeString(favouriteFood);
		dest.writeString(favouriteDrink);
		dest.writeString(hatedFood);
		dest.writeStringArray(foodPreferences);
		dest.writeStringArray(allergies);
		dest.writeStringArray(dietaryRequirements);
		dest.writeString(email);
	}
	
	private EpicuriCustomer(Parcel in){
		name = in.readString();
		id =  in.readString();
		phoneNumber = in.readString();
		address = in.readParcelable(Address.class.getClassLoader());
		blackMarked = in.readByte() == 1;

		favouriteFood = in.readString();
		favouriteDrink = in.readString();
		hatedFood = in.readString();
		foodPreferences = in.createStringArray();
		allergies = in.createStringArray();
		dietaryRequirements = in.createStringArray();
		email = in.readString();
	}

	public static final Parcelable.Creator<EpicuriCustomer> CREATOR = new Creator<EpicuriCustomer>() {
		
		@Override
		public EpicuriCustomer[] newArray(int size) {
			return new EpicuriCustomer[size];
		}
		
		@Override
		public EpicuriCustomer createFromParcel(Parcel source) {
			return new EpicuriCustomer(source);
		}
	};
	
	public static class Checkin implements Parcelable {
		private static final String TAG_TIME= "Time";
		
		private final Date checkinTime;
		private final EpicuriCustomer customer;
		
		public final EpicuriCustomer getCustomer(){ return customer; }
		public Date getDate(){ return checkinTime;}
		
		public Checkin(JSONObject jsonObject) throws JSONException {
			JSONObject customerJson = jsonObject.getJSONObject(TAG_CUSTOMER);
			customer = new EpicuriCustomer(customerJson);
			checkinTime = new Date(1000L * jsonObject.getInt(TAG_TIME));
		}
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(checkinTime.getTime());
			dest.writeParcelable(customer, 0);
		}
		public static final Parcelable.Creator<Checkin> CREATOR = new Creator<Checkin>() {
			@Override
			public Checkin createFromParcel(Parcel source) {
				return new Checkin(source);
			}

			@Override
			public Checkin[] newArray(int size) {
				return new Checkin[size];
			}
		};
		
		@Override
		public String toString() {
			return customer.getName();
		}
		
		private Checkin(Parcel in){
			checkinTime = new Date(in.readLong());
			customer = in.readParcelable(EpicuriCustomer.class.getClassLoader());
		}

		@Override
		public boolean equals(Object o) {
			if(null == o || !(o instanceof Checkin)) return false;
			Checkin c = (Checkin)o;
			return c.getDate().equals(checkinTime) && c.customer.id.equals(customer.id);
		}

		@Override
		public int hashCode() {
			int result = checkinTime != null ? checkinTime.hashCode() : 0;
			result = 31 * result + (customer != null ? customer.hashCode() : 0);
			return result;
		}
	}
	
	public static class Address implements Parcelable,Serializable {
		private static final String TAG_STREET = "Street";
		private static final String TAG_TOWN = "Town";
		private static final String TAG_CITY = "City";
		private static final String TAG_POSTCODE = "PostCode";
		/*
		 *      "$id": "3",
       "Street": "north street tirk",
       "Town": "york",
       "City": "joeh",
       "PostCode": "yo105dg"
		 */
		private final CharSequence street;
		private final CharSequence town;
		private final CharSequence city;
		private final CharSequence postcode;
		
		public CharSequence getStreet() {
			return street;
		}

		public CharSequence getTown() {
			return town;
		}

		public CharSequence getCity() {
			return city;
		}

		public CharSequence getPostcode() {
			return postcode;
		}
		
		public Address(CharSequence street, CharSequence town, CharSequence city, CharSequence postcode){
			this.street = street;
			this.town = town;
			this.city = city;
			this.postcode = postcode;
		}
		
		public Address(JSONObject addressJson) throws JSONException {
			street = addressJson.isNull(TAG_STREET) ? null : addressJson.getString(TAG_STREET);
			town = addressJson.isNull(TAG_TOWN) ? null :addressJson.getString(TAG_TOWN);
			city = addressJson.isNull(TAG_CITY) ? null :addressJson.getString(TAG_CITY);
			postcode = addressJson.isNull(TAG_POSTCODE) ? null :addressJson.getString(TAG_POSTCODE);
		}
		
		public JSONObject toJson() throws JSONException{
			JSONObject obj = new JSONObject();
			obj.put(TAG_STREET, street);
			obj.put(TAG_TOWN, town);
			obj.put(TAG_CITY, city);
			obj.put(TAG_POSTCODE, postcode);
			return obj;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(CharSequence s: new CharSequence[]{street, town, city, postcode}){
				if(null != s && s.length() > 0){
					sb.append(s + "\n");
				}
			}
			return sb.toString();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(null == street ? null : street.toString());
			dest.writeString(null == town ? null : town.toString());
			dest.writeString(null == city ? null : city.toString());
			dest.writeString(null == postcode ? null : postcode.toString());
		}
		
		private Address(Parcel in){
			street = in.readString();
			town = in.readString();
			city = in.readString();
			postcode = in.readString();
		}
		
		public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<EpicuriCustomer.Address>() {

			@Override
			public Address createFromParcel(Parcel source) {
				return new Address(source);
			}

			@Override
			public Address[] newArray(int size) {
				return new Address[size];
			}
		};
		
		
	}
}
