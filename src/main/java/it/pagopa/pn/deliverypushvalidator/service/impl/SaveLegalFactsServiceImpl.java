package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.deliverypushvalidator.legalfact.LegalFactGenerator;
import it.pagopa.pn.deliverypushvalidator.service.SafeStorageService;
import it.pagopa.pn.deliverypushvalidator.service.SaveLegalFactsService;
import it.pagopa.pn.deliverypushvalidator.service.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_SAVELEGALFACTSFAILED;


@Slf4j
@Service
public class SaveLegalFactsServiceImpl implements SaveLegalFactsService {

    public static final String SAVE_LEGAL_FACT_EXCEPTION_MESSAGE = "Generating %s legal fact for IUN=%s and recipientId=%s";
    public static final String LEGALFACTS_MEDIATYPE_STRING = "application/pdf";
    public static final String PN_LEGAL_FACTS = "PN_LEGAL_FACTS";
    public static final String SAVED = "SAVED";

    private final LegalFactGenerator legalFactBuilder;

    private final SafeStorageService safeStorageService;

    public SaveLegalFactsServiceImpl(LegalFactGenerator legalFactBuilder,
                                     SafeStorageService safeStorageService) {
        this.legalFactBuilder = legalFactBuilder;
        this.safeStorageService = safeStorageService;
    }

    public Mono<String> saveLegalFact(byte[] legalFact) {
        FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
        fileCreationRequest.setContentType(LEGALFACTS_MEDIATYPE_STRING);
        fileCreationRequest.setDocumentType(PN_LEGAL_FACTS);
        fileCreationRequest.setStatus(SAVED);
        fileCreationRequest.setContent(legalFact);

        return safeStorageService.createAndUploadContent(fileCreationRequest)
                .map( fileCreationResponse -> FileUtils.getKeyWithStoragePrefix(fileCreationResponse.getKey()));
    }

    public String sendCreationRequestForNotificationReceivedLegalFact(NotificationInt notification) {
        try {
            log.debug("Start sendCreationRequestForNotificationReceivedLegalFact - iun={}", notification.getIun());

            return MDCUtils.addMDCToContextAndExecute(
                    this.saveLegalFact(legalFactBuilder.generateNotificationReceivedLegalFact(notification))
                            .map( responseUrl -> {
                                log.debug("sendCreationRequestForNotificationReceivedLegalFact completed with fileKey={} - iun={}", responseUrl, notification.getIun());
                                return responseUrl;
                            })
            ).block();

        } catch (Exception exc) {
            String msg = String.format(SAVE_LEGAL_FACT_EXCEPTION_MESSAGE, "REQUEST_ACCEPTED", notification.getIun(), "N/A");
            log.error("Exception in saveNotificationReceivedLegalFact ex=", exc);
            throw new PnInternalException(msg, ERROR_CODE_DELIVERYPUSH_SAVELEGALFACTSFAILED, exc);
        }

    }

}
