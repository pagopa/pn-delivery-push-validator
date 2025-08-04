package it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.templatesengine;

import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.api.TemplateApi;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.NotificationReceivedLegalFact;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link TemplatesClient} interface for interacting with a template engine to generate various legal facts.
 * <p>
 * This class is responsible for delegating calls to the {@link TemplateApi} client for generating legal facts in byte array or string format.
 * </p>
 */
@Component
@RequiredArgsConstructor
@CustomLog
public class TemplatesClientImpl implements TemplatesClient {

    private final TemplateApi templateEngineClient;

    /**
     * Generates a legal fact for a notification received event in the specified language.
     *
     * @param xLanguage The language for the legal fact.
     * @param legalFact The notification received legal fact to generate.
     * @return A byte array representing the generated legal fact.
     */
    @Override
    public byte[] notificationReceivedLegalFact(LanguageEnum xLanguage, NotificationReceivedLegalFact legalFact) {
        return templateEngineClient.notificationReceivedLegalFact(xLanguage, legalFact);
    }

}
