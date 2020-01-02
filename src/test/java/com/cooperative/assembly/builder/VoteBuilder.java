package com.cooperative.assembly.builder;

import com.cooperative.assembly.v1.vote.Vote;

public class VoteBuilder {

    public static GenericBuilder<Vote> get() {
        return GenericBuilder.of(() -> new Vote());
    }

}
