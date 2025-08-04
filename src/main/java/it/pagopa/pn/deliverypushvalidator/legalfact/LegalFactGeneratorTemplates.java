package it.pagopa.pn.deliverypushvalidator.legalfact;

import it.pagopa.pn.deliverypushvalidator.config.PnDeliveryPushValidatorConfigs;
import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.LanguageEnum;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.templatesengine.model.NotificationReceivedLegalFact;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.templatesengine.TemplatesClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static it.pagopa.pn.deliverypushvalidator.service.mapper.TemplatesEngineMapper.notificationReceivedLegalFact;

@Slf4j
@AllArgsConstructor
@Component
public class LegalFactGeneratorTemplates implements LegalFactGenerator {

    private final CustomInstantWriter instantWriter;
    private final PhysicalAddressWriter physicalAddressWriter;
    private final PnDeliveryPushValidatorConfigs pnDeliveryPushConfigs;
    private final TemplatesClient templatesClient;


    /**
     * Generates the legal fact for a received notification.
     *
     * @param notification the {@link NotificationInt} object containing notification details,
     *                     including sender, recipients, and metadata.
     * @return a byte[] representing the generated pdf notification received legal fact.
     * @throws IllegalArgumentException if the notification is null or contains incomplete data.
     *
     * <p><strong>Note:</strong></p>
     * Ensure the {@code templatesClient} is properly configured to handle the generated
     * {@link NotificationReceivedLegalFact} and return the expected byte array.
     */
    @Override
    public byte[] generateNotificationReceivedLegalFact(NotificationInt notification) {
        log.info("retrieve NotificationReceivedLegalFact template for iun {}", notification.getIun());
        NotificationReceivedLegalFact legalFact =
                notificationReceivedLegalFact(notification, physicalAddressWriter, instantWriter);
        LanguageEnum language = getLanguage(notification.getAdditionalLanguages());
        return templatesClient.notificationReceivedLegalFact(language, legalFact);
    }

    /**
     * Determines the language to be used for the notification based on the provided list of additional languages.
     *
     * @param additionalLanguages a {@link List} of {@link String} representing the additional languages to be considered.
     *                            If the list is empty or null, the default language (Italian) is returned.
     * @return a {@link LanguageEnum} representing the selected language. It returns {@link LanguageEnum#IT}
     *         if no additional languages are available or enabled, otherwise the first language from the list.
     * @throws IllegalArgumentException if the provided list contains invalid language values.
     */
    private LanguageEnum getLanguage(List<String> additionalLanguages) {
        return (!pnDeliveryPushConfigs.isAdditionalLangsEnabled() || CollectionUtils.isEmpty(additionalLanguages))
                ? LanguageEnum.IT : LanguageEnum.fromValue(additionalLanguages.get(0));
    }


}
