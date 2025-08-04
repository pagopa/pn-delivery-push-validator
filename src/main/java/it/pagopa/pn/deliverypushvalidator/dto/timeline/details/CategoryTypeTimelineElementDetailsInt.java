package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder( toBuilder = true )
@ToString
public class CategoryTypeTimelineElementDetailsInt {
    protected String categoryType;
}
