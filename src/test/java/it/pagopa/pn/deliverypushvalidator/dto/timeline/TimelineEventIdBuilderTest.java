package it.pagopa.pn.deliverypushvalidator.dto.timeline;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimelineEventIdBuilderTest {

    private static final String IUN = "KWKU-JHXN-HJXM-202304-U-1";


    @Test
    void buildSENDERACK_CREATION_REQUESTTest() {
        String timeLineEventIdExpected = "SENDERACK_LEGALFACT_CREATION_REQUEST.IUN_KWKU-JHXN-HJXM-202304-U-1";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.SENDERACK_CREATION_REQUEST.getValue())
                .withIun(IUN)
                .build();

        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);

        String timeLineEventIdActualFromBuildEvent = TimelineEventId.SENDERACK_CREATION_REQUEST.buildEventId(EventId
                .builder()
                .iun(IUN)
                .build());


        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);

    }

    @Test
    void buildVALIDATE_NORMALIZE_ADDRESSTest() {
        String timeLineEventIdExpected = "VALIDATE_NORMALIZE_ADDRESSES_REQUEST.IUN_KWKU-JHXN-HJXM-202304-U-1";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.VALIDATE_NORMALIZE_ADDRESSES_REQUEST.getValue())
                .withIun(IUN)
                .build();

        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);

        String timeLineEventIdActualFromBuildEvent = TimelineEventId.VALIDATE_NORMALIZE_ADDRESSES_REQUEST.buildEventId(EventId
                .builder()
                .iun(IUN)
                .build());

        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildNORMALIZED_ADDRESSTest() {
        String timeLineEventIdExpected = "NORMALIZED_ADDRESS.IUN_KWKU-JHXN-HJXM-202304-U-1.RECINDEX_0";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.NORMALIZED_ADDRESS.getValue())
                .withIun(IUN)
                .withRecIndex(0)
                .build();

        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);

        String timeLineEventIdActualFromBuildEvent = TimelineEventId.NORMALIZED_ADDRESS.buildEventId(EventId
                .builder()
                .iun(IUN)
                .recIndex(0)
                .build());

        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);
    }

    @Test
    void buildREQUEST_ACCEPTEDTest() {
        String timeLineEventIdExpected = "REQUEST_ACCEPTED.IUN_KWKU-JHXN-HJXM-202304-U-1";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.REQUEST_ACCEPTED.getValue())
                .withIun(IUN)
                .build();

        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);

        String timeLineEventIdActualFromBuildEvent = TimelineEventId.REQUEST_ACCEPTED.buildEventId(EventId
                .builder()
                .iun(IUN)
                .build());


        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);

    }

    @Test
    void buildREQUEST_REFUSEDTest() {
        //vecchia versione 123456789_request_refused
        String timeLineEventIdExpected = "REQUEST_REFUSED.IUN_KWKU-JHXN-HJXM-202304-U-1";
        String timeLineEventIdActual = new TimelineEventIdBuilder()
                .withCategory(TimelineEventId.REQUEST_REFUSED.getValue())
                .withIun(IUN)
                .build();

        assertThat(timeLineEventIdActual).isEqualTo(timeLineEventIdExpected);

        String timeLineEventIdActualFromBuildEvent = TimelineEventId.REQUEST_REFUSED.buildEventId(EventId
                .builder()
                .iun(IUN)
                .build());


        assertThat(timeLineEventIdActualFromBuildEvent).isEqualTo(timeLineEventIdExpected);

    }
}
