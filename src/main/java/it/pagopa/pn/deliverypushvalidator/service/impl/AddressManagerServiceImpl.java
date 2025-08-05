package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;

import it.pagopa.pn.deliverypushvalidator.action.utils.NotificationUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.AcceptedResponse;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.AnalogAddress;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.NormalizeItemsRequest;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.addressmanager.model.NormalizeRequest;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.addressmanager.AddressManagerClient;
import it.pagopa.pn.deliverypushvalidator.service.AddressManagerService;
import it.pagopa.pn.deliverypushvalidator.service.mapper.AddressManagerMapper;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@CustomLog
@AllArgsConstructor
@Service
public class AddressManagerServiceImpl implements AddressManagerService {

    private final AddressManagerClient addressManagerClient;
    
    public Mono<AcceptedResponse> normalizeAddresses(NotificationInt notification, String correlationId){
        log.debug("Start normalize and validate address - iun={}", notification.getIun());
        
        NormalizeItemsRequest normalizeItemsRequest = getRequest(notification, correlationId);
        return addressManagerClient.normalizeAddresses(normalizeItemsRequest);
    }

    @NotNull
    private NormalizeItemsRequest getRequest(NotificationInt notification, String correlationId) {
        log.debug("Start getRequest - iun={} corrId={}", notification.getIun(), correlationId);
        
        List<NormalizeRequest> normalizeRequestList = new ArrayList<>();

        notification.getRecipients().forEach(recipient -> {
            int recIndex = NotificationUtils.getRecipientIndexFromTaxId(notification, recipient.getTaxId());

            if(recipient.getPhysicalAddress() != null){
                
                NormalizeRequest normalizeRequest = new NormalizeRequest();

                AnalogAddress address = AddressManagerMapper.getAnalogAddressFromPhysical(recipient.getPhysicalAddress());

                normalizeRequest.setAddress(address);
                normalizeRequest.setId(Integer.toString(recIndex));

                normalizeRequestList.add(normalizeRequest);

                log.debug("Add normalize request for recIndex={} - iun={} corrId={}", recIndex, notification.getIun(), correlationId);
            } else {
                handleError(notification, correlationId, recIndex);
            }
            
        });
        
        log.debug("Normalize itemRequest created with size={} - iun={} corrId={}",  normalizeRequestList.size(), notification.getIun(), correlationId);

        NormalizeItemsRequest normalizeItemsRequest = new NormalizeItemsRequest();
        normalizeItemsRequest.setRequestItems(normalizeRequestList);
        normalizeItemsRequest.setCorrelationId(correlationId);

        return normalizeItemsRequest;
    }

    private static void handleError(NotificationInt notification, String correlationId, int recIndex) {
        String errorMsg = String.format(
                "Recipient haven't physicalAddress - iun=%s recIndex=%d correlationId=%s",
                notification.getIun(),
                recIndex,
                correlationId
        );
        log.fatal(errorMsg);
        throw new PnInternalException(errorMsg, PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_PHYSICAL_ADDRESS_NOT_PRESENT);
    }
}
