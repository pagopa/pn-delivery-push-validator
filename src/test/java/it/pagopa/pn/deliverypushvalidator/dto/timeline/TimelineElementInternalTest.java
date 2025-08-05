package it.pagopa.pn.deliverypushvalidator.dto.timeline;


import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

class TimelineElementInternalTest {


    @Test
    void compareToBase() {

        Instant tPrimo = Instant.EPOCH.plus(1, ChronoUnit.DAYS);
        Instant tSecondo = Instant.EPOCH.plus(2, ChronoUnit.DAYS);

        // caso 1: un xxx con data maggiore va dopo
        TimelineElementInternal t1Progress = TimelineElementInternal.builder()
                .timestamp(tPrimo)
                .category(TimelineElementCategoryInt.PUBLIC_REGISTRY_VALIDATION_RESPONSE)
                .build();

        TimelineElementInternal t2Progress = TimelineElementInternal.builder()
                .timestamp(tSecondo)
                .category(TimelineElementCategoryInt.PUBLIC_REGISTRY_VALIDATION_RESPONSE)
                .build();


        Assertions.assertTrue(t1Progress.compareTo(t2Progress) < 0);
    }


    @Test
    void compareTo() {

        Instant t1 = Instant.EPOCH.plus(1, ChronoUnit.DAYS);
        Instant t2 = Instant.EPOCH.plus(2, ChronoUnit.DAYS);

        TimelineElementInternal t1Progress = TimelineElementInternal.builder()
                .timestamp(t1)
                .category(TimelineElementCategoryInt.PUBLIC_REGISTRY_VALIDATION_RESPONSE)
                .build();

        TimelineElementInternal t2Progress = TimelineElementInternal.builder()
                .timestamp(t2)
                .category(TimelineElementCategoryInt.PUBLIC_REGISTRY_VALIDATION_RESPONSE)
                .build();

        Set<TimelineElementInternal> set = Set.of(t1Progress, t2Progress);
        List<TimelineElementInternal> list = set.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        Assertions.assertEquals(t1Progress, list.get(0));
        Assertions.assertEquals(t2Progress, list.get(1));

    }

    @Test
    void compareToSame() {

        Instant t1 = Instant.EPOCH.plus(1, ChronoUnit.DAYS);

        TimelineElementInternal t1Progress = TimelineElementInternal.builder()
                .elementId("a")
                .timestamp(t1)
                .category(TimelineElementCategoryInt.PUBLIC_REGISTRY_VALIDATION_RESPONSE)
                .build();

        TimelineElementInternal t2Progress = TimelineElementInternal.builder()
                .elementId("b")
                .timestamp(t1)
                .category(TimelineElementCategoryInt.PUBLIC_REGISTRY_VALIDATION_CALL)
                .build();

        Set<TimelineElementInternal> set = Set.of(t1Progress, t2Progress);
        List<TimelineElementInternal> list = set.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        Assertions.assertEquals(t1Progress, list.get(0));
        Assertions.assertEquals(t2Progress, list.get(1));

    }
}