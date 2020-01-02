package com.cooperative.assembly.builder;

import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;

public class VotingSessionCanvassBuilder {

    public static GenericBuilder<VotingSessionCanvass> get() {
        return GenericBuilder.of(() -> new VotingSessionCanvass());
    }

}
