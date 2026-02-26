package uk.co.epicuri.serverapi.common.pojo.model;

public enum PreferenceType {
    ALLERGY("Allergies"),
    FOOD("FoodPreferences"),
    DIETARY("DietaryRequirements");

    private final String key;
    PreferenceType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
