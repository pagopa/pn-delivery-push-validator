package it.pagopa.pn.deliverypushvalidator.exception;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.NotificationRefusedErrorCodeInt.CAMPAIGN_NOT_FOUND;

@Getter
public class PnCampaignNotFoundException extends PnRuntimeException {

  public PnCampaignNotFoundException(String message, String description) {
    super(message, description, HttpStatus.NOT_FOUND.value(), String.valueOf(CAMPAIGN_NOT_FOUND), null, null);
  }

}
