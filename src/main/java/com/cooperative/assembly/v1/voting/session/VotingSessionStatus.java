package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;

import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;

public enum VotingSessionStatus {
    OPENED,
    CLOSED,
    WAITING;

    public static VotingSessionStatus getStatusBySessionPeriod(final VotingSession session) {
        if (now().isBefore(session.getOpeningTime())) {
            return WAITING;
        }

        if (isClosed(session)) {
            return CLOSED;
        }

        return OPENED;
    }

    /**
     * Check if session is no longer opened by status or time period.
     *
     * @param session
     * @return
     */
    public static Boolean isNoLongerOpenSession(final VotingSession session) {
        VotingSessionCanvass canvass = session.getCanvass();
        return isNoLongerOpenSession(canvass) ? TRUE : isClosed(session);
    }

    /**
     * Check if session canvas is no longer opened by status.
     *
     * @param canvass
     * @return
     */
    public static Boolean isNoLongerOpenSession(final VotingSessionCanvass canvass) {
        VotingSessionStatus status = canvass.getStatus();
        return CLOSED.equals(status);
    }

    /**
     * Check if session is closed by time period.
     *
     * @param session
     * @return
     */
    public static Boolean isClosed(final VotingSession session) {
        return now().isAfter(session.getClosingTime());
    }

}
