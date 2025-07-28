package it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class FileCreationResponseInt {
    private String key;
}
