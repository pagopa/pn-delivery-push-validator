package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementDetailsInt;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.timeline.TimelineClient;

import java.util.List;

public class TimelineClientMock implements TimelineClient {
    @Override
    public boolean addTimelineElement(TimelineElementInternal element, NotificationInt notification) {
        return false;
    }

    @Override
    public Long retrieveAndIncrementCounterForTimelineEvent(String timelineId) {
        return 0L;
    }

    @Override
    public TimelineElementInternal getTimelineElement(String iun, String timelineId, Boolean strongly) {
        return null;
    }

    @Override
    public TimelineElementDetailsInt getTimelineElementDetails(String iun, String timelineId) {
        return null;
    }

    @Override
    public TimelineElementDetailsInt getTimelineElementDetailForSpecificRecipient(String iun, Integer recIndex, Boolean confidentialInfoRequired, TimelineElementCategoryInt category) {
        return null;
    }

    @Override
    public TimelineElementInternal getTimelineElementForSpecificRecipient(String iun, Integer recIndex, TimelineElementCategoryInt category) {
        return null;
    }

    @Override
    public List<TimelineElementInternal> getTimeline(String iun, Boolean confidentialInfoRequired, Boolean strongly, String timelineId) {
        return List.of();
    }
}
