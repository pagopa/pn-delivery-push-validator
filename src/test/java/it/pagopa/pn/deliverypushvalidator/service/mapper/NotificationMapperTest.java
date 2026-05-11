package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationRecipientTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.NotificationTestBuilder;
import it.pagopa.pn.deliverypushvalidator.action.it.utils.PhysicalAddressBuilder;
import it.pagopa.pn.deliverypushvalidator.dto.address.PhysicalAddressInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.datavault.RecipientTypeInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationRecipientInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationPaymentInfoInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class NotificationMapperTest {


    @Test
    void internalToExternal() {
        String denomination = "Mario rossi";
        NotificationInt expected = NotificationTestBuilder.builder()
                .withIun("IUN01")
                .withNotificationRecipient( NotificationRecipientTestBuilder.builder()
                        .withTaxId("TAXID01")
                        .withDenomination(denomination)
                        .withPhysicalAddress(PhysicalAddressBuilder.builder()
                                .withAddress("Via Nuova")
                                .withFullName(denomination)
                                .build())
                        .build())
                .withNotificationFeePolicy(it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.delivery.model.NotificationFeePolicy.DELIVERY_MODE)
                .build();
        expected = expected.toBuilder()
                .version("v1")
                .vat(22)
                .build();
        
        SentNotificationV25 sent = NotificationMapper.internalToExternal( expected );
        NotificationInt actual = NotificationMapper.externalToInternal( sent );
        
        Assertions.assertEquals(expected, actual );
        
    }

    @Test
    void externalToInternal() {
        SentNotificationV25 expected = getExternalNotification();

        NotificationInt internal = NotificationMapper.externalToInternal( expected );
        SentNotificationV25 actual = NotificationMapper.internalToExternal( internal );
        
        Assertions.assertEquals( expected, actual );
    }

    @Test
    void externalInformalToInternal() {
        InformalSentNotificationV1 informal = new InformalSentNotificationV1()
                .iun("IUN_INF_01")
                .paProtocolNumber("protocol_inf_01")
                .subject("Subject informal")
                .senderPaId("pa_02")
                .senderTaxId("taxId")
                .senderDenomination("Comune")
                .recipients(Collections.singletonList(
                        new InformalNotificationRecipientV1()
                                .taxId("Codice Fiscale 01")
                                .recipientType(InformalNotificationRecipientV1.RecipientTypeEnum.PF)
                                .denomination("Nome Cognome")
                                .digitalDomicile(
                                        new NotificationDigitalAddress()
                                                .address("pec@example.com")
                                                .type(NotificationDigitalAddress.TypeEnum.PEC)
                                )
                                .physicalAddress(
                                        new NotificationPhysicalAddress()
                                                .address("Via Roma 10")
                                                .municipality("Roma")
                                                .zip("00100")
                                )
                                .payments(Collections.singletonList(
                                        new InformalNotificationPaymentItem()
                                                .pagoPa(new PagoPaPaymentBase()
                                                        .creditorTaxId("77777777777")
                                                        .noticeCode("302000100000019421"))
                                ))
                ))
                .documents(Collections.singletonList(
                        new NotificationDocument()
                                .ref(new NotificationAttachmentBodyRef()
                                        .key("doc_inf_01")
                                        .versionToken("v_doc_inf_01"))
                                .digests(new NotificationAttachmentDigests()
                                        .sha256("sha256_doc_inf_01"))
                ));

        NotificationInt actual = NotificationMapper.externalToInternal(informal);

        Assertions.assertEquals("IUN_INF_01", actual.getIun());
        Assertions.assertEquals("Subject informal", actual.getSubject());
        Assertions.assertEquals("pa_02", actual.getSender().getPaId());
        Assertions.assertEquals("taxId", actual.getSender().getPaTaxId());
        Assertions.assertEquals("Comune", actual.getSender().getPaDenomination());
        Assertions.assertEquals(1, actual.getRecipients().size());

        NotificationRecipientInt recipient = actual.getRecipients().getFirst();
        Assertions.assertEquals("Codice Fiscale 01", recipient.getTaxId());
        Assertions.assertEquals(RecipientTypeInt.PF, recipient.getRecipientType());
        Assertions.assertNotNull(recipient.getDigitalDomicile());
        Assertions.assertEquals("pec@example.com", recipient.getDigitalDomicile().getAddress());
        Assertions.assertNotNull(recipient.getPhysicalAddress());
        Assertions.assertEquals("Via Roma 10", recipient.getPhysicalAddress().getAddress());
        Assertions.assertEquals("Roma", recipient.getPhysicalAddress().getMunicipality());

        List<NotificationPaymentInfoInt> payments = recipient.getPayments();
        Assertions.assertNotNull(payments);
        Assertions.assertEquals(1, payments.size());
        Assertions.assertEquals("77777777777", payments.getFirst().getPagoPA().getCreditorTaxId());
        Assertions.assertEquals("302000100000019421", payments.getFirst().getPagoPA().getNoticeCode());

        Assertions.assertNotNull(actual.getDocuments());
        Assertions.assertEquals(1, actual.getDocuments().size());
        Assertions.assertEquals("doc_inf_01", actual.getDocuments().getFirst().getRef().getKey());
        Assertions.assertEquals("v_doc_inf_01", actual.getDocuments().getFirst().getRef().getVersionToken());
        Assertions.assertEquals("sha256_doc_inf_01", actual.getDocuments().getFirst().getDigests().getSha256());
    }

    @Test
    void externalInformalToInternal_withPhysicalAddress() {
        // Il destinatario ha physicalAddress completamente valorizzato: tutti i campi devono essere mappati correttamente
        InformalSentNotificationV1 informal = new InformalSentNotificationV1()
                .iun("IUN_INF_03")
                .paProtocolNumber("protocol_inf_03")
                .subject("Subject informal with address")
                .senderPaId("pa_04")
                .senderTaxId("taxId")
                .senderDenomination("Comune")
                .recipients(Collections.singletonList(
                        new InformalNotificationRecipientV1()
                                .taxId("TAXID03")
                                .recipientType(InformalNotificationRecipientV1.RecipientTypeEnum.PF)
                                .denomination("Mario Rossi")
                                .physicalAddress(
                                        new NotificationPhysicalAddress()
                                                .at("c/o Condominio")
                                                .address("Via Roma 1")
                                                .addressDetails("Scala B")
                                                .municipality("Roma")
                                                .municipalityDetails("RM")
                                                .province("RM")
                                                .zip("00100")
                                                .foreignState("Italia")
                                )
                ));

        NotificationInt actual = NotificationMapper.externalToInternal(informal);

        Assertions.assertEquals(1, actual.getRecipients().size());
        NotificationRecipientInt recipient = actual.getRecipients().getFirst();
        PhysicalAddressInt physicalAddress = recipient.getPhysicalAddress();

        Assertions.assertNotNull(physicalAddress, "Il physicalAddress non deve essere null");
        Assertions.assertEquals("Mario Rossi", physicalAddress.getFullname());
        Assertions.assertEquals("c/o Condominio", physicalAddress.getAt());
        Assertions.assertEquals("Via Roma 1", physicalAddress.getAddress());
        Assertions.assertEquals("Scala B", physicalAddress.getAddressDetails());
        Assertions.assertEquals("Roma", physicalAddress.getMunicipality());
        Assertions.assertEquals("RM", physicalAddress.getMunicipalityDetails());
        Assertions.assertEquals("RM", physicalAddress.getProvince());
        Assertions.assertEquals("00100", physicalAddress.getZip());
        Assertions.assertEquals("Italia", physicalAddress.getForeignState());
    }

    @Test
    void externalInformalToInternal_withNullPhysicalAddress() {
        // Il destinatario non ha physicalAddress: deve essere mappato senza eccezioni e physicalAddress deve essere null
        InformalSentNotificationV1 informal = new InformalSentNotificationV1()
                .iun("IUN_INF_02")
                .paProtocolNumber("protocol_inf_02")
                .subject("Subject informal null address")
                .senderPaId("pa_03")
                .senderTaxId("taxId")
                .senderDenomination("Comune")
                .recipients(Collections.singletonList(
                        new InformalNotificationRecipientV1()
                                .taxId("TAXID02")
                                .recipientType(InformalNotificationRecipientV1.RecipientTypeEnum.PF)
                                .denomination("Nome Cognome")
                                .digitalDomicile(
                                        new NotificationDigitalAddress()
                                                .address("pec@example.com")
                                                .type(NotificationDigitalAddress.TypeEnum.PEC)
                                )
                        // physicalAddress non impostato → null
                ));

        NotificationInt actual = NotificationMapper.externalToInternal(informal);

        Assertions.assertEquals(1, actual.getRecipients().size());
        NotificationRecipientInt recipient = actual.getRecipients().getFirst();
        Assertions.assertNull(recipient.getPhysicalAddress(),
                "Il physicalAddress deve essere null quando non viene fornito");
        Assertions.assertNotNull(recipient.getDigitalDomicile(),
                "Il digitalDomicile deve essere valorizzato");
    }

    @Test
    void mapNotificationPaymentInfo_withPagoPaAndAttachment() {
        // Pagamento con PagoPa e attachment valorizzati
        InformalSentNotificationV1 informal = new InformalSentNotificationV1()
                .iun("IUN_PAY_01")
                .paProtocolNumber("prot_pay_01")
                .subject("Subject payment")
                .senderPaId("pa_pay")
                .senderTaxId("taxIdPay")
                .senderDenomination("Comune")
                .recipients(Collections.singletonList(
                        new InformalNotificationRecipientV1()
                                .taxId("TAXID_PAY")
                                .recipientType(InformalNotificationRecipientV1.RecipientTypeEnum.PF)
                                .denomination("Pagatore")
                                .payments(Collections.singletonList(
                                        new InformalNotificationPaymentItem()
                                                .pagoPa(new PagoPaPaymentBase()
                                                        .creditorTaxId("77777777777")
                                                        .noticeCode("302000100000019421")
                                                        .attachment(new NotificationPaymentAttachment()
                                                                .ref(new NotificationAttachmentBodyRef()
                                                                        .key("key_att")
                                                                        .versionToken("v1"))
                                                                .digests(new NotificationAttachmentDigests()
                                                                        .sha256("sha256_att"))))
                                ))
                ));

        NotificationInt actual = NotificationMapper.externalToInternal(informal);

        List<NotificationPaymentInfoInt> payments = actual.getRecipients().getFirst().getPayments();
        Assertions.assertEquals(1, payments.size());
        NotificationPaymentInfoInt payment = payments.getFirst();
        Assertions.assertNotNull(payment.getPagoPA());
        Assertions.assertEquals("77777777777", payment.getPagoPA().getCreditorTaxId());
        Assertions.assertEquals("302000100000019421", payment.getPagoPA().getNoticeCode());
        Assertions.assertNotNull(payment.getPagoPA().getAttachment());
        Assertions.assertEquals("key_att", payment.getPagoPA().getAttachment().getRef().getKey());
        Assertions.assertEquals("sha256_att", payment.getPagoPA().getAttachment().getDigests().getSha256());
    }

    @Test
    void mapNotificationPaymentInfo_withNullAttachment() {
        // Pagamento con PagoPa presente ma senza attachment
        InformalSentNotificationV1 informal = new InformalSentNotificationV1()
                .iun("IUN_PAY_03")
                .paProtocolNumber("prot_pay_03")
                .subject("Subject payment no attachment")
                .senderPaId("pa_pay3")
                .senderTaxId("taxIdPay3")
                .senderDenomination("Comune")
                .recipients(Collections.singletonList(
                        new InformalNotificationRecipientV1()
                                .taxId("TAXID_PAY3")
                                .recipientType(InformalNotificationRecipientV1.RecipientTypeEnum.PF)
                                .denomination("Pagatore3")
                                .payments(Collections.singletonList(
                                        new InformalNotificationPaymentItem()
                                                .pagoPa(new PagoPaPaymentBase()
                                                        .creditorTaxId("88888888888")
                                                        .noticeCode("302000100000019422")
                                                        // attachment non impostato → null
                                                )
                                ))
                ));

        NotificationInt actual = NotificationMapper.externalToInternal(informal);

        List<NotificationPaymentInfoInt> payments = actual.getRecipients().getFirst().getPayments();
        Assertions.assertEquals(1, payments.size());
        Assertions.assertNotNull(payments.getFirst().getPagoPA());
        Assertions.assertNull(payments.getFirst().getPagoPA().getAttachment(),
                "L'attachment deve essere null quando non viene fornito");
    }

    @Test
    void mapNotificationPaymentInfo_withEmptyPayments() {
        // Lista payments vuota: deve restituire lista vuota
        InformalSentNotificationV1 informal = new InformalSentNotificationV1()
                .iun("IUN_PAY_04")
                .paProtocolNumber("prot_pay_04")
                .subject("Subject payment empty")
                .senderPaId("pa_pay4")
                .senderTaxId("taxIdPay4")
                .senderDenomination("Comune")
                .recipients(Collections.singletonList(
                        new InformalNotificationRecipientV1()
                                .taxId("TAXID_PAY4")
                                .recipientType(InformalNotificationRecipientV1.RecipientTypeEnum.PF)
                                .denomination("Pagatore4")
                                .payments(Collections.emptyList())
                ));

        NotificationInt actual = NotificationMapper.externalToInternal(informal);

        List<NotificationPaymentInfoInt> payments = actual.getRecipients().getFirst().getPayments();
        Assertions.assertNotNull(payments);
        Assertions.assertTrue(payments.isEmpty(), "La lista dei pagamenti deve essere vuota");
    }

    @Test
    void externalInformalToInternal_withNullRecipientsAndDocuments() {
        // recipients/documents == null: deve restituire liste vuote senza eccezioni
        InformalSentNotificationV1 informal = new InformalSentNotificationV1()
                .iun("IUN_INF_NULL")
                .paProtocolNumber("protocol_null")
                .subject("Subject null recipients")
                .senderPaId("pa_null")
                .senderTaxId("taxId")
                .senderDenomination("Comune")
                .recipients(null)
                .documents(null);

        NotificationInt actual = NotificationMapper.externalToInternal(informal);

        Assertions.assertNotNull(actual.getRecipients(), "La lista dei destinatari non deve essere null");
        Assertions.assertTrue(actual.getRecipients().isEmpty(),
                "La lista dei destinatari deve essere vuota quando recipients è null");
        Assertions.assertNotNull(actual.getDocuments(), "La lista dei documenti non deve essere null");
        Assertions.assertTrue(actual.getDocuments().isEmpty(),
                "La lista dei documenti deve essere vuota quando documents è null");
    }

    @Test
    void externalInformalToInternal_withEmptyRecipientsAndDocuments() {
        InformalSentNotificationV1 informal = new InformalSentNotificationV1()
                .iun("IUN_INF_EMPTY")
                .paProtocolNumber("protocol_empty")
                .subject("Subject empty recipients and docs")
                .senderPaId("pa_empty")
                .senderTaxId("taxId")
                .senderDenomination("Comune")
                .recipients(Collections.emptyList())
                .documents(Collections.emptyList());

        NotificationInt actual = NotificationMapper.externalToInternal(informal);

        Assertions.assertNotNull(actual.getRecipients());
        Assertions.assertTrue(actual.getRecipients().isEmpty(),
                "La lista dei destinatari deve essere vuota quando recipients è empty");
        Assertions.assertNotNull(actual.getDocuments());
        Assertions.assertTrue(actual.getDocuments().isEmpty(),
                "La lista dei documenti deve essere vuota quando documents è empty");
    }

    @Test
    void externalToInternal_withNullRecipients() {
        // recipients == null nel mapping formale: deve restituire lista vuota senza eccezioni
        SentNotificationV25 sent = new SentNotificationV25()
                .iun("IUN_FORMAL_NULL")
                .paProtocolNumber("protocol_formal_null")
                .subject("Subject formal null recipients")
                .senderPaId("pa_formal_null")
                .physicalCommunicationType(SentNotificationV25.PhysicalCommunicationTypeEnum.REGISTERED_LETTER_890)
                .amount(0)
                .paymentExpirationDate("2025-12-31")
                .notificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .recipients(null)
                .documents(Collections.emptyList());

        NotificationInt actual = NotificationMapper.externalToInternal(sent);

        Assertions.assertNotNull(actual.getRecipients(), "La lista dei destinatari non deve essere null");
        Assertions.assertTrue(actual.getRecipients().isEmpty(),
                "La lista dei destinatari deve essere vuota quando recipients è null");
    }

    private SentNotificationV25 getExternalNotification() {
        return new SentNotificationV25()
                .iun("IUN_01")
                .paProtocolNumber("protocol_01")
                .subject("Subject 01")
                .senderPaId( "pa_02" )
                .physicalCommunicationType(SentNotificationV25.PhysicalCommunicationTypeEnum.REGISTERED_LETTER_890)
                .amount(18)
                .paymentExpirationDate("2022-10-22")
                .notificationFeePolicy(NotificationFeePolicy.DELIVERY_MODE)
                .recipients( Collections.singletonList(
                       new NotificationRecipientV24()
                                .taxId("Codice Fiscale 01")
                                .recipientType(NotificationRecipientV24.RecipientTypeEnum.PF)
                                .denomination("Nome Cognome/Ragione Sociale")
                               .digitalDomicile(
                                       new NotificationDigitalAddress()
                                               .address("address")
                                               .type(NotificationDigitalAddress.TypeEnum.PEC)
                               )
                               .physicalAddress(
                                       new NotificationPhysicalAddress()
                                               .address("physicalAddress")
                                               .municipality("municipality")
                               )
                ))
                .documents(Arrays.asList(
                        new NotificationDocument()
                                .ref( new NotificationAttachmentBodyRef()
                                        .key("doc00")
                                        .versionToken("v01_doc00")
                                )
                                .digests(new NotificationAttachmentDigests()
                                        .sha256("sha256_doc00")
                                ),
                        new NotificationDocument()
                                .ref(  new NotificationAttachmentBodyRef()
                                        .key("doc01")
                                        .versionToken("v01_doc01")
                                )
                                .digests(new NotificationAttachmentDigests()
                                        .sha256("sha256_doc01")
                                )
                ));
    }
}