package uk.co.epicuri.serverapi.common.pojo.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.*;
import uk.co.epicuri.serverapi.db.TableNames;

@Document(collection = TableNames.PRINTERS)
public class Printer extends Deletable {
    @MgmtEditableField(editable = false)
    @MgmtSetTopLevelId
    @Indexed
    private String restaurantId;

    @MgmtDisplayField
    private String name;
    private String ip;

    @MgmtIgnoreField
    private String redirect; // id of another printer

    private String duplicateTo;
    private String macAddress;

    private PrinterType printerType = PrinterType.NONE;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public PrinterType getPrinterType() {
        return printerType;
    }

    public void setPrinterType(PrinterType printerType) {
        this.printerType = printerType;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && this.getClass() == o.getClass() && EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getDuplicateTo() {
        return duplicateTo;
    }

    public void setDuplicateTo(String duplicateTo) {
        this.duplicateTo = duplicateTo;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
