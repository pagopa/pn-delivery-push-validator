package it.pagopa.pn.deliverypushvalidator.dto.ext.delivery.notification;

public enum NotificationType {
    LEGAL,
    INFORMAL;

    /**
     * Resolve notification type with default fallback to LEGAL for backward compatibility.
     * @param type the notification type, may be null
     * @return LEGAL if null, otherwise the provided type
     */
    public static NotificationType resolveOrDefault(NotificationType type) {
        return type != null ? type : LEGAL;
    }
}

