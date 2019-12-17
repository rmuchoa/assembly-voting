package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.meeting.agenda.MeetingAgenda;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VoteSessionRepository extends MongoRepository<VoteSession, String> {

    /**
     * Find a vote session obeject optionally by id property string value.
     *
     * @param id
     * @return
     */
    public Optional<VoteSession> findById(String id);

    /**
     * Find a vote session list of obejects filtering by agenda id property string value.
     *
     * @param agendaId
     * @return
     */
    public List<VoteSession> findByAgendaId(String agendaId);

}
