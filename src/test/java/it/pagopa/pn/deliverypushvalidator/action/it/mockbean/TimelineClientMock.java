package it.pagopa.pn.deliverypushvalidator.action.it.mockbean;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.RecipientRelatedTimelineElementDetails;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementDetailsInt;
import it.pagopa.pn.deliverypushvalidator.middleware.externalclient.pnclient.timeline.TimelineClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class TimelineClientMock implements TimelineClient {
    private CopyOnWriteArrayList<TimelineElementInternal> timelineList;
    private Instant notificationCancellationRequestedTimestamp;

    final HashMap<String, Long> counter = new HashMap<>();

    public TimelineClientMock() {
        timelineList = new CopyOnWriteArrayList<>();
    }

    public void clear() {
        this.timelineList = new CopyOnWriteArrayList<>();
        this.counter.clear();
    }

    @Override
    public boolean addTimelineElement(TimelineElementInternal timelineElementInternal, NotificationInt notificationInt) {
        timelineList.add(timelineElementInternal);
        return false;
    }

    @Override
    public Long retrieveAndIncrementCounterForTimelineEvent(String timelineId) {
        Long v = 0L;
        synchronized (counter) {
            if (counter.containsKey(timelineId)) {
                v = counter.get(timelineId);
            }
            v = v + 1;
            counter.put(timelineId, v);
        }
        return v;
    }

    @Override
    public TimelineElementInternal getTimelineElement(String iun, String timelineId, Boolean strongly) {
        log.debug("[TEST] Start getTimelineElement iun={} timelineId={} in timelineIds={}", iun, timelineId, timelineList.stream().map(TimelineElementInternal::getElementId).toList());
        return timelineList.stream().filter(timelineElement -> timelineId.equals(timelineElement.getElementId()) && iun.equals(timelineElement.getIun())).findFirst().orElse(null);
    }

    @Override
    public TimelineElementDetailsInt getTimelineElementDetails(String iun, String timelineId) {
        TimelineElementInternal timelineElement = getTimelineElement(iun, timelineId, false);
        if (timelineElement != null) {
            return timelineElement.getDetails();
        } else {
            log.debug("[TEST] Timeline element not found for iun={} and timelineId={}", iun, timelineId);
            return null;
        }
    }

    @Override
    public TimelineElementDetailsInt getTimelineElementDetailForSpecificRecipient(String iun, Integer recIndex, Boolean confidentialInfoRequired, TimelineElementCategoryInt category) {
        TimelineElementInternal timelineElement = getTimelineElementForSpecificRecipient(iun, recIndex, category);
        if (timelineElement != null) {
            return timelineElement.getDetails();
        } else {
            log.debug("[TEST] Timeline element not found for iun={}, recIndex={}, category={}", iun, recIndex, category);
            return null;
        }
    }

    @Override
    public TimelineElementInternal getTimelineElementForSpecificRecipient(String iun, Integer recIndex, TimelineElementCategoryInt category) {
        return timelineList.stream()
                .filter(timelineElement -> iun.equals(timelineElement.getIun()) &&
                        timelineElement.getCategory() == category &&
                        ((RecipientRelatedTimelineElementDetails) timelineElement.getDetails()).getRecIndex() == recIndex
                ).findFirst()
                .orElse(null);
    }

    @Override
    public List<TimelineElementInternal> getTimeline(String iun, Boolean confidentialInfoRequired, Boolean strongly, String timelineId) {
        return timelineList.stream()
                .filter(timelineElement ->
                        iun.equals(timelineElement.getIun()) &&
                                (timelineId == null || timelineElement.getElementId().startsWith(timelineId))
                )
                .toList();
    }

    @Override
    public Instant getNotificationCancellationRequested(String iun) {
        return notificationCancellationRequestedTimestamp;
    }
}
