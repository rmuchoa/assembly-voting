package com.cooperative.assembly.v1.vote.counting;

public enum VoteCountingStatus {
    APPROVED,
    REJECTED;

    public static VoteCountingStatus getByCountingVotes(final Integer affirmativeVotes, final Integer negativeVotes) {
        if (hasMoreAffirmativeVotes(affirmativeVotes, negativeVotes)) {
            return APPROVED;
        }
        return REJECTED;
    }

    private static Boolean hasMoreAffirmativeVotes(final Integer affirmativeVotes, final Integer negativeVotes) {
        return Integer.compare(affirmativeVotes, negativeVotes) > 0;
    }
}
