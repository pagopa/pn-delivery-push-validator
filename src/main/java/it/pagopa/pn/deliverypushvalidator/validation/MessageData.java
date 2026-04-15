package it.pagopa.pn.deliverypushvalidator.validation;

import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class MessageData {
    private String messageId;
    private String paId;
    private String language;        // primary language, always "IT"
    private String subject;
    private String longBody;
    private String shortBody;
    private Instant createdAt;
    /**
     * Additional message content for the secondary language.
     * Null when the message is IT monolingual.
     */
    private String additionalMessage;
}

