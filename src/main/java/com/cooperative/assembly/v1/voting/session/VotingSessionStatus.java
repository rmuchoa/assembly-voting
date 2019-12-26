package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;

import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;

public enum VotingSessionStatus {
    OPENED,
    CLOSED;

    /**
     * Check if session is no longer opened by status or time period.
     *
     * @param session
     * @return
     */
    public static Boolean isNoLongerOpenSession(final VotingSession session) {
        return isNoLongerOpen(session) ? TRUE : isClosed(session);
    }

    /**
     * Check if session is no longer opened by status.
     *
     * @param session
     * @return
     */
    public static Boolean isNoLongerOpen(final VotingSession session) {
        VotingSessionStatus status = session.getStatus();
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
