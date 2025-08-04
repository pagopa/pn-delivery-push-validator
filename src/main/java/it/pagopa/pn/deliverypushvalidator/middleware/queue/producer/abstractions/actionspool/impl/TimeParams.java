package it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.impl;

import lombok.Data;

import java.time.Duration;

@Data
public class TimeParams {
    private Duration attachmentRetentionTimeAfterValidation;
    private Duration checkAttachmentTimeBeforeExpiration;
}
