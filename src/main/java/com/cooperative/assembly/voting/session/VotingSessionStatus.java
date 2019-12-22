package com.cooperative.assembly.voting.session;

import java.time.LocalDateTime;
import static java.time.LocalDateTime.now;

public enum VotingSessionStatus {
    OPEN,
    CLOSED,
    WAITING;

    public static VotingSessionStatus getStatusByTimeRange(final LocalDateTime openingTime, final LocalDateTime closingTime) {
        if (now().isBefore(openingTime)) {
            return WAITING;
        }

        if (now().isAfter(closingTime)) {
            return CLOSED;
        }

        return OPEN;
    }
}
