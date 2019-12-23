package com.cooperative.assembly.v1.voting.agenda;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.UUID.randomUUID;

@Log4j2
@Service
public class VotingAgendaService {

    private VotingAgendaRepository repository;

    @Autowired
    public VotingAgendaService(final VotingAgendaRepository repository) {
        this.repository = repository;
    }

    /**
     * Create new agenda for Cooperative assembly meeting.
     * This agenda will be related to open a voting session later.
     *
     * @param title
     * @return
     */
    public VotingAgenda create(final String title) {
        String id = randomUUID().toString();
        VotingAgenda votingAgenda = new VotingAgenda(id, title);

        log.debug("Saving agenda to start voting session");
        return repository.save(votingAgenda);
    }

    /**
     * Load some agenda based on a specific agenda id.
     * This agenda can be related on a voting session.
     *
     * @param id
     * @return
     */
    public Optional<VotingAgenda> loadAgenda(final String id) {
        return repository.findById(id);
    }

}
