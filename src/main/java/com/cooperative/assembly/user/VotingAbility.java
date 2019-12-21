package com.cooperative.assembly.user;

public enum VotingAbility {
    ABLE_TO_VOTE,
    UNABLE_TO_VOTE;

    public Boolean isUserUnableToVote() {
        return UNABLE_TO_VOTE.equals(this);
    }
}
