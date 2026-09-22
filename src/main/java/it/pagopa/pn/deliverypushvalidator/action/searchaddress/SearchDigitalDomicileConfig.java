package it.pagopa.pn.deliverypushvalidator.action.searchaddress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchDigitalDomicileConfig {
    private Instant validFrom;
    List<DigitalAddressSourceInt> pec;
    List<DigitalAddressSourceInt> sms;
    List<DigitalAddressSourceInt> email;
}
