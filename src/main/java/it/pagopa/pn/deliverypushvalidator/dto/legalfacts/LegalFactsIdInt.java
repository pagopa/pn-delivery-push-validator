package it.pagopa.pn.deliverypushvalidator.dto.legalfacts;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class LegalFactsIdInt {
    private String key;
    private LegalFactCategoryInt category;
}
