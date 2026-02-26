package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Preferences implements Parcelable {
    public static final String TAG_DIETARY  = "DietaryRequirements";
    public static final String TAG_FOOD = "FoodPreferences";
    public static final String TAG_ALLERGIES = "Allergies";
    private ArrayList<Preference> dietaryRequirements;
    private ArrayList<Preference> foodPreferences;
    private ArrayList<Preference> allergies;
    private String asJson;

    public Preferences(JSONObject jsonObject) throws JSONException{
        dietaryRequirements = new ArrayList<>();
        foodPreferences = new ArrayList<>();
        allergies = new ArrayList<>();

        JSONArray dietaryReqJson = jsonObject.getJSONArray(TAG_DIETARY);
        for (int i = 0; i < dietaryReqJson.length(); i++){
            JSONObject dietJson = dietaryReqJson.getJSONObject(i);
            Preference diet = new Preference(dietJson);
            dietaryRequirements.add(diet);
        }

        JSONArray foodPrefJson = jsonObject.getJSONArray(TAG_FOOD);
        for (int i = 0; i < foodPrefJson.length(); i++){
            JSONObject foodJson = foodPrefJson.getJSONObject(i);
            Preference food = new Preference(foodJson);
            foodPreferences.add(food);
        }

        JSONArray allergiesJson = jsonObject.getJSONArray(TAG_ALLERGIES);
        for (int i = 0; i < allergiesJson.length(); i++){
            JSONObject allergyJson = allergiesJson.getJSONObject(i);
            Preference allergy = new Preference(allergyJson);
            allergies.add(allergy);
        }

        asJson = jsonObject.toString();
    }

    protected Preferences(Parcel in) {
        dietaryRequirements = in.createTypedArrayList(Preference.CREATOR);
        foodPreferences = in.createTypedArrayList(Preference.CREATOR);
        allergies = in.createTypedArrayList(Preference.CREATOR);
        asJson = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(dietaryRequirements);
        dest.writeTypedList(foodPreferences);
        dest.writeTypedList(allergies);
        dest.writeString(asJson);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Preferences> CREATOR = new Creator<Preferences>() {
        @Override
        public Preferences createFromParcel(Parcel in) {
            return new Preferences(in);
        }

        @Override
        public Preferences[] newArray(int size) {
            return new Preferences[size];
        }
    };

    public ArrayList<Preference> getDietaryRequirements() {
        return dietaryRequirements;
    }

    public void setDietaryRequirements(ArrayList<Preference> dietaryRequirements) {
        this.dietaryRequirements = dietaryRequirements;
    }

    public ArrayList<Preference> getFoodPreferences() {
        return foodPreferences;
    }

    public void setFoodPreferences(ArrayList<Preference> foodPreferences) {
        this.foodPreferences = foodPreferences;
    }

    public ArrayList<Preference> getAllergies() {
        return allergies;
    }

    public void setAllergies(ArrayList<Preference> allergies) {
        this.allergies = allergies;
    }

    public String toJson() {
        return asJson;
    }

    public String getAllergyKey(String allergy) {
        for (Preference prefAllergy :
                getAllergies()) {
            if (prefAllergy.getValue().equals(allergy)) return prefAllergy.getKey();
        }
        return null;
    }

    public String getDietKey(String diet) {
        for(Preference prefDiet: dietaryRequirements){
            if (prefDiet.getValue().equals(diet)) return prefDiet.getKey();
        }
        return null;
    }

    public String getAllergyByKey(String key) {
        for(Preference prefAllergy: getAllergies()){
            if (prefAllergy.getKey().equals(key)) return prefAllergy.getValue();
        }
        return null;
    }

    public String getDietByKey(String key) {
        for(Preference prefDiet: getDietaryRequirements()){
            if(prefDiet.getKey().equals(key)) return prefDiet.getValue();
        }
        return null;
    }
}
