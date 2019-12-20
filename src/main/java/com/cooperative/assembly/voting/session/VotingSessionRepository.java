package com.cooperative.assembly.voting.session;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VotingSessionRepository extends MongoRepository<VotingSession, String> {

    /**
     * Find a vote session obeject optionally by id property string value.
     *
     * @param id
     * @return
     */
    public Optional<VotingSession> findById(String id);

    /**
     * Find a vote session list of obejects filtering by agenda id property string value.
     *
     * @param agendaId
     * @return
     */
    public Optional<VotingSession> findByAgendaId(String agendaId);

}
