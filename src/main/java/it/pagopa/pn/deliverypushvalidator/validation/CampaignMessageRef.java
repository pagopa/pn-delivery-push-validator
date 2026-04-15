package it.pagopa.pn.deliverypushvalidator.validation;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class CampaignMessageRef {
    private String messageId;
    /**
     * Lingua secondaria del messaggio (es. "DE", "EN", "FR").
     * Se null, indica che il messaggio è solo in italiano (IT monolingua, additionalMessage == null).
     */
    private String secondaryLanguage;
}

