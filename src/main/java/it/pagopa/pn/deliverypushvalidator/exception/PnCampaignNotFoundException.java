package it.pagopa.pn.deliverypushvalidator.exception;

import lombok.Getter;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_CAMPAIGN_NOT_FOUND;

@Getter
public class PnCampaignNotFoundException extends PnNotFoundException {

  public PnCampaignNotFoundException(String description) {
    super("Campaign not found", description, ERROR_CODE_CAMPAIGN_NOT_FOUND);
  }

}
