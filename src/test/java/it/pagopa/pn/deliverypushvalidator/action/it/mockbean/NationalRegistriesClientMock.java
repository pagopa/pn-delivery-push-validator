package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.publicregistry.NationalRegistriesResponse;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.CheckTaxIdOK;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.PhysicalAddressesRequestBody;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.nationalregistries.model.RecipientAddressRequestBody;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.nationalregistries.NationalRegistriesClient;
import it.pagopa.pn.deliverypushvalidator.service.TimelineService;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class NationalRegistriesClientMock implements NationalRegistriesClient {

    private int getNationalRegistriesCalledTimes = 0;

    public static final String NOT_VALID = "NOT_VALID";
    public static final String EXCEPTION = "EXCEPTION";
    public static final String PHYS_ADDR_NOT_FOUND = "NOT_FOUND";
    public static final String PHYS_ADDR_ERROR = "ERROR";
    private ConcurrentMap<String, LegalDigitalAddressInt> digitalAddressResponse;
    private ConcurrentMap<String, LegalDigitalAddressInt> digitalAddressResponseSecondCycle;
    private final TimelineService timelineService;


    public NationalRegistriesClientMock(
            TimelineService timelineService
    ) {
        this.timelineService = timelineService;
    }

    public void clear() {
        this.digitalAddressResponse = new ConcurrentHashMap<>();
        this.digitalAddressResponseSecondCycle = new ConcurrentHashMap<>();
        this.getNationalRegistriesCalledTimes = 0;
    }

    public void addDigital(String key, LegalDigitalAddressInt value) {
        this.digitalAddressResponse.put(key,value);
        this.digitalAddressResponseSecondCycle.put(key,value);
    }

    public void addDigitalSecondCycle(String key, LegalDigitalAddressInt value) {
        this.digitalAddressResponseSecondCycle.put(key,value);
    }

    @Override
    public CheckTaxIdOK checkTaxId(String taxId) {
        if(taxId.contains(NOT_VALID)){
            return new CheckTaxIdOK()
                    .taxId(taxId)
                    .isValid(false)
                    .errorCode(CheckTaxIdOK.ErrorCodeEnum.B001_CHECK_TAX_ID_ERR01);
        } else if (taxId.contains(EXCEPTION)){
            throw new RuntimeException("mock exception from server");
        }

        return new CheckTaxIdOK()
                .taxId(taxId)
                .isValid(true);
    }

    @Override
    public List<NationalRegistriesResponse> sendRequestForGetPhysicalAddresses(PhysicalAddressesRequestBody physicalAddressesRequestBody) {
        String correlationId = physicalAddressesRequestBody.getCorrelationId();

        return physicalAddressesRequestBody.getAddresses().stream()
                .map(addr -> {
                    String taxId = addr.getTaxId();
                    var builder = NationalRegistriesResponse.builder()
                            .correlationId(correlationId)
                            .registry(addr.getRecipientType() == RecipientAddressRequestBody.RecipientTypeEnum.PF ? "ANPR" : "REG_IMPRESE")
                            .recIndex(addr.getRecIndex());
                    switch(taxId) {
                        case PHYS_ADDR_ERROR -> builder.error("Mocked error")
                            .errorStatus(HttpStatus.GATEWAY_TIMEOUT.value());
                        case PHYS_ADDR_NOT_FOUND -> builder.physicalAddress(null);
                        default -> builder.physicalAddress(defaultPhysicalAddress());
                    }

                    return builder.build();
                }).toList();
    }

    private PhysicalAddressInt defaultPhysicalAddress() {
        return PhysicalAddressInt.builder()
                .address("Test address")
                .at("At")
                .zip("00133")
                .municipality("Test municipality")
                .province("TS")
                .build();
    }
}
