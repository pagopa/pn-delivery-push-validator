package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.*;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NewNotificationCostRequest;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.PaymentData;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.RecipientCostData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

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
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCostRecipients());
        assertTrue(result.getCostRecipients().isEmpty());
    }

    @Test
    void testMapNotificationToRequest_WithRecipientWithoutPayments() {
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
                                .payments(null)
                                .build()
                ))
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

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
    void testMapNotificationToRequest_WithRecipientWithEmptyPayments() {
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
                                .payments(Collections.emptyList())
                                .build()
                ))
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCostRecipients().size());
        RecipientCostData recipient = result.getCostRecipients().getFirst();
        assertNotNull(recipient.getPayments());
        assertTrue(recipient.getPayments().isEmpty());
    }

    @Test
    void testMapNotificationToRequest_WithRecipientWithF24Payment() {
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
                                                .f24(F24Int.builder()
                                                        .title("F24 Title")
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCostRecipients().size());
        RecipientCostData recipient = result.getCostRecipients().getFirst();
        assertNotNull(recipient.getPayments());
        // F24 payments don't have PagoPA, so they should not be included
        assertTrue(recipient.getPayments().isEmpty());
    }

    @Test
    void testMapNotificationToRequest_WithMixedPaymentTypes() {
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
                                                        .noticeCode("302000100000019421")
                                                        .applyCost(true)
                                                        .build())
                                                .build(),
                                        NotificationPaymentInfoInt.builder()
                                                .f24(F24Int.builder()
                                                        .title("F24 Title")
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCostRecipients().size());
        RecipientCostData recipient = result.getCostRecipients().getFirst();
        assertEquals(1, Objects.requireNonNull(recipient.getPayments()).size());
        assertEquals("77777777777##302000100000019421", recipient.getPayments().getFirst().getIuv());
    }

    @Test
    void testMapNotificationToRequest_WithApplyCostFalse() {
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
                                                        .noticeCode("302000100000019421")
                                                        .applyCost(false)
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCostRecipients().size());
        RecipientCostData recipient = result.getCostRecipients().getFirst();
        assertEquals(1, Objects.requireNonNull(recipient.getPayments()).size());
        assertFalse(recipient.getPayments().getFirst().getApplyCost());
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
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

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
    void testMapNotificationToRequest_WithDifferentNotificationFeePolicies() {
        // Test FLAT_RATE
        NotificationInt notificationFlatRate = buildCompleteNotification()
                .toBuilder()
                .notificationFeePolicy(NotificationFeePolicy.FLAT_RATE)
                .build();

        NewNotificationCostRequest resultFlatRate = mapper.mapNotificationToRequest(notificationFlatRate);
        assertEquals(
                it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NotificationFeePolicy.FLAT_RATE,
                resultFlatRate.getNotificationFeePolicy()
        );

        // Test DELIVERY_MODE
        NotificationInt notificationDeliveryMode = buildCompleteNotification()
                .toBuilder()
                .notificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .build();

        NewNotificationCostRequest resultDeliveryMode = mapper.mapNotificationToRequest(notificationDeliveryMode);
        assertEquals(
                it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.NotificationFeePolicy.DELIVERY_MODE,
                resultDeliveryMode.getNotificationFeePolicy()
        );
    }

    @Test
    void testMapNotificationToRequest_WithDifferentPagoPaIntModes() {
        // Test NONE
        NotificationInt notificationNone = buildCompleteNotification()
                .toBuilder()
                .pagoPaIntMode(PagoPaIntMode.NONE)
                .build();

        NewNotificationCostRequest resultNone = mapper.mapNotificationToRequest(notificationNone);
        assertEquals(
                it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.PagoPaIntMode.NONE,
                resultNone.getPagoPaIntMode()
        );

        // Test SYNC
        NotificationInt notificationSync = buildCompleteNotification()
                .toBuilder()
                .pagoPaIntMode(PagoPaIntMode.SYNC)
                .build();

        NewNotificationCostRequest resultSync = mapper.mapNotificationToRequest(notificationSync);
        assertEquals(
                it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.PagoPaIntMode.SYNC,
                resultSync.getPagoPaIntMode()
        );

        // Test ASYNC
        NotificationInt notificationAsync = buildCompleteNotification()
                .toBuilder()
                .pagoPaIntMode(PagoPaIntMode.ASYNC)
                .build();

        NewNotificationCostRequest resultAsync = mapper.mapNotificationToRequest(notificationAsync);
        assertEquals(
                it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.notificationcostservice.model.PagoPaIntMode.ASYNC,
                resultAsync.getPagoPaIntMode()
        );
    }

    @Test
    void testMapNotificationToRequest_IuvFormatting() {
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
                                                        .creditorTaxId("12345678901")
                                                        .noticeCode("987654321098765432")
                                                        .applyCost(true)
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .build();

        // When
        NewNotificationCostRequest result = mapper.mapNotificationToRequest(notification);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCostRecipients().size());
        assertNotNull(result.getCostRecipients().getFirst().getPayments());
        assertEquals(1, Objects.requireNonNull(result.getCostRecipients().getFirst().getPayments()).size());

        PaymentData payment = result.getCostRecipients().getFirst().getPayments().getFirst();
        assertEquals("12345678901##987654321098765432", payment.getIuv());
        assertTrue(payment.getIuv().contains("##"));
        String[] parts = payment.getIuv().split("##");
        assertEquals(2, parts.length);
        assertEquals("12345678901", parts[0]);
        assertEquals("987654321098765432", parts[1]);
    }
}
