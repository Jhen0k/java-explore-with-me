package ru.practicum.ewm.event.enums;

import java.util.Optional;

public enum UserState {
    SEND_TO_REVIEW, CANCEL_REVIEW;

    public static Optional<UserState> from(String stringState) {
        for (UserState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
