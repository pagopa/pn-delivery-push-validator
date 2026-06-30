package it.pagopa.pn.deliverypushvalidator.dto.campaign;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    public enum AdditionalLanguage {
        DE,
        SL,
        FR
    }

    public enum PrimaryLanguage {
        IT
    }

    private AdditionalLanguage additionalLanguage;

    @Builder.Default
    @NotNull
    private PrimaryLanguage primaryLanguage = PrimaryLanguage.IT;

    @NotEmpty
    private String messageId;
}

