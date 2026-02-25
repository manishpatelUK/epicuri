package uk.co.epicuri.serverapi.common.pojo.model;


import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.db.TableNames;

@Document(collection = TableNames.COUNTRIES)
public class Country extends Deletable {
    @MgmtDisplayField
    private String name;
    private String acronym;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Country country = (Country) o;

        if (name != null ? !name.equals(country.name) : country.name != null) return false;
        return acronym != null ? acronym.equals(country.acronym) : country.acronym == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (acronym != null ? acronym.hashCode() : 0);
        return result;
    }
}
