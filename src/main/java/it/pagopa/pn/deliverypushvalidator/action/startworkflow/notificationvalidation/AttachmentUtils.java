package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationDocumentInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileDownloadResponseInt;
import it.pagopa.pn.deliverypushvalidator.exception.*;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.model.UpdateFileMetadataRequest;
import it.pagopa.pn.deliverypushvalidator.service.SafeStorageService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_ATTACHMENTCHANGESTATUSFAILED;

@Component
@CustomLog
@AllArgsConstructor
public class AttachmentUtils {
    private static final String VALIDATE_ATTACHMENT_PROCESS = "Validate attachment";

    private final SafeStorageService safeStorageService;
    private final PnDeliveryPushValidatorConfigs pnDeliveryPushConfigs;

    public void validateAttachment(NotificationInt notification ) throws PnValidationException {
        log.logChecking(VALIDATE_ATTACHMENT_PROCESS);
        forEachAttachment(notification, this::checkAttachment, false);
        log.logCheckingOutcome(VALIDATE_ATTACHMENT_PROCESS, true);
    }

    public void changeAttachmentsStatusToAttached(NotificationInt notification ) {
        log.info( "changeAttachmentsStatusToAttached iun={}", notification.getIun());

        forEachAttachment(notification, this::changeAttachmentStatusToAttached, true);
    }

    private void forEachAttachment(NotificationInt notification, Consumer<NotificationDocumentInt> callback, boolean includeF24Metadata)
    {
        for(NotificationDocumentInt attachment : notification.getDocuments()) {
            callback.accept(attachment);
        }

        for(NotificationRecipientInt recipient : notification.getRecipients()) {
            if(recipient.getPayments() != null) {
                recipient.getPayments().forEach(
                        payment -> {
                            if(payment.getPagoPA() != null && payment.getPagoPA().getAttachment() != null) {
                                callback.accept(payment.getPagoPA().getAttachment());
                            }

                            if(includeF24Metadata && payment.getF24() != null && payment.getF24().getMetadataAttachment() != null) {
                                callback.accept(payment.getF24().getMetadataAttachment());
                            }
                        }
                );
            }
        }
    }

    private void checkAttachment(NotificationDocumentInt attachment) {
        NotificationDocumentInt.Ref ref = attachment.getRef();

        FileDownloadResponseInt fd = MDCUtils.addMDCToContextAndExecute(
                safeStorageService.getFile(ref.getKey(),false)
                        .onErrorResume(PnFileNotFoundException.class, this::handleNotFoundError)
                        .onErrorResume(PnFileGoneException.class, this::handleGoneError)
        ).block();

        if(fd != null){
            String attachmentKey = fd.getKey();
            log.debug( "Check preload digest for attachment with key={}", attachmentKey);
            if ( !attachment.getDigests().getSha256().equals( fd.getChecksum() )) {
                final String errorDetail = "Validation failed, different sha256 expected=" + attachment.getDigests().getSha256()
                        + " actual=" + fd.getChecksum();
                log.logCheckingOutcome(VALIDATE_ATTACHMENT_PROCESS, false, errorDetail);

                throw new PnValidationNotMatchingShaException(errorDetail);
            }

            // check della size, con -1 si intende disabilitata
            if ( !pnDeliveryPushConfigs.getCheckPdfSize().isNegative() && pnDeliveryPushConfigs.getCheckPdfSize().toBytes() < fd.getContentLength().longValue() ) {
                final String errorDetail = "Validation failed, file too big, max expected=" +  pnDeliveryPushConfigs.getCheckPdfSize()
                        + "  actual=" + DataSize.ofBytes(fd.getContentLength().longValue());
                log.logCheckingOutcome(VALIDATE_ATTACHMENT_PROCESS, false, errorDetail);

                throw new PnValidationPDFTooBigValidException(errorDetail);
            }

            // check del contenuto del documento, che sia un PDF
            if (pnDeliveryPushConfigs.isCheckPdfValidEnabled()) {
                // scarico una porzione di pdf (per fare il check per ora, mi interessa controllare che inizi per %PDF-)
                byte[] pieceOfPdf = this.safeStorageService.downloadPieceOfContent(fd.getKey(), fd.getDownload().getUrl(), 1024).block();

                if ( !checkIsPDF(pieceOfPdf) )
                {
                    final String errorDetail = "Validation failed, file pdf check failed";
                    log.logCheckingOutcome(VALIDATE_ATTACHMENT_PROCESS, false, errorDetail);

                    throw new PnValidationPDFNotValidException(errorDetail);
                }
            }


        } else {
            final String errorDetail = "Validation failed, different sha256 expected=" + attachment.getDigests().getSha256()
                    + " actual=" + null;
            log.logCheckingOutcome(VALIDATE_ATTACHMENT_PROCESS, false, errorDetail);
            
            throw new PnValidationNotMatchingShaException(errorDetail);
        }
    }


    @NotNull
    private Mono<FileDownloadResponseInt> handleNotFoundError(PnFileNotFoundException ex) {
        log.logCheckingOutcome(VALIDATE_ATTACHMENT_PROCESS, false, "handleNotFoundError:"+ex.getMessage());
        return Mono.error(
                new PnValidationFileNotFoundException(
                        ex.getMessage(),
                        ex.getCause()
                )
        );
    }

    @NotNull
    private Mono<FileDownloadResponseInt> handleGoneError(PnFileGoneException ex) {
        log.logCheckingOutcome(VALIDATE_ATTACHMENT_PROCESS, false, "handleGoneError:"+ex.getMessage());
        return Mono.error(
                new PnValidationFileGoneException(
                        ex.getMessage(),
                        ex.getCause()
                )
        );
    }

    private void changeAttachmentStatusToAttached(NotificationDocumentInt attachment) {
        NotificationDocumentInt.Ref ref = attachment.getRef();
        final String ATTACHED_STATUS = "ATTACHED";
        log.debug( "changeAttachmentStatusToAttached begin changing status for attachment with key={}", ref.getKey());

        MDCUtils.addMDCToContextAndExecute(
                updateFileMetadata(ref.getKey(), ATTACHED_STATUS, null)
                        .doOnSuccess( res -> log.info( "changeAttachmentStatusToAttached changed status for attachment with key={}", ref.getKey()))
        ).block();

    }

    private Mono<Void> updateFileMetadata(String fileKey, String statusRequest, OffsetDateTime retentionUntilRequest) {
        UpdateFileMetadataRequest request = new UpdateFileMetadataRequest();
        request.setStatus(statusRequest);
        request.setRetentionUntil(retentionUntilRequest);

        return safeStorageService.updateFileMetadata(fileKey, request)
                .flatMap( fd -> {
                    log.info( "Response updateFileMetadata returned={}",fd);

                    if (fd != null && !fd.getResultCode().startsWith("2"))
                    {
                        // è un FAIL
                        log.error("Cannot change metadata for attachment key={} result={}", fileKey, fd);
                        return Mono.error(new PnInternalException("Failed update metadata attachment", ERROR_CODE_DELIVERYPUSH_ATTACHMENTCHANGESTATUSFAILED));
                    }

                    return Mono.empty();

                });
    }

    private boolean checkIsPDF(byte[] data) {
        if (data == null || data.length < 4)
            return false;

        // check header
        return data[0] == 0x25 && // %
                data[1] == 0x50 && // P
                data[2] == 0x44 && // D
                data[3] == 0x46 && // F
                data[4] == 0x2D;   // -
    }
}
