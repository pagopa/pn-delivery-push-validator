package it.pagopa.pn.deliverypushvalidator.validation;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.exception.PnValidationMessageException;
import it.pagopa.pn.deliverypushvalidator.service.MessageClient;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@AllArgsConstructor
@CustomLog
public class MessageValidator {

    private final MessageClient messageClient;

    /**
     * Validates and resolves the message for an INFORMAL notification.
     * <p>
     * Resolution rules:
     * <ol>
     *   <li>If notification.messageId is present → use it directly.</li>
     *   <li>Otherwise, determine secondary language from notification.additionalLanguages:
     *     <ul>
     *       <li>If empty/null → look for IT monolingual message in campaign (secondaryLanguage == null).</li>
     *       <li>If present → look for IT + secondaryLanguage pair in campaign.</li>
     *     </ul>
     *   </li>
     *   <li>If no matching message found in campaign → refuse notification.</li>
     *   <li>Validate message exists in data store via MessageClient.</li>
     * </ol>
     *
     * @param notification the informal notification
     * @param campaign     the validated campaign data
     * @return MessageData if validation succeeds
     * @throws PnValidationMessageException if validation fails (causes notification refusal)
     */
    public MessageData validateMessage(NotificationInt notification, CampaignData campaign) {
        log.debug("Start message validation - iun={}", notification.getIun());

        String resolvedMessageId = resolveMessageId(notification, campaign);

        MessageData message = messageClient.getById(resolvedMessageId)
                .orElseThrow(() -> new PnValidationMessageException(
                        String.format("Message not found for messageId=%s - iun=%s",
                                resolvedMessageId, notification.getIun())));

        // Validate primary language is IT
        if (!"IT".equalsIgnoreCase(message.getLanguage())) {
            throw new PnValidationMessageException(
                    String.format("Message primary language is not IT, found=%s for messageId=%s - iun=%s",
                            message.getLanguage(), resolvedMessageId, notification.getIun()));
        }

        // If notification has a secondary language, additionalMessage must be present
        String secondaryLanguage = getSecondaryLanguage(notification);
        if (secondaryLanguage != null && StringUtils.isBlank(message.getAdditionalMessage())) {
            throw new PnValidationMessageException(
                    String.format("Message is missing additionalMessage for language=%s, messageId=%s - iun=%s",
                            secondaryLanguage, resolvedMessageId, notification.getIun()));
        }

        log.info("Message validation passed for messageId={} - iun={}", resolvedMessageId, notification.getIun());
        return message;
    }

    private String resolveMessageId(NotificationInt notification, CampaignData campaign) {
        // 1. Direct messageId on the notification takes precedence
        if (StringUtils.isNotBlank(notification.getMessageId())) {
            log.debug("Using notification-level messageId={} - iun={}", notification.getMessageId(), notification.getIun());
            return notification.getMessageId();
        }

        // 2. Resolve from campaign generic messages based on language
        String secondaryLanguage = getSecondaryLanguage(notification);

        if (CollectionUtils.isEmpty(campaign.getMessages())) {
            throw new PnValidationMessageException(
                    String.format("Campaign has no generic messages and notification has no messageId - campaignId=%s iun=%s",
                            campaign.getCampaignId(), notification.getIun()));
        }

        CampaignMessageRef matchingRef = campaign.getMessages().stream()
                .filter(ref -> matchesLanguage(ref, secondaryLanguage))
                .findFirst()
                .orElseThrow(() -> new PnValidationMessageException(
                        String.format("No matching generic message found in campaign for secondaryLanguage=%s - campaignId=%s iun=%s",
                                secondaryLanguage, campaign.getCampaignId(), notification.getIun())));

        log.debug("Resolved generic messageId={} from campaign for secondaryLanguage={} - iun={}",
                matchingRef.getMessageId(), secondaryLanguage, notification.getIun());
        return matchingRef.getMessageId();
    }

    /**
     * Extracts the secondary language from the notification.
     * The additionalLanguages list contains only the secondary language(s); IT is always implicit primary.
     *
     * @return null if no secondary language specified (IT monolingual), otherwise the first additional language
     */
    private String getSecondaryLanguage(NotificationInt notification) {
        List<String> additionalLanguages = notification.getAdditionalLanguages();
        if (CollectionUtils.isEmpty(additionalLanguages)) {
            return null;
        }
        return additionalLanguages.get(0);
    }

    /**
     * Matches a campaign message ref by secondary language.
     * null secondaryLanguage matches refs with null secondaryLanguage (IT monolingual).
     */
    private boolean matchesLanguage(CampaignMessageRef ref, String secondaryLanguage) {
        if (secondaryLanguage == null) {
            return ref.getSecondaryLanguage() == null;
        }
        return secondaryLanguage.equalsIgnoreCase(ref.getSecondaryLanguage());
    }
}

