package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;

import java.util.List;

public class PnValidationDigitalAddressMissingException extends PnValidationException {
    public PnValidationDigitalAddressMissingException(List<Integer> recipients) {
        super("Validation failed, message not found", buildProblemErrorList(recipients));
    }

    private static List<ProblemError> buildProblemErrorList(List<Integer> recipients) {
        return recipients.stream().map(recIndex -> ProblemError.builder()
                .code(PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.DIGITAL_ADDRESS_MISSING.getValue())
                .detail("Recipient of type PG must have a non-blank digitalAddress when campaign workflow includes PEC")
                .element("recipients[" + recIndex + "].digitalDomicile.address")
                .build()).toList();
    }
}