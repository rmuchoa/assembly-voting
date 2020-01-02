package com.cooperative.assembly.builder;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;

public class VotingAgendaBuilder {

    public static GenericBuilder<VotingAgenda> get() {
        return GenericBuilder.of(() -> new VotingAgenda());
    }

}
