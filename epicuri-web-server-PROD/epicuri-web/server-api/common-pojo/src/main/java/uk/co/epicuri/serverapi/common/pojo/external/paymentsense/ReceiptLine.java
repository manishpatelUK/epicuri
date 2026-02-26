package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish on 27/07/2017.
 *
 * "MERCHANT": [
 {
 "format": [
 "BOLD"
 ],
 "type": "TEXT",
 "value": "string"
 }
 */
public class ReceiptLine {
    private List<ReceiptLineFormat> format = new ArrayList<>();
    private ReceiptLineType type = ReceiptLineType.TEXT;
    private String value;

    public List<ReceiptLineFormat> getFormat() {
        return format;
    }

    public void setFormat(List<ReceiptLineFormat> format) {
        this.format = format;
    }

    public ReceiptLineType getType() {
        return type;
    }

    public void setType(ReceiptLineType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
