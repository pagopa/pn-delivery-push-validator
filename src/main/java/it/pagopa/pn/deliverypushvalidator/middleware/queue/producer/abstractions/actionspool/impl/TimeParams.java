package it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.impl;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
public class TimeParams {
    @NotNull
    private Duration attachmentRetentionTimeAfterValidation;
    @NotNull
    private Duration checkAttachmentTimeBeforeExpiration;
}
