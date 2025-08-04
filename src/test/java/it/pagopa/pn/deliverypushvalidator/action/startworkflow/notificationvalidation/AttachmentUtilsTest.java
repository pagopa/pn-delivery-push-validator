package it.pagopa.pn.deliverypushvalidator.action.startworkflow.notificationvalidation;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.exceptions.PnValidationException;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationRecipientTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.TestUtils;
import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.address.LegalDigitalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileDownloadInfoInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileDownloadResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.UpdateFileMetadataResponseInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnFileGoneException;
import it.pagopa.pn.deliverypushvalidator.exception.PnNotFoundException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationFileGoneException;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationNotMatchingShaException;
import it.pagopa.pn.deliverypushvalidator.service.SafeStorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import static it.pagopa.pn.deliverypushvalidator.exception.PnDeliveryPushValidatorExceptionCodes.ERROR_CODE_DELIVERYPUSH_NOTFOUND;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;


class AttachmentUtilsTest {

    private AttachmentUtils attachmentUtils;

    private SafeStorageService safeStorageService;

    private PnDeliveryPushValidatorConfigs pnDeliveryPushConfigs;

    @BeforeEach
    void init(){
        safeStorageService = Mockito.mock(SafeStorageService.class);
        pnDeliveryPushConfigs = Mockito.mock(PnDeliveryPushValidatorConfigs.class);
        attachmentUtils = new AttachmentUtils(safeStorageService, pnDeliveryPushConfigs);

    }


    @Test
    void validateAttachmentWithoutF24() {
        NotificationInt notification = TestUtils.getNotificationV2();
        when(safeStorageService.getFile(any(), any())).thenReturn(Mono.just(FileDownloadResponseInt.builder()
                .key("key")
                .checksum("sha256")
                .contentLength(BigDecimal.TEN)
                .download(FileDownloadInfoInt.builder().build())
                .contentType("contentType")
                .build()));
        when(safeStorageService.downloadPieceOfContent(any(), any(), anyLong())).thenReturn(Mono.just("%PDF-".getBytes(StandardCharsets.UTF_8)));
        when(pnDeliveryPushConfigs.isCheckPdfValidEnabled()).thenReturn(true);
        when(pnDeliveryPushConfigs.getCheckPdfSize()).thenReturn(DataSize.ofBytes(10));
        Assertions.assertDoesNotThrow(() -> attachmentUtils.validateAttachment(notification));
    }

    @Test
    void validateAttachmentDigestNotMatch() {
        NotificationInt notification = TestUtils.getNotificationV2();
        when(safeStorageService.getFile(any(), any())).thenReturn(Mono.just(FileDownloadResponseInt.builder()
                .key("key")
                .checksum("digest")
                .contentLength(BigDecimal.TEN)
                .download(FileDownloadInfoInt.builder().build())
                .contentType("contentType")
                .build()));
        when(pnDeliveryPushConfigs.isCheckPdfValidEnabled()).thenReturn(true);
        Assertions.assertThrows(PnValidationNotMatchingShaException.class,
                () -> attachmentUtils.validateAttachment(notification));
    }

    @Test
    void validateAttachmentWithF24() {
        NotificationInt notification = TestUtils.getNotificationV2WithF24();
        when(safeStorageService.getFile(any(), any())).thenReturn(Mono.just(FileDownloadResponseInt.builder()
                .key("key")
                .checksum("sha256")
                .contentLength(BigDecimal.TEN)
                .download(FileDownloadInfoInt.builder().build())
                .contentType("contentType")
                .build()));
        when(safeStorageService.downloadPieceOfContent(any(), any(), anyLong())).thenReturn(Mono.just("%PDF-".getBytes(StandardCharsets.UTF_8)));
        when(pnDeliveryPushConfigs.isCheckPdfValidEnabled()).thenReturn(true);
        when(pnDeliveryPushConfigs.getCheckPdfSize()).thenReturn(DataSize.ofBytes(10));

        Assertions.assertDoesNotThrow(() -> attachmentUtils.validateAttachment(notification));
    }

    @Test
    void validateAttachment() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        FileDownloadResponseInt resp1 = new FileDownloadResponseInt();
        resp1.setKey("abcd");
        resp1.setChecksum( "c2hhMjU2X2RvYzAw" );
        resp1.setDownload(FileDownloadInfoInt.builder().build());

        FileDownloadResponseInt resp2 = new FileDownloadResponseInt();
        resp2.setKey("abcd");
        resp2.setChecksum( "c2hhMjU2X2RvYzAx" );
        resp2.setDownload(FileDownloadInfoInt.builder().build());

        FileDownloadResponseInt resp3 = new FileDownloadResponseInt();
        resp3.setKey("keyPagoPaForm");
        resp3.setChecksum( "a2V5UGFnb1BhRm9ybQ==" );
        resp3.setDownload(FileDownloadInfoInt.builder().build());

        Mockito.when(safeStorageService.getFile( "c2hhMjU2X2RvYzAw", false)).thenReturn(Mono.just(resp1));
        Mockito.when(safeStorageService.getFile( "c2hhMjU2X2RvYzAx", false)).thenReturn(Mono.just(resp2));
        Mockito.when(safeStorageService.getFile( "keyPagoPaForm", false)).thenReturn(Mono.just(resp3));
        Mockito.when(pnDeliveryPushConfigs.getCheckPdfSize()).thenReturn(DataSize.ofBytes(-1));


        //WHEN
        attachmentUtils.validateAttachment(notification);

        //THEN
        Mockito.verify(safeStorageService, Mockito.times(2)).getFile(any(), Mockito.anyBoolean());
    }

    @Test
    void validateAttachmentFailDifferentKey() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        FileDownloadResponseInt resp = new FileDownloadResponseInt();
        resp.setKey("abcd");

        Mockito.when(safeStorageService.getFile(any(), Mockito.anyBoolean())).thenReturn(Mono.just(resp));

        //THEN
        assertThrows(PnValidationException.class, () -> attachmentUtils.validateAttachment(notification));
    }

    @Test
    void validateAttachmentFailErrorSafeStorage() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        FileDownloadResponseInt resp = new FileDownloadResponseInt();
        resp.setKey("abcd");

        String message = String.format("Get file failed for - fileKey=%s isMetadataOnly=%b", resp.getKey(), false);

        Mockito.when(safeStorageService.getFile(any(), Mockito.anyBoolean())).thenReturn(Mono.error(new PnNotFoundException("Not found", message, ERROR_CODE_DELIVERYPUSH_NOTFOUND)));

        //THEN
        assertThrows(PnNotFoundException.class, () -> attachmentUtils.validateAttachment(notification));
    }


    @Test
    void validateAttachmentFailTooBig() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        FileDownloadResponseInt resp = new FileDownloadResponseInt();
        resp.setKey("abcd");
        resp.setChecksum( "c2hhMjU2X2RvYzAw" );
        resp.setContentLength(BigDecimal.valueOf(100000));

        Mockito.when(pnDeliveryPushConfigs.getCheckPdfSize()).thenReturn(DataSize.ofBytes(100));
        Mockito.when(safeStorageService.getFile(any(), Mockito.anyBoolean())).thenReturn(Mono.just(resp));

        //THEN
        assertThrows(PnValidationException.class, () -> attachmentUtils.validateAttachment(notification));
    }


    @Test
    void validateAttachmentFailBadFile() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        FileDownloadResponseInt resp = new FileDownloadResponseInt();
        resp.setKey("abcd");
        resp.setChecksum( "c2hhMjU2X2RvYzAw" );
        resp.setContentLength(BigDecimal.valueOf(99));
        resp.setDownload(FileDownloadInfoInt.builder()
                .url("https://fileurl")
                .build());


        Mockito.when(pnDeliveryPushConfigs.getCheckPdfSize()).thenReturn(DataSize.ofBytes(100));
        Mockito.when(pnDeliveryPushConfigs.isCheckPdfValidEnabled()).thenReturn(true);
        Mockito.when(safeStorageService.getFile(any(), Mockito.anyBoolean())).thenReturn(Mono.just(resp));
        Mockito.when(safeStorageService.downloadPieceOfContent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(downloadPieceOfContent(false));

        //THEN
        assertThrows(PnValidationException.class, () -> attachmentUtils.validateAttachment(notification));
    }

    @Test
    void validateAttachmentOkFile() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        FileDownloadResponseInt resp = new FileDownloadResponseInt();
        resp.setKey("abcd");
        resp.setChecksum( "c2hhMjU2X2RvYzAw" );
        resp.setContentLength(BigDecimal.valueOf(99));
        resp.setDownload(FileDownloadInfoInt.builder()
                .url("https://fileurl")
                .build());

        FileDownloadResponseInt resp3 = new FileDownloadResponseInt();
        resp3.setKey("keyPagoPaForm");
        resp3.setChecksum( "a2V5UGFnb1BhRm9ybQ==" );
        resp3.setContentLength(BigDecimal.valueOf(99));
        resp3.setDownload(FileDownloadInfoInt.builder()
                .url("https://fileurl")
                .build());

        Mockito.when(pnDeliveryPushConfigs.getCheckPdfSize()).thenReturn(DataSize.ofBytes(100));
        Mockito.when(pnDeliveryPushConfigs.isCheckPdfValidEnabled()).thenReturn(true);
        Mockito.when(safeStorageService.getFile( "c2hhMjU2X2RvYzAw", false)).thenReturn(Mono.just(resp));
        Mockito.when(safeStorageService.getFile( "keyPagoPaForm", false)).thenReturn(Mono.just(resp3));
        Mockito.when(safeStorageService.downloadPieceOfContent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(downloadPieceOfContent(true));

        //THEN
        attachmentUtils.validateAttachment(notification);


        //THEN
        Mockito.verify(safeStorageService, Mockito.times(2)).getFile(any(), Mockito.anyBoolean());
    }

    @Test
    void changeAttachmentsStatusToAttached() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        UpdateFileMetadataResponseInt resp = new UpdateFileMetadataResponseInt();
        resp.setResultCode("200.00");

        Mockito.when(safeStorageService.updateFileMetadata(any(), any())).thenReturn(Mono.just(resp));

        //WHEN
        attachmentUtils.changeAttachmentsStatusToAttached(notification);

        //THEN
        Mockito.verify(safeStorageService, Mockito.times(2)).updateFileMetadata(any(), any());
    }

    @Test
    void changeAttachmentsStatusToAttachedFail() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);


        Mockito.when(safeStorageService.updateFileMetadata(any(), any())).thenThrow(new PnInternalException("test", "test"));

        //WHEN
        assertThrows(PnInternalException.class, () -> attachmentUtils.changeAttachmentsStatusToAttached(notification));

        //THEN
        Mockito.verify(safeStorageService, Mockito.times(1)).updateFileMetadata(any(), any());
    }


    @Test
    void changeAttachmentsStatusToAttachedFail400() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        UpdateFileMetadataResponseInt resp = new UpdateFileMetadataResponseInt();
        resp.setResultCode("400.00");

        Mockito.when(safeStorageService.updateFileMetadata(any(), any())).thenReturn(Mono.just(resp));

        //WHEN
        assertThrows(PnInternalException.class, () -> attachmentUtils.changeAttachmentsStatusToAttached(notification));

        //THEN
        Mockito.verify(safeStorageService, Mockito.times(1)).updateFileMetadata(any(), any());
    }

    private NotificationInt getNotificationInt(NotificationRecipientInt recipient) {
        return NotificationTestBuilder.builder()
                .withIun("iun_01")
                .withPaId("paId01")
                .withNotificationRecipient(recipient)
                .build();
    }

    private NotificationRecipientInt getNotificationRecipientInt() {
        String taxId = "TaxId";
        return NotificationRecipientTestBuilder.builder()
                .withTaxId(taxId)
                .withInternalId("ANON_" + taxId)
                .withDigitalDomicile(
                        LegalDigitalAddressInt.builder()
                                .address("address")
                                .type(LegalDigitalAddressInt.LEGAL_DIGITAL_ADDRESS_TYPE.PEC)
                                .build()
                )
                .withPayments(Collections.singletonList(
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .attachment(NotificationDocumentInt.builder()
                                                .ref(NotificationDocumentInt.Ref.builder()
                                                        .key("keyPagoPaForm")
                                                        .build())
                                                .digests(NotificationDocumentInt.Digests.builder()
                                                        .sha256(Base64.getEncoder().encodeToString("keyPagoPaForm".getBytes()))
                                                        .build())
                                                .build())
                                        .build())
                                .build()
                ))
                .build();
    }


    public Mono<byte[]> downloadPieceOfContent(boolean isPdf) {
        byte[] res = new byte[8];
        res[0] = 0x25;
        res[1] = 0x50;
        res[2] = 0x44;
        res[3] = 0x46;
        res[4] = 0x2D;
        res[5] = 0x2D;
        res[6] = 0x2D;
        res[7] = 0x2D;

        if (!isPdf)
            res[1] = 0x2D;

        return Mono.just(res);
    }

    @Test
    void validateAttachmentSafeStorageDeleted() {
        //GIVEN
        NotificationRecipientInt recipient = getNotificationRecipientInt();
        NotificationInt notification = getNotificationInt(recipient);

        FileDownloadResponseInt resp = new FileDownloadResponseInt();
        resp.setKey("abcd");

        String message = String.format("Get file failed for - fileKey=%s isMetadataOnly=%b", resp.getKey(), false);

        Mockito.when(safeStorageService.getFile(any(), Mockito.anyBoolean())).thenReturn(Mono.error(new PnFileGoneException("File removed", new RuntimeException())));

        //THEN
        assertThrows(PnValidationFileGoneException.class, () -> attachmentUtils.validateAttachment(notification));
    }
}
