package com.cooperative.assembly.voting.agenda;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VotingAgendaRepository extends MongoRepository<VotingAgenda, String> {

    /**
     * Find a meeting agenda obeject optionally by id property string value.
     *
     * @param id
     * @return
     */
    public Optional<VotingAgenda> findById(String id);

    /**
     * Find a meeting agenda obeject optionally by title property string value.
     *
     * @param title
     * @return
     */
    public Optional<VotingAgenda> findByTitle(String title);

}
