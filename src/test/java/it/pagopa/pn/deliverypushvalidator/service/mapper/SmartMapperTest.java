package it.pagopa.pn.deliverypushvalidator.service.mapper;

import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.NormalizedAddressDetailsInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.RequestRefusedDetailsInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class SmartMapperTest {

    @Test
    @Disabled("In questo dominio non ci sono elementi ti timeline con date di business da rimappare al posto del timestamp")
    void testTimelineElementInternalMappingTransformer(){
        Instant elementTimestamp = Instant.EPOCH.plusMillis(100);

        Instant eventTimestamp = Instant.EPOCH.plusMillis(10);

        TimelineElementInternal source = TimelineElementInternal.builder()
                .elementId("elementid")
                .iun("iun")
                .timestamp(elementTimestamp)
                .details(NormalizedAddressDetailsInt.builder()
                        .recIndex(0)
                        .build())
                .build();

        TimelineElementInternal ret = SmartMapper.mapToClass(source, TimelineElementInternal.class);

        Assertions.assertNotSame(ret, source);
        Assertions.assertEquals(eventTimestamp, ret.getTimestamp());
    }


    @Test
    void testTimelineElementInternalMappingTransformerNo1(){
        Instant elementTimestamp = Instant.EPOCH.plusMillis(100);

        TimelineElementInternal source = TimelineElementInternal.builder()
                .elementId("elementid")
                .iun("iun")
                .timestamp(elementTimestamp)
                .details(RequestRefusedDetailsInt.builder()
                        .numberOfRecipients(2)
                        .build())
                .build();

        TimelineElementInternal ret = SmartMapper.mapToClass(source, TimelineElementInternal.class);


        Assertions.assertEquals(elementTimestamp, ret.getTimestamp());
    }

}