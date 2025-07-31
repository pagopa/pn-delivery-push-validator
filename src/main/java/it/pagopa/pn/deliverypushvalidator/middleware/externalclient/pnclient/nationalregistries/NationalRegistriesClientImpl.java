package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.nationalregistries;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.api.AddressApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.api.AgenziaEntrateApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.CheckTaxIdOK;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.CheckTaxIdRequestBody;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.CheckTaxIdRequestBodyFilter;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.PhysicalAddressesRequestBody;
import it.pagopa.pn.deliverypushvalidator.utils.NationalRegistriesMessageUtil;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@CustomLog
public class NationalRegistriesClientImpl extends CommonBaseClient implements NationalRegistriesClient {

    private final AddressApi addressApi;
    private final AgenziaEntrateApi agenziaEntrateApi;

    @Override
    public CheckTaxIdOK checkTaxId(String taxId) {
        log.logInvokingExternalService(CLIENT_NAME, CHECK_TAX_ID);

        CheckTaxIdRequestBody checkTaxIdRequestBody = new CheckTaxIdRequestBody()
                .filter(
                    new CheckTaxIdRequestBodyFilter()
                            .taxId(taxId)
                );

        return MDCUtils.addMDCToContextAndExecute(
                agenziaEntrateApi.checkTaxId(checkTaxIdRequestBody)
        ).block();
    }

    @Override
    public List<NationalRegistriesResponse> sendRequestForGetPhysicalAddresses(PhysicalAddressesRequestBody physicalAddressesRequestBody) {
        String correlationId = physicalAddressesRequestBody.getCorrelationId();
        log.logInvokingExternalService(CLIENT_NAME, GET_PHYSICAL_ADDRESSES);

        return MDCUtils.addMDCToContextAndExecute(
                addressApi.getPhysicalAddresses(physicalAddressesRequestBody)
                        .doOnSuccess(response -> log.info("Completed getPhysicalAddresses response={} - correlationId={}", response, correlationId))
                        .map(NationalRegistriesMessageUtil::buildPublicRegistryValidationResponse)
                        .doOnError(throwable -> log.error(String.format("Error calling getPhysicalAddresses with correlationId: %s", correlationId), throwable))
        ).block();
    }

}