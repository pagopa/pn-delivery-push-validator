package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;
import lombok.Getter;

import java.util.List;

@Getter
public class PnCampaignValidationException extends PnValidationException {

    public PnCampaignValidationException(String message, List<ProblemError> problemErrorList) {
        super(message, problemErrorList);
    }
}
