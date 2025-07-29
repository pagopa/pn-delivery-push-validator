package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.deliverypushvalidator.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditLogServiceImplTest {

    AuditLogServiceImpl auditLogService;

    @BeforeEach
    void beforeEach(){
        auditLogService = new AuditLogServiceImpl();
    }

    @Test
    void buildAuditLogEvent() {

        PnAuditLogEvent event = auditLogService.buildAuditLogEvent("iun1", 0, PnAuditLogEventType.AUD_DD_SEND,"messaggio");
        assertNotNull(event);
        event.generateSuccess().log();
    }

    @Test
    void buildAuditLogEvent2() {

        PnAuditLogEvent event = auditLogService.buildAuditLogEvent("iun1", 0, PnAuditLogEventType.AUD_DD_SEND,"messaggio par1={} par2={}", "parametro1", 2);
        assertNotNull(event);
        event.generateSuccess().log();
    }


    @Test
    void buildAuditLogEvent0() {

        PnAuditLogEvent event = auditLogService.buildAuditLogEvent("iun1", 0, PnAuditLogEventType.AUD_DD_SEND,"messaggio ");
        assertNotNull(event);
        event.generateSuccess().log();
    }
}