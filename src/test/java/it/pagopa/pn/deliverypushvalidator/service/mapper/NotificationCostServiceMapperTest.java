package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.PagoPaIntMode;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationCostServiceMapperTest {

    @InjectMocks
    private NotificationCostServiceMapper mapper;

    private NotificationInt buildCompleteNotification() {
        return NotificationInt.builder()
                .iun("FAKE-IUN-123")
                .sender(NotificationSenderInt.builder()
                        .paId("pa-123")
                        .paTaxId("12345678901")
                        .paDenomination("Test PA")
                        .build())
                .vat(22)
                .paFee(100)
                .notificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .pagoPaIntMode(PagoPaIntMode.SYNC)
                .recipients(buildRecipients())
                .build();
    }

    private List<NotificationRecipientInt> buildRecipients() {
        List<NotificationRecipientInt> recipients = new ArrayList<>();

        // Recipient 1 with PagoPA payment
        recipients.add(NotificationRecipientInt.builder()
                .internalId("recipient-internal-1")
                .taxId("RSSMRA80A01H501U")
                .payments(List.of(
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .creditorTaxId("77777777777")
                                        .noticeCode("302000100000019421")
                                        .applyCost(true)
                                        .build())
                                .build()
                ))
                .build());

        // Recipient 2 with multiple payments
        recipients.add(NotificationRecipientInt.builder()
                .internalId("recipient-internal-2")
                .taxId("RSSMRA80A01H502U")
                .payments(List.of(
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .creditorTaxId("88888888888")
                                        .noticeCode("302000100000019422")
                                        .applyCost(false)
                                        .build())
                                .build(),
                        NotificationPaymentInfoInt.builder()
                                .pagoPA(PagoPaInt.builder()
                                        .creditorTaxId("99999999999")
                                        .noticeCode("302000100000019423")
                                        .applyCost(true)
                                        .build())
                                .build()
                ))
                .build());

        return recipients;
    }

    @Test
    void testMapNotificationToRequest_WithCompleteNotification() {
        // Given
        NotificationInt notification = buildCompleteNotification();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToNewNotificationCostRequest(notification);

        // Then
        assertNotNull(result);
        assertEquals("12345678901", result.getSenderTaxId());
        assertEquals("pa-123", result.getSenderPaId());
        assertEquals(22, result.getVat());
        assertEquals(100, result.getPaFee());
        assertEquals(
                it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NotificationFeePolicy.DELIVERY_MODE,
                result.getNotificationFeePolicy()
        );
        assertEquals(
                it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.PagoPaIntMode.SYNC,
                result.getPagoPaIntMode()
        );

        // Verify recipients
        assertNotNull(result.getCostRecipients());
        assertEquals(2, result.getCostRecipients().size());

        // Verify first recipient
        RecipientCostData recipient1 = result.getCostRecipients().getFirst();
        assertEquals(0, recipient1.getRecIndex());
        assertEquals("recipient-internal-1", recipient1.getRecipientInternalId());

        // Verify that only the payments related to the first recipient are present.
        assertNotNull(recipient1.getPayments());
        assertEquals(1, recipient1.getPayments().size());

        // Verify payment data structure (first payment)
        PaymentData payment1 = recipient1.getPayments().getFirst();
        assertEquals("77777777777##302000100000019421", payment1.getIuv());
        assertTrue(payment1.getApplyCost());

        // Verify second recipient
        RecipientCostData recipient2 = result.getCostRecipients().get(1);
        assertEquals(1, recipient2.getRecIndex());
        assertEquals("recipient-internal-2", recipient2.getRecipientInternalId());

        // Verifica che anche per il secondo recipient vengano restituiti SOLO i pagamenti ad esso relativi
        assertNotNull(recipient2.getPayments());
        assertEquals(2, recipient2.getPayments().size());
    }

    @Test
    void testMapNotificationToRequest_WithEmptyRecipients() {
        // Given
        NotificationInt notification = buildCompleteNotification()
                .toBuilder()
                .recipients(Collections.emptyList())
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToNewNotificationCostRequest(notification);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCostRecipients());
        assertTrue(result.getCostRecipients().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testMapNotificationToRequest_WithRecipientWithoutPayments(List<NotificationPaymentInfoInt> payments) {
        // Given
        NotificationInt notification = NotificationInt.builder()
                .sender(NotificationSenderInt.builder()
                        .paId("pa-123")
                        .paTaxId("12345678901")
                        .build())
                .vat(22)
                .paFee(100)
                .recipients(List.of(
                        NotificationRecipientInt.builder()
                                .internalId("recipient-internal-1")
                                .taxId("RSSMRA80A01H501U")
                                .payments(payments)
                                .build()
                ))
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToNewNotificationCostRequest(notification);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCostRecipients());
        assertEquals(1, result.getCostRecipients().size());

        RecipientCostData recipient = result.getCostRecipients().getFirst();
        assertEquals(0, recipient.getRecIndex());
        assertEquals("recipient-internal-1", recipient.getRecipientInternalId());
        assertNotNull(recipient.getPayments());
        assertTrue(recipient.getPayments().isEmpty());
    }

    @Test
    void testMapNotificationToRequest_WithMultipleRecipients() {
        // Given
        NotificationInt notification = NotificationInt.builder()
                .sender(NotificationSenderInt.builder()
                        .paId("pa-123")
                        .paTaxId("12345678901")
                        .build())
                .vat(22)
                .paFee(100)
                .recipients(List.of(
                        NotificationRecipientInt.builder()
                                .internalId("recipient-internal-1")
                                .taxId("RSSMRA80A01H501U")
                                .payments(List.of(
                                        NotificationPaymentInfoInt.builder()
                                                .pagoPA(PagoPaInt.builder()
                                                        .creditorTaxId("77777777777")
                                                        .noticeCode("111")
                                                        .applyCost(true)
                                                        .build())
                                                .build()
                                ))
                                .build(),
                        NotificationRecipientInt.builder()
                                .internalId("recipient-internal-2")
                                .taxId("RSSMRA80A01H502U")
                                .payments(List.of(
                                        NotificationPaymentInfoInt.builder()
                                                .pagoPA(PagoPaInt.builder()
                                                        .creditorTaxId("88888888888")
                                                        .noticeCode("222")
                                                        .applyCost(false)
                                                        .build())
                                                .build()
                                ))
                                .build(),
                        NotificationRecipientInt.builder()
                                .internalId("recipient-internal-3")
                                .taxId("RSSMRA80A01H503U")
                                .payments(List.of(
                                        NotificationPaymentInfoInt.builder()
                                                .pagoPA(PagoPaInt.builder()
                                                        .creditorTaxId("99999999999")
                                                        .noticeCode("333")
                                                        .applyCost(true)
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToNewNotificationCostRequest(notification);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getCostRecipients().size());

        // Verify indexes are correctly assigned
        assertEquals(0, result.getCostRecipients().get(0).getRecIndex());
        assertEquals(1, result.getCostRecipients().get(1).getRecIndex());
        assertEquals(2, result.getCostRecipients().get(2).getRecIndex());

        // Verify internal IDs
        assertEquals("recipient-internal-1", result.getCostRecipients().get(0).getRecipientInternalId());
        assertEquals("recipient-internal-2", result.getCostRecipients().get(1).getRecipientInternalId());
        assertEquals("recipient-internal-3", result.getCostRecipients().get(2).getRecipientInternalId());

        // Each recipient should only have its own payments (no sharing of payments across recipients)
        assertEquals(1, Objects.requireNonNull(result.getCostRecipients().get(0).getPayments()).size());
        assertEquals(1, Objects.requireNonNull(result.getCostRecipients().get(1).getPayments()).size());
        assertEquals(1, Objects.requireNonNull(result.getCostRecipients().get(2).getPayments()).size());
    }

    @Test
    void mapNotificationToNewInformalNotificationCostRequest_shouldMapSenderAndPayments() {
        NotificationInt notification = buildNotificationWithRecipients();

        NewInformalNotificationCostRequest result =
                mapper.mapNotificationToNewInformalNotificationCostRequest(notification);

        assertNotNull(result);
        assertEquals("12345678901", result.getSenderTaxId());
        assertEquals("pa-123", result.getSenderPaId());
        assertNotNull(result.getCostRecipients());
        assertEquals(2, result.getCostRecipients().size());

        InformalRecipientCostData recipient1 = result.getCostRecipients().getFirst();
        assertEquals(0, recipient1.getRecIndex());
        assertEquals("recipient-internal-1", recipient1.getRecipientInternalId());
        assertNotNull(recipient1.getPayments());
        assertEquals(1, recipient1.getPayments().size());
        assertEquals("77777777777##302000100000019421", recipient1.getPayments().getFirst().getIuv());

        InformalRecipientCostData recipient2 = result.getCostRecipients().get(1);
        assertEquals(1, recipient2.getRecIndex());
        assertEquals("recipient-internal-2", recipient2.getRecipientInternalId());
        assertNotNull(recipient2.getPayments());
        assertEquals(2, recipient2.getPayments().size());
        assertEquals(
                List.of("88888888888##302000100000019422", "99999999999##302000100000019423"),
                recipient2.getPayments().stream().map(InformalPaymentData::getIuv).toList()
        );
    }

    @Test
    void mapNotificationToNewInformalNotificationCostRequest_shouldReturnEmptyRecipientsWhenNotificationHasNoRecipients() {
        NotificationInt notification = NotificationInt.builder()
                .sender(NotificationSenderInt.builder()
                        .paId("pa-123")
                        .paTaxId("12345678901")
                        .build())
                .recipients(Collections.emptyList())
                .build();

        NewInformalNotificationCostRequest result =
                mapper.mapNotificationToNewInformalNotificationCostRequest(notification);

        assertNotNull(result);
        assertEquals("12345678901", result.getSenderTaxId());
        assertEquals("pa-123", result.getSenderPaId());
        assertNotNull(result.getCostRecipients());
        assertTrue(result.getCostRecipients().isEmpty());
    }

    @Test
    void mapNotificationToNewInformalNotificationCostRequest_shouldReturnEmptyRequestWhenNotificationIsNull() {
        NewInformalNotificationCostRequest result =
                mapper.mapNotificationToNewInformalNotificationCostRequest(null);

        assertNotNull(result);
        assertNull(result.getSenderTaxId());
        assertNull(result.getSenderPaId());
        assertNotNull(result.getCostRecipients());
        assertTrue(result.getCostRecipients().isEmpty());
    }

    @Test
    void mapNotificationToNewInformalNotificationCostRequest_shouldMapEmptyPaymentListForRecipientWithoutPayments() {
        NotificationInt notification = NotificationInt.builder()
                .sender(NotificationSenderInt.builder()
                        .paId("pa-123")
                        .paTaxId("12345678901")
                        .build())
                .recipients(List.of(
                        NotificationRecipientInt.builder()
                                .internalId("recipient-internal-1")
                                .taxId("RSSMRA80A01H501U")
                                .payments(null)
                                .build()
                ))
                .build();

        NewInformalNotificationCostRequest result =
                mapper.mapNotificationToNewInformalNotificationCostRequest(notification);

        assertNotNull(result);
        assertNotNull(result.getCostRecipients());
        assertEquals(1, result.getCostRecipients().size());

        InformalRecipientCostData recipient = result.getCostRecipients().getFirst();
        assertEquals(0, recipient.getRecIndex());
        assertEquals("recipient-internal-1", recipient.getRecipientInternalId());
        assertNotNull(recipient.getPayments());
        assertTrue(recipient.getPayments().isEmpty());
    }

    private NotificationInt buildNotificationWithRecipients() {
        return NotificationInt.builder()
                .sender(NotificationSenderInt.builder()
                        .paId("pa-123")
                        .paTaxId("12345678901")
                        .build())
                .recipients(List.of(
                        NotificationRecipientInt.builder()
                                .internalId("recipient-internal-1")
                                .taxId("RSSMRA80A01H501U")
                                .payments(List.of(
                                        NotificationPaymentInfoInt.builder()
                                                .pagoPA(PagoPaInt.builder()
                                                        .creditorTaxId("77777777777")
                                                        .noticeCode("302000100000019421")
                                                        .applyCost(true)
                                                        .build())
                                                .build()
                                ))
                                .build(),
                        NotificationRecipientInt.builder()
                                .internalId("recipient-internal-2")
                                .taxId("RSSMRA80A01H502U")
                                .payments(List.of(
                                        NotificationPaymentInfoInt.builder()
                                                .pagoPA(PagoPaInt.builder()
                                                        .creditorTaxId("88888888888")
                                                        .noticeCode("302000100000019422")
                                                        .applyCost(false)
                                                        .build())
                                                .build(),
                                        NotificationPaymentInfoInt.builder()
                                                .pagoPA(PagoPaInt.builder()
                                                        .creditorTaxId("99999999999")
                                                        .noticeCode("302000100000019423")
                                                        .applyCost(true)
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
}
