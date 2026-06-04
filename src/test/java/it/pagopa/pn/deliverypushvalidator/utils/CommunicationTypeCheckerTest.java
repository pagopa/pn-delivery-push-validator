package it.pagopa.pn.deliverypushvalidator.utils;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.CommunicationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationTypeCheckerTest {
    @Test
    void checkAgainstIun_legalCommunicationTypeWithLegalIun_doesNotThrow() {
        CommunicationTypeChecker checker = new CommunicationTypeChecker();
        assertDoesNotThrow(() -> checker.checkAgainstIun(CommunicationType.LEGAL, "1ABC12-1"));
    }

    @Test
    void checkAgainstIun_informalCommunicationTypeWithInformalIun_doesNotThrow() {
        CommunicationTypeChecker checker = new CommunicationTypeChecker();
        assertDoesNotThrow(() -> checker.checkAgainstIun(CommunicationType.INFORMAL, "2ABC12-A"));
    }

    @Test
    void checkAgainstIun_legalCommunicationTypeWithInformalIun_throwsPnInternalException() {
        CommunicationTypeChecker checker = new CommunicationTypeChecker();
        assertThrows(PnInternalException.class, () -> checker.checkAgainstIun(CommunicationType.LEGAL, "DABC123-A"));
    }

    @Test
    void checkAgainstIun_informalCommunicationTypeWithLegalIun_throwsPnInternalException() {
        CommunicationTypeChecker checker = new CommunicationTypeChecker();
        assertThrows(PnInternalException.class, () -> checker.checkAgainstIun(CommunicationType.INFORMAL, "BABC123-1"));
    }

    @Test
    void checkAgainstIun_nullCommunicationType_throwsPnInternalException() {
        CommunicationTypeChecker checker = new CommunicationTypeChecker();
        assertThrows(PnInternalException.class, () -> checker.checkAgainstIun(null, "1ABC123"));
    }
}