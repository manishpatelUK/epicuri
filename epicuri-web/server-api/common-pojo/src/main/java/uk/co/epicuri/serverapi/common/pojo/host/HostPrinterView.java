package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.common.pojo.model.PrinterType;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostPrinterView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("IP")
    private String ip;

    @JsonProperty("printerType")
    private PrinterType printerType = PrinterType.NONE;

    @JsonProperty("macAddress")
    private String macAddress;

    public HostPrinterView() {}
    public HostPrinterView(Printer printer) {
        this.id = printer.getId();
        this.name = printer.getName();
        this.ip = printer.getIp();
        this.printerType = printer.getPrinterType();
        this.macAddress = printer.getMacAddress();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public PrinterType getPrinterType() {
        return printerType;
    }

    public void setPrinterType(PrinterType printerType) {
        this.printerType = printerType;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
