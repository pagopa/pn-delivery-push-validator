package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.timelineservice.model.*;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.timeline.TimelineClient;

import java.time.Instant;
import java.util.List;

public class TimelineClientMock implements TimelineClient {
    @Override
    public boolean addTimelineElement(NewTimelineElement newTimelineElement) {
        return false;
    }

    @Override
    public Long retrieveAndIncrementCounterForTimelineEvent(String timelineId) {
        return 0L;
    }

    @Override
    public TimelineElement getTimelineElement(String iun, String timelineId, Boolean strongly) {
        return null;
    }

    @Override
    public TimelineElementDetails getTimelineElementDetails(String iun, String timelineId) {
        return null;
    }

    @Override
    public TimelineElementDetails getTimelineElementDetailForSpecificRecipient(String iun, Integer recIndex, Boolean confidentialInfoRequired, TimelineCategory category) {
        return null;
    }

    @Override
    public TimelineElement getTimelineElementForSpecificRecipient(String iun, Integer recIndex, TimelineCategory category) {
        return null;
    }

    @Override
    public List<TimelineElement> getTimeline(String iun, Boolean confidentialInfoRequired, Boolean strongly, String timelineId) {
        return List.of();
    }

    @Override
    public NotificationHistoryResponse getTimelineAndStatusHistory(String iun, Integer numberOfRecipients, Instant createdAt) {
        return null;
    }
}
