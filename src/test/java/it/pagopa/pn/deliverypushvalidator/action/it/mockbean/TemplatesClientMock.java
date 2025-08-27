package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.NotificationReceivedLegalFact;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.templatesengine.TemplatesClient;

import java.io.IOException;

public class TemplatesClientMock implements TemplatesClient {
    @Override
    public byte[] notificationReceivedLegalFact(LanguageEnum xLanguage, NotificationReceivedLegalFact notificationReceivedLegalFact) {
        return resultPdf();
    }

    private byte[] resultPdf() {
        try (var result = this.getClass().getResourceAsStream("/pdf/response.pdf")) {
            if (result == null) {
                throw new PnInternalException("resultPdf", "resultPdf no pdf found");
            }
            return result.readAllBytes();
        } catch (IOException ex) {
            throw new PnInternalException(ex.getMessage(), ex.getLocalizedMessage());
        }
    }
}
