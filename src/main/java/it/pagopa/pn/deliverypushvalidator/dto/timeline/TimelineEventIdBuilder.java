package it.pagopa.pn.deliverypushvalidator.dto.timeline;


import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.DeliveryModeInt;
import jakarta.validation.constraints.NotNull;

import javax.annotation.Nullable;

import static java.lang.Boolean.TRUE;

/**
 * Classe builder che permette di costruire un timelineEventId
 *
 * Il formato dello della stringa di input dovrà essere:
 * <CATEGORY_VALUE>;IUN_<IUN_VALUE>;RECINDEX_<RECINDEX_VALUE>...
 * tutti i value sono facoltativi, tranne il campo category.
 * Sarà responsabilità del builder concatenare ogni singolo value alla timelineEventId solo se non gli viene passato null.
 */
public class TimelineEventIdBuilder {

    public static final String DELIMITER = ".";

    private String iun = "";

    private String recIndex = "";

    private String category = "";

    private String source = "";

    private String sentAttemptMade = "";

    private String progressIndex = "";

    private String deliveryMode = "";

    private String contactPhase = "";

    private String correlationId = ""; // for national registries

    private String courtesyAddressType = "";

    private String paymentCode = "";

    private String isFirstSendRetry = "";

    private String optin = "";
    
    public TimelineEventIdBuilder withIun(@Nullable String iun) {
        if(iun != null)
            this.iun = DELIMITER.concat("IUN_").concat(iun);
        return this;
    }

    public TimelineEventIdBuilder withRecIndex(@Nullable Integer recIndex) {
        if(recIndex != null)
            this.recIndex = DELIMITER.concat("RECINDEX_").concat(recIndex + "");
        return this;
    }

    public TimelineEventIdBuilder withCategory(@NotNull String category) {
        this.category = category;
        return this;
    }

    public TimelineEventIdBuilder withDeliveryMode(@Nullable DeliveryModeInt deliveryMode) {
        if(deliveryMode != null)
            this.deliveryMode = DELIMITER.concat("DELIVERYMODE_").concat(deliveryMode.getValue());
        return this;
    }

    public TimelineEventIdBuilder withCorrelationId(@Nullable String correlationId) {
        if(correlationId != null)
            this.correlationId = DELIMITER.concat("CORRELATIONID_").concat(correlationId);
        return this;
    }

    public String build() {
        return new StringBuilder()
                .append(category)
                .append(iun)
                .append(recIndex)
                .append(courtesyAddressType)
                .append(source)
                .append(deliveryMode)
                .append(contactPhase)
                .append(isFirstSendRetry)
                .append(sentAttemptMade)
                .append(progressIndex)
                .append(correlationId)
                .append(paymentCode)
                .append(optin)
                .toString();
    }

}
