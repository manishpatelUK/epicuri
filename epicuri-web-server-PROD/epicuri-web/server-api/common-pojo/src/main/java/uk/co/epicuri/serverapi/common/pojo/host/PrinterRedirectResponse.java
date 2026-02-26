package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrinterRedirectResponse  {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("From")
    private HostPrinterView from;

    @JsonProperty("To")
    private HostPrinterView to;

    public PrinterRedirectResponse(){}

    public PrinterRedirectResponse(Printer from, Printer to){
        this.from = new HostPrinterView(from);
        this.to = new HostPrinterView(to);
        this.id = from.getRedirect();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HostPrinterView getFrom() {
        return from;
    }

    public void setFrom(HostPrinterView from) {
        this.from = from;
    }

    public HostPrinterView getTo() {
        return to;
    }

    public void setTo(HostPrinterView to) {
        this.to = to;
    }

}
