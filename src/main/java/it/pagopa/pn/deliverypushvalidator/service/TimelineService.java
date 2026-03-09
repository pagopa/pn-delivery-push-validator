package it.pagopa.pn.deliverypushvalidator.service;

import it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification.NotificationInt;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.TimelineElementInternal;
import it.pagopa.pn.deliverypushvalidator.dto.timeline.details.TimelineElementCategoryInt;

import java.util.Optional;
import java.util.Set;

public interface TimelineService {
    boolean addTimelineElement(TimelineElementInternal element, NotificationInt notification);

    Long retrieveAndIncrementCounterForTimelineEvent(String timelineId);

    Optional<TimelineElementInternal> getTimelineElement(String iun, String timelineId);

    Optional<TimelineElementInternal> getTimelineElementStrongly(String iun, String timelineId);

    <T> Optional<T> getTimelineElementDetails(String iun, String timelineId, Class<T> timelineDetailsClass);

    <T> Optional<T> getTimelineElementDetailForSpecificRecipient(String iun, int recIndex, boolean confidentialInfoRequired, TimelineElementCategoryInt category, Class<T> timelineDetailsClass);

    Optional<TimelineElementInternal> getTimelineElementForSpecificRecipient(String iun, int recIndex, TimelineElementCategoryInt category);

    Set<TimelineElementInternal> getTimeline(String iun, boolean confidentialInfoRequired);

    Set<TimelineElementInternal> getTimelineStrongly(String iun, boolean confidentialInfoRequired);

    boolean isNotificationCancellationRequested(String iun);
}
