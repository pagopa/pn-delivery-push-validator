package it.pagopa.pn.deliverypushvalidator.dto.timeline.details;


import lombok.Getter;

@Getter
public enum TimelineElementCategoryInt {
    SENDER_ACK_CREATION_REQUEST(SenderAckCreationRequestDetailsInt.class, TimelineElementCategoryInt.VERSION_10),
    VALIDATE_F24_REQUEST(ValidateF24Int.class, TimelineElementCategoryInt.VERSION_20),
    VALIDATE_NORMALIZE_ADDRESSES_REQUEST(ValidateNormalizeAddressDetailsInt.class,TimelineElementCategoryInt.VERSION_10),
    VALIDATED_F24(ValidatedF24DetailInt.class, TimelineElementCategoryInt.VERSION_20),
    NORMALIZED_ADDRESS(NormalizedAddressDetailsInt.class, TimelineElementCategoryInt.VERSION_10),
    REQUEST_ACCEPTED(NotificationRequestAcceptedDetailsInt.class, TimelineElementCategoryInt.VERSION_10),
    GENERATE_F24_REQUEST(ValidateF24Int.class, TimelineElementCategoryInt.VERSION_23),
    GENERATED_F24(GeneratedF24DetailsInt.class,  TimelineElementCategoryInt.VERSION_23),
    REQUEST_REFUSED(RequestRefusedDetailsInt.class,  TimelineElementCategoryInt.VERSION_10),
    PUBLIC_REGISTRY_VALIDATION_CALL(PublicRegistryValidationCallDetailsInt.class, TimelineElementCategoryInt.VERSION_27),
    PUBLIC_REGISTRY_VALIDATION_RESPONSE(PublicRegistryValidationResponseDetailsInt.class, TimelineElementCategoryInt.VERSION_27),
    NOTIFICATION_COST_VALIDATION_REQUEST(NotificationCostValidationRequestDetailsInt.class, TimelineElementCategoryInt.VERSION_28),
    NOTIFICATION_COST_VALIDATION_RESPONSE(NotificationCostValidationResponseDetailsInt.class, TimelineElementCategoryInt.VERSION_28);


    private final Class<? extends TimelineElementDetailsInt> detailsJavaClass;
    private final int priority;
    private final int version;

    public static final int PRIORITY_BEFORE = 10;

    public static final int VERSION_10 = 10;
    public static final int VERSION_20 = 20;
    public static final int VERSION_23 = 23;
    public static final int VERSION_27 = 27;
    public static final int VERSION_28 = 28;

    TimelineElementCategoryInt(Class<? extends TimelineElementDetailsInt> detailsJavaClass, int version) {
        this(detailsJavaClass, PRIORITY_BEFORE, version);
    }


    TimelineElementCategoryInt(Class<? extends TimelineElementDetailsInt> detailsJavaClass, int priority, int version) {
        this.detailsJavaClass = detailsJavaClass;
        this.priority = priority;
        this.version = version;
    }

    /**
     * Checks if the given category is a known TimelineElementCategoryInt.
     *
     * @param category the category to check
     * @return true if the category is known, false otherwise
     */
    public static boolean isKnownCategory(String category) {
        try {
            TimelineElementCategoryInt.valueOf(category);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
