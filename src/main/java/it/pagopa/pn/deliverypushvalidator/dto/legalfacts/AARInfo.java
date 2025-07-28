package it.pagopa.pn.deliverypushvalidator.dto.legalfacts;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AARInfo {
    private byte[] bytesArrayGeneratedAar;
    private AarTemplateType templateType;
}
