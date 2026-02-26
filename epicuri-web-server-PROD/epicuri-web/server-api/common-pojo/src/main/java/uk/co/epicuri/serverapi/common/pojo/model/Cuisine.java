package uk.co.epicuri.serverapi.common.pojo.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.db.TableNames;

@Document(collection = TableNames.CUISINES)
public class Cuisine extends Deletable {
    @Indexed(unique = true)
    @MgmtDisplayField
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
