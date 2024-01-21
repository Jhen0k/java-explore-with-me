package ru.practicum.ewm.event.enums;

import java.util.Optional;

public enum AdminState {

    PUBLISH_EVENT, REJECT_EVENT;

    public static Optional<AdminState> from(String stringState) {
        for (AdminState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
