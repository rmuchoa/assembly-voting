package com.cooperative.assembly.builder;

import com.cooperative.assembly.v1.voting.session.VotingSession;

public class VotingSessionBuilder {

    public static GenericBuilder<VotingSession> get() {
        return GenericBuilder.of(() -> new VotingSession());
    }

}
