package it.pagopa.pn.deliverypushvalidator.dto.timeline;

public enum TimelineEventId {
    SENDERACK_CREATION_REQUEST("SENDERACK_LEGALFACT_CREATION_REQUEST") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .build();
        }
    },

    VALIDATE_F24_REQUEST("VALIDATE_F24_REQUEST") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .build();
        }
    },

    VALIDATED_F24("VALIDATED_F24") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .build();
        }
    },

    VALIDATE_NORMALIZE_ADDRESSES_REQUEST("VALIDATE_NORMALIZE_ADDRESSES_REQUEST") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .build();
        }
    },

    NORMALIZED_ADDRESS("NORMALIZED_ADDRESS") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    },

    REQUEST_ACCEPTED("REQUEST_ACCEPTED") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .build();
        }
    },

    GENERATE_F24_REQUEST("GENERATE_F24_REQUEST"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .build();
        }
    },

    GENERATED_F24("GENERATED_F24"){
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    },

    REQUEST_REFUSED("REQUEST_REFUSED") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .build();
        }
    },

    NATIONAL_REGISTRY_VALIDATION_CALL("NATIONAL_REGISTRY_VALIDATION_CALL") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withIun(eventId.getIun())
                    .withDeliveryMode(eventId.getDeliveryMode())
                    .build();
        }
    },

    NATIONAL_REGISTRY_VALIDATION_RESPONSE("NATIONAL_REGISTRY_VALIDATION_RESPONSE") {
        @Override
        public String buildEventId(EventId eventId) {
            return new TimelineEventIdBuilder()
                    .withCategory(this.getValue())
                    .withCorrelationId(eventId.getRelatedTimelineId())
                    .withRecIndex(eventId.getRecIndex())
                    .build();
        }
    };

    public String buildEventId(EventId eventId) {
        throw new UnsupportedOperationException("Must be implemented for each action type event ID");
    }

    private final String value;

    TimelineEventId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
