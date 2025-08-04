package it.pagopa.pn.deliverypushvalidator.service.mapper;


import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.UsedServicesInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.UsedServices;

public class UsedServicesMapper {
    private UsedServicesMapper() {
    }

    public static UsedServicesInt externalToInternal(UsedServices external) {
        return external != null ? UsedServicesInt.builder()
                .physicalAddressLookUp(external.getPhysicalAddressLookup())
                .build() : null;
    }
}
