package uk.co.epicuri.serverapi.common.pojo.model;

import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.db.TableNames;

@Document(collection = TableNames.PREFERENCES)
public class Preference extends Deletable {
    private PreferenceType preferenceType;

    @MgmtDisplayField
    private String name;

    public Preference() {

    }

    public Preference(PreferenceType preferenceType, String name) {
        this.preferenceType = preferenceType;
        this.name = name;
    }

    public PreferenceType getPreferenceType() {
        return preferenceType;
    }

    public void setPreferenceType(PreferenceType preferenceType) {
        this.preferenceType = preferenceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
