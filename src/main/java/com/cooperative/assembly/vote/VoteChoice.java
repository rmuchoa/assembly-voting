package com.cooperative.assembly.vote;

public enum VoteChoice {
    YES,
    NO;

    /**
     * check if made choice is affirmative
     *
     * @return
     */
    public Boolean isAffirmative() {
        return YES.equals(this);
    }

    /**
     * check if made choice is negative
     *
     * @return
     */
    public Boolean isNegative() {
        return NO.equals(this);
    }
}
