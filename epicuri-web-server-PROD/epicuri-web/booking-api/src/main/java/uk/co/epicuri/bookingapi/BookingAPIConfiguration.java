package uk.co.epicuri.bookingapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 30/04/2015
 */
public class BookingAPIConfiguration extends Configuration{
    @Valid
    @NotNull
    private EpicuriAPIFactory epicuriFactory = new EpicuriAPIFactory();

    @Valid
    @NotNull
    private KxTickerplantFactory kxTickerplantFactory = new KxTickerplantFactory();

    @Valid
    @NotNull
    private KxSecurityDBFactory kxSecurityDBFactory = new KxSecurityDBFactory();

    @Valid
    @NotNull
    private KxAurusitDBFactory kxAurusitDBFactory = new KxAurusitDBFactory();

    @Valid
    @NotNull
    private KxStaticsFactory kxStaticsFactory = new KxStaticsFactory();

    @JsonProperty("kxtickerplant")
    public KxTickerplantFactory getKxTickerplantFactory() {
        return kxTickerplantFactory;
    }

    @JsonProperty("kxtickerplant")
    public void setKxTickerplantFactory(KxTickerplantFactory kxTickerplantFactory) {
        this.kxTickerplantFactory = kxTickerplantFactory;
    }

    @JsonProperty("kxsecurity")
    public KxSecurityDBFactory getKxSecurityDBFactory() {
        return kxSecurityDBFactory;
    }

    @JsonProperty("kxsecurity")
    public void setKxSecurityDBFactory(KxSecurityDBFactory kxSecurityDBFactory) {
        this.kxSecurityDBFactory = kxSecurityDBFactory;
    }

    @JsonProperty("kxaurusit")
    public KxAurusitDBFactory getKxAurusitDBFactory() {
        return kxAurusitDBFactory;
    }

    @JsonProperty("kxaurusit")
    public void setKxAurusitDBFactory(KxAurusitDBFactory kxAurusitDBFactory) {
        this.kxAurusitDBFactory = kxAurusitDBFactory;
    }

    @JsonProperty("kxstatics")
    public KxStaticsFactory getKxStaticsFactory() {
        return kxStaticsFactory;
    }

    @JsonProperty("kxstatics")
    public void setKxStaticsFactory(KxStaticsFactory kxStaticsFactory) {
        this.kxStaticsFactory = kxStaticsFactory;
    }

    @JsonProperty("epicuriAPI")
    public EpicuriAPIFactory getEpicuriFactory() {
        return epicuriFactory;
    }

    @JsonProperty("epicuriAPI")
    public void setEpicuriFactory(EpicuriAPIFactory epicuriFactory) {
        this.epicuriFactory = epicuriFactory;
    }
}
